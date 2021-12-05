package org.beynet.ebook.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookFactory;
import org.beynet.ebook.EBookUtils;
import org.beynet.ebook.model.EBookDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public class EBookGUI extends Application {
    private final static Logger logger = LogManager.getLogger(EBookGUI.class);
    public static final String CLICK = "click";
    private Scene currentScene;
    private Stage currentStage;

    private WebView           ebookView    ;
    private Optional<EBook>   currentEBook ;
    private boolean nightMode=false ;
    private boolean smartDisplayMode = true ;
    private final static String CONF_FILE_NAME ="ebooks.ini";
    private final static String CURRENT_EBOOK_PATH ="CurrentEbookPath";
    private final static String CURRENT_WIDTH ="WIDTH";
    private final static String CURRENT_HEIGHT ="HEIGHT";
    private Properties properties = null;
    private Path propertyFilePath = null ;

    private final static String nextPreviousJS ;
    static {
        try (InputStream is = EBookGUI.class.getResourceAsStream("/javascript.js")) {
            byte[] b = new byte[1024];
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int cr = 1;
            while (cr >= 0) {
                try {
                    cr = is.read(b);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (cr > 0) bo.write(b, 0, cr);
            }
            nextPreviousJS = bo.toString("UTF-8");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(Path eBookPath) {
        if (eBookPath!=null) launch(eBookPath.toString());
        else launch();
    }


    protected Properties getProperties() {
        synchronized (this) {
            if (this.properties == null) {
                this.propertyFilePath = EBookUtils.getTargetDirectory().resolve(Paths.get(CONF_FILE_NAME));
                this.properties = new Properties();
                if (Files.exists(this.propertyFilePath)) {
                    try (InputStream is = Files.newInputStream(this.propertyFilePath)) {
                        this.properties.load(is);
                    } catch (IOException e) {
                        logger.error("unable to load file ", e);
                    }
                }
            }
        }
        return this.properties;
    }

    private void saveCurrenEbook() {
        currentEBook.ifPresent(e->getProperties().put(CURRENT_EBOOK_PATH,e.getPath().toString()));
        saveProperties();
    }

    private void loadCurrentEbook() {
        this.currentEBook=Optional.ofNullable(getProperties().getProperty(CURRENT_EBOOK_PATH)).map(p->{
            try {
                return EBookFactory.createEBook(Paths.get(p));
            } catch (IOException e) {
                logger.error("unable to load ebook",e);
                return null;
            }
        });
    }

    protected void saveProperties() {
        synchronized (this) {
            getProperties();
            try (OutputStream os = Files.newOutputStream(this.propertyFilePath)){
                properties.store(os, null);
            } catch (IOException e) {
                logger.error("unable to save property file",e);
            }
        }
    }

    private void loadContent(String content) {
        content=content.replaceAll("<title\\s*/>","<title> </title>");
        ebookView.getEngine().loadContent(content);
    }

    private void saveCurrentPageRatio() {
        Object page = ebookView.getEngine().executeScript(nextPreviousJS + "getRatio();");
        if (page!=null) currentEBook.ifPresent(e->e.saveCurrentPageRatio(page.toString()));
        logger.info("page ratio=" + page.toString());
    }

    private void saveCurrentSceneSize() {
        double height = currentScene.getHeight();
        double width = currentScene.getWidth();
        getProperties().put(CURRENT_HEIGHT,Double.valueOf(height).toString());
        getProperties().put(CURRENT_WIDTH,Double.valueOf(width).toString());
        saveProperties();
    }

    @Override
    public void start(Stage stage) throws Exception {

        this.currentStage = stage ;
        currentStage.setOnCloseRequest((t) -> {
            saveCurrentSceneSize();
            currentEBook.ifPresent(b->{
                saveCurrentPageRatio();
            });
            quitApp();
        });
        Parameters parameters = getParameters();
        if (!parameters.getRaw().isEmpty()) {
            currentEBook = Optional.of(EBookFactory.createEBook(Paths.get(parameters.getRaw().get(0))));
            saveCurrenEbook();
        }
        else {
            loadCurrentEbook();
        }
        Group group = new Group();

        if (getProperties().getProperty(CURRENT_HEIGHT)!=null && getProperties().getProperty(CURRENT_WIDTH)!=null) {
            currentScene = new Scene(group, Double.valueOf(getProperties().getProperty(CURRENT_WIDTH)), Double.valueOf(getProperties().getProperty(CURRENT_HEIGHT)));
        }
        else {
            currentScene = new Scene(group, 640, 480);
        }

        VBox mainVBOX = new VBox();
        mainVBOX.prefHeightProperty().bind(currentScene.heightProperty());
        mainVBOX.prefWidthProperty().bind(currentScene.widthProperty());

        // next button and previous button in the top
        HBox htop = new HBox();

        Button openLibrary = new Button("library");
        openLibrary.setTooltip(new Tooltip("open ebook library"));
        openLibrary.setOnAction(event -> {
            LibaryWindow libaryWindow = new LibaryWindow(currentStage,Double.valueOf(430),Double.valueOf(600),this::openEBook);
            libaryWindow.show();
        });

        Button firstPage = new Button("first page");
        firstPage.setTooltip(new Tooltip("first page"));
        firstPage.setOnAction(event -> {
            currentEBook.ifPresent(e->loadContent(e.getFirstPage().orElse("")));
        });

        Button nextPage = new Button("next");
        nextPage.setTooltip(new Tooltip("next"));
        nextPage.setOnAction(event -> {
            currentEBook.ifPresent(e->loadContent(e.getNextPage().orElse("")));
            if (smartDisplayMode==true) {
                currentEBook.ifPresent(e -> e.saveCurrentPageInPage("1"));
            }
            else {
                currentEBook.ifPresent(e -> e.saveCurrentPageRatio("0"));
            }
        });

        Button previousPage = new Button("previous");
        previousPage.setTooltip(new Tooltip("previous"));
        previousPage.setOnAction(event -> {
            currentEBook.ifPresent(e->loadContent(e.getPreviousPage().orElse("")));
            if (smartDisplayMode==true) {
                currentEBook.ifPresent(e -> e.saveCurrentPageInPage("1"));
            }
            else {
                currentEBook.ifPresent(e -> e.saveCurrentPageRatio("0"));
            }
        });

        Button nextInPage = new Button(">>");
        nextInPage.setTooltip(new Tooltip("next in page"));
        nextInPage.setOnAction(event -> {
            if (smartDisplayMode==true) {
                Object page = ebookView.getEngine().executeScript(nextPreviousJS + "next();");
                currentEBook.ifPresent(e -> e.saveCurrentPageInPage(page.toString()));
                logger.info("page number=" + page.toString());
            }
        });
        Button prevInPage = new Button("<<");
        prevInPage.setTooltip(new Tooltip("prev in page"));
        prevInPage.setOnAction(event -> {
            if (smartDisplayMode==true) {
                Object page = ebookView.getEngine().executeScript(nextPreviousJS + "prev();");
                currentEBook.ifPresent(e -> e.saveCurrentPageInPage(page.toString()));
                logger.info("page number=" + page.toString());
            }
        });

        Button plus = new Button("+");
        plus.setTooltip(new Tooltip("+"));
        plus.setOnAction(event -> {
            ebookView.setZoom(ebookView.getZoom()+0.1);
            currentEBook.ifPresent(e->e.saveCurrentZoom(ebookView.getZoom()));
        });

        Button minus = new Button("-");
        minus.setTooltip(new Tooltip("-"));
        minus.setOnAction(event -> {
            ebookView.setZoom(ebookView.getZoom()-0.1);
            currentEBook.ifPresent(e->e.saveCurrentZoom(ebookView.getZoom()));
        });

        Button openEBook = new Button("open");

        Button nightModeButton = new Button("night");
        nightModeButton.setOnAction(event->{
            this.nightMode = !this.nightMode;
            if (this.nightMode==true) {
                ebookView.getEngine().executeScript("document.body.styledocument.body.style.backgroundColor = \"black\";\ndocument.body.style.color = \"grey\";");
            }
            else {
                ebookView.getEngine().executeScript("document.body.style.backgroundColor = null;\ndocument.body.style.color = null;");
            }
            currentEBook.ifPresent(e->e.saveNightMode(nightMode));
        });
        Button smartDisplay = new Button("smart");
        smartDisplay.setOnAction(event->{
            this.smartDisplayMode = !this.smartDisplayMode;
            currentEBook.ifPresent(e->{
                e.saveCurrentPageInPage("1");
                loadContent(e.getCurrentPage().orElse(""));
            });
            currentEBook.ifPresent(e->e.saveSmartDisplayMode(smartDisplayMode));
        });

        htop.getChildren().addAll(openLibrary,firstPage,previousPage,nextPage,nextInPage,prevInPage,minus,plus,openEBook,nightModeButton,smartDisplay);

        mainVBOX.getChildren().add(htop);


        //adding webview in the center
        HBox pane = new HBox();
        pane.setPrefWidth(currentScene.getWidth()-htop.getWidth());
        pane.setPrefHeight(currentScene.getHeight()-htop.getHeight());
        currentScene.widthProperty().addListener(c ->{
            pane.setPrefHeight(currentScene.getWidth()-htop.getWidth());
        });
        currentScene.heightProperty().addListener(c ->{
            pane.setPrefHeight(currentScene.getHeight()-htop.getHeight());
        });
        mainVBOX.getChildren().add(pane);
        ebookView = new WebView();
        loadEBook();
        ebookView.prefHeightProperty().bind(pane.heightProperty());
        ebookView.prefWidthProperty().bind(pane.widthProperty());


        ebookView.getEngine().documentProperty().addListener((observableValue, document, t1) -> {
            if (document==null) return;
            NodeList headList = document.getElementsByTagName("head");
            Element head ;
            if (headList.getLength()>0) {
                head = (Element) headList.item(0);
            }
            else {
                head = document.createElement("head");
                document.getDocumentElement().appendChild(head);
            }
            Element meta = document.createElement("meta");
            meta.setAttribute("charset","UTF-8");
            head.appendChild(meta);
        });

        ebookView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    EventListener listener = new EventListener() {
                        @Override
                        public void handleEvent(Event ev) {
                            if (CLICK.equals(ev.getType())) {
                                String href = ((Element)ev.getCurrentTarget()).getAttribute("href");
                                if (href==null) return;
                                Platform.runLater(()->currentEBook.ifPresent(e->loadContent(e.loadPage(href).orElse(""))));
                            }
                        }
                    };

                    Document doc = ebookView.getEngine().getDocument();
                    NodeList nodeList = doc.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener(CLICK, listener, true);
                    }

                    //display local images
                    nodeList = doc.getElementsByTagName("img");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Element img = (Element) nodeList.item(i);
                        String href = img.getAttribute("src");

                        currentEBook.map(e -> e.convertRessourceLocalPathToGlobalURL(href).orElse(null)).ifPresent(s->img.setAttribute("src",s));
                    }

                    // add smart display mode
                    if (smartDisplayMode==true) {
                        String page = currentEBook.map(e -> e.loadSavedCurrentPageInPage().orElse("1")).orElse("1");
                        ebookView.getEngine().executeScript(nextPreviousJS + "startSmartDisplay("+page+");");
                    }
                    else {
                        String page = currentEBook.map(e -> e.loadSavedCurrentPageRatio().orElse("0")).orElse("0");
                        ebookView.getEngine().executeScript(nextPreviousJS + "scrollToRatio("+page+");");
                    }

                    if (nightMode==true) {
                        ebookView.getEngine().executeScript("document.body.style.paddingRight = \"50px\";document.body.style.backgroundColor = \"black\";\ndocument.body.style.color = \"grey\";");
                    }

                }
            }
        });

        openEBook.setOnAction(event -> {
            if (smartDisplayMode==false) {
                saveCurrentPageRatio();
            }

            FileChooser directoryChooser = new FileChooser();
            File result = directoryChooser.showOpenDialog(currentStage);
            if (result!=null) {
                try {
                    Path path = result.toPath();
                    EBook eBook = EBookFactory.createEBook(path);
                    openEBook(eBook);
                } catch (Exception e) {
                    logger.error("unable to load ebook", e);
                }
            }
        });

        pane.getChildren().add(ebookView);
        group.getChildren().add(mainVBOX);


        currentStage.setScene(currentScene);
        currentStage.show();
    }

    private void quitApp() {
        EBookDatabase.getInstance().stopWatchService();
    }

    public void openEBook(EBook ebook) {
        Platform.runLater(() -> {
            this.currentEBook = Optional.of(ebook);
            saveCurrenEbook();
            loadEBook();
        });
    }

    private Optional<String> readInputStream(InputStream input)  {
        Optional<String> result ;
        byte[] read = new byte[1024];
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            try (InputStream is = input) {
                int cr = 1;
                while (cr>=0) {
                    cr = is.read(read);
                    if (cr>0) os.write(read,0,cr);
                }
            }
        } catch (IOException e) {
            logger.error("unable to read resource",e);
        }
        result=Optional.of(os.toString());
        return result;
    }

    private Optional<String> readResource(String path) {
        Optional<String> result = Optional.empty();
        try {
            result = readInputStream(getClass().getResource(path).openStream());
        } catch (IOException e) {
            logger.error("unable to obtain resource "+path+" input stream",e);
        }
        return result;
    }

    private void loadEBook() {
        currentEBook.ifPresent(e->{
            WebEngine engine = ebookView.getEngine();
            final String defaultCSS = readResource("/default.css").orElse("");
            engine.setUserStyleSheetLocation(e.getDefaultCSS().map(s -> "data:,".concat(s).concat(defaultCSS)).orElse("data:,".concat(defaultCSS)));
            Optional<String> page = e.getCurrentPage().or(() -> e.getFirstPage());
            nightMode = e.loadSavedNightMode().orElse(Boolean.FALSE).booleanValue();
            smartDisplayMode = e.loadSmartDisplayMode().orElse(Boolean.TRUE).booleanValue();
            ebookView.setZoom(e.loadSavedCurrentZoom().orElse(Double.valueOf(1.0)));
            loadContent(page.get());
            //engine.loadContent(e.getPath().toUri().toString());
        });
    }
}
