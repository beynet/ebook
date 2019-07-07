package org.beynet.ebook;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Hello world!
 *
 */
public interface EBook {

    /**
     * @return the author of this ebook
     */
    Optional<String> getAuthor();

    /**
     * @return the title of current ebook
     */
    Optional<String> getTitle() ;

    /**
     * @return the subjects of this ebook
     */
    List<String> getSubjects();

    /**
     * copy current ebook to target directory
     * @param targetDirectorty
     * @param options
     * @return
     * @throws IOException
     */
    EBook copyTo(Path targetDirectorty, CopyOption... options) throws IOException;

    /**
     * @return to current ebook file
     */
    Path getPath();

    String UNDEFINED_AUTHOR="undefined";
    String UNDEFINED_TITLE="undefined title";
    String UNDEFINED_SUBJECT="undefined subject";
}

