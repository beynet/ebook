package org.beynet.ebook.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.beynet.ebook.EBook;
import org.beynet.ebook.model.*;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class LibaryWindow extends DialogNotModal implements Observer,EbookEventWatcher {

    public LibaryWindow(Stage parent, Double with, Double height,Consumer<EBook> open) {
        super(parent, with, height);

        VBox pane = new VBox();
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
        tree=new EbookTree(this,open);
        pane.getChildren().add(tree);
        getRootGroup().getChildren().add(pane);
        tree.prefWidthProperty().bind(this.widthProperty());
        tree.prefHeightProperty().bind(this.heightProperty());

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
