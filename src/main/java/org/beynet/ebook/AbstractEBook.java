package org.beynet.ebook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractEBook implements EBook {

    public AbstractEBook(Path path) throws IOException {
        this.path = path;
        if (!Files.exists(this.path)) throw new IOException("File not found "+path.toString());
    }

    private String toFileName(String p) {
        return p.replaceAll("[\\\\<>:|/?*\"]","").stripLeading().stripTrailing();
    }

    @Override
    public EBook copyTo(Path targetDirectory, CopyOption... options) throws IOException,InvalidPathException {
        if (targetDirectory==null) throw new IllegalArgumentException("targetDirectory is mandatory");
        if (!Files.exists(targetDirectory)) throw new IOException("target directory "+targetDirectory.toString()+" does not exist");
        if (!Files.isDirectory(targetDirectory)) throw new IOException(targetDirectory.toString()+" is not a directory");

        if (options!=null && options.length>0) {
            for (CopyOption option : options) {
                if (EbookCopyOption.AddSubjectToPath.equals(option)) {

                    String subject = toFileName(getSubjects().stream().findFirst().map(t->"".equals(t)?null:t).orElse(UNDEFINED_SUBJECT));
                    try {
                        targetDirectory = targetDirectory.resolve(subject);
                    } catch(InvalidPathException e) {
                        logger.error("invalid path ["+subject+"]",e);
                        throw e;
                    }
                }
                else if (EbookCopyOption.AddAuthorToPath.equals(option)) {
                    String author = toFileName(getAuthor().map(t->"".equals(t)?null:t).orElse(UNDEFINED_AUTHOR));
                    try {
                        targetDirectory = targetDirectory.resolve(author);
                    } catch(InvalidPathException e) {
                        logger.error("invalid path ["+author+"]",e);
                        throw e;
                    }
                }
            }
        }
        options = Arrays.stream(options).filter(option -> !EbookCopyOption.class.isAssignableFrom(option.getClass())).collect(Collectors.toList()).toArray(new CopyOption[0]);
        logger.info("copy ebook "+getPath().toString()+" to "+targetDirectory);
        Optional<String> originalFileName = Optional.of(getPath().getFileName().toString()).map(t->(t.contains(".")&&t.lastIndexOf(".")!=0)?t.substring(0,t.lastIndexOf(".")):t);

        // create expected directories
        Files.createDirectories(targetDirectory);
        // use book title as file name or original filename
        targetDirectory=targetDirectory.resolve(toFileName(getTitle().map(t->"".equals(t)?null:t).orElse(originalFileName.get()).concat(getFileExtension())));
        Files.copy(getPath(),targetDirectory,options);
        return EBookFactory.createEBook(targetDirectory);
    }

    @Override
    public String getFileExtension() {
        Optional<String> extension = Optional.of(getPath().getFileName().toString()).filter(f -> f.lastIndexOf(".") >= 0).map(f -> f.substring(f.lastIndexOf(".")));
        return extension.orElse("");
    }


    @Override
    public Path getPath() {
        return path;
    }

    private Path path;
    private final static Logger logger = LogManager.getLogger(AbstractEBook.class);
}
