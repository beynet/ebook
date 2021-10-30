package org.beynet.ebook.epub;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.beynet.ebook.AbstractEBook;
import org.beynet.ebook.EBook;
import org.beynet.ebook.epub.oasis.container.Container;
import org.beynet.ebook.epub.oasis.container.RootFile;
import org.beynet.ebook.epub.opf.Identifier;
import org.beynet.ebook.epub.opf.Item;
import org.beynet.ebook.epub.opf.ItemRef;
import org.beynet.ebook.epub.opf.Package;

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


    @Override
    public Optional<String> getIdentifier() {
        Optional<String> result = Optional.empty();
        for (Identifier identifier : opfDocument.getMetadata().getIdentifiers()) {
            // return first identifier with content not null
            if (identifier.getValue()!=null) {
                if (result.isEmpty() || ( identifier.getValue().contains("uid") && !result.get().contains("uid")) ) {
                    result = Optional.of(identifier.getValue());
                }
            }
        }
        return result;
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
        opfDocumentPath = rootFile.getFullPath();
        try {
            opfDocument = (Package) unmarshaller.unmarshal(Files.newInputStream(fs.getPath(opfDocumentPath)));
        } catch (JAXBException e) {
            throw new IOException("unable to read root package file ",e);
        }
        title=Optional.ofNullable(opfDocument.getMetadata().getTitle());
        subjects = new ArrayList<>();
        subjects.addAll(opfDocument.getMetadata().getSubjects());
        opfDocument.getMainCreator().ifPresentOrElse(
                a -> author = Optional.of(a.getName()),
                ()->author = opfDocument.getMetadata().getCreators().stream().findFirst().map(c->c.getName()));


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
        Map<String,String> env = new HashMap<>();
        env.put("encoding","UTF-8");
        URI uri = URI.create("jar:"+getPath().toUri().toString());
        return FileSystems.newFileSystem(uri, env);
    }

    /**
     * save back package file in epub
     * @throws IOException
     */
    private void savePackageDocument() throws IOException {
        try (FileSystem fs = getFileSystem()) {
            Path path = fs.getPath(opfDocumentPath);
            try(OutputStream os = Files.newOutputStream(path,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.CREATE)) {
                try {
                    context.createMarshaller().marshal(opfDocument, os);
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void updateSubjects() throws IOException {
        opfDocument.getMetadata().getSubjects().clear();
        opfDocument.getMetadata().getSubjects().addAll(getSubjects());
        savePackageDocument();
    }

    @Override
    public void changeAuthor(String newName) throws IOException {
        opfDocument.replaceMainCreator(newName);
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
    public Optional<String> getCurrentPage(){
        if (!currentItem.isPresent()) {
            currentItem=loadSavedCurrentPage();
        }
        return readCurrentItem();
    }

    @Override
    public Optional<String> convertRessourceLocalPathToGlobalURL(String localPath) {
        logger.info("converting localPath "+localPath);
        return convertRessourceLocalPathToGlobalPath(localPath).or(()->Optional.of(localPath)).map(p->{
            try (FileSystem fs = getFileSystem()) {
                // links, if relative, in current page are relative to current page
                // current page path is relative to packageDocpath
                // ----------------------------------------------------------------
                Path packageDirectory = fs.getPath(opfDocumentPath).getParent();
                Path itemPath;
                if (packageDirectory!=null) {
                    itemPath = packageDirectory.resolve(p);
                }
                else {
                    itemPath = fs.getPath(p);
                }
                return itemPath.toUri().toString();
            } catch (IOException e) {
                logger.error("unable to read section",e);
                return null;
            }
        });
    }

    protected Optional<String> convertRessourceLocalPathToGlobalPath(String localPath) {
        final String page ;
        if (localPath.contains("#")) {
            page=localPath.substring(0,localPath.indexOf("#"));
        }
        else {
            page=localPath;
        }
        return currentItem.map(id->{
            for (Item item : opfDocument.getManifest().getItems()) {
                if (id.equals(item.getId())) {
                    return Paths.get(item.getHref());
                }
            }
            return null;
        }).map(p->p.getParent()).map(p->p.resolve(page).toString());
    }

    @Override
    public Optional<String> loadPage(String expectedPage) {
        Optional<Path> expectedPath = convertRessourceLocalPathToGlobalPath(expectedPage).map(s->Paths.get(s));

        currentItem = Optional.empty();
        for (Item item : opfDocument.getManifest().getItems()) {

            expectedPath.ifPresentOrElse(
                    expected -> {
                        if (expected.equals(Paths.get(item.getHref())))  {
                            currentItem=Optional.of(item.getId());
                        }
                    }
                    ,
                    () -> {
                        if (expectedPage.equals(item.getHref())) {
                            currentItem=Optional.of(item.getId());
                        }
                    }
            );
            if (currentItem.isPresent()) break;

        }
        return readCurrentItem();
    }

    @Override
    public Optional<String> getFirstPage() {
        currentItem = Optional.empty();
        return getNextPage();
    }

    @Override
    public Optional<String> getNextPage() {
        List<ItemRef> itemRefs = opfDocument.getSpine().getItemRefs();

        currentItem.ifPresentOrElse(
                // search the next page
                curr-> {
                    boolean currentPageReached = false ;
                    for (ItemRef itemRef : itemRefs) {
                        if (curr.equals(itemRef.getIdref())) {
                            currentPageReached = true ;
                            currentItem = Optional.empty();
                            continue;
                        }
                        if (currentPageReached==true) {
                            currentItem = Optional.of(itemRef.getIdref());
                            break;
                        }
                    }
                },
                // go to first page
                ()->{
                    currentItem=Optional.ofNullable(itemRefs.get(0).getIdref());
                }
        );
        return readCurrentItem();
    }

    @Override
    public Optional<String> getPreviousPage() {
        List<ItemRef> itemRefs = opfDocument.getSpine().getItemRefs();

        currentItem.ifPresent(curr->{
            currentItem = Optional.empty();
            for (ItemRef itemRef : itemRefs) {
                if (curr.equals(itemRef.getIdref())) {
                    break;
                }
                currentItem = Optional.of(itemRef.getIdref());
            }
        });

        return readCurrentItem();
    }

    @Override
    public Optional<String> getDefaultCSS() {
        Optional<String> result = Optional.empty();
        List<Item> items = opfDocument.getManifest().getItems();
        List<Optional<String>> css = new ArrayList<>();
        for (Item item : items) {
            if (item!=null && CSS.equals(item.getMediaType())) {
                css.add(readItem(Optional.of(item.getId())));
            }
        }
        if (css.size()>0) {
            StringBuilder st = new StringBuilder();
            for (Optional<String> s : css) {
                s.ifPresent(val->st.append(val.concat("\n")));

            }
            result = Optional.of(st.toString());
        }
        return result;
    }

    private Optional<String> readCurrentItem() {
        currentItem.ifPresent(item->{
            saveCurrentPage(item);
        });
        return readItem(currentItem);
    }


    private Optional<String> readItem(Optional<String> optItem) {
        return optItem.map(s->{
            for (Item item : opfDocument.getManifest().getItems()) {
                if (s.equals(item.getId())) return item;
            }
            return null;
        }).map(item -> {
            try (FileSystem fs = getFileSystem()) {
                Path packageDirectory = fs.getPath(opfDocumentPath).getParent();
                Path itemPath;
                if (packageDirectory!=null) {
                    itemPath = packageDirectory.resolve(item.getHref());
                }
                else {
                    itemPath = fs.getPath(item.getHref());
                }
                //return itemPath.toUri().toString();
                byte[] bytes = Files.readAllBytes(itemPath);
                return new String(bytes, "UTF-8");
            } catch (IOException e) {
                logger.error("unable to read section",e);
                return null;
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

    protected Package getOpfDocument() {
        return this.opfDocument;
    }

    private boolean isProtected;
    private Optional<String> author;
    private Optional<String> title;
    private List<String>     subjects;
    private Package          opfDocument;
    private String           opfDocumentPath;
    private Optional<String> currentItem ;

    private final static String XHTML="application/xhtml+xml";
    private final static String CSS="text/css";
    private final static Logger logger = LogManager.getLogger(EPub.class);

}
