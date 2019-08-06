package org.beynet.ebook.epub;

import org.beynet.AbstractTests;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookFactory;
import org.beynet.ebook.EbookCopyOption;
import org.beynet.ebook.epub.opf.Package;
import org.beynet.ebook.epub.opf.*;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class EPubTest extends AbstractTests {

    @Test
    public void creatorWithIDStartingWithID() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/univers multiples I _ Temps, Les - Stephen Baxter.epub"));
        assertEquals("Les univers multiples I : Temps",epub.getTitle().get());
        assertEquals("Stephen Baxter",epub.getAuthor().get());
        assertEquals(Integer.valueOf(1),epub.getSubjects().size());
        assertEquals("Science-Fiction",epub.getSubjects().get(0));
    }

    @Test
    public void creatorWithNoAttribut() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/A Fire Upon The Deep.epub"));
        assertEquals("A Fire Upon The Deep",epub.getTitle().get());
        assertEquals("Vinge, Vernor",epub.getAuthor().get());
        assertEquals(Integer.valueOf(1),epub.getSubjects().size());
        assertEquals("",epub.getSubjects().get(0));
    }

    @Test
    public void changeSubjects() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/Hunger Games.epub"));
        assertFalse(epub.isProtected());
        assertEquals("Hunger Games",epub.getTitle().get());
        assertEquals("Collins Suzanne",epub.getAuthor().get());
        assertEquals(Integer.valueOf(0),epub.getSubjects().size());
        basicTestOnPackage(epub.getPackageDoc());


        Path test = Files.createTempFile("test", ".epub");

        try {
            EBook result = epub.copy(test, StandardCopyOption.REPLACE_EXISTING);
            assertEquals(Integer.valueOf(0),result.getSubjects().size());
            result.getSubjects().add("Science-Fiction");
            result.getSubjects().add("truc1");
            result.updateSubjects();

            result = new EPub(test);
            assertEquals(Integer.valueOf(2),result.getSubjects().size());
            assertEquals("Science-Fiction",result.getSubjects().get(0));
            assertEquals("truc1",result.getSubjects().get(1));
        } finally {
            Files.deleteIfExists(test);
        }
    }

    @Test
    public void changeAuthorWithRole() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/Hunger Games.epub"));
        assertFalse(epub.isProtected());
        assertEquals("Hunger Games",epub.getTitle().get());
        assertEquals("Collins Suzanne",epub.getAuthor().get());
        basicTestOnPackage(epub.getPackageDoc());


        Path test = Files.createTempFile("test", ".epub");

        try {
            EBook result = epub.copy(test, StandardCopyOption.REPLACE_EXISTING);
            result.changeAuthor("Suzanne Collins");

            result = new EPub(test);
            assertEquals("Suzanne Collins",result.getAuthor().get());
        } finally {
            Files.deleteIfExists(test);
        }
    }

    @Test
    public void changeAuthorWithNoRole() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/A Fire Upon The Deep.epub"));
        assertEquals("Vinge, Vernor",epub.getAuthor().get());
        basicTestOnPackage(epub.getPackageDoc());


        Path test = Files.createTempFile("test", ".epub");

        try {
            EBook result = epub.copy(test, StandardCopyOption.REPLACE_EXISTING);
            result.changeAuthor("Vernors Vinge");

            result = new EPub(test);
            assertEquals("Vernors Vinge",result.getAuthor().get());
        } finally {
            Files.deleteIfExists(test);
        }
    }


    @Test
    public void isWithDRM() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/DRM Hunger Games II. Lembrasement .epub"));
        basicTestOnPackage(epub.getPackageDoc());
        assertTrue(epub.isProtected());
    }


    private void basicTestOnPackage(Package packageDoc) {
        assertNotNull(packageDoc.getMetadata());
        assertNotNull(packageDoc.getManifest());
        assertNotNull(packageDoc.getSpine());
    }

    @Test
    public void epub1() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/Pyramides - Romain BENASSAYA.epub"));
        assertEquals("Pyramides",epub.getTitle().get());
        assertEquals("Romain Benassaya",epub.getAuthor().get());
        assertEquals(Integer.valueOf(1),epub.getSubjects().size());
        assertEquals("Science-Fiction",epub.getSubjects().get(0));

        //xml tests
        Package packageDoc = epub.getPackageDoc();
        basicTestOnPackage(packageDoc);
        Metadata metadata = packageDoc.getMetadata();


        Date firstDate = new Date();
        firstDate.setContent("2018-01-10");
        firstDate.setEvent("creation");

        Date secondDate = new Date();
        secondDate.setContent("2018-02-01");
        secondDate.setEvent("publication");

        Date thirdDate = new Date();
        thirdDate.setContent("2018-03-23");
        thirdDate.setEvent("modification");

        assertEquals(Arrays.asList(firstDate,secondDate,thirdDate),metadata.getDates());


        CreatorOrContributor a,c1,c2,c3,c4;
        a = new CreatorOrContributor();
        a.setRole("aut");
        a.setName("Romain Benassaya");
        c1=new CreatorOrContributor();
        c1.setRole("ill");
        c1.setName("Niko Henrichon");
        c2=new CreatorOrContributor();
        c2.setRole("pbd");
        c2.setName("Simon Pinel");
        c3=new CreatorOrContributor();
        c3.setRole("edt");
        c3.setName("Xavier Dollo");
        c4=new CreatorOrContributor();
        c4.setRole("bkp");
        c4.setName("Frédéric Hugot");

        assertEquals(Arrays.asList(a),metadata.getCreators());
        assertEquals(Arrays.asList(c1,c2,c3,c4),metadata.getContributors());

        String p = "Éditions Critic";
        assertEquals(Arrays.asList(p),metadata.getPublishers());

        String r = "Romain Benassaya et Éditions Critic 2018";
        assertEquals(Arrays.asList(r),metadata.getRights());


        Meta m1,m2;
        m1=new Meta();
        m1.setContent("0.9.8");
        m1.setName("Sigil version");
        m2=new Meta();
        m2.setContent("pyramides.jpg");
        m2.setName("cover");

        assertEquals(Arrays.asList(m1,m2),metadata.getMetas());

        Identifier identifier = new Identifier();
        identifier.setId("BookID");
        identifier.setValue("urn:uuid:446cd1b2-7a79-48a0-b6cf-607fc618c09e");
        assertEquals(identifier,metadata.getIdentifier());

        Reference ref = new Reference();
        ref.setHref("Text/couverture.xhtml");
        ref.setTitle("Cover");
        ref.setType("cover");
        Guide guide = new Guide();
        guide.getReferences().add(ref);

        assertEquals(guide,epub.getPackageDoc().getGuide());
    }

    @Test
    public void path() throws IOException {
        EPub epub = new EPub(Paths.get("src/test/resources/books/univers multiples I _ Temps, Les - Stephen Baxter.epub"));
        Path test = Files.createTempDirectory("test");
        Path expected=test.resolve("Science-Fiction").resolve("Stephen Baxter").resolve("Les univers multiples I  Temps.epub");

        basicTestOnPackage(epub.getPackageDoc());
        Metadata metadata = epub.getPackageDoc().getMetadata();
        /*
         <dc:creator id="creator02">Sylvie Denis</dc:creator>
         <dc:creator id="creator03">Roland C. Wagner</dc:creator>
         <dc:creator id="id-2">Stephen Baxter</dc:creator>
         */
        CreatorOrContributor c1 = new CreatorOrContributor();
        c1.setId("creator02");
        c1.setName("Sylvie Denis");
        CreatorOrContributor c2 = new CreatorOrContributor();
        c2.setId("creator03");
        c2.setName("Roland C. Wagner");
        CreatorOrContributor c3 = new CreatorOrContributor();
        c3.setId("id-2");
        c3.setName("Stephen Baxter");
        assertEquals(Arrays.asList(c1,c2,c3),metadata.getCreators());

        try {
            EBook result = epub.copyToDirectory(test, EbookCopyOption.AddSubjectToPath, EbookCopyOption.AddAuthorToPath);
            assertEquals(expected,result.getPath());

            Files.delete(result.getPath());
            Files.delete(result.getPath().getParent());
            Files.delete(result.getPath().getParent().getParent());
        }
        finally {
            Files.delete(test);
        }
    }


    @Test
    public void readBook() throws IOException {
        final String firstPage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "\t<title>Le Livre de Saskia</title>\n" +
                "\t<link href=\"e9782919755394_css.css\" type=\"text/css\" rel=\"stylesheet\"/>\n" +
                "</head>\n" +
                "<body style=\"margin: 0px;\">\n" +
                "<div class=\"cover\">\n" +
                "<a id=\"cover\"/><div class=\"cover_image\"><div class=\"image\"><img src=\"e9782919755394_cover.jpg\" alt=\"e9782919755394_cover.jpg\"/></div></div>\n" +
                "<a id=\"title26\"/></div>\n" +
                "</body>\n" +
                "</html>\n";

        final String secondPage ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "\t<title>Le Livre de Saskia</title>\n" +
                "\t<link href=\"e9782919755394_css.css\" type=\"text/css\" rel=\"stylesheet\"/>\n" +
                "</head>\n" +
                "<body style=\"margin: 0px;\">\n" +
                "<div class=\"titlePage\">\n" +
                "<a id=\"title27\"/>\n" +
                "<div class=\"illustype_image\"><div class=\"image\"><img src=\"e9782919755394_i0001.jpg\" alt=\"e9782919755394_i0001.jpg\"/></div></div></div>\n" +
                "</body>\n" +
                "</html>\n";

        final String thirdPage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "\t<title>Le Livre de Saskia</title>\n" +
                "\t<link href=\"e9782919755394_css.css\" type=\"text/css\" rel=\"stylesheet\"/>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"toc\">\n" +
                "<a id=\"title28\"/><h1 class=\"title-toc\">Sommaire</h1>\n" +
                "\n" +
                "<div class=\"toc_entry\"><a href=\"e9782919755394_tp01.html\" class=\"toc_entry_titlePage\">Page de titre</a><br/>\n" +
                "\n" +
                "<a href=\"e9782919755394_ded01.html\" class=\"toc_entry_dedicationPage\">Dédicace</a><br/>\n" +
                "<a href=\"e9782919755394_fm01.html\" class=\"toc_entry_frontMatter\"><span class=\"b\">Résumé du tome I</span> - <span class=\"b\">Le Réveil</span></a><br/>\n" +
                "<a href=\"e9782919755394_fm02.html\" class=\"toc_entry_frontMatter\"><span class=\"b\">Prologue</span></a><br/>\n" +
                "<a href=\"e9782919755394_c01.html\" class=\"toc_entry_chapter\"><span class=\"b\">1</span> - <span class=\"b\">L’Initié</span></a><br/>\n" +
                "<a href=\"e9782919755394_c02.html\" class=\"toc_entry_chapter\"><span class=\"b\">2</span> - <span class=\"b\">L’attaque</span></a><br/>\n" +
                "<a href=\"e9782919755394_c03.html\" class=\"toc_entry_chapter\"><span class=\"b\">3</span> - <span class=\"b\">Torturée</span></a><br/>\n" +
                "<a href=\"e9782919755394_c04.html\" class=\"toc_entry_chapter\"><span class=\"b\">4</span> - <span class=\"b\">Le Nid</span></a><br/>\n" +
                "<a href=\"e9782919755394_c05.html\" class=\"toc_entry_chapter\"><span class=\"b\">5</span> - <span class=\"b\">Le Maître des kartans</span></a><br/>\n" +
                "<a href=\"e9782919755394_c06.html\" class=\"toc_entry_chapter\"><span class=\"b\">6</span> - <span class=\"b\">Claire</span></a><br/>\n" +
                "<a href=\"e9782919755394_c07.html\" class=\"toc_entry_chapter\"><span class=\"b\">7</span> - <span class=\"b\">La Pierre qui crie</span></a><br/>\n" +
                "<a href=\"e9782919755394_c08.html\" class=\"toc_entry_chapter\"><span class=\"b\">8</span> - <span class=\"b\">Mauvaises nouvelles</span></a><br/>\n" +
                "<a href=\"e9782919755394_c09.html\" class=\"toc_entry_chapter\"><span class=\"b\">9</span> - <span class=\"b\">Le Maître des arushs</span></a><br/>\n" +
                "<a href=\"e9782919755394_c10.html\" class=\"toc_entry_chapter\"><span class=\"b\">10</span> - <span class=\"b\">Premier test</span></a><br/>\n" +
                "<a href=\"e9782919755394_c11.html\" class=\"toc_entry_chapter\"><span class=\"b\">11</span> - <span class=\"b\">Le kartan blanc</span></a><br/>\n" +
                "<a href=\"e9782919755394_c12.html\" class=\"toc_entry_chapter\"><span class=\"b\">12</span> - <span class=\"b\">Entraînement</span></a><br/>\n" +
                "<a href=\"e9782919755394_c13.html\" class=\"toc_entry_chapter\"><span class=\"b\">13</span> - <span class=\"b\">Tempête</span></a><br/>\n" +
                "<a href=\"e9782919755394_c14.html\" class=\"toc_entry_chapter\"><span class=\"b\">14</span> - <span class=\"b\">Nahia</span></a><br/>\n" +
                "<a href=\"e9782919755394_c15.html\" class=\"toc_entry_chapter\"><span class=\"b\">15</span> - <span class=\"b\">Le vol</span></a><br/>\n" +
                "<a href=\"e9782919755394_c16.html\" class=\"toc_entry_chapter\"><span class=\"b\">16</span> - <span class=\"b\">Combat mortel</span></a><br/>\n" +
                "<a href=\"e9782919755394_c17.html\" class=\"toc_entry_chapter\"><span class=\"b\">17</span> - <span class=\"b\">Le piège</span></a><br/>\n" +
                "<a href=\"e9782919755394_c18.html\" class=\"toc_entry_chapter\"><span class=\"b\">18</span> - <span class=\"b\">Hécatombe</span></a><br/>\n" +
                "<a href=\"e9782919755394_ack01.html\" class=\"toc_entry_acknowPage\"><span class=\"b\">Remerciements</span></a><br/>\n" +
                "<a href=\"e9782919755394_bm01.html\" class=\"toc_entry_backMatter\"><span class=\"b\">LE LIVRE DE SASKIA</span> - <span class=\"b\">PRÉSENTATION DE LA SÉRIE</span></a><br/>\n" +
                "<a href=\"e9782919755394_bm02.html\" class=\"toc_entry_backMatter\"><span class=\"b\">LES HAUT CONTEURS</span></a><br/>\n" +
                "<a href=\"e9782919755394_bm03.html\" class=\"toc_entry_backMatter\"><span class=\"b\">VIA TEMPORIS</span></a><br/>\n" +
                "<a href=\"e9782919755394_bm04.html\" class=\"toc_entry_backMatter\"><span class=\"b\">Scrineo</span> - <span class=\"i\"><span class=\"b\">LE PUITS DES MÉMOIRES</span></span></a><br/>\n" +
                "<a href=\"e9782919755394_cop01.html\" class=\"toc_entry_pubInfo\">Page de Copyright</a><br/>\n" +
                "</div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>\n";

        final String lastPageExpected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "\t<title>Le Livre de Saskia</title>\n" +
                "\t<link href=\"e9782919755394_css.css\" type=\"text/css\" rel=\"stylesheet\"/>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"footnotes\">\n" +
                "<a id=\"title31\"/>\n" +
                "<div class=\"footnote\"><a id=\"ftn_fn1\" href=\"e9782919755394_c01.html#ref_ftn_fn1\"><span class=\"footnote_number\">1</span></a>\n" +
                "<p class=\"p\">Voir <span class=\"i\">Le Livre de Saskia</span>, tome I, <span class=\"i\">Le Réveil</span>.</p></div>\n" +
                "<div class=\"footnote\"><a id=\"ftn_fn2\" href=\"e9782919755394_c01.html#ref_ftn_fn2\"><span class=\"footnote_number\">2</span></a>\n" +
                "<p class=\"p\">Voir <span class=\"i\">Le Livre de Saskia</span>, tome I, <span class=\"i\">Le Réveil</span>.</p></div>\n" +
                "<div class=\"footnote\"><a id=\"ftn_fn3\" href=\"e9782919755394_c02.html#ref_ftn_fn3\"><span class=\"footnote_number\">3</span></a>\n" +
                "<p class=\"p\">Voir <span class=\"i\">Le Livre de Saskia</span>, tome I, <span class=\"i\">Le Réveil</span>.</p></div>\n" +
                "<div class=\"footnote\"><a id=\"ftn_fn4\" href=\"e9782919755394_c03.html#ref_ftn_fn4\"><span class=\"footnote_number\">4</span></a>\n" +
                "<p class=\"p\">Voir <span class=\"i\">Le Livre de Saskia</span>, tome I, <span class=\"i\">Le Réveil</span>.</p></div>\n" +
                "<div class=\"footnote\"><a id=\"ftn_fn5\" href=\"e9782919755394_c05.html#ref_ftn_fn5\"><span class=\"footnote_number\">5</span></a>\n" +
                "<p class=\"p\">Voir <span class=\"i\">Le Livre de Saskia</span>, tome I, <span class=\"i\">Le Réveil</span>.</p></div>\n" +
                "<div class=\"footnote\"><a id=\"ftn_fn6\" href=\"e9782919755394_c05.html#ref_ftn_fn6\"><span class=\"footnote_number\">6</span></a>\n" +
                "<p class=\"p\">Voir <span class=\"i\">Le Livre de Saskia</span>, tome I, <span class=\"i\">Le Réveil</span>.</p></div>\n" +
                "<div class=\"footnote\"><a id=\"ftn_fn7\" href=\"e9782919755394_c07.html#ref_ftn_fn7\"><span class=\"footnote_number\">7</span></a>\n" +
                "<p class=\"p\">Voir <span class=\"i\">Le Livre de Saskia</span>, tome I, <span class=\"i\">Le Réveil</span>.</p></div></div></body>\n" +
                "</html>\n";
        Path book = Paths.get("./src/test/resources/books/Livre de Saskia, Le 2 - Pavlenko, Marie.epub");
        EBook eBook = EBookFactory.createEBook(book);
        Optional<String> nextPage = eBook.getNextPage();
        assertEquals(firstPage,nextPage.get());

        //check second page
        nextPage = eBook.getNextPage();
        assertEquals(secondPage,nextPage.get());

        //check third page
        nextPage = eBook.getNextPage();
        assertEquals(thirdPage,nextPage.get());

        int i=3;
        Optional<String> lastPage ;
        while (true) {
            lastPage = nextPage;
            nextPage = eBook.getNextPage();
            if (nextPage.isPresent()) {
                i++;
            }
            else {
                break;
            }
        }
        assertEquals(32,i);
        assertEquals(lastPageExpected,lastPage.get());
    }

}
