package org.beynet.ebook.epub.opf;

import javax.xml.bind.annotation.XmlAttribute;

public class ItemRef {

    @XmlAttribute(name="idref")
    public String getIdref() {
        return idref;
    }
    public void setIdref(String idref) {
        this.idref = idref;
    }

    @XmlAttribute(name="linear")
    public String getLinear() {
        return linear;
    }
    public void setLinear(String linear) {
        this.linear = linear;
    }


    private String idref;
    private String linear;
}
