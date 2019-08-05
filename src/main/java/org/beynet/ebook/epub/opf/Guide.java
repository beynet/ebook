package org.beynet.ebook.epub.opf;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Guide {

    @XmlElement(name="reference")
    public List<Reference> getReferences() {
        return references;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guide guide = (Guide) o;
        return Objects.equals(getReferences(), guide.getReferences());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReferences());
    }

    private List<Reference> references = new ArrayList<>();


}
