package org.beynet.ebook.model;

import java.nio.file.Path;

public class EBookDeleted  implements Event{

    public EBookDeleted(Path p){
        this.path = p;
    }

    @Override
    public void accept(EbookEventWatcher watcher) {
        watcher.visit(this);
    }

    public Path getPath() {
        return path;
    }

    private Path path;
}
