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
    private final static String CONF_FILE_NAME ="ebooks.ini";
    private final static String CURRENT_EBOOK_PATH ="CurrentEbookPath";
    private Properties properties = null;
    private Path propertyFilePath = null ;

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

    @Override
    public void start(Stage stage) throws Exception {

        this.currentStage = stage ;
        Parameters parameters = getParameters();
        if (!parameters.getRaw().isEmpty()) {
            currentEBook = Optional.of(EBookFactory.createEBook(Paths.get(parameters.getRaw().get(0))));
            saveCurrenEbook();
        }
        else {
            loadCurrentEbook();
        }
        Group group = new Group();


        currentScene = new Scene(group, 640, 480);

        VBox mainVBOX = new VBox();
        mainVBOX.prefHeightProperty().bind(currentScene.heightProperty());
        mainVBOX.prefWidthProperty().bind(currentScene.widthProperty());

        // next button and previous button in the top
        HBox htop = new HBox();
        Button firstPage = new Button("first page");
        firstPage.setTooltip(new Tooltip("first page"));
        firstPage.setOnAction(event -> {
            currentEBook.ifPresent(e->ebookView.getEngine().loadContent(e.getFirstPage().orElse("")));
        });

        Button nextPage = new Button("next");
        nextPage.setTooltip(new Tooltip("next"));
        nextPage.setOnAction(event -> {
            currentEBook.ifPresent(e->ebookView.getEngine().loadContent(e.getNextPage().orElse("")));
        });

        Button previousPage = new Button("previous");
        previousPage.setTooltip(new Tooltip("previous"));
        previousPage.setOnAction(event -> {
            currentEBook.ifPresent(e->ebookView.getEngine().loadContent(e.getPreviousPage().orElse("")));
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
                ebookView.getEngine().executeScript("document.body.style.backgroundColor = \"black\";\ndocument.body.style.color = \"grey\";");
            }
            else {
                ebookView.getEngine().executeScript("document.body.style.backgroundColor = null;\ndocument.body.style.color = null;");
            }
            currentEBook.ifPresent(e->e.saveNightMode(nightMode));
        });

        htop.getChildren().addAll(firstPage,previousPage,nextPage,minus,plus,openEBook,nightModeButton);

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
                                Platform.runLater(()->currentEBook.ifPresent(e->ebookView.getEngine().loadContent(e.loadPage(href).orElse(""))));
                            }
                        }
                    };

                    Document doc = ebookView.getEngine().getDocument();
                    NodeList nodeList = doc.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener(CLICK, listener, true);
                    }
                    nodeList = doc.getElementsByTagName("img");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Element img = (Element) nodeList.item(i);
                        String href = img.getAttribute("src");

                        currentEBook.map(e -> e.convertRessourceLocalPathToGlobalURL(href).orElse(null)).ifPresent(s->img.setAttribute("src",s));
                    }
                    if (nightMode==true) {
                        ebookView.getEngine().executeScript("document.body.style.paddingRight = \"50px\";document.body.style.backgroundColor = \"black\";\ndocument.body.style.color = \"grey\";");
                    }

                    //ebookView.getEngine().executeScript("document.charset = \"UTF-8\"");
                }
            }
        });

        openEBook.setOnAction(event -> {
            FileChooser directoryChooser = new FileChooser();
            File result = directoryChooser.showOpenDialog(currentStage);
            if (result!=null) {
                try {
                    Path path = result.toPath();
                    EBook eBook = EBookFactory.createEBook(path);
                    Platform.runLater(() -> {
                        this.currentEBook = Optional.of(eBook);
                        saveCurrenEbook();
                        loadEBook();
                    });
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
            ebookView.setZoom(e.loadSavedCurrentZoom().orElse(Double.valueOf(1.0)));
            engine.loadContent(page.get());
            //engine.loadContent(e.getPath().toUri().toString());
        });
    }
}
