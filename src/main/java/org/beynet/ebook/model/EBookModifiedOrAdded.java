package org.beynet.ebook.model;

import org.beynet.ebook.EBook;

public class EBookModifiedOrAdded implements Event {

    public EBookModifiedOrAdded(EBook ebook) {
        this.ebook = ebook;
    }

    @Override
    public void accept(EbookEventWatcher watcher) {
       watcher.visit(this);
    }

    public EBook getEbook() {
        return ebook;
    }

    private EBook ebook;

}
