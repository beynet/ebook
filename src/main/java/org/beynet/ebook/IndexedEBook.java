package org.beynet.ebook;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class IndexedEBook implements EBook {

    private final String identifier;
    private final String author;
    private final String title;
    private final Path path;
    private final List<String> subjects;


    public IndexedEBook(String identifier, String path,String author, String title, List<String> subjects){
        this.identifier = identifier;
        this.path = Paths.get(path);
        this.author = author;
        this.title = title;
        this.subjects = new ArrayList<>();
        this.subjects.addAll(subjects);
    }


    @Override
    public void index(Consumer<EBook> index, Runnable postIndex) {
        // no indexation supported
    }

    @Override
    public Optional<String> getAuthor() {
        return Optional.ofNullable(author);
    }

    @Override
    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    @Override
    public List<String> getSubjects() {
        return subjects;
    }

    @Override
    public void updateSubjects() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void changeAuthor(String newName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public EBook copyToDirectory(Path targetDirectory, CopyOption... options) throws IOException, InvalidPathException {
        throw new UnsupportedOperationException();
    }

    @Override
    public EBook copy(Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String getFileExtension() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> getNextPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> getCurrentPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> getFirstPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> loadPage(String expectedPage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> convertRessourceLocalPathToGlobalURL(String localPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveCurrentPage(String page) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> loadSavedCurrentPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveCurrentPageInPage(String pageInPage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> loadSavedCurrentPageInPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveCurrentPageRatio(String pageRatio) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> loadSavedCurrentPageRatio() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveCurrentZoom(double zoom) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Double> loadSavedCurrentZoom() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveNightMode(boolean nightMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Boolean> loadSavedNightMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveSmartDisplayMode(boolean nightMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Boolean> loadSmartDisplayMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> getPreviousPage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> getIdentifier() {
        return Optional.of(identifier);
    }

    @Override
    public Optional<String> getDefaultCSS() {
        throw new UnsupportedOperationException();
    }
}
