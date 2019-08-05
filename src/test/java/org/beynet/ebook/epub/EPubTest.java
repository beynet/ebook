package org.beynet.ebook.epub;

import org.beynet.AbstractTests;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EbookCopyOption;
import org.beynet.ebook.epub.opf.Package;
import org.beynet.ebook.epub.opf.*;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class EPubTest extends AbstractTests {

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
    public void changeSubjects() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/Hunger Games.epub"));
        assertThat(epub.isProtected(),is(false));
        assertThat(epub.getTitle().get(),is("Hunger Games"));
        assertThat(epub.getAuthor().get(),is("Collins Suzanne"));
        assertThat(epub.getSubjects().size(),is(Integer.valueOf(0)));
        basicTestOnPackage(epub.getPackageDoc());


        Path test = Files.createTempFile("test", ".epub");

        try {
            EBook result = epub.copy(test, StandardCopyOption.REPLACE_EXISTING);
            assertThat(result.getSubjects().size(), is(Integer.valueOf(0)));
            result.getSubjects().add("Science-Fiction");
            result.getSubjects().add("truc1");
            result.updateSubjects();

            result = new EPub(test);
            assertThat(result.getSubjects().size(), is(Integer.valueOf(2)));
            assertThat(result.getSubjects().get(0), is("Science-Fiction"));
            assertThat(result.getSubjects().get(1), is("truc1"));
        } finally {
            Files.deleteIfExists(test);
        }
    }

    @Test
    public void changeAuthorWithRole() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/Hunger Games.epub"));
        assertThat(epub.isProtected(),is(false));
        assertThat(epub.getTitle().get(),is("Hunger Games"));
        assertThat(epub.getAuthor().get(),is("Collins Suzanne"));
        basicTestOnPackage(epub.getPackageDoc());


        Path test = Files.createTempFile("test", ".epub");

        try {
            EBook result = epub.copy(test, StandardCopyOption.REPLACE_EXISTING);
            result.changeAuthor("Suzanne Collins");

            result = new EPub(test);
            assertThat(result.getAuthor().get(),is("Suzanne Collins"));
        } finally {
            Files.deleteIfExists(test);
        }
    }

    @Test
    public void changeAuthorWithNoRole() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/A Fire Upon The Deep.epub"));
        assertThat(epub.getAuthor().get(),is("Vinge, Vernor"));
        basicTestOnPackage(epub.getPackageDoc());


        Path test = Files.createTempFile("test", ".epub");

        try {
            EBook result = epub.copy(test, StandardCopyOption.REPLACE_EXISTING);
            result.changeAuthor("Vernors Vinge");

            result = new EPub(test);
            assertThat(result.getAuthor().get(),is("Vernors Vinge"));
        } finally {
            Files.deleteIfExists(test);
        }
    }


    @Test
    public void isWithDRM() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/DRM Hunger Games II. Lembrasement .epub"));
        basicTestOnPackage(epub.getPackageDoc());
        assertThat(epub.isProtected(),is(true));
    }


    private void basicTestOnPackage(Package packageDoc) {
        assertThat(packageDoc.getMetadata(),is(notNullValue()));
        assertThat(packageDoc.getManifest(),is(notNullValue()));
        assertThat(packageDoc.getSpine(),is(notNullValue()));
    }

    @Test
    public void epub1() throws IOException {
        EPub epub = new EPub(Paths.get("./src/test/resources/books/Pyramides - Romain BENASSAYA.epub"));
        assertThat(epub.getTitle().get(),is("Pyramides"));
        assertThat(epub.getAuthor().get(),is("Romain Benassaya"));
        assertThat(epub.getSubjects().size(),is(Integer.valueOf(1)));
        assertThat(epub.getSubjects().get(0),is("Science-Fiction"));

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

        assertThat(metadata.getDates(),is(Arrays.asList(firstDate,secondDate,thirdDate)));


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

        assertThat(metadata.getCreators(),is(Arrays.asList(a)));
        assertThat(metadata.getContributors(),is(Arrays.asList(c1,c2,c3,c4)));

        String p = "Éditions Critic";
        assertThat(metadata.getPublishers(),is(Arrays.asList(p)));

        String r = "Romain Benassaya et Éditions Critic 2018";
        assertThat(metadata.getRights(),is(Arrays.asList(r)));


        Meta m1,m2;
        m1=new Meta();
        m1.setContent("0.9.8");
        m1.setName("Sigil version");
        m2=new Meta();
        m2.setContent("pyramides.jpg");
        m2.setName("cover");

        assertThat(metadata.getMetas(),is(Arrays.asList(m1,m2)));

        Identifier identifier = new Identifier();
        identifier.setId("BookID");
        identifier.setValue("urn:uuid:446cd1b2-7a79-48a0-b6cf-607fc618c09e");
        assertThat(metadata.getIdentifier(),is(identifier));

        Reference ref = new Reference();
        ref.setHref("Text/couverture.xhtml");
        ref.setTitle("Cover");
        ref.setType("cover");
        Guide guide = new Guide();
        guide.getReferences().add(ref);

        assertThat(epub.getPackageDoc().getGuide(),is(guide));
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
        assertThat(metadata.getCreators(),is(Arrays.asList(c1,c2,c3)));

        try {
            EBook result = epub.copyToDirectory(test, EbookCopyOption.AddSubjectToPath, EbookCopyOption.AddAuthorToPath);
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
