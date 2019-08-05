package org.beynet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookFactory;
import org.beynet.ebook.EBookUtils;
import org.beynet.ebook.EbookCopyOption;
import org.beynet.ebook.epub.EPub;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

/**
 * Unit test for simple EBook.
 */
public class AppTest extends AbstractTests{
    private final static Logger logger = LogManager.getLogger(AppTest.class);





    @Test
    public void replace(){

        String p="<>essai:\\test*|><\"";
        System.out.println(p.replaceAll("[\\\\<>:|/?*\"]",""));
    }

    @Test
    @Ignore
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
