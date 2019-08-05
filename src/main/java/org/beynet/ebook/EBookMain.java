package org.beynet.ebook;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.beynet.utils.admin.commandline.BooleanOption;
import org.beynet.utils.admin.commandline.CommandLineOptionsAnalyzer;
import org.beynet.utils.admin.commandline.Option;
import org.beynet.utils.admin.commandline.StringOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EBookMain {
    private final static Logger logger = LogManager.getLogger(EBookMain.class);
    final static BooleanOption sort = new BooleanOption("--sort", "sort an ebook directory (all ebook found in source into target)");
    final static StringOption target = new StringOption("--target", "target folder or file", false);
    final static StringOption source = new StringOption("--source", "source folder", false);
    final static StringOption addSubject = new StringOption("--addSubject", "add the subject to target", false);
    final static StringOption removeSubject = new StringOption("--removeSubject", "remove the subject from target subjects", false);
    final static StringOption changeAuthor = new StringOption("--changeAuthor", "change target ebook author", false);
    final static BooleanOption info = new BooleanOption("--info", "show info about target ebook");

    final static List<Option> options = Arrays.asList(
            sort,
            target,
            source,
            addSubject,
            removeSubject,
            changeAuthor,
            info
    );

    static final CommandLineOptionsAnalyzer analyser = new CommandLineOptionsAnalyzer(options);

    private static void printHelp(){
        for (Option option : options) {
            System.out.println("Option "+option.getName()+" "+option.getDescription());
        }
    }

    private static void sort() throws IOException {
        if (target.getValue()==null || "".equals(target.getValue().stripTrailing().stripTrailing())) {
            printHelp();
            throw new IllegalArgumentException("target must no be empty");
        }
        if (source.getValue()==null || "".equals(source.getValue().stripTrailing().stripTrailing())) {
            printHelp();
            throw new IllegalArgumentException("source must no be empty");
        }
        logger.debug("source="+source.getValue()+" target="+target.getValue());
        Path target = Paths.get(EBookMain.target.getValue());
        Path source = Paths.get(EBookMain.source.getValue());
        if (!Files.exists(target)) throw new IllegalArgumentException("Target "+target+" does not exist");
        if (!Files.exists(source)) throw new IllegalArgumentException("Source "+source+" does not exist");
        EBookUtils.sort(source, target,EbookCopyOption.AddSubjectToPath,EbookCopyOption.AddAuthorToPath);
    }

    private static void addSubject() throws IOException {
        if (addSubject.getValue()==null || "".equals(addSubject.getValue().stripTrailing().stripTrailing())) {
            printHelp();
            throw new IllegalArgumentException("addSubject must no be empty");
        }

        if (target.getValue()==null || "".equals(target.getValue().stripTrailing().stripTrailing())) {
            printHelp();
            throw new IllegalArgumentException("target must no be empty");
        }

        Path t = Paths.get(target.getValue());
        EBook eBook = EBookFactory.createEBook(t);

        logger.info("will add subject \""+addSubject.getValue()+"\" to "+t.toString());
        eBook.getSubjects().add(addSubject.getValue());
        eBook.updateSubjects();

    }

    private static void changeAuthor() throws IOException {
        if (changeAuthor.getValue()==null || "".equals(changeAuthor.getValue().stripTrailing().stripTrailing())) {
            printHelp();
            throw new IllegalArgumentException("author must no be empty");
        }

        if (target.getValue()==null || "".equals(target.getValue().stripTrailing().stripTrailing())) {
            printHelp();
            throw new IllegalArgumentException("target must no be empty");
        }

        Path t = Paths.get(target.getValue());
        EBook eBook = EBookFactory.createEBook(t);

        logger.info("will change author to \""+changeAuthor.getValue()+"\" to "+t.toString());
        eBook.changeAuthor(changeAuthor.getValue());

    }

    private static void removeSubject() throws IOException {
        if (removeSubject.getValue()==null || "".equals(removeSubject.getValue().stripTrailing().stripTrailing())) {
            printHelp();
            throw new IllegalArgumentException("removeSubject must no be empty");
        }

        if (target.getValue()==null || "".equals(target.getValue().stripTrailing().stripTrailing())) {
            printHelp();
            throw new IllegalArgumentException("target must no be empty");
        }

        Path t = Paths.get(target.getValue());
        EBook eBook = EBookFactory.createEBook(t);

        logger.info("will remove subject \""+removeSubject.getValue()+"\" to "+t.toString());
        Iterator<String> iterator = eBook.getSubjects().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (next.equals(removeSubject.getValue())) iterator.remove();
        }
        eBook.updateSubjects();

    }


    private static void info() throws IOException {
        if (target.getValue()==null || "".equals(target.getValue().stripTrailing().stripTrailing())) {
            printHelp();
            throw new IllegalArgumentException("target must no be empty");
        }

        Path t = Paths.get(target.getValue());
        EBook eBook = EBookFactory.createEBook(t);
        StringBuilder sb = new StringBuilder();
        sb.append(t.toString());
        sb.append("\n\ttitle=");
        sb.append(eBook.getTitle().orElse(EBook.UNDEFINED_TITLE));
        sb.append("\n\tauthor=");
        sb.append(eBook.getAuthor().orElse(EBook.UNDEFINED_AUTHOR));
        sb.append("\n\tsubject: ");

        for (int i=0;i<eBook.getSubjects().size();i++) {
            if (i>0) sb.append(",");
            sb.append(eBook.getSubjects().get(i));
        }
        sb.append("\n");
        System.out.println(sb.toString());

    }


    public static void main(String[] args ) throws IOException {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.DEBUG);
        analyser.analyseCommandLine(args);

        if (sort.isOptionFound()) {
            sort();
        }
        else if (info.isOptionFound()) {
            info();
        }
        else if (addSubject.isOptionFound()){
            addSubject();
        }
        else if (removeSubject.isOptionFound()){
            removeSubject();
        }
        else if (changeAuthor.isOptionFound()){
            changeAuthor();
        }
        else {
            printHelp();
        }
    }
}
