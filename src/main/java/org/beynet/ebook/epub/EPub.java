package org.beynet.ebook.epub;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.beynet.ebook.AbstractEBook;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookUtils;
import org.beynet.ebook.epub.oasis.container.Container;
import org.beynet.ebook.epub.oasis.container.RootFile;
import org.beynet.ebook.epub.opf.Item;
import org.beynet.ebook.epub.opf.Package;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

public class EPub extends AbstractEBook implements EBook {

    public EPub(Path epub) throws IOException {
        super(epub);
        readProperties();
        currentItem = Optional.empty() ;
    }


    @Override
    public String getFileExtension() {
        return ".epub";
    }



    private void checkIsWithDRM(FileSystem fs) throws IOException {
        Path encryptionFile = fs.getPath("META-INF","encryption.xml");

        if (Files.exists(encryptionFile)) {
            isProtected =true;
        }
        else  {
            isProtected =false;
        }
    }

    /**
     * check the mimetype file expected content
     * @param fs
     * @throws IOException
     */
    private void checkMimetypeFile(FileSystem fs) throws IOException {
        Path mimetype = fs.getPath("mimetype");
        if (!Files.exists(mimetype)) throw new IOException("execpected mimetype file not found");
        String mimetypeContent = new String(Files.readAllBytes(mimetype));

        mimetypeContent=mimetypeContent.stripTrailing().stripLeading();
        if (!"application/epub+zip".equals(mimetypeContent)) {
            throw new IOException("mimetype file contains an unexpected value " + mimetypeContent);
        }
    }

    private void readContainer(FileSystem fs) throws IOException{
        Path containerPath = fs.getPath("META-INF","container.xml");
        if (!Files.exists(containerPath)) throw new IOException("unable to read "+containerPath.toString()+" file");
        final Unmarshaller unmarshaller;
        try {
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("unable to create unmarshaller",e);
        }
        final Container container;
        try {
            container = (Container) unmarshaller.unmarshal(Files.newInputStream(containerPath));
        } catch (JAXBException e) {
            throw new RuntimeException("unable to unmarshalle",e);
        }

        // read each ebook description
        if (container.getRootFiles()==null || container.getRootFiles().getRootFiles().size()==0) {
            throw new IOException("no root file found in container");
        }
        RootFile rootFile = container.getRootFiles().getRootFiles().get(0);
        packageDocPath = rootFile.getFullPath();
        try {
            packageDoc= (Package) unmarshaller.unmarshal(Files.newInputStream(fs.getPath(packageDocPath)));
        } catch (JAXBException e) {
            throw new IOException("unable to read root package file ",e);
        }
        title=Optional.ofNullable(packageDoc.getMetadata().getTitle());
        subjects = new ArrayList<>();
        subjects.addAll(packageDoc.getMetadata().getSubjects());
        packageDoc.getMainCreator().ifPresentOrElse(
                a -> author = Optional.of(a.getName()),
                ()->author = packageDoc.getMetadata().getCreators().stream().findFirst().map(c->c.getName()));


    }


    @Override
    public Optional<String> getAuthor() {
        return author;
    }

    @Override
    public Optional<String> getTitle() {
        return title;
    }

    @Override
    public List<String> getSubjects(){
        return subjects;
    }


    private FileSystem getFileSystem() throws IOException {
        Map<String,?> env = new HashMap<>();
        URI uri = URI.create("jar:"+getPath().toUri().toString());
        return FileSystems.newFileSystem(uri, env);
    }

    /**
     * save back package file in epub
     * @throws IOException
     */
    private void savePackageDocument() throws IOException {
        try (FileSystem fs = getFileSystem()) {
            Path path = fs.getPath(packageDocPath);
            try(OutputStream os = Files.newOutputStream(path,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.CREATE)) {
                try {
                    context.createMarshaller().marshal(packageDoc, os);
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void updateSubjects() throws IOException {
        packageDoc.getMetadata().getSubjects().clear();
        packageDoc.getMetadata().getSubjects().addAll(getSubjects());
        savePackageDocument();
    }

    @Override
    public void changeAuthor(String newName) throws IOException {
        packageDoc.replaceMainCreator(newName);
        savePackageDocument();
    }

    private void readProperties() throws IOException {
        try (FileSystem fs = getFileSystem()) {
            checkMimetypeFile(fs);
            checkIsWithDRM(fs);
            readContainer(fs);
        }
    }

    @Override
    public boolean isProtected() {
        return isProtected;
    }

    @Override
    public Optional<String> getNextPage() {
        List<Item> items = packageDoc.getManifest().getItems();

        currentItem.ifPresentOrElse(
                // search the next page
                curr-> {
                    boolean currentPageReached = false ;
                    for (Item item : items) {
                        if (curr.equals(item)) {
                            currentPageReached = true ;
                            currentItem = Optional.empty();
                            continue;
                        }
                        if (item!=null && currentPageReached==true && XHTML.equals(item.getMediaType())) {
                            currentItem = Optional.of(item);
                            break;
                        }
                    }
                },
                // go to first page
                ()->{
                    if (items.size()>0) {
                        for (Item item : items) {
                            if (XHTML.equals(item.getMediaType())) {
                                currentItem = Optional.ofNullable(item);
                                break;
                            }
                        }
                    }
                }
        );

        return readCurrentItem();
    }


    @Override
    public Optional<String> getPreviousPage() {
        List<Item> items = packageDoc.getManifest().getItems();

        currentItem.ifPresent(curr->{
            currentItem = Optional.empty();
            for (Item item : items) {
                if (curr.equals(item)) {
                    break;
                }
                if (item!=null && XHTML.equals(item.getMediaType())) {
                    currentItem = Optional.of(item);
                }
            }
        });

        return readCurrentItem();
    }

    @Override
    public Optional<String> getDefaultCSS() {
        List<Item> items = packageDoc.getManifest().getItems();
        for (Item item : items) {
            if (item!=null && CSS.equals(item.getMediaType())) {
                return readItem(Optional.of(item));
            }
        }
        return Optional.empty();
    }

    private Optional<String> readCurrentItem() {
        return readItem(currentItem);
    }


    private Optional<String> readItem(Optional<Item> optItem) {
        return optItem.map(item -> {
            try (FileSystem fs = getFileSystem()) {
                Path packageDirectory = fs.getPath(packageDocPath).getParent();
                Path itemPath = packageDirectory.resolve(item.getHref());
                byte[] bytes = Files.readAllBytes(itemPath);
                return new String(bytes, "UTF-8");
            } catch (IOException e) {
                logger.error("");
                return "";
            }
        });
    }


    private  static JAXBContext context ;
    static {
        try {
            context = JAXBContext.newInstance(Container.class,Package.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    protected Package getPackageDoc() {
        return this.packageDoc;
    }

    private boolean isProtected;
    private Optional<String> author;
    private Optional<String> title;
    private List<String>     subjects;
    private Package          packageDoc;
    private String           packageDocPath;
    private Optional<Item>   currentItem ;

    private final static String XHTML="application/xhtml+xml";
    private final static String CSS="text/css";
    private final static Logger logger = LogManager.getLogger(EPub.class);

}
