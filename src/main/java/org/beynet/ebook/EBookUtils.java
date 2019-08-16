package org.beynet.ebook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class EBookUtils {

    private final static Logger logger = LogManager.getLogger(EBookUtils.class);


    public static Path getTargetDirectory() {
        final Path userHome = Paths.get((String) System.getProperty("user.home"));
        return userHome.resolve(Paths.get(".ebook"));
    }

    public static void createConfDirectory() throws IOException {
        final Path targetDirectory = getTargetDirectory();
        if (!Files.exists(targetDirectory)) Files.createDirectories(targetDirectory);
    }

    /**
     * copy all ebooks found in source to target
     * @param source
     * @param target
     * @param options copy options see {@link EbookCopyOption} or {@link StandardCopyOption}
     * @throws IOException
     */
    public static void sort(final Path source,final Path target,CopyOption...options) throws IOException {
        Files.walkFileTree(source, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                logger.debug("visit file "+file.toString());
                final EBook eBook;
                try {
                    eBook = EBookFactory.createEBook(file);
                } catch (IOException e) {
                    logger.info("unable to sort file "+file.toString());
                    return FileVisitResult.CONTINUE;
                }
                if (eBook.isProtected()) logger.warn("ebook "+eBook.getPath().toString()+" is protected");
                try {
                    eBook.copyToDirectory(target,options);
                }catch(Exception e) {
                    logger.error("unable to copy ebook "+file.toString(),e);
                }
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
    }
}
