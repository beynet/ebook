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
        basicTestOnPackage(epub.getOpfDocument());


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
        basicTestOnPackage(epub.getOpfDocument());


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
        basicTestOnPackage(epub.getOpfDocument());


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
        basicTestOnPackage(epub.getOpfDocument());
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
        Package packageDoc = epub.getOpfDocument();
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
        assertEquals(Integer.valueOf(1),Integer.valueOf(metadata.getIdentifiers().size()));
        assertEquals(identifier,metadata.getIdentifiers().get(0));

        Reference ref = new Reference();
        ref.setHref("Text/couverture.xhtml");
        ref.setTitle("Cover");
        ref.setType("cover");
        Guide guide = new Guide();
        guide.getReferences().add(ref);

        assertEquals(guide,epub.getOpfDocument().getGuide());
    }

    @Test
    public void path() throws IOException {
        EPub epub = new EPub(Paths.get("src/test/resources/books/univers multiples I _ Temps, Les - Stephen Baxter.epub"));
        Path test = Files.createTempDirectory("test");
        Path expected=test.resolve("Science-Fiction").resolve("Stephen Baxter").resolve("Les univers multiples I  Temps.epub");

        basicTestOnPackage(epub.getOpfDocument());
        Metadata metadata = epub.getOpfDocument().getMetadata();
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
        Optional<String> nextPage = eBook.getFirstPage();
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
        {
            EBook copy =  EBookFactory.createEBook(book);
            assertEquals(thirdPage,copy.getCurrentPage().get());
        }

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

        {
            EBook copy =  EBookFactory.createEBook(book);
            assertEquals(lastPageExpected,copy.getCurrentPage().get());
        }

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
                "\t}\n";

        assertEquals(defaultCSS,eBook.getDefaultCSS().get());
    }

    @Test
    public void leMondeDiplo() throws IOException {
        Path lmd = Paths.get("./src/test/resources/books/Juillet 2019.epub");
        EBook eBook = EBookFactory.createEBook(lmd);
        Optional<String> firstPage = eBook.getFirstPage();
        final String expectedFirstPage =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE html>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n" +
                "   <head>\n" +
                "      <meta charset=\"utf-8\" />\n" +
                "      <link href='ebook_diplo_base.css' rel='stylesheet' media='all' type='text/css' />\n" +
                "      <link href='ebook_diplo_plus.css' rel='stylesheet' media='all' type='text/css' />\n" +
                "        <style type=\"text/css\">\n" +
                "            img{\n" +
                "                max-width:100%;\n" +
                "            }\n" +
                "        </style>\n" +
                "   </head>\n" +
                "   <body xml:lang=\"fr\">\n" +
                "      <section class=\"base\" id=\"cover\">\n" +
                "         <div id=\"cover-image\">\n" +
                "           <img src=\"cover.jpg\" style=\"max-width:100%;\" alt=\"Juillet 2019\" />\n" +
                "         </div>\n" +
                "      </section>\n" +
                "   </body>\n" +
                "</html>";
        assertEquals(expectedFirstPage,firstPage.get());

        Optional<String> secondPage = eBook.getNextPage();
        final String expectedSecondPage = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "<link href='../ebook_diplo_base.css' rel='stylesheet' media='all' type='text/css' />\n" +
                "<link href='../ebook_diplo_plus.css' rel='stylesheet' media='all' type='text/css' />\n" +
                "<title>…</title>\n" +
                "</head>\n" +
                "<body xml:lang=\"fr\" lang=\"fr\">\n" +
                "<div id=\"conteneur\">\n" +
                "<div class=\"tetiere\"><i>Le Monde diplomatique</i>, juillet 2019 : au sommaire</div>\n" +
                "<div id=\"contenu\" class=\"sommaire\">\n" +
                "<ul>\n" +
                "<li class=\"liste\">\n" +
                "<ul class=\"hermetique une\">\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60058\">\n" +
                "<h3><a href='48297a6e74f19d19617e8a796ba1a73d.xhtml'>Libre-échange ou écologie !</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\"><span class=\"inedit edito\">Editorial,</span> Serge Halimi <span class=\"pages\">• page 1</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60058 intro lintro\">En remportant 10 % des sièges lors de l’élection du Parlement européen, les écologistes ont réveillé un vieux débat sur le positionnement politique de leur mouvement. Est-il plutôt de gauche, ou plutôt libéral ? A priori, libéralisme et protection de l’environnement devraient former un couple explosif.</div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60012\">\n" +
                "<h3><a href='da1b86bd0419ac8fd575cb9eb7f65131.xhtml'>« Quelle est votre race ? »</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Benoît Bréville <span class=\"pages\">• pages 1 et 23</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60012 intro lintro\">Êtes-vous « noir », « blanc », « amérindien », « asiatique »… ? Depuis plus de deux siècles, les résidents américains doivent déclarer leur « race » aux agents du recensement. Instrument des politiques de lutte contre la discrimination, les statistiques ethniques ainsi obtenues ont fini par renforcer le sentiment d’appartenance identitaire. Au risque de légitimer les divisions qu’elles étaient supposées combattre.</div>\n" +
                "</div>\n" +
                "</li>\n" +
                "</ul>\n" +
                "</li>\n" +
                "<li class=\"seul\">\n" +
                "<h3><a href='83fa9f20c0f1c1919d7f43e851e8b990.xhtml'>La page deux</a></h3>\n" +
                "<div class=\"intro\">« Rectificatif », courriers, coupures de presse.</div>\n" +
                "</li>\n" +
                "<li class=\"dossiers liste\">\n" +
                "<div class=\"dossier\">\n" +
                "<ul class=\"hermetique\">\n" +
                "<li class=\"une\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60076\">\n" +
                "<h3><span class=\"pretitre\">Dossier</span> <a href='75a00c40dfda91e1354b47e333c95d73.xhtml'>L’avenir de l’usine</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\"><span class=\"pages\">• page 17</span></div>\n" +
                "</div>\n" +
                "<div class=\"intro\">La contribution de l’industrie dans l’économie a été divisée par deux, parfois par trois, dans l’ensemble des pays riches depuis 1970. Pour les uns, le phénomène résulte d’une évolution naturelle : comme un papillon émerge de la chrysalide, l’économie passerait spontanément de l’usine au bureau. D’autres suggèrent que la désindustrialisation s’explique avant tout par un choix politique : délocaliser les ateliers vers les pays du sud, moins coûteux pour le patronat. Alors que l’industrie demeure l’une des principales sources d’emploi, sa relance soulève des oppositions idéologiques, techniques et environnementales. La seule loi du marché pourra-t-elle y répondre ?</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60022\">\n" +
                "<h3><a href='75a00c40dfda91e1354b47e333c95d73.xhtml#ancre60022'>Réconcilier l’industrie et la nature</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Jean Gadrey <span class=\"pages\">• pages 1, 20 et 21</span></div>\n" +
                "</div>\n" +
                "<div class=\"intro\">Ayant associé le développement économique et l’amélioration des conditions de vie, les forces politiques progressistes ont longtemps négligé l’impact des activités humaines sur l’environnement. L’urgence de protéger la planète impliquerait-elle de renoncer aux bienfaits de la société industrielle ? Pas nécessairement, dès lors que mutent certaines des habitudes de consommation auxquelles elle a donné naissance.</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60025\">\n" +
                "<h3><a href='75a00c40dfda91e1354b47e333c95d73.xhtml#ancre60025'>Idées reçues sur la relance</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Laura Raim <span class=\"pages\">• page 17</span></div>\n" +
                "</div>\n" +
                "<div class=\"intro\">« L’industrie, c’est fini, place aux services », « L’État n’a pas à se mêler de ça », « L’innovation vient toujours du privé », « La compétitivité exige de réduire le coût du travail », « Le protectionnisme est inefficace et dangereux » : Laura Raim déconstruit cinq préjugés sur la relance économique.</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60085\">\n" +
                "<h3><a href='75a00c40dfda91e1354b47e333c95d73.xhtml#ancre60085'>Quand le Sud misait sur la« bourgeoisie nationale »</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Vivek Chibber <span class=\"pages\">• pages 18 et 19</span></div>\n" +
                "</div>\n" +
                "<div class=\"intro\">Suffit-il à l’État de venir en aide aux entrepreneurs nationaux pour développer son secteur industriel ? Au cours des années 1960 et 1970, plusieurs pays ont opté pour cette stratégie, se heurtant à chaque fois aux mêmes difficultés.</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60084\">\n" +
                "<h3><a href='75a00c40dfda91e1354b47e333c95d73.xhtml#ancre60084'>L’Aube sur un fil</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Maurice Midena <span class=\"pages\">• pages 18 et 19</span></div>\n" +
                "</div>\n" +
                "<div class=\"intro\">Le mouvement de relocalisation d’activités industrielles au sein de l’Hexagone suscite un engouement pour le « made in France ». Ranimer une filière n’a toutefois rien d’aisé, car on a détruit plus que des emplois en licenciant des salariés.</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60079\">\n" +
                "<h4><a href='75a00c40dfda91e1354b47e333c95d73.xhtml#ancre60079'>Entreprises frénétiques</a></h4>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\"><span class=\"pages\">• page 19</span></div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60023\">\n" +
                "<h4><a href='75a00c40dfda91e1354b47e333c95d73.xhtml#ancre60023'>Façonner demain</a></h4>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\"><span class=\"pages\">• pages 20 et 21</span></div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60024\">\n" +
                "<h4><a href='75a00c40dfda91e1354b47e333c95d73.xhtml#ancre60024'>La condition de l’indépendance</a></h4>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\"><span class=\"pages\">• page 21</span></div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60086\">\n" +
                "<h4><a href='75a00c40dfda91e1354b47e333c95d73.xhtml#ancre60086'>Laminage</a></h4>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Cécile Marin <span class=\"pages\">•  Cartographie</span></div>\n" +
                "</div>\n" +
                "</li>\n" +
                "</ul>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li class=\"liste\">\n" +
                "<ul>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60057\">\n" +
                "<h3><a href='ff01e338e14a37e99aee869a3450728f.xhtml'>Keynes et le prix de la paix</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Alain Garrigou <span class=\"et\">&amp;</span> Jean-Paul Guichard <span class=\"pages\">• page 3</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60057 intro lintro\">Un siècle après sa signature, le 28 juin 1919, le traité de Versailles est généralement abordé à la lumière de ses conséquences supposées : accablant l’Allemagne, il aurait favorisé la montée du nazisme. Les conditions concrètes de son élaboration sont en revanche souvent négligées, et notamment le rôle de certains personnages-clés, tel l’économiste John Maynard Keynes.</div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60019\">\n" +
                "<h3><a href='658eaf5ba6d873a1c5dcebc1445a957e.xhtml'>En Corse, « il y a un éléphant dans le salon »</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Jean-François Bernardini <span class=\"pages\">• pages 4 et 5</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60019 intro lintro\">Après l’adieu aux armes et les victoires électorales, les autonomistes corses sont confrontés à l’exercice du pouvoir depuis 2015. La relance de la production locale et la lutte contre la désertification des zones rurales restent des défis majeurs. Et, si le projet de réforme constitutionnelle consacre la reconnaissance d’un statut particulier, les relations avec Paris demeurent marquées par la défiance.</div>\n" +
                "</div>\n" +
                "<ul class=\"voiraussis\">\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<h4><a href='658eaf5ba6d873a1c5dcebc1445a957e.xhtml#ancre60017'>Vivre et travailler au pays</a></h4>\n" +
                "<div class=\"dates_auteurs\">Dominique Franceschetti <span class=\"pages\">• page 5</span></div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60018\">\n" +
                "<h3><a href='658eaf5ba6d873a1c5dcebc1445a957e.xhtml#ancre60018'>Un trésor agricole et pastoral</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\"><span title='Dominique Franceschetti' class='abbr'>D. F.</span> <span class=\"pages\">• pages 4 et 5</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60018 intro lintro\">On ne compte plus les reportages, les livres, les films célébrant la « beauté sauvage » des paysages corses, villages haut perchés, maquis délicieusement odorant et impénétrable, sommets enneigés, vastes forêts... Cette vision idyllique masque une réalité dont les Corses n’ont aucune raison de se réjouir.</div>\n" +
                "</li>\n" +
                "</ul>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60056\">\n" +
                "<h3><a href='ddbf782c20bf13327e03a18ff8e404bc.xhtml'>Le cadeau empoisonné du tourisme culturel</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Geneviève Clastres <span class=\"pages\">• page 6</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60056 intro lintro\">Chaque année, une cinquantaine de sites naturels ou culturels se portent candidats à l’inscription sur la liste du patrimoine mondial pour se voir accorder une protection au bénéfice de toute l’humanité. Cependant, en délivrant ce label, l’Unesco oriente aussi fortement les flux touristiques. Un appel d’air rémunérateur, mais qui peut s’avérer ravageur.</div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60011\">\n" +
                "<h3><a href='f6f2e1a5f2afe583c3ef4da90fc6cd84.xhtml'>Le non-procès de la violence néonazie</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Massimo Perinelli <span class=\"et\">&amp;</span> Christopher Pollmann <span class=\"pages\">• page 7</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60011 intro lintro\">Une cellule néonazie, des meurtres en série, une police qui regarde ailleurs : tels sont les ingrédients d’un drame qui hante l’Allemagne depuis le début des années 2000. Instruit de 2013 à 2018 à Munich, le procès a révélé par ses carences mêmes les ambiguïtés des services de sécurité ainsi que de l’institution judiciaire vis-à-vis de la violence d’extrême droite.</div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60038\">\n" +
                "<h3><a href='c5987ed5eb1a227c8b6733905b5462ce.xhtml'>Géopolitique de la crise vénézuélienne</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Alexander Main <span class=\"pages\">• pages 8 et 9</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60038 intro lintro\">L’offensive de Washington contre le président vénézuélien Nicolás Maduro s’est appuyée sur l’assentiment des dirigeants conservateurs de la région, désormais majoritaires. Grâce à eux, l’interventionnisme américain a pu se grimer en préoccupation humanitaire… Mais le jusqu’au-boutisme de l’administration Trump semble être parvenu à exaspérer la droite latino-américaine, pourtant docile.</div>\n" +
                "</div>\n" +
                "<ul class=\"voiraussis\">\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60016\">\n" +
                "<h3><a href='c5987ed5eb1a227c8b6733905b5462ce.xhtml#ancre60016'>Le retour des pieuvres médiatiques</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Anne-Dominique Correa <span class=\"pages\">• pages 8 et 9</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60016 intro lintro\">Argentine, Équateur, Brésil : partout, le même scénario. Des dirigeants conservateurs parviennent au pouvoir après une longue période de gouvernements de gauche. À peine sont-ils élus qu’une urgence les anime : détricoter les mesures de réglementation de la presse qu’avaient instaurées leurs prédécesseurs pour encadrer le pouvoir politique des médias privés.</div>\n" +
                "</li>\n" +
                "</ul>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60035\">\n" +
                "<h3><a href='160fe2605e0d3183bb72c3f55876abc3.xhtml'>En Inde, comment remporter les élections avec un bilan désastreux</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Christophe Jaffrelot <span class=\"pages\">• pages 10 et 11</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60035 intro lintro\">À la suite de sa victoire électorale de 2014, le premier ministre indien Narendra Modi avait convié son homologue pakistanais à sa prestation de serment, laissant espérer des négociations de paix. Cinq ans plus tard, il l’a exclu des cérémonies d’investiture. lors de la campagne des législatives, M. Modi a misé sur la peur de l’ennemi traditionnel, ainsi que sur le nationalisme hindou.</div>\n" +
                "</div>\n" +
                "<ul class=\"voiraussis\">\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<h4><a href='160fe2605e0d3183bb72c3f55876abc3.xhtml#ancre60034'>Mainmise sur le Parlement</a></h4>\n" +
                "<div class=\"dates_auteurs\"><span title='Christophe Jaffrelot' class='abbr'>Ch. J.</span> <span class=\"pages\">• page 11</span></div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<h4><a href='160fe2605e0d3183bb72c3f55876abc3.xhtml#ancre60077'>L’inde, après les élection de 2019</a></h4>\n" +
                "<div class=\"dates_auteurs\"><span title='Cécile Marin' class='abbr'>C. M.</span> <span class=\"pages\">• Cartographie</span></div>\n" +
                "</div>\n" +
                "</li>\n" +
                "</ul>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60036\">\n" +
                "<h3><span class=\"pages\"><a href='3f4dca9e44f00bf5afb75046202d8870.xhtml'>Les éternels disparus du Liban</a></span></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\"><span class=\"pages\">Emmanuel Haddad <span class=\"pages\">• page 12</span></span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60036 intro lintro\">Le sort des disparus durant la guerre civile libanaise (1975-1990) — pour la plupart victimes d’enlèvement — ne semble guère intéresser des autorités politiques soucieuses de tourner la page pour favoriser la reconstruction du pays. Mais la mobilisation des familles concernées empêche l’oubli de s’installer et contribue à documenter l’un des épisodes les plus tragiques du conflit.</div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60053\">\n" +
                "<h3><a href='40b397a1b8ed1473256dfd87c4387e26.xhtml'>Sahel, les militaires évincent le Quai d’Orsay</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Rémi Carayol <span class=\"pages\">• page 13</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60053 intro lintro\">Malgré un important déploiement armé (opération française « Barkhane », mission des Nations unies, etc.), les massacres de civils se multiplient au Mali et dans la sous-région. Cause méconnue de cette impasse : le Sahel est devenu la chasse gardée des militaires, qui imposent aux diplomates du Quai d’Orsay une vision trop étroitement sécuritaire pour être efficace.</div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60037\">\n" +
                "<h3><a href='1a405368e1a878ad10ef134ff14cd7a1.xhtml'>Les Louises en insurrection</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Éloi Valat <span class=\"pages\">• pages 14 et 15</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60037 intro lintro\">Blanchisseuses, relieuses, cantinières, journalistes… celles que leurs adversaires appelleront les « pétroleuses » interviennent splendidement dans les combats de la Commune. Elles sont privées du droit de vote, mais elles se font entendre dans les clubs de quartier, demandent l’égalité des salaires et la création de crèches, engagent la reconnaissance de l’union libre. La Commune fut exterminée, les idées et les idéaux survécurent.</div>\n" +
                "</div>\n" +
                "<ul class=\"voiraussis\">\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<h4><a href='1a405368e1a878ad10ef134ff14cd7a1.xhtml#ancre60015'>« Vivre libres »</a></h4>\n" +
                "<div class=\"dates_auteurs\"><span class=\"pages\">• page 15</span></div>\n" +
                "</div>\n" +
                "</li>\n" +
                "</ul>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60026\">\n" +
                "<h3><a href='8c460c9acb634a60e89c628bf5da6fb4.xhtml'>Europe de la défense, une armée de papier</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Philippe Leymarie <span class=\"pages\">• page 16</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60026 intro lintro\">Le 18 avril dernier, le Parlement de Strasbourg a approuvé la création du Fonds européen de la défense. Doté de 13 milliards d’euros, il financera des projets industriels intéressant plusieurs États. Mais au service de quelle vision stratégique ? Depuis trente ans, l’Union bricole des outils militaires et techniques sans parvenir à donner corps à une véritable politique de sécurité.</div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60033\">\n" +
                "<h3><a href='53e1a6a8033b7401e9d547c19e136d33.xhtml'>La bagarre de l’hectare</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Lucile Leclair <span class=\"pages\">• page 22</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60033 intro lintro\">Le désengagement de l’État en milieu rural et le dévoiement de ses outils de régulation se manifestent par l’inflation des prix des terres cultivables. En abandonnant au marché cette ressource limitée et non reproductible, les pouvoirs publics entravent l’installation de jeunes exploitants et fragilisent la profession agricole, qui peine à assurer son renouvellement générationnel.</div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60049\">\n" +
                "<h3><a href='c6f3c185eb5a403798ebcb8d84ad1a2e.xhtml'>L’art de détourner George Orwell</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Thierry Discepolo <span class=\"pages\">• page 27</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60049 intro lintro\">Les références à l’auteur de « 1984 » se sont multipliées depuis une vingtaine d’années. Alors que ses engagements revendiqués l’ancraient à gauche, c’est désormais une pensée néoconservatrice qui se revendique de son œuvre. Récupération d’ambiguïtés possibles ou dévoiement ?</div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"tige\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60014\">\n" +
                "<h3><a href='eea4b35800f8288b512ead004a5fab3e.xhtml'>Rigolez, vous êtes exploité</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Julien Brygo <span class=\"pages\">• page 28</span></div>\n" +
                "</div>\n" +
                "<div class=\"crayon article-descriptif-60014 intro lintro\">Des conditions de travail déplorables, des contraintes de rentabilité qui interdisent d’améliorer le sort du personnel, des salariés qui préfèrent mettre fin à leurs jours plutôt que d’endurer leur activité professionnelle ? Il fallait réagir. C’est chose faite grâce à une initiative de la DRH du Centre hospitalier universiaire de Toulouse : des séances de rigologie, cette « approche globale permettant une harmonie entre le corps, l’esprit et les émotions ».</div>\n" +
                "</div>\n" +
                "</li>\n" +
                "</ul>\n" +
                "</li>\n" +
                "<li class=\"seul\">\n" +
                "<h3><a href='033b8fb81a19f90aba4bf45fbac1ab2a.xhtml'>Les livres du mois</a></h3>\n" +
                "</li>\n" +
                "<li class=\"dossiers liste\">\n" +
                "<div class=\"dossier supp\">\n" +
                "<ul class=\"hermetique\">\n" +
                "<li class=\"une\">\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60070\">\n" +
                "<h3><span class=\"pretitre\">Supplément</span> <a href='bb95cec6daa0b8be78efe9be7a8d7c07.xhtml'>La santé pour tous, un défi planétaire</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\"><span class=\"pages\">• page I</span></div>\n" +
                "</div>\n" +
                "<div class=\"intro\">L’Afrique demeure, de loin, le continent le plus touché par les trois pandémies les plus meurtrières : le sida, le paludisme et la tuberculose. Frein au développement, celles-ci frappent en priorité les populations pauvres dans des pays où les systèmes de santé ont été affaiblis par les politiques néolibérales des années 1990. Leur élimination d’ici à 2030 figure parmi les Objectifs de développement durable adoptés par les Nations unies, avec une attention particulière accordée à la santé des jeunes et des femmes. L’aide internationale ne doit pas faiblir.</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60071\">\n" +
                "<h3><a href='bb95cec6daa0b8be78efe9be7a8d7c07.xhtml#ancre60071'>Abidjan se mobilise contre le sida</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Christelle Gérand <span class=\"pages\">• pages I, II et III</span></div>\n" +
                "</div>\n" +
                "<div class=\"intro\">Dans ce pays, le sida provoque 24 000 morts par an, selon l’Onusida. Le gouvernement affiche sa volonté d’éradiquer le sida à l’horizon 2030 dans le cadre de la poursuite des Objectifs de développement durable (ODD). Cependant, les autorités et les associations locales rencontrent des difficultés à toucher les populations concernées.</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60060\">\n" +
                "<h3><a href='bb95cec6daa0b8be78efe9be7a8d7c07.xhtml#ancre60060'>Comment réussir la transition démographique au Sahel</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Aïssa Diarra <span class=\"pages\">• pages II et III</span></div>\n" +
                "</div>\n" +
                "<div class=\"intro\">L’autonomie des adolescentes est devenue un objectif prioritaire de santé publique au Sahel. Considéré comme une des clés du développement, le contrôle de la fécondité implique l’amélioration de la condition matérielle des femmes et leur émancipation de certaines normes socioculturelles dans une région ravagée par les inégalités et la pauvreté.</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60059\">\n" +
                "<h3><a href='bb95cec6daa0b8be78efe9be7a8d7c07.xhtml#ancre60059'>Le Bénin en pointe contre la tuberculose</a></h3>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Khadija Sylva <span class=\"pages\">• page IV</span></div>\n" +
                "</div>\n" +
                "<div class=\"intro\">Dès 1983, le Bénin a lancé des traitements courts dans le cadre de la relance du programme national de lutte contre la tuberculose. Le pays s’inscrit depuis dans une politique d’éradication de la maladie. Pourtant, malgré ces efforts, le nombre de personnes infectées a augmenté de 12 % entre 2017 et 2018, pour atteindre quatre mille cas.</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60062\">\n" +
                "<h4><a href='bb95cec6daa0b8be78efe9be7a8d7c07.xhtml#ancre60062'>Vers un accès universel aux soins</a></h4>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Peter Sands <span class=\"et\">&amp;</span> Stéphanie Seydoux <span class=\"pages\">• page I</span></div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60061\">\n" +
                "<h4><a href='bb95cec6daa0b8be78efe9be7a8d7c07.xhtml#ancre60061'>Pour une Afrique égalitaire</a></h4>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\">Monica Geingos <span class=\"pages\">• page IV</span></div>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li>\n" +
                "<div class=\"titraille\">\n" +
                "<div class=\"crayon article-titre-60083\">\n" +
                "<h4><a href='bb95cec6daa0b8be78efe9be7a8d7c07.xhtml#ancre60083'>Pauvre et femme : la double peine</a></h4>\n" +
                "</div>\n" +
                "<div class=\"dates_auteurs\"><span class=\"pages\">• pages II et III</span> <span class=\"pages\">•  Cartographie</span></div>\n" +
                "</div>\n" +
                "</li>\n" +
                "</ul>\n" +
                "</div>\n" +
                "</li>\n" +
                "<li><a href='49e6e3fa3288239e59067144fa0968f1.xhtml' style=\"display:none;\"></a></li>\n" +
                "</ul>\n" +
                "<div class=\"encadre notes\">Les pages mentionnées dans ce sommaire sont celles de la version imprimée</div>\n" +
                "</div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
        assertEquals(expectedSecondPage,secondPage.get());
    }


    @Test
    public void lmdLoadFomTableOfContent() throws IOException {
        String currentPage = "c-f2bd7cf3d027096262aa9aec81e1bd9e";
        String expectedPage = "da1b86bd0419ac8fd575cb9eb7f65131.xhtml";
        Path lmd = Paths.get("./src/test/resources/books/Juillet 2019.epub");
        EBook eBook = EBookFactory.createEBook(lmd);
        eBook.getFirstPage();
        eBook.getNextPage();
        Optional<String> expected = eBook.loadPage(expectedPage);
        assertTrue(expected.isPresent());
    }

    @Test
    public void lmdLoadFomTableOfContentWithAnchor() throws IOException {
        String currentPage = "c-f2bd7cf3d027096262aa9aec81e1bd9e";
        String expectedPage = "75a00c40dfda91e1354b47e333c95d73.xhtml#ancre60084";
        Path lmd = Paths.get("./src/test/resources/books/Juillet 2019.epub");
        EBook eBook = EBookFactory.createEBook(lmd);
        eBook.getFirstPage();
        eBook.getNextPage();
        Optional<String> expected = eBook.loadPage(expectedPage);
        assertTrue(expected.isPresent());
    }

    @Test
    public void lmdLoadRessourceLocal1() throws IOException {
        Path lmd = Paths.get("./src/test/resources/books/Juillet 2019.epub").toAbsolutePath();
        String expectedURI = "jar:"+lmd.toUri().toString()+"!/cover.jpg";
        EBook eBook = EBookFactory.createEBook(lmd);
        eBook.getFirstPage();
        Optional<String> found = eBook.convertRessourceLocalPathToGlobalURL("cover.jpg");
        assertEquals(expectedURI,found.get());
    }

    @Test
    public void lmdLoadRessourceLocal2() throws IOException {
        Path lmd = Paths.get("./src/test/resources/books/Juillet 2019.epub").toAbsolutePath();
        String expectedURI = "jar:"+lmd.toUri().toString()+"!/pages/../images/img023-10-7febf5.jpg";
        EBook eBook = EBookFactory.createEBook(lmd);
        eBook.getFirstPage();
        eBook.getNextPage();
        eBook.getNextPage();
        eBook.getNextPage();
        Optional<String> found = eBook.convertRessourceLocalPathToGlobalURL("../images/img023-10-7febf5.jpg");
        assertEquals(expectedURI,found.get());
    }




    @Test
    public void saskiadLoadFomTableOfContent() throws IOException {
        String currentPage = "toc";
        String expectedPage = "e9782919755394_c04.html";
        Path lmd = Paths.get("./src/test/resources/books/Livre de Saskia, Le 2 - Pavlenko, Marie.epub");
        EBook eBook = EBookFactory.createEBook(lmd);
        eBook.getFirstPage();
        eBook.getNextPage();
        eBook.getNextPage();
        Optional<String> expected = eBook.loadPage(expectedPage);
        assertTrue(expected.isPresent());
    }

}
