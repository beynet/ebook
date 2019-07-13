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

    private List<String> subjects = new ArrayList<>();
}
