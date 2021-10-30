package org.beynet.ebook.epub.opf;

import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

public class Manifest {
    @XmlElement(name="item")
    public List<Item> getItems() {
        return items;
    }

    private List<Item> items = new ArrayList<>();
}
