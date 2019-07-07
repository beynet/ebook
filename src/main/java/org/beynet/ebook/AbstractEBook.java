package org.beynet.ebook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;

public abstract class AbstractEBook implements EBook{

    private String toFileName(String p) {
        return p.replaceAll("[ \\\\<>:|/?*\"]","");
    }

    @Override
    public EBook copyTo(Path targetDirectorty, CopyOption... options) throws IOException,InvalidPathException {
        if (targetDirectorty==null) throw new IllegalArgumentException("targetDirectory is mandatory");
        if (!Files.exists(targetDirectorty)) throw new IOException("target directory "+targetDirectorty.toString()+" does not exist");
        if (!Files.isDirectory(targetDirectorty)) throw new IOException(targetDirectorty.toString()+" is not a directory");
        if (options!=null && options.length>0) {
            for (CopyOption option : options) {
                if (EbookCopyOption.AddSubjectToPath.equals(option)) {
                    try {
                        targetDirectorty = targetDirectorty.resolve(toFileName(getSubjects().stream().findFirst().orElse(UNDEFINED_SUBJECT)));
                    }catch(InvalidPathException e) {
                        logger.error("invalid path ["+getSubjects().stream().findFirst().get());
                        throw e;
                    }
                }
                else if (EbookCopyOption.AddAuthorToPath.equals(option)) {
                    targetDirectorty=targetDirectorty.resolve(toFileName(getAuthor().orElse(UNDEFINED_AUTHOR)));
                }
            }
        }
        logger.debug("copy ebbook "+getPath().toString()+" to "+targetDirectorty);
        Files.createDirectories(targetDirectorty);
        targetDirectorty=targetDirectorty.resolve(toFileName(getTitle().orElse(UNDEFINED_TITLE)).concat(".epub"));
        Files.copy(getPath(),targetDirectorty,StandardCopyOption.REPLACE_EXISTING);
        return EBookFactory.createEBook(targetDirectorty);
    }


    private final static Logger logger = LogManager.getLogger(AbstractEBook.class);
}
