package org.beynet.ebook.epub.opf;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CreatorOrContributorTest {

    @Test
    public void equals() {
        CreatorOrContributor c1,c2;
        c1 = new CreatorOrContributor();
        c2 = new CreatorOrContributor();

        c1.setName("name");
        c1.setRole("role");
        c1.setId("id");

        c2.setName("name");
        c2.setRole("role");
        c2.setId("id");

        assertThat(c1,is(c2));

    }


    @Test
    public void notEquals1() {
        CreatorOrContributor c1,c2;
        c1 = new CreatorOrContributor();
        c2 = new CreatorOrContributor();

        c1.setName("name");
        c1.setRole("role");
        c1.setId("id");

        c2.setName("name1");
        c2.setRole("role");
        c2.setId("id");

        assertThat(c1.equals(c2),is(Boolean.FALSE));

    }


    @Test
    public void notEquals2() {
        CreatorOrContributor c1,c2;
        c1 = new CreatorOrContributor();
        c2 = new CreatorOrContributor();

        c1.setName("name");
        c1.setRole("role");
        c1.setId("id");

        c2.setName("name");
        c2.setRole("role2");
        c2.setId("id");

        assertThat(c1.equals(c2),is(Boolean.FALSE));

    }

    @Test
    public void notEquals3() {
        CreatorOrContributor c1,c2;
        c1 = new CreatorOrContributor();
        c2 = new CreatorOrContributor();

        c1.setName("name");
        c1.setRole("role");
        c1.setId("id");

        c2.setName("name");
        c2.setRole("role");
        c2.setId("id3");

        assertThat(c1.equals(c2),is(Boolean.FALSE));

    }

}
