package org.beynet.ebook;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.beynet.utils.admin.commandline.BooleanOption;
import org.beynet.utils.admin.commandline.CommandLineOptionsAnalyzer;
import org.beynet.utils.admin.commandline.Option;
import org.beynet.utils.admin.commandline.StringOption;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class EBookMain {

    final static BooleanOption sort = new BooleanOption("--sort", "sort an ebook directory (all ebook found in source into target)");
    final static StringOption target = new StringOption("--target", "target folder", false);
    final static StringOption source = new StringOption("--source", "source folder", false);
    final static List<Option> options = Arrays.asList(
    sort,
    target,
    source
    );

    static final CommandLineOptionsAnalyzer analyser = new CommandLineOptionsAnalyzer(options);

    private static void printHelp(){
        for (Option option : options) {
            System.out.println(option.getDescription());
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
        EBookUtils.sort(Paths.get(source.getValue()),Paths.get(target.getValue()),EbookCopyOption.AddSubjectToPath,EbookCopyOption.AddAuthorToPath);
    }

    public static void main(String[] args ) throws IOException {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.INFO);
        analyser.analyseCommandLine(args);

        if (sort.isOptionFound()) {
            System.out.println("sort option present");
            sort();
        }
    }
}
