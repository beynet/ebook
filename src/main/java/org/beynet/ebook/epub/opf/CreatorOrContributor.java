package org.beynet.ebook.epub.opf;


import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

import java.util.Objects;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreatorOrContributor that = (CreatorOrContributor) o;
        return Objects.equals(getRole(), that.getRole()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRole(), getName(), getId());
    }

    private String role;
    private String name;
    private String id;
}
