package org.beynet.ebook.epub.opf;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class CreatorOrContributor {

    @XmlValue
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name="id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name="role",namespace = "http://www.idpf.org/2007/opf")
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    private String role;
    private String name;
    private String id;
}
