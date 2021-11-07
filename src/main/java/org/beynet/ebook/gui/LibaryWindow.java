package org.beynet.ebook.gui;

import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class LibaryWindow extends DialogNotModal{

    public LibaryWindow(Stage parent, Double with, Double height) {
        super(parent, with, height);


        HBox pane = new HBox();

        tree=new EbookTree(this,pane);
        pane.getChildren().add(tree);
        getRootGroup().getChildren().add(pane);
    }


    private EbookTree tree;
}
