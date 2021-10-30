package org.beynet.ebook.epub.opf;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

public class Spine {

    @XmlAttribute(name="toc")
    public String getToc() {
        return toc;
    }
    public void setToc(String toc) {
        this.toc = toc;
    }

    @XmlAttribute(name="page-map")
    public String getPagemap() {
        return pagemap;
    }
    public void setPagemap(String pagemap) {
        this.pagemap = pagemap;
    }

    @XmlElement(name="itemref")
    public List<ItemRef> getItemRefs() {
        return itemRefs;
    }

    private String toc;
    private String pagemap;
    private List<ItemRef> itemRefs = new ArrayList<>();
}
