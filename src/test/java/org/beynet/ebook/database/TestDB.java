package org.beynet.ebook.database;

import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDB {

    /*@Test
    public void indexAll() throws IOException {
        EBookDatabase db = null ;
        try {
            db = EBookDatabase.getInstance();
            db.indexePath(Paths.get("C:\\Users\\beyne\\OneDrive\\ebooks"));
            List<EBook> list = db.list();
            System.out.println(list.size());
        }finally {

        }
    }*/


    @Test
    public void indexAndRead() throws IOException {
        Path indexPath = Files.createTempDirectory("ebook_test");
        System.out.println("index path="+indexPath.toString());
        EBookDatabase db = null ;
        try {
            db = EBookDatabase.getInstance(indexPath);
            EBook eBook = EBookFactory.createEBook(Paths.get("src/test/resources/books/A Fire Upon The Deep.epub"));

            eBook.updateSubjects();
            db.indexe(eBook);
            List<EBook> list = db.list();

            EBook found = list.get(0);
            assertEquals(eBook.getIdentifier(), found.getIdentifier());
            assertEquals(eBook.getAuthor(), found.getAuthor());
            assertEquals(eBook.getTitle(), found.getTitle());
            assertEquals(eBook.getPath(), found.getPath());
            assertEquals(eBook.getSubjects(), found.getSubjects());
        }
        finally {
            if (db!=null) db.dispose();
            if (Files.exists(indexPath)) {
                Files.walkFileTree(indexPath, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
                Files.delete(indexPath);
            }
        }
    }
}
