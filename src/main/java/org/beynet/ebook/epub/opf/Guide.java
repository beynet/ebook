package org.beynet.ebook.epub.opf;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class Guide {

    @XmlElement(name="reference")
    public List<Reference> getReferences() {
        return references;
    }

    private List<Reference> references = new ArrayList<>();
}
