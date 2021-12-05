package org.beynet.ebook.model;

public interface EbookEventWatcher {
    void visit(EBookModifiedOrAdded evt);
    void visit(EBookDeleted eBookDeleted);
}
