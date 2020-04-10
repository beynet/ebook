package org.beynet.ebook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class AbstractEBook implements EBook {

    public AbstractEBook(Path path) throws IOException {
        this.path = path;
        if (!Files.exists(this.path)) throw new IOException("File not found "+path.toString());
        EBookUtils.createConfDirectory();
        this.properties = null ;
        this.propertyFilePath = Optional.empty();
    }

    protected Properties getProperties() {
        synchronized (this) {
            if (this.properties == null) {
                this.properties = new Properties();
                getIdentifier().ifPresentOrElse(id ->{
                    id = toFileName(id);
                    this.propertyFilePath = Optional.ofNullable(EBookUtils.getTargetDirectory().resolve(Paths.get(id+".ini")));
                    this.propertyFilePath.ifPresent(path-> {
                        if (Files.exists(path)) {
                            try (InputStream is = Files.newInputStream(path)) {
                                this.properties.load(is);
                            } catch (IOException e) {
                                logger.error("unable to load file ", e);
                            }
                        }
                    });
                },
                        ()->{
                            logger.error("current ebook has no identifier"+getPath().toString());
                        }
                        );

            }
        }
        return this.properties;
    }

    protected void saveProperties() {
        synchronized (this) {
            getProperties();
            this.propertyFilePath.ifPresent(path-> {
                try (OutputStream os = Files.newOutputStream(path)) {
                    properties.store(os, null);
                } catch (IOException e) {
                    logger.error("unable to save property file", e);
                }
            });
        }
    }

    @Override
    public void saveCurrentPage(String page) {
        getProperties().put(CURRENT_PAGE,page);
        saveProperties();
    }

    @Override
    public Optional<String> loadSavedCurrentPage() {
        return Optional.ofNullable(getProperties().getProperty(CURRENT_PAGE));
    }

    @Override
    public void saveCurrentPageInPage(String pageInPage) {
        if (pageInPage!=null && !"".equals(pageInPage)) {
            getProperties().put(CURRENT_PAGE_IN_PAGE, pageInPage);
            saveProperties();
        }
    }

    @Override
    public Optional<String> loadSavedCurrentPageInPage() {
        return Optional.ofNullable(getProperties().getProperty(CURRENT_PAGE_IN_PAGE));
    }

    @Override
    public void saveCurrentZoom(double zoom){
        getProperties().put(ZOOM,Double.valueOf(zoom).toString());
        saveProperties();
    }
    @Override
    public Optional<Double> loadSavedCurrentZoom() {
        return Optional.ofNullable(getProperties().getProperty(ZOOM)).map(l->Double.valueOf(l));
    }

    @Override
    public void saveNightMode(boolean nightMode) {
        getProperties().put(NIGHT_MODE,Boolean.valueOf(nightMode).toString());
        saveProperties();
    }

    @Override
    public Optional<Boolean> loadSavedNightMode() {
        return Optional.ofNullable(getProperties().getProperty(NIGHT_MODE)).map(n->Boolean.valueOf(n));
    }

    @Override
    public void saveSmartDisplayMode(boolean nightMode) {
        getProperties().put(SMART_DISPLAY_MODE,Boolean.valueOf(nightMode).toString());
        saveProperties();
    }
    @Override
    public Optional<Boolean> loadSmartDisplayMode() {
        return Optional.ofNullable(getProperties().getProperty(SMART_DISPLAY_MODE)).map(n->Boolean.valueOf(n));
    }

    protected String toFileName(String p) {
        return p.replaceAll("[\\\\<>:|/?*\"]","").stripLeading().stripTrailing();
    }

    @Override
    public EBook copyToDirectory(Path targetDirectory, CopyOption... options) throws IOException,InvalidPathException {
        if (targetDirectory==null) throw new IllegalArgumentException("targetDirectory is mandatory");
        if (!Files.exists(targetDirectory)) throw new IOException("target directory "+targetDirectory.toString()+" does not exist");
        if (!Files.isDirectory(targetDirectory)) throw new IOException(targetDirectory.toString()+" is not a directory");

        if (options!=null && options.length>0) {
            for (CopyOption option : options) {
                if (EbookCopyOption.AddSubjectToPath.equals(option)) {

                    String subject = toFileName(getSubjects().stream().findFirst().map(t->"".equals(t)?null:t).orElse(UNDEFINED_SUBJECT));
                    try {
                        targetDirectory = targetDirectory.resolve(subject);
                    } catch(InvalidPathException e) {
                        logger.error("invalid path ["+subject+"]",e);
                        throw e;
                    }
                }
                else if (EbookCopyOption.AddAuthorToPath.equals(option)) {
                    String author = toFileName(getAuthor().map(t->"".equals(t)?null:t).orElse(UNDEFINED_AUTHOR));
                    try {
                        targetDirectory = targetDirectory.resolve(author);
                    } catch(InvalidPathException e) {
                        logger.error("invalid path ["+author+"]",e);
                        throw e;
                    }
                }
            }
        }
        options = Arrays.stream(options).filter(option -> !EbookCopyOption.class.isAssignableFrom(option.getClass())).collect(Collectors.toList()).toArray(new CopyOption[0]);
        logger.info("copy ebook "+getPath().toString()+" to "+targetDirectory);
        Optional<String> originalFileName = Optional.of(getPath().getFileName().toString()).map(t->(t.contains(".")&&t.lastIndexOf(".")!=0)?t.substring(0,t.lastIndexOf(".")):t);

        // create expected directories
        Files.createDirectories(targetDirectory);
        // use book title as file name or original filename
        targetDirectory=targetDirectory.resolve(toFileName(getTitle().map(t->"".equals(t)?null:t).orElse(originalFileName.get()).concat(getFileExtension())));
        if (Files.exists(targetDirectory)) logger.warn("!!!!!!!!!!!!!!!! book "+targetDirectory+" orig ="+getPath());
        Files.copy(getPath(),targetDirectory,options);
        return EBookFactory.createEBook(targetDirectory);
    }

    @Override
    public EBook copy(Path target,CopyOption ...options) throws IOException {
        logger.info("copy book "+getPath().toString()+" to "+target.toString());
        Files.copy(getPath(),target,options);
        return EBookFactory.createEBook(target);
    }

    @Override
    public String getFileExtension() {
        Optional<String> extension = Optional.of(getPath().getFileName().toString()).filter(f -> f.lastIndexOf(".") >= 0).map(f -> f.substring(f.lastIndexOf(".")));
        return extension.orElse("");
    }


    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public Path getPath() {
        return path;
    }

    private Path       path            ;
    private Properties properties      ;
    private Optional<Path>       propertyFilePath;
    private final static Logger logger = LogManager.getLogger(AbstractEBook.class);
    private final static String CURRENT_PAGE = "currentPage";
    private final static String CURRENT_PAGE_IN_PAGE = "pageInPage";
    private final static String  ZOOM = "zoom";
    private final static String  NIGHT_MODE = "nightMode";
    private final static String  SMART_DISPLAY_MODE = "smartDisplayMode";
}
