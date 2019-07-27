package org.beynet.ebook.epub.opf;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

public class Meta {

    @XmlAttribute(name="name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name="content")
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meta meta = (Meta) o;
        return Objects.equals(getName(), meta.getName()) &&
                Objects.equals(getContent(), meta.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getContent());
    }

    private String name;
    private String content;
}
