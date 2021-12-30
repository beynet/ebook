package org.beynet.ebook.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import org.beynet.ebook.EBook;
import org.beynet.ebook.model.*;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * window use to display user library
 */
public class LibaryWindow extends DialogNotModal implements Observer,EbookEventWatcher {

    /**
     * constructor from main window
     * @param parent
     * @param with
     * @param height
     * @param open the method to be call to open selected ebook in main window
     */
    public LibaryWindow(Stage parent, Double with, Double height,Consumer<EBook> open) {
        super(parent, with, height);
        VBox pane = new VBox();
        pane.setPadding(Insets.EMPTY);
        bar = new MenuBar();
        bar.setUseSystemMenuBar(true);
        bar.prefWidthProperty().bind(widthProperty());
        bar.setMaxHeight(30);


        EBookDatabase.getInstance().addObserver(this);

        setOnCloseRequest(evt->{
            EBookDatabase.getInstance().deleteObserver(this);
        });

        final Menu library = new Menu("library");
        
        // add note book menu item
        // -----------------------
        {
            final MenuItem addToLibrary = new MenuItem("add folder");
            addToLibrary.setOnAction((evt) -> {
                addToLibrary();
            });
            library.getItems().add(addToLibrary);
        }
        // clear database
        // --------------
        {
            final MenuItem clear = new MenuItem("clear");
            clear.setOnAction((evt) -> {
                clear();
            });
            library.getItems().add(clear);
        }
        
        bar.getMenus().add(library);
        pane.getChildren().add(bar);

        // add search box
        search = new TextField();
        search.prefWidthProperty().bind(this.widthProperty());
        /*search.setOnKeyPressed(evt->{
            
        });*/
        search.setOnAction(evt->{
            String text = search.getText();
            tree.setQuery(text);
        });
        pane.getChildren().add(search);


        // display password
        ScrollPane box = new ScrollPane();
        box.getStyleClass().add(Styles.SCROLL_PANE);
        tree=new EbookTree(this,open);
        tree.getStyleClass().add(Styles.TREE);
        box.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        box.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        box.setContent(tree);
        //box.getChildren().add(tree);

        setOnShown(evt->{
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            tree.setPrefHeight(getCurrentScene().getHeight()-bar.getHeight()-search.getHeight());
            tree.setPrefWidth(getCurrentScene().getWidth());
        });


        getCurrentScene().heightProperty().addListener((observable, oldValue, newValue) -> {
            tree.setPrefHeight(newValue.doubleValue()-bar.getHeight()-search.getHeight());
        });


        getCurrentScene().widthProperty().addListener((observable, oldValue, newValue) -> {
            tree.setPrefWidth(newValue.doubleValue());
        });

        //tree.prefWidthProperty().bind(box.prefWidthProperty());

        /*tree.setPrefWidth(getWidth()-40);
        this.widthProperty().addListener((observable, oldValue, newValue) -> {
            tree.setPrefWidth(newValue.doubleValue()-40);
        });

        tree.setPrefHeight(getHeight()-bar.getHeight()-search.getHeight());
        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            box.setPrefHeight(newValue.doubleValue()-bar.getHeight()-search.getHeight());
            tree.setPrefHeight(newValue.doubleValue()-bar.getHeight()-search.getHeight());
        });*/

        //tree.prefWidthProperty().bind(box.widthProperty());
        //tree.prefHeightProperty().bind(box.heightProperty());

        pane.getChildren().add(box);

        getRootGroup().getChildren().add(pane);



        //tree.prefWidthProperty().bind(this.widthProperty());
        //box.prefHeightProperty().bind(this.heightProperty());

        EBookDatabase.getInstance().addObserver(this);
    }


    @Override
    public void update(Observable o, Object arg) {
        ((Event)arg).accept(this);
    }

    @Override
    public void visit(EBookModifiedOrAdded evt) {
        Platform.runLater(()->tree.reloadEbook(evt.getEbook()));
    }

    @Override
    public void visit(EBookDeleted eBookDeleted) {
        Platform.runLater(()->tree.deleteEBook(eBookDeleted.getPath()));
    }

    private void addToLibrary() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File result = directoryChooser.showDialog(this);
            try {
                Path path = result.toPath();
                Service<Void> reIndexService = new Service<Void>() {

                    @Override
                    protected Task<Void> createTask() {
                        return new Task<Void>() {

                            @Override
                            protected Void call() throws Exception {
                                EBookDatabase.getInstance().indexePath(path);
                                Platform.runLater(() -> {
                                        tree.display();
                                });
                                // TODO Auto-generated method stub
                                return null;
                            }

                        };
                    }
                };
                reIndexService.start();
                
            } catch (Exception e) {

            }
        
    }
    private void clear() {
        Platform.runLater(() -> {
            try {
                EBookDatabase.getInstance().clearIndexes();
                tree.display();
            } catch (IOException e) {
                
            }
        });
    }

    private EbookTree tree;
    private MenuBar bar;
    private TextField search;
}
