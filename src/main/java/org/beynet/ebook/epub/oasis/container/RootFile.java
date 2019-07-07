package org.beynet.ebook.epub.oasis.container;

import javax.xml.bind.annotation.XmlAttribute;

public class RootFile {


    private String fullPath;
    private String mediaType;

    @XmlAttribute(name="full-path")
    public String getFullPath() {
        return fullPath;
    }
    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    @XmlAttribute(name="media-type")
    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
