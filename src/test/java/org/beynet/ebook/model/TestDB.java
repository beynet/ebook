package org.beynet.ebook.model;

import org.beynet.AbstractTests;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookFactory;
import org.beynet.ebook.model.EBookDatabase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestDB extends AbstractTests {





    @Test
    public void indexAll() throws IOException, InterruptedException {
        Path expectedModified = Paths.get("src/test/resources/books/Juillet 2019.epub");

        class ObserveEvent implements EbookEventWatcher,Observer {
            Path expected ;
            boolean OK = false ;
            public ObserveEvent(Path expected) {
                this.expected = expected.toAbsolutePath();
            }

            @Override
            public void visit(EBookModifiedOrAdded evt) {
                System.out.println("modified "+evt.getEbook().getTitle());
                if (expected.equals(evt.getEbook().getPath())) OK = true;
            }

            @Override
            public void visit(EBookDeleted eBookDeleted) {

            }

            public boolean isOK() {
                return OK;
            }

            @Override
            public void update(Observable o, Object arg) {
                Event evt = ((Event)arg);
                evt.accept(this);
            }
        };
        ObserveEvent observer = new ObserveEvent(expectedModified);
        assertFalse(observer.isOK());

        Path indexPath = Files.createTempDirectory("ebook_test");
        EBookDatabase db = null ;
        try {
            db = EBookDatabase.getInstance(indexPath);
            Path toIndex = Paths.get("src/test/resources/books");
            db.indexePath(toIndex);
            List<EBook> list = db.list();
            System.out.println(list.size());
            List<Path> folders = db.listIndexedFolder();
            assertEquals(1,folders.size());
            assertEquals(toIndex.toAbsolutePath(),folders.get(0)); // path to index is converted to absolute path

            db.startWatchService();
            db.addObserver(observer);
            Thread.sleep(1000*2);
            FileTime now = FileTime.fromMillis(System.currentTimeMillis());
            Files.setLastModifiedTime(expectedModified,now);
            Thread.sleep(1000*2);
            assertTrue(observer.isOK());
            db.stopWatchService();

        } finally {
            removeIndexDirectories(db, indexPath);
        }
    }

    private void removeIndexDirectories(EBookDatabase db, Path indexPath) throws IOException {
        if (db != null)
            db.dispose();
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
            removeIndexDirectories(db, indexPath);
        }
    }
    @Test
    public void findInDirectory() throws IOException {
        Path indexPath = Files.createTempDirectory("ebook_test");
        System.out.println("index path="+indexPath.toString());
        EBookDatabase db = null ;
        Path expectedDirectory = Paths.get("src/test/resources/books/").normalize().toAbsolutePath();
        try {
            db = EBookDatabase.getInstance(indexPath);
            EBook eBook1 = EBookFactory.createEBook(Paths.get("src/test/resources/books/A Fire Upon The Deep.epub"));
            EBook eBook2 = EBookFactory.createEBook(Paths.get("src/test/resources/books/Juillet 2019.epub"));
            db.indexe(eBook1);
            db.indexe(eBook2);

            List<EBook> list = db.listInDirectory(expectedDirectory);
            assertEquals(2,list.size());
            boolean eBook1Found=false;
            boolean eBook2Found=false;
            for (EBook eBook : list) {
                if (eBook.getPath().equals(eBook1.getPath())){
                    eBook1Found = true;
                }
                if (eBook.getPath().equals(eBook2.getPath())){
                    eBook2Found = true;
                }
            }
            assertTrue(eBook1Found);
            assertTrue(eBook2Found);
        }
        finally {
            removeIndexDirectories(db, indexPath);
        }
    }



}
