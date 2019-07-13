package org.beynet.ebook.epub;

import org.beynet.ebook.EBook;
import org.beynet.ebook.EbookCopyOption;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EPubTest {

    @Test
    public void creatorWithIDStartingWithID() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/univers multiples I _ Temps, Les - Stephen Baxter.epub"));
        assertThat(epub.getTitle().get(),is("Les univers multiples I : Temps"));
        assertThat(epub.getAuthor().get(),is("Stephen Baxter"));
        assertThat(epub.getSubjects().size(),is(Integer.valueOf(1)));
        assertThat(epub.getSubjects().get(0),is("Science-Fiction"));
    }

    @Test
    public void creatorWithNoAttribut() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/A Fire Upon The Deep.epub"));
        assertThat(epub.getTitle().get(),is("A Fire Upon The Deep"));
        assertThat(epub.getAuthor().get(),is("Vinge, Vernor"));
        assertThat(epub.getSubjects().size(),is(Integer.valueOf(1)));
        assertThat(epub.getSubjects().get(0),is(""));
    }

    @Test
    public void emptySubject() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/Hunger Games.epub"));
        assertThat(epub.getTitle().get(),is("Hunger Games "));
        assertThat(epub.getAuthor().get(),is("Collins Suzanne"));
        assertThat(epub.getSubjects().size(),is(Integer.valueOf(1)));
        assertThat(epub.getSubjects().get(0),is(""));
    }

    @Test
    public void creatorWithRoleAut() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/Pyramides - Romain BENASSAYA.epub"));
        assertThat(epub.getTitle().get(),is("Pyramides"));
        assertThat(epub.getAuthor().get(),is("Romain Benassaya"));
        assertThat(epub.getSubjects().size(),is(Integer.valueOf(1)));
        assertThat(epub.getSubjects().get(0),is("Science-Fiction"));
    }

    @Test
    public void path() throws IOException {
        Path test = Files.createTempDirectory("test");
        Path expected=test.resolve("Science-Fiction").resolve("Stephen Baxter").resolve("Les univers multiples I  Temps.epub");

        try {
            EPub epub = new EPub(Paths.get("src/test/resources/books/univers multiples I _ Temps, Les - Stephen Baxter.epub"));
            EBook result = epub.copyTo(test, EbookCopyOption.AddSubjectToPath, EbookCopyOption.AddAuthorToPath);
            assertThat(result.getPath(),is(expected));

            Files.delete(result.getPath());
            Files.delete(result.getPath().getParent());
            Files.delete(result.getPath().getParent().getParent());
        }
        finally {
            Files.delete(test);
        }
    }


}
