package org.beynet.ebook.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class EBookGUI extends Application {
    private final static Logger logger = LogManager.getLogger(EBookGUI.class);
    private Scene currentScene;
    private Stage currentStage;

    private WebView ebookView    ;
    private EBook   currentEBook ;

    public static void main(Path eBookPath) {
        if (eBookPath!=null) launch(eBookPath.toString());
        else launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.currentStage = stage ;
        Parameters parameters = getParameters();
        if (!parameters.getRaw().isEmpty()) {
            currentEBook = EBookFactory.createEBook(Paths.get(parameters.getRaw().get(0)));
        }
        Group group = new Group();


        currentScene = new Scene(group, 640, 480);

        VBox mainVBOX = new VBox();
        mainVBOX.prefHeightProperty().bind(currentScene.heightProperty());
        mainVBOX.prefWidthProperty().bind(currentScene.widthProperty());

        // next button and previous button in the top
        HBox htop = new HBox();
        Button nextPage = new Button("next");
        nextPage.setTooltip(new Tooltip("next"));
        nextPage.setOnAction(event -> {
            ebookView.getEngine().loadContent(currentEBook.getNextPage().orElse(""));
        });

        Button previousPage = new Button("previous");
        previousPage.setTooltip(new Tooltip("previous"));
        previousPage.setOnAction(event -> {
            ebookView.getEngine().loadContent(currentEBook.getPreviousPage().orElse(""));
        });

        Button plus = new Button("+");
        plus.setTooltip(new Tooltip("+"));
        plus.setOnAction(event -> {
            ebookView.setZoom(ebookView.getZoom()+0.1);
        });

        Button minus = new Button("-");
        minus.setTooltip(new Tooltip("-"));
        minus.setOnAction(event -> {
            ebookView.setZoom(ebookView.getZoom()-0.1);
        });

        Button openEBook = new Button("open");


        htop.getChildren().addAll(previousPage,nextPage,minus,plus,openEBook);

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


        ebookView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    EventListener listener = new EventListener() {
                        @Override
                        public void handleEvent(Event ev) {
                            if ("click".equals(ev.getType())) {
                                String href = ((Element)ev.getCurrentTarget()).getAttribute("href");
                                if (href==null) return;
                                Platform.runLater(()->ebookView.getEngine().loadContent(currentEBook.loadPage(href).orElse("")));
                            }
                        }
                    };

                    Document doc = ebookView.getEngine().getDocument();
                    NodeList nodeList = doc.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener("click", listener, true);
                    }
                }
            }
        });

        openEBook.setOnAction(event -> {
            FileChooser directoryChooser = new FileChooser();
            File result = directoryChooser.showOpenDialog(currentStage);
            try {
                Path path = result.toPath();
                EBook eBook = EBookFactory.createEBook(path);
                Platform.runLater(()->{
                    this.currentEBook = eBook;
                    loadEBook();
                });
            } catch(Exception e) {
                logger.error("unable to load ebook",e);
            }
        });

        pane.getChildren().add(ebookView);
        group.getChildren().add(mainVBOX);


        currentStage.setScene(currentScene);
        currentStage.show();
    }

    private void loadEBook() {
        if (currentEBook!=null) {
            ebookView.getEngine().setUserStyleSheetLocation(currentEBook.getDefaultCSS().map(s -> "data:,".concat(s)).orElse("data:,"));
            Optional<String> page = currentEBook.getCurrentPage().or(() -> currentEBook.getFirstPage());
            ebookView.getEngine().loadContent(page.get());
        }
    }
}
