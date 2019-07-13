package org.beynet.ebook.epub;

import org.beynet.ebook.AbstractEBook;
import org.beynet.ebook.EBook;
import org.beynet.ebook.epub.oasis.container.Container;
import org.beynet.ebook.epub.oasis.container.RootFile;
import org.beynet.ebook.epub.opf.Package;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EPub extends AbstractEBook implements EBook {

    public EPub(Path epub) throws IOException {
        this.path = epub;
        readProperties();
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String getFileExtension() {
        return ".epub";
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
        final Package packageDoc;
        try {
            packageDoc= (Package) unmarshaller.unmarshal(Files.newInputStream(fs.getPath(rootFile.getFullPath())));
        } catch (JAXBException e) {
            throw new IOException("unable to read root package file ",e);
        }
        title=Optional.ofNullable(packageDoc.getMetadata().getTitle());
        subjects = packageDoc.getMetadata().getSubjects();
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

    private void readProperties() throws IOException {
        Map<String,?> env = new HashMap<>();
        URI uri = URI.create("jar:"+path.toUri().toString());
        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
            checkMimetypeFile(fs);
            readContainer(fs);
        }
    }


    private  static JAXBContext context ;
    static {
        try {
            context = JAXBContext.newInstance(Container.class,Package.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }


    private final Path       path;
    private Optional<String> author;
    private Optional<String> title;
    private List<String>     subjects;
}
