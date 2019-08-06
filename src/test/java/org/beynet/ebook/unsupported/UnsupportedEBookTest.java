package org.beynet.ebook.unsupported;

import org.beynet.AbstractTests;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EbookCopyOption;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UnsupportedEBookTest extends AbstractTests {
    @Test
    public void fileExtension() throws IOException {
        UnsupportedEBook unsupportedEBook = new UnsupportedEBook(Paths.get("./src/test/resources/books/test.pdf"));
        assertEquals(".pdf",unsupportedEBook.getFileExtension());
    }

    @Test
    public void title() throws IOException {
        UnsupportedEBook unsupportedEBook = new UnsupportedEBook(Paths.get("./src/test/resources/books/test.pdf"));
        assertEquals(Optional.empty(),unsupportedEBook.getTitle());
    }

    @Test
    public void subjects() throws IOException {
        UnsupportedEBook unsupportedEBook = new UnsupportedEBook(Paths.get("./src/test/resources/books/test.pdf"));
        assertEquals(new ArrayList(),unsupportedEBook.getSubjects());
    }

    @Test
    public void copy() throws IOException {

        Path test = Files.createTempDirectory("test");
        Path expected=test.resolve(EBook.UNDEFINED_SUBJECT).resolve(EBook.UNDEFINED_AUTHOR).resolve("test.pdf");
        EBook result = null;
        try {
            UnsupportedEBook unsupportedEBook = new UnsupportedEBook(Paths.get("./src/test/resources/books/test.pdf"));
            result = unsupportedEBook.copyToDirectory(test, EbookCopyOption.AddSubjectToPath, EbookCopyOption.AddAuthorToPath);
            assertEquals(expected,result.getPath());
            assertTrue(Files.exists(result.getPath()));
        }
        finally {
            if (result!=null && Files.exists(result.getPath())) Files.delete(result.getPath());
            if (Files.exists(expected.getParent())) Files.delete(expected.getParent());
            if (Files.exists(expected.getParent().getParent())) Files.delete(expected.getParent().getParent());

        }
    }

}

