package org.beynet.ebook;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.InvalidPathException;
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
     * update current ebook subjects
     */
    void updateSubjects() throws IOException;


    /**
     * change current ebook author
     * @param newName
     * @throws IOException
     */
    void changeAuthor(String newName) throws IOException;

    /**
     * @return true if current ebook is protected, just like epub with DRM
     */
    boolean isProtected();

    /**
     * copy current ebook to target directory
     * @param targetDirectory
     * @param options
     * @return
     * @throws IOException
     * @throws InvalidPathException if an unexpected error happens during path formating
     */
    EBook copyToDirectory(Path targetDirectory, CopyOption... options) throws IOException, InvalidPathException;

    /**
     *
     * @param target
     * @return
     */
    EBook copy(Path target,CopyOption ...options) throws IOException;

    /**
     * @return to current ebook file
     */
    Path getPath();

    String getFileExtension();

    /**
     * @return the next page to be read
     */
    Optional<String> getNextPage() ;

    Optional<String> getCurrentPage();

    Optional<String> getFirstPage();

    Optional<String> loadPage(String expectedPage);

    Optional<String> convertRessourceLocalPathToGlobalURL(String localPath);

    /**
     * save current page
     * @param page
     */
    void saveCurrentPage(String page);

    Optional<String> loadSavedCurrentPage() ;

    void saveCurrentPageInPage(String pageInPage);
    Optional<String> loadSavedCurrentPageInPage();

    void saveCurrentPageRatio(String pageRatio);
    Optional<String> loadSavedCurrentPageRatio();

    void saveCurrentZoom(double zoom);
    Optional<Double> loadSavedCurrentZoom();

    void saveNightMode(boolean nightMode);
    Optional<Boolean> loadSavedNightMode();

    void saveSmartDisplayMode(boolean nightMode);
    Optional<Boolean> loadSmartDisplayMode();


    /**
     * @return the ebook previous page
     */
    Optional<String> getPreviousPage() ;

    Optional<String> getIdentifier() ;

    Optional<String> getDefaultCSS();

    String UNDEFINED_AUTHOR="undefined";
    String UNDEFINED_TITLE="undefined title";
    String UNDEFINED_ID   ="undefined id";
    String UNDEFINED_SUBJECT="undefined subject";

}

