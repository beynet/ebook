package org.beynet.ebook.epub.opf;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.Optional;

@XmlRootElement(name="package")
@XmlType(name="package",propOrder = {"metadata","manifest","spine","guide"})
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

    public void replaceMainCreator(String newName) {
        if (metadata!=null) metadata.replaceMainCreator(newName);
    }

    @XmlElement(name="manifest")
    public Manifest getManifest() {
        return manifest;
    }
    public void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    @XmlElement(name="spine")
    public Spine getSpine() {
        return spine;
    }
    public void setSpine(Spine spine) {
        this.spine = spine;
    }

    @XmlElement(name="guide")
    public Guide getGuide() {
        return guide;
    }
    public void setGuide(Guide guide) {
        this.guide = guide;
    }

    private Metadata metadata;
    private Spine    spine;
    private Guide    guide ;
    private Manifest manifest;
}
