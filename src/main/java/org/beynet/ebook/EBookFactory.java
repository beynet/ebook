package org.beynet.ebook;

import org.beynet.ebook.epub.EPub;

import java.io.IOException;
import java.nio.file.Path;

public class EBookFactory {
    public static EBook createEBook(Path ebook) throws IOException {
        if (ebook.getFileName().toString().endsWith(".epub")){
            return new EPub(ebook);
        }
        else {
            throw new IOException("unsupported type");
        }
    }
}
