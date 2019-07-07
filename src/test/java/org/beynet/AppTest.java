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
import org.beynet.ebook.EbookCopyOption;
import org.beynet.ebook.epub.EPub;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

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
    public void result() throws IOException {
        Path test = Files.createTempDirectory("test");
        Files.walkFileTree(Paths.get("G:\\Mon Drive\\EBooks"), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                logger.debug("visit file "+file.toString());
                if (file.getFileName().toString().endsWith(".epub")) {
                    try {
                        EBookFactory.createEBook(file).copyTo(test,EbookCopyOption.AddSubjectToPath,EbookCopyOption.AddAuthorToPath);
                    }catch(Exception e) {
                        logger.error("unable to read ebook "+file.toString(),e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }



    /**
     * Rigorous Test :-)
     */
    @Test
    public void creatorWithRoleAut() throws IOException {
        EPub epub = new EPub(Paths.get("C:\\Users\\beyne\\IdeaProjects\\ebook\\src\\test\\resources\\Pyramides - Romain BENASSAYA.epub"));
        assertThat(epub.getTitle().get(),is("Pyramides"));
        assertThat(epub.getAuthor().get(),is("Romain Benassaya"));
        assertThat(epub.getSubjects().size(),is(Integer.valueOf(1)));
        assertThat(epub.getSubjects().get(0),is("Science-Fiction"));
    }
}
