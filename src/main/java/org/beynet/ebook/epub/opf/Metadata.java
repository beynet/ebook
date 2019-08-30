package org.beynet.ebook.epub.opf;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@XmlType(name = "metadata",propOrder = {"title","creators","contributors","subjects","publishers", "dates", "identifiers","language","rights","metas"})
public class Metadata {

    public static final String AUT = "aut";

    @XmlElement(name = "title",namespace = "http://purl.org/dc/elements/1.1/")
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElement(name="creator",namespace = "http://purl.org/dc/elements/1.1/")
    public List<CreatorOrContributor> getCreators(){
        return creators;
    }

    @XmlElement(name="contributor",namespace = "http://purl.org/dc/elements/1.1/")
    public List<CreatorOrContributor> getContributors() {
        return contributors;
    }

    @XmlElement(name="subject",namespace = "http://purl.org/dc/elements/1.1/")
    public List<String> getSubjects() {
        return subjects;
    }

    @XmlTransient
    public Optional<CreatorOrContributor> getMainCreator(){
        return creators.stream().filter(c-> AUT.equals(c.getRole())|| ( c.getId()!=null && c.getId().startsWith("id")) ).findFirst().or(()->creators.stream().findFirst());
    }
    public void replaceMainCreator(String newName) {
        Optional<CreatorOrContributor> mainCreator = getMainCreator();
        mainCreator.ifPresentOrElse(c->{
            c.setName(newName);
        },
        ()->{
            CreatorOrContributor newCreator = new CreatorOrContributor();
            newCreator.setName(newName);
            newCreator.setRole(AUT);
            creators.add(newCreator);
        }
                );
    }


    @XmlElement(name="publisher",namespace = "http://purl.org/dc/elements/1.1/")
    public List<String> getPublishers() {
        return publisher;
    }

    @XmlElement(name="date",namespace = "http://purl.org/dc/elements/1.1/")
    public List<Date> getDates() {
        return date;
    }

    @XmlElement(name="identifier",namespace = "http://purl.org/dc/elements/1.1/")
    public List<Identifier> getIdentifiers() {
        return identifier;
    }

    @XmlElement(name="language",namespace = "http://purl.org/dc/elements/1.1/")
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }


    @XmlElement(name="rights",namespace = "http://purl.org/dc/elements/1.1/")
    public List<String> getRights() {
        return rights;
    }

    @XmlElement(name="meta")
    public List<Meta> getMetas() {
        return metas;
    }

    private String                     title;
    private String                     language;
    private List<Identifier>           identifier=new ArrayList<>();
    private List<Meta>                 metas = new ArrayList<>();
    private List<CreatorOrContributor> creators=new ArrayList<>();
    private List<CreatorOrContributor> contributors=new ArrayList<>();
    private List<String>               subjects=new ArrayList<>();
    private List<String>               publisher = new ArrayList<>();
    private List<String>               rights = new ArrayList<>();
    private List<Date>                 date=new ArrayList<>();
}
