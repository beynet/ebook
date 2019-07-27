package org.beynet.ebook.epub.opf;

import javax.xml.bind.annotation.XmlAttribute;

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

    private String title;
    private String type;
    private String href;
}
