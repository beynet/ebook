package org.beynet.ebook.epub.opf;

import javax.xml.bind.annotation.XmlAttribute;

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

    private String id;
    private String href;
    private String mediaType;
}
