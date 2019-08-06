package org.beynet.ebook.epub.opf;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

public class Item {


    @XmlAttribute(name="id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name="href")
    public String getHref() {
        return href;
    }
    public void setHref(String href) {
        this.href = href;
    }

    @XmlAttribute(name="media-type")
    public String getMediaType() {
        return mediaType;
    }
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(getId(), item.getId()) &&
                Objects.equals(getHref(), item.getHref()) &&
                Objects.equals(getMediaType(), item.getMediaType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getHref(), getMediaType());
    }

    private String id;
    private String href;
    private String mediaType;
}
