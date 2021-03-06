package org.beynet.ebook;

import org.beynet.ebook.epub.EPub;
import org.beynet.ebook.unsupported.UnsupportedEBook;

import java.io.IOException;
import java.nio.file.Path;

public class EBookFactory {
    public static EBook createEBook(Path ebook) throws IOException {
        if (ebook.getFileName().toString().endsWith(".epub")){
            return new EPub(ebook);
        }
        else {
            return new UnsupportedEBook(ebook);
        }
    }
}
