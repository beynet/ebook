package org.beynet.ebook.epub.opf;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

import java.util.Objects;

public class Date {

    @XmlValue
    public String getContent() {
        return content;
    }
    public void setContent(String name) {
        this.content = name;
    }

    @XmlAttribute(name="id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name="event",namespace = "http://www.idpf.org/2007/opf")
    public String getEvent() {
        return event;
    }
    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Date date = (Date) o;
        return Objects.equals(getEvent(), date.getEvent()) &&
                Objects.equals(getContent(), date.getContent()) &&
                Objects.equals(getId(), date.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEvent(), getContent(), getId());
    }

    private String event;
    private String content;
    private String id;
}
