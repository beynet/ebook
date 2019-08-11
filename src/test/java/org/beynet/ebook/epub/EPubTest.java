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

        nextPage = eBook.getPreviousPage();
        assertEquals(secondPage,nextPage.get());

        nextPage = eBook.getPreviousPage();
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

        final String defaultCSS = "@charset \"utf-8\"; \n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Page */\n" +
                "\n" +
                "\t@page {\n" +
                "\t\tmargin-top: 5px;\n" +
                "\t\tmargin-bottom: 5px;\n" +
                "\t}\n" +
                "\t\n" +
                "\tbody {\n" +
                "\t\ttext-align: justify;\n" +
                "\t\tfont-size: 0.8em;\n" +
                "\t\tmargin-left: 10px;\n" +
                "\t\tmargin-right: 25px;\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Styles */\n" +
                "\n" +
                "\t.b {\n" +
                "\t\tfont-weight: bold;\n" +
                "\t}\n" +
                "\n" +
                "\t.i {\n" +
                "\t\tfont-style: italic;\n" +
                "\t}\n" +
                "\n" +
                "\t.u {\n" +
                "\t\ttext-decoration: underline;\n" +
                "\t}\n" +
                "\n" +
                "  .color {\n" +
                "\t}\n" +
                "\n" +
                "/*lettrine*/\n" +
                "\t.let {\n" +
                "\t\tfont-size : 1.5em;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t}\n" +
                "\n" +
                "\t.go_to_new_line {\n" +
                "\t\tclear:both;\n" +
                "\t}\n" +
                "\t\n" +
                "\tsup {\n" +
                "\t\tfont-size: 0.7em;\n" +
                "\t\tvertical-align: super;\n" +
                "\t\ttext-decoration: none;\n" +
                "\t\tline-height: 0.3em;\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Sections */\n" +
                "\n" +
                "\t.part, .chapter, .section1, .section2, .section3, .section4, .section5, .section6, .section7 {\n" +
                "\t\ttext-align: justify;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.pubInfo {\n" +
                "\t}\n" +
                "\t\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Headlines  */\n" +
                "\t\n" +
                "\t\n" +
                "\n" +
                "\t.title  {\n" +
                "\t\tfont-size: 1.4em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-top: 2em;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.subtitle  {\n" +
                "\t\tfont-size: 1.2em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "\t/* Sections  */\n" +
                "\t\n" +
                "\t.title-part {\n" +
                "\t\tfont-size: 2em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-top: 2em;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.title-chapter {\n" +
                "\t\tfont-size: 1.6em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-top: 2em;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.title-section1 {\n" +
                "\t\tfont-size: 1.3em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-top: 2em;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.title-section2 {\n" +
                "\t\tfont-size: 1em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-top: 2em;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.title-section3, .title-section4, .title-section5, .title-section6, .title-section7 {\n" +
                "\t\tfont-size: 1em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-top: 1em;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\n" +
                "\t.title-blocktext-grey, .title-blocktext, .title-box-grey, .title-box, .title-sidebar-large-grey, .title-sidebar-large, .title-sidebar-grey, .title-sidebar {\n" +
                "\t\tfont-size: 1em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\ttext-align: center;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.title-pubInfo ,.title-alsoby ,.title-aboutAuthorPage, title-definitions ,.title-aboutPublisherPage  ,.title-teaser, .title-forewordPage ,.title-praisePage ,.title-creditsPage ,.title-acknowPage ,.title-dedicationPage, .title-epigraphPage ,.title-index ,.title-glossary_full, .title-glossary, .title-appendix, .title-epilog, .title-frontMatter, .title-orderPage, .title-backMatter, .title-biblio, .title-toc, .title-introduction {\n" +
                "\t\tfont-size: 1.6em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-top: 2em;\n" +
                "\t\tmargin-bottom: 2em;\n" +
                "\t\ttext-align: center;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.subtitle-part {\n" +
                "\t\tfont-size: 1.5em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.subtitle-chapter {\n" +
                "\t\tfont-size: 1.2em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.subtitle-section1 {\n" +
                "\t\tfont-size: 1em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.subtitle-section2 {\n" +
                "\t\tfont-size: 1em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\t\t\t\n" +
                "\t.subtitle-section3, .subtitle-section4, .subtitle-section5, .subtitle-section6, .subtitle-section7 {\n" +
                "\t\tfont-size: 1em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\n" +
                "\t.subtitle-blocktext-grey, .subtitle-blocktext, .subtitle-box-grey, .subtitle-box, .subtitle-sidebar-large-grey, .subtitle-sidebar-large, .subtitle-sidebar-grey, .subtitle-sidebar {\n" +
                "\t\tfont-size: 1em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\ttext-align: center;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.subtitle-pubInfo ,.subtitle-alsoby ,.subtitle-aboutAuthorPage, .subtitle-definitions ,.subtitle-aboutPublisherPage  ,.subtitle-teaser, .subtitle-forewordPage ,.subtitle-praisePage ,.subtitle-creditsPage ,.subtitle-acknowPage ,.subtitle-dedicationPage, .subtitle-epigraphPage ,.subtitle-index ,.subtitle-glossary_full, .subtitle-glossary, .subtitle-appendix, .subtitle-epilog, .subtitle-frontMatter, .subtitle-orderPage, .subtitle-backMatter, .subtitle-biblio, .subtitle-toc {\n" +
                "\t\tfont-size: 1.2em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\ttext-align: center;\n" +
                "\t\tmargin-bottom: 2em;\n" +
                "\t}\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Paragraphs */\n" +
                "\n" +
                "\t.p, .p-br, .p-indent, .p-indent-br, .p-d, .p-d-br, .p-d-indent, .p-c, .p-c-br, .p-c-indent {\n" +
                "\t\tmargin-top: 0;\n" +
                "\t\tmargin-bottom: 0;\n" +
                "\t}\n" +
                "\t\n" +
                "\t/* P-blanc */\n" +
                " \t.p-blanc, .p-blanc-box, .p-blanc-blocktext, .p-blanc-box-grey, .p-blanc-blocktext-grey, .p-blanc-sidebar, .p-blanc-sidebar-large, .p-blanc-sidebar-grey, .p-blanc-sidebar-large-grey {\n" +
                "\t      margin:0em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.p, .p-br {\n" +
                "\t}\n" +
                "\t\n" +
                "\t.p-indent {\n" +
                "\t\ttext-indent: 1em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.p-indent-inverse {\n" +
                "\t\tmargin-left: 3em;\n" +
                "\t\ttext-indent: -3em;\n" +
                "\t}\n" +
                "\t.p-indent-inverse-suite {\n" +
                "\t\tmargin-left: 3em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.p-indent-br {\n" +
                "\t\ttext-indent: 1em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.p-d, .p-d-br {\n" +
                "\t\ttext-align: right;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.p-d-indent {\n" +
                "\t\ttext-align: right;\n" +
                "\t\ttext-indent: 1em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.p-c, .p-c-br {\n" +
                "\t\ttext-align: center;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.p-c-indent {\n" +
                "\t\ttext-align: center;\n" +
                "\t\ttext-indent: 1em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.p-indent-inverse-blocktext {\n" +
                "\t\tmargin-left: 3em;\n" +
                "\t\ttext-indent: -3em;\n" +
                "\t\tmargin-top: 0em;\n" +
                "\t\tmargin-bottom: 0em;\n" +
                "\t}\n" +
                "\t.p-indent-inverse-suite-blocktext {\n" +
                "\t\tmargin-left: 3em;\n" +
                "\t\tmargin-top: 0em;\n" +
                "\t\tmargin-bottom: 0em;\n" +
                "\t}\n" +
                "\n" +
                "\t.p-blocktext-grey, .p-blocktext, .p-box-grey, .p-box, .p-sidebar-large-grey, .p-sidebar-large, .p-sidebar-grey, .p-sidebar, .p-br-blocktext-grey, .p-br-blocktext, .p-br-box-grey, .p-br-box, .p-br-sidebar-large-grey, .p-br-sidebar-large, .p-br-sidebar-grey, .p-br-sidebar {\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t\tmargin-top: 0em;\n" +
                "\t\tmargin-bottom: 0em;\n" +
                "\t}\n" +
                "\n" +
                "\t.p-c-blocktext-grey, .p-c-blocktext, .p-c-box-grey, .p-c-box, .p-c-sidebar-large-grey, .p-c-sidebar-large, .p-c-sidebar-grey, .p-c-sidebar, .p-c-br-blocktext-grey, .p-c-br-blocktext, .p-c-br-box-grey, .p-c-br-box, .p-c-br-sidebar-large-grey, .p-c-br-sidebar-large, .p-c-br-sidebar-grey, .p-c-br-sidebar {\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t\ttext-align: center;\n" +
                "\t\tmargin-top: 0em;\n" +
                "\t\tmargin-bottom: 0em;\n" +
                "\t}\n" +
                "\n" +
                "\t.p-d-blocktext-grey, .p-d-blocktext, .p-d-box-grey, .p-d-box, .p-d-sidebar-large-grey, .p-d-sidebar-large, .p-d-sidebar-grey, .p-d-sidebar, .p-d-br-blocktext-grey, .p-d-br-blocktext, .p-d-br-box-grey, .p-d-br-box, .p-d-br-sidebar-large-grey, .p-d-br-sidebar-large, .p-d-br-sidebar-grey, .p-d-br-sidebar {\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t\ttext-align: right;\n" +
                "\t\tmargin-top: 0em;\n" +
                "\t\tmargin-bottom: 0em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.p-indent-blocktext-grey, .p-indent-blocktext, .p-indent-box-grey, .p-indent-box, .p-indent-sidebar-large-grey, .p-indent-sidebar-large, .p-indent-sidebar-grey, .p-indent-sidebar, .p-indent-br-blocktext-grey, .p-indent-br-blocktext, .p-indent-br-box-grey, .p-indent-br-box, .p-indent-br-sidebar-large-grey, .p-indent-br-sidebar-large, .p-indent-br-sidebar-grey, .p-indent-br-sidebar {\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t\ttext-indent: 1em;\n" +
                "\t\tmargin-top: 0em;\n" +
                "\t\tmargin-bottom: 0em;\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Images */\n" +
                "\n" +
                "\t\n" +
                "\t.margin_image_left, .margin_image_left_net {\n" +
                "\t\ttext-align: left;\n" +
                "\t\tmargin-top: 1.5em;\n" +
                "\t\tmargin-right: 0.7em;\n" +
                "\t}\n" +
                "\n" +
                "\t.margin_image_right, .margin_image_right_net {\n" +
                "\t\ttext-align: right;\n" +
                "\t\tdisplay: block;\n" +
                "\t\tmargin-top: 1.5em;\n" +
                "\t\tmargin-left: 0.7em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.image_right_in_paragraph, .image_right_in_paragraph_net, .image_right_in_paragraph_text_net,  {\n" +
                "\t\tfloat: right;\n" +
                "\t\tmargin-left: 0.7em;\n" +
                "\t\tmargin-top: 1em;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t\tmax-height: 250px;\n" +
                "\t\tmax-width: 250px;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.image_left_in_paragraph, .image_left_in_paragraph_net, .image_left_in_paragraph_text_net {\n" +
                "\t\tfloat: left;\n" +
                "\t\tmargin-right: 0.7em;\n" +
                "\t\tmargin-top: 1em;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t\tmax-height: 250px;\n" +
                "\t\tmax-width: 250px;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.image_left_in_paragraph img {\n" +
                "\t\tmax-height: 250px;\n" +
                "\t\tmax-width: 250px;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.image_right_in_paragraph img {\n" +
                "\t\tmax-height: 250px;\n" +
                "\t\tmax-width: 250px;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t}\n" +
                "\n" +
                "\t.image_left_in_paragraph_net img {\n" +
                "\t\tmax-height: 250px;\n" +
                "\t\tmax-width: 250px;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.image_right_in_paragraph_net img {\n" +
                "\t\tmax-height: 250px;\n" +
                "\t\tmax-width: 250px;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.image_left_in_paragraph_text_net img {\n" +
                "\t\tmax-height: 250px;\n" +
                "\t\tmax-width: 250px;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.image_right_in_paragraph_text_net img {\n" +
                "\t\tmax-height: 250px;\n" +
                "\t\tmax-width: 250px;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t}\n" +
                "\n" +
                "\t.go_to_new_line {\n" +
                "\t\tclear:both;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.illustype_image, .illustype_image_net, .illustype_image_tab, .illustype_image_text, .illustype_image_text_net, .illustype_image_math, .illustype_image_deco, .illustype_image_deco_net, .illustype_image_externe, .image_orig_tab, .image_sep_para, .illustype_image_right_in_paragraph_net, .illustype_image_left_in_paragraph_net, .illustype_image_left_in_paragraph_text_net, .illustype_image_right_in_paragraph_text_net, .illustype_image_right_in_paragraph, .illustype_image_left_in_paragraph {\n" +
                "\t\ttext-align: center;\n" +
                "\t\tmargin-top: 1em;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t}\n" +
                "\n" +
                "\t.illustype_fullpage_image, .illustype_fullpage_image_net, .illustype_fullpage_image_tab, .illustype_fullpage_image_text, .illustype_fullpage_image_text_net, .illustype_fullpage_image_math, .illustype_fullpage_image_deco, .illustype_fullpage_image_deco_net, .fullpage_image_orig_tab, .illustype_fullpage_image_externe {\n" +
                "\t\ttext-align: center;\n" +
                "\t}\n" +
                "\t\n" +
                "\timg {\n" +
                "\t\tmax-width: 100%;\n" +
                "\t\tmax-height: 100%;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.image_deco {\n" +
                "\t\ttext-align: center;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.caption {\n" +
                "\t\tfont-size: 0.9em;\n" +
                "\t\ttext-align: center;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.credits {\n" +
                "\t\tfont-size: 0.7em;\n" +
                "\t\ttext-align: center;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t}\n" +
                "\t.cover_image {\n" +
                "\t      margin-top: 0em;\n" +
                "\t      margin-bottom: 0em;\n" +
                "\t      text-align: center;\n" +
                "\t}\n" +
                "\t.cover .illustype, .cover .illustype_image, .cover .illustype_image_net, .cover .illustype_image_tab, .cover .illustype_image_text, .cover .illustype_image_text_net, .cover .illustype_image_math, .cover .illustype_image_deco, .cover .illustype_image_deco_net, .cover .image_orig_tab {\n" +
                "\t\tmargin-top: 0em;\n" +
                "\t\tmargin-bottom: 0em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.cover img, .titlePage img {\n" +
                "\t\theight: 100%;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.titlePage .illustype_image {\n" +
                "\t\tmargin-top: 0em;\n" +
                "\t\tmargin-bottom: 0em;\n" +
                "\t\t}\n" +
                "\n" +
                "\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Box */\n" +
                "\n" +
                "\t.box {\n" +
                "\t\tdisplay: block;\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\tpadding: 1em;\n" +
                "\t\tborder: 1px solid #333333;\n" +
                "\t\ttext-align: justify;\n" +
                "\t}\n" +
                "\n" +
                "\t.box-grey {\n" +
                "\t\tdisplay: block;\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\tpadding: 1em;\n" +
                "\t\tborder: 1px solid #333333;\n" +
                "\t\tbackground-color: #CCCCCC;\n" +
                "\t\ttext-align: justify;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.blocktext {\n" +
                "\t\tdisplay: block;\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\tpadding: 1em;\n" +
                "\t\ttext-align: justify;\n" +
                "\t}\n" +
                "\n" +
                "\t.blocktext-grey {\n" +
                "\t\tdisplay: block;\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\tpadding: 1em;\n" +
                "\t\tbackground-color: #CCCCCC;\n" +
                "\t\ttext-align: justify;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.sidebar {\n" +
                "\t\tdisplay: block;\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\tpadding: 1em;\n" +
                "\t\ttext-align: justify;\n" +
                "\t}\n" +
                "\n" +
                "\t.sidebar-grey {\n" +
                "\t\tdisplay: block;\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\tpadding: 1em;\n" +
                "\t\tbackground-color: #CCCCCC;\n" +
                "\t\ttext-align: justify;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.sidebar-large {\n" +
                "\t\tdisplay: block;\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\tpadding: 1em;\n" +
                "\t\ttext-align: justify;\n" +
                "\t}\n" +
                "\n" +
                "\t.sidebar-large-grey {\n" +
                "\t\tdisplay: block;\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\tpadding: 1em;\n" +
                "\t\tbackground-color: #CCCCCC;\n" +
                "\t\ttext-align: justify;\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* TOC */\n" +
                "\n" +
                "\t.toc_entry {\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.toc_entry_part {\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-top: 1em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.toc_entry_chapter {\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* MINITOC */\n" +
                "\n" +
                "\t.minitoc {\n" +
                "\t\tborder-top: 1px solid #555555;\n" +
                "\t\tborder-bottom: 1px solid #555555;\n" +
                "\t\tmargin-left: 2em;\n" +
                "\t\tpadding-top: 1em;\n" +
                "\t\tpadding-bottom: 1em;\n" +
                "\t}\n" +
                "\n" +
                "\t.title-minitoc {\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\tfont-size: 1em;\n" +
                "\t\tfont-style: normal;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tmargin-top: 0em;\n" +
                "\t\ttext-align: left;\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t}\n" +
                "\n" +
                "\t.minitoc .toc_entry_section1 {\n" +
                "\t\tfont-size: 0.8em;\n" +
                "\t}\n" +
                "\t.minitoc .toc_entry_section2 {\n" +
                "\t\tfont-size: 0.7em;\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Index */\n" +
                "\n" +
                "\t.index_section {\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tfont-size: 1em;\n" +
                "\t\tmargin-top: 0em;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Tables */\n" +
                "\n" +
                "\t.tableau {\n" +
                "\t\tdisplay: block;\n" +
                "\t\tmargin-top: 1.5em;\n" +
                "\t\tmargin-bottom: 1.5em;\n" +
                "\t\tpadding: 0.25em;\n" +
                "\t}\n" +
                "\t\n" +
                "\ttable\n" +
                "\t{\n" +
                "\t\tborder-collapse: collapse;\n" +
                "\t\tmargin-top: 1em;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t\tmargin-left:auto;\n" +
                "\t   \tmargin-right:auto;\n" +
                "\t   \tfont-size: small;\n" +
                "\t   \ttext-align: left;\n" +
                "\t}\n" +
                "\t\n" +
                "\tth {\n" +
                "\t\tfont-weight: bold;\n" +
                "\t}\t\n" +
                "\t\n" +
                "\t.filet_l\n" +
                "\t{\n" +
                "\t\tborder-left: thin solid;\n" +
                "\t\tpadding:10px;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.filet_r\n" +
                "\t{\n" +
                "\t\tborder-right: thin solid;\n" +
                "\t\tpadding:10px;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.filet_t\n" +
                "\t{\n" +
                "\t\tborder-top: thin solid;\n" +
                "\t\tpadding:10px;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.filet_b\n" +
                "\t{\n" +
                "\t\tborder-bottom: thin solid;\n" +
                "\t\tpadding:10px;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.filet_x\n" +
                "\t{\n" +
                "\t\tpadding:10px;\n" +
                "}\n" +
                "\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Lists, no marge in list otherwise displayed inline by Kindle*/\n" +
                "\n" +
                "\tol, ul {\n" +
                "\t\tmargin-left: 2.5em;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\tlist-style: none;\n" +
                "\t\ttext-indent: -1em;\n" +
                "\t}\n" +
                "\n" +
                "\tp ul, p ol {\n" +
                "\t\tmargin-top: 0em;\n" +
                "\t}\n" +
                "\n" +
                "\tul.bl {\n" +
                "\t\tlist-style: disc;\n" +
                "\t\ttext-indent: 0em;\n" +
                "\t}\n" +
                "\n" +
                "\tol.al {\n" +
                "\t\tlist-style: lower-latin;\n" +
                "\t\ttext-indent: 0em;\n" +
                "\t}\n" +
                "\n" +
                "\tol.nl {\n" +
                "\t\tlist-style: decimal;\n" +
                "\t\ttext-indent: 0em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.li {}\n" +
                "\t\n" +
                "\t\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Footnotes */\n" +
                "\n" +
                "\t.footnote {\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\tfont-size: 1em;\n" +
                "\t}\n" +
                "\n" +
                "\t\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Endnotes */\n" +
                "\n" +
                "\t.endnotes {\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-bottom: 1em;\n" +
                "\t}\n" +
                "\t\n" +
                "\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Computer & scri */\n" +
                "\n" +
                "\t.computer  {\n" +
                "\t\tfont-family : monospace;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\ttext-indent: 0em;\n" +
                "\t\tfont-size: 0.8em;\n" +
                "\t}\n" +
                "\n" +
                "\t.scri  {\n" +
                "\t\tfont-family : monospace;\n" +
                "\t\tfont-size: 0.8em;\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Verse Source */\n" +
                "\n" +
                "\t.source {\n" +
                "\t\tmargin-left: 15%;\n" +
                "\t\tmargin-right: 15%;\n" +
                "\t\ttext-align: right;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t\n" +
                "\t.verse {\n" +
                "\t\tmargin-left: 15%;\n" +
                "\t\tmargin-right: 15%;\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\ttext-align: left;\n" +
                "\t}\n" +
                "\t\n" +
                "\t\n" +
                "\t.source-blocktext-grey, .source-blocktext, .source-box-grey, .source-box, .source-sidebar-large-grey, .source-sidebar-large, .source-sidebar-grey, .source-sidebar {\n" +
                "\t\ttext-align: right;\n" +
                "\t\tmargin-bottom: 0.5em;\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t}\n" +
                "\n" +
                "\t.verse-blocktext-grey, .verse-blocktext, .verse-box-grey, .verse-box, .verse-sidebar-large-grey, .verse-sidebar-large, .verse-sidebar-grey, .verse-sidebar {\n" +
                "\t\tmargin-top: 0.5em;\n" +
                "\t\ttext-align: right;\n" +
                "\t\tmargin-left: 1em;\n" +
                "\t\tmargin-right: 1em;\n" +
                "\t}\n" +
                "\t\n" +
                "\t\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Glossaire */\n" +
                "\n" +
                "\t.termdef {\n" +
                "\t}\n" +
                "\t\n" +
                "\t.term {\n" +
                "\t}\n" +
                "\t\n" +
                "\t.def {\n" +
                "\t}\n" +
                "\t\n" +
                "\t.definition {\n" +
                "\t}\n" +
                "\n" +
                "\t\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Divers */\n" +
                "\t\n" +
                "\t.copyrights {\n" +
                "\t\ttext-align: center;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.isbn {\n" +
                "\t\ttext-align: center;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.author {\n" +
                "\t\ttext-align: center;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.author-blocktext, .author-box {\n" +
                "\t\ttext-align: center;\n" +
                "\t}\n" +
                "\t\n" +
                "\t.contributor {\n" +
                "\t}\n" +
                "\t\n" +
                "\t.serie {\n" +
                "\t}\n" +
                "\t\n" +
                "\t.translator {\n" +
                "\t}\n" +
                "\t\n" +
                "\t.publisher {\n" +
                "\t}\n" +
                "\n" +
                "/* ---------------------------------------------------------- */\n" +
                "/* Flashcards */\n" +
                "\t.flashcard {\n" +
                "\t}\n" +
                "\t\n" +
                "\t.question {\n" +
                "\t}\n" +
                "\t\n" +
                "\t.answer {\n" +
                "\t}";

        assertEquals(defaultCSS,eBook.getDefaultCSS().get());
    }

}
