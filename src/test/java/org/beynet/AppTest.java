package org.beynet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.beynet.ebook.EBookUtils;
import org.beynet.ebook.EbookCopyOption;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * Unit test for simple EBook.
 */
public class AppTest extends AbstractTests{
    private final static Logger logger = LogManager.getLogger(AppTest.class);

    @Test
    public void replace(){

        String p="dsdf<title/>\"";
        System.out.println(p.replaceAll("<title\\s*/>","<title> </title>"));
    }

    @Test
    @Disabled
    public void sortBooks() throws IOException {
        Path test = Files.createTempDirectory("sorttest");
        EBookUtils.sort(Paths.get("G:\\Mon Drive\\EBooks_Old"),test,EbookCopyOption.AddSubjectToPath,EbookCopyOption.AddAuthorToPath,StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void t() {
        String file="t.jpg";
        Optional<String> originalFileName = Optional.of(file).map(t->(t.contains(".")&&t.lastIndexOf(".")!=0)?t.substring(0,t.lastIndexOf(".")-1):t);
        System.out.println(originalFileName);
    }

}
