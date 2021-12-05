package org.beynet.ebook.model;

public interface Event {
    void accept(EbookEventWatcher watcher);
}
