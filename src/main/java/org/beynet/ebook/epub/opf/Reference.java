package org.beynet.ebook.epub.opf;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

public class Reference {

    @XmlAttribute(name="title")
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @XmlAttribute(name="type")
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    @XmlAttribute(name="href")
    public String getHref() {
        return href;
    }
    public void setHref(String href) {
        this.href = href;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reference reference = (Reference) o;
        return Objects.equals(getTitle(), reference.getTitle()) &&
                Objects.equals(getType(), reference.getType()) &&
                Objects.equals(getHref(), reference.getHref());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getType(), getHref());
    }

    private String title;
    private String type;
    private String href;
}
