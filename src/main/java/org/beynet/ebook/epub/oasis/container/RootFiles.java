package org.beynet.ebook.epub.oasis.container;

import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

public class RootFiles {

    @XmlElement(name="rootfile")
    public List<RootFile> getRootFiles() {
        return rootFiles;
    }

    private List<RootFile> rootFiles=new ArrayList<>();
}
