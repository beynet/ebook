package org.beynet.ebook.epub.oasis.container;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "container")
public class Container {

    @XmlElement(name="rootfiles")
    public RootFiles getRootFiles() {
        return this.rootFiles;
    }
    public void setRootFiles(RootFiles r) {
        this.rootFiles=r;
    }


    private RootFiles rootFiles;
}
