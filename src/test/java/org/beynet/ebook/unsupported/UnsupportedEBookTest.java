package org.beynet.ebook.unsupported;

import org.beynet.AbstractTests;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EbookCopyOption;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UnsupportedEBookTest extends AbstractTests {
    @Test
    public void fileExtension() throws IOException {
        UnsupportedEBook unsupportedEBook = new UnsupportedEBook(Paths.get("./src/test/resources/books/test.pdf"));
        assertThat(unsupportedEBook.getFileExtension(),is(".pdf"));
    }

    @Test
    public void title() throws IOException {
        UnsupportedEBook unsupportedEBook = new UnsupportedEBook(Paths.get("./src/test/resources/books/test.pdf"));
        assertThat(unsupportedEBook.getTitle(),is(Optional.empty()));
    }

    @Test
    public void subjects() throws IOException {
        UnsupportedEBook unsupportedEBook = new UnsupportedEBook(Paths.get("./src/test/resources/books/bookss/test.pdf"));
        assertThat(unsupportedEBook.getSubjects(),is(new ArrayList()));
    }

    @Test
    public void copy() throws IOException {

        Path test = Files.createTempDirectory("test");
        Path expected=test.resolve(EBook.UNDEFINED_SUBJECT).resolve(EBook.UNDEFINED_AUTHOR).resolve("test.pdf");
        EBook result = null;
        try {
            UnsupportedEBook unsupportedEBook = new UnsupportedEBook(Paths.get("./src/test/resources/books/test.pdf"));
            result = unsupportedEBook.copyToDirectory(test, EbookCopyOption.AddSubjectToPath, EbookCopyOption.AddAuthorToPath);
            assertThat(result.getPath(),is(expected));
            assertThat(Files.exists(result.getPath()),is(true));
        }
        finally {
            if (result!=null && Files.exists(result.getPath())) Files.delete(result.getPath());
            if (Files.exists(expected.getParent())) Files.delete(expected.getParent());
            if (Files.exists(expected.getParent().getParent())) Files.delete(expected.getParent().getParent());

        }
    }

}

