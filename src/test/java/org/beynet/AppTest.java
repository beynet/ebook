package org.beynet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookFactory;
import org.beynet.ebook.EBookUtils;
import org.beynet.ebook.EbookCopyOption;
import org.beynet.ebook.epub.EPub;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

/**
 * Unit test for simple EBook.
 */
public class AppTest {
    private final static Logger logger = LogManager.getLogger(AppTest.class);
    static {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.INFO);
    }

    @Test
    public void creatorWithIDStartingWithID() throws IOException {
        EPub epub = new EPub(Paths.get("src\\test\\resources\\univers multiples I _ Temps, Les - Stephen Baxter.epub"));
        assertThat(epub.getTitle().get(),is("Les univers multiples I : Temps"));
        assertThat(epub.getAuthor().get(),is("Stephen Baxter"));
        assertThat(epub.getSubjects().size(),is(Integer.valueOf(1)));
        assertThat(epub.getSubjects().get(0),is("Science-Fiction"));
    }

    @Test
    public void creatorWithNoAttribut() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/A Fire Upon The Deep.epub"));
        assertThat(epub.getTitle().get(),is("A Fire Upon The Deep"));
        assertThat(epub.getAuthor().get(),is("Vinge, Vernor"));
        assertThat(epub.getSubjects().size(),is(Integer.valueOf(1)));
        assertThat(epub.getSubjects().get(0),is(""));
    }

    @Test
    public void emptySubject() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/Hunger Games.epub"));
        assertThat(epub.getTitle().get(),is("Hunger Games "));
        assertThat(epub.getAuthor().get(),is("Collins Suzanne"));
        assertThat(epub.getSubjects().size(),is(Integer.valueOf(1)));
        assertThat(epub.getSubjects().get(0),is(""));
    }


    @Test
    public void path() throws IOException {
        Path test = Files.createTempDirectory("test");
        Path expected=test.resolve("Science-Fiction").resolve("Stephen Baxter").resolve("Les univers multiples I  Temps.epub");

        try {
            EPub epub = new EPub(Paths.get("src\\test\\resources\\univers multiples I _ Temps, Les - Stephen Baxter.epub"));
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


    @Test
    public void replace(){

        String p="<>essai:\\test*|><\"";
        System.out.println(p.replaceAll("[\\\\<>:|/?*\"]",""));
    }

    @Test
    public void sortBooks() throws IOException {
        Path test = Files.createTempDirectory("test");
        EBookUtils.sort(Paths.get("G:\\Mon Drive\\EBooks"),test,EbookCopyOption.AddSubjectToPath,EbookCopyOption.AddAuthorToPath,StandardCopyOption.REPLACE_EXISTING);
    }


    @Test
    public void t() {
        String file="t.jpg";
        Optional<String> originalFileName = Optional.of(file).map(t->(t.contains(".")&&t.lastIndexOf(".")!=0)?t.substring(0,t.lastIndexOf(".")-1):t);
        System.out.println(originalFileName);
    }


    /**
     * Rigorous Test :-)
     */
    @Test
    public void creatorWithRoleAut() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/Pyramides - Romain BENASSAYA.epub"));
        assertThat(epub.getTitle().get(),is("Pyramides"));
        assertThat(epub.getAuthor().get(),is("Romain Benassaya"));
        assertThat(epub.getSubjects().size(),is(Integer.valueOf(1)));
        assertThat(epub.getSubjects().get(0),is("Science-Fiction"));
    }
}
