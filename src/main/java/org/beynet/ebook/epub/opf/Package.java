package org.beynet.ebook.epub.opf;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Optional;

@XmlRootElement(name="package")
public class Package {

    @XmlElement(name="metadata")
    public Metadata getMetadata() {
        return metadata;
    }
    public void setMetadata(Metadata metadata) {
        this.metadata=metadata;
    }

    public Optional<CreatorOrContributor> getMainCreator(){
        return metadata==null?Optional.empty():metadata.getMainCreator();
    }
    private Metadata metadata;
}
