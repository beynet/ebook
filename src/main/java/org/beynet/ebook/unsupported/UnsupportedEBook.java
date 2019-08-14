package org.beynet.ebook.unsupported;

import org.beynet.ebook.AbstractEBook;
import org.beynet.ebook.EBook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnsupportedEBook extends AbstractEBook implements EBook {

    public UnsupportedEBook(Path ebook) throws IOException {
        super(ebook);
    }

    @Override
    public Optional<String> getAuthor() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getTitle() {
        return Optional.empty();
    }

    @Override
    public List<String> getSubjects() {
        return subjects;
    }

    @Override
    public void updateSubjects() {
        throw new UnsupportedOperationException("unable to update subjects");
    }

    @Override
    public void changeAuthor(String newName) {
        throw new UnsupportedOperationException("unable to update subjects");
    }

    @Override
    public Optional<String> getNextPage() {
        throw new UnsupportedOperationException("unable to read this ebook");
    }
    @Override
    public Optional<String> getFirstPage() {
        throw new UnsupportedOperationException("unable to read this ebook");
    }
    @Override
    public Optional<String> getCurrentPage() {
        throw new UnsupportedOperationException("unable to read this ebook");
    }

    @Override
    public Optional<String> getPreviousPage() {
        throw new UnsupportedOperationException("unable to read this ebook");
    }

    @Override
    public Optional<String> loadPage(String expectedPage) {
        throw new UnsupportedOperationException("unable to read this ebook");
    }

    @Override
    public Optional<String> getDefaultCSS() {
        throw new UnsupportedOperationException("unable to read this ebook");
    }

    @Override
    protected Optional<String> getIdentifier() {
        throw new UnsupportedOperationException("unable to read this ebook");
    }

    private List<String> subjects = new ArrayList<>();
}
