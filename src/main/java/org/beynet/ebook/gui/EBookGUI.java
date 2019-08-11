package org.beynet.ebook.gui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class EBookGUI extends Application {
    private Scene currentScene;
    private Stage currentStage;

    private WebView ebookView    ;
    private EBook   currentEBook ;

    public static void main(Path eBookPath) {
        launch(eBookPath.toString());
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.currentStage = stage ;
        Parameters parameters = getParameters();
        if (parameters.getRaw().isEmpty()) throw new IllegalArgumentException("must provide an ebook path");
        currentEBook = EBookFactory.createEBook(Paths.get(parameters.getRaw().get(0)));
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

        htop.getChildren().addAll(previousPage,nextPage,minus,plus);

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
        ebookView.getEngine().setUserStyleSheetLocation(currentEBook.getDefaultCSS().map(s->"data:,".concat(s)).orElse("data:,"));
        ebookView.getEngine().loadContent(currentEBook.getNextPage().orElse(""));
        ebookView.prefHeightProperty().bind(pane.heightProperty());
        ebookView.prefWidthProperty().bind(pane.widthProperty());


        pane.getChildren().add(ebookView);
        group.getChildren().add(mainVBOX);


        currentStage.setScene(currentScene);
        currentStage.show();
    }
}
