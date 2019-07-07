package org.beynet.ebook.epub.opf;


import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Metadata {

    @XmlElement(name = "title",namespace = "http://purl.org/dc/elements/1.1/")
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElement(name="creator",namespace = "http://purl.org/dc/elements/1.1/")
    public List<CreatorOrContributor> getCreators() {
        return creators;
    }

    @XmlElement(name="subject",namespace = "http://purl.org/dc/elements/1.1/")
    public List<String> getSubjects() {
        return subjects;
    }

    public Optional<CreatorOrContributor> getMainCreator(){
        return creators.stream().filter(c->"aut".equals(c.getRole())|| ( c.getId()!=null && c.getId().startsWith("id")) ).findFirst();
    }

    private String title;
    private List<CreatorOrContributor> creators=new ArrayList<>();
    private List<String> subjects=new ArrayList<>();
}
