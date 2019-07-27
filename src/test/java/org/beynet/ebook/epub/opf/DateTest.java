package org.beynet.ebook.epub.opf;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DateTest {
    @Test
    public void equals() {
        Date d1,d2;

        d1 = new Date();
        d1.setContent("content");
        d1.setEvent("event");
        d1.setId("id");

        d2 = new Date();
        d2.setContent("content");
        d2.setEvent("event");
        d2.setId("id");

        assertThat(d1,is(d2));
    }

    @Test
    public void notEquals1() {
        Date d1,d2;

        d1 = new Date();
        d1.setContent("content");
        d1.setEvent("event");
        d1.setId("id");

        d2 = new Date();
        d2.setContent("content1");
        d2.setEvent("event");
        d2.setId("id");

        assertThat(d1.equals(d2),is(Boolean.FALSE));
    }

    @Test
    public void notEquals2() {
        Date d1,d2;

        d1 = new Date();
        d1.setContent("content");
        d1.setEvent("event");
        d1.setId("id");

        d2 = new Date();
        d2.setContent("content");
        d2.setEvent("event2");
        d2.setId("id");

        assertThat(d1.equals(d2),is(Boolean.FALSE));
    }

    @Test
    public void notEquals3() {
        Date d1,d2;

        d1 = new Date();
        d1.setContent("content");
        d1.setEvent("event");
        d1.setId("id");

        d2 = new Date();
        d2.setContent("content");
        d2.setEvent("event");
        d2.setId("id3");

        assertThat(d1.equals(d2),is(Boolean.FALSE));
    }

}
