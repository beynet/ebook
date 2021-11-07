package org.beynet.ebook.gui;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.beynet.ebook.EBook;


public class EBookTreeNode implements EBookOrFolderTreeNode {

    public EBookTreeNode(EBook ebook) {
        if (ebook==null) throw new IllegalArgumentException("password must not be null");
        this.ebook = ebook;
    }

    @Override
    public Node getImageView() {
        return null;
    }

    @Override
    public void setExpanded(Boolean newValue) {

    }

    public String getText() {
        return ebook.getTitle().orElse("Undefined");
    }

    @Override
    public boolean match(Object o) {
        return false;
    }

    @Override
    public void remove(TreeItem<EBookOrFolderTreeNode> parent, TreeItem<EBookOrFolderTreeNode> itemSelected) {
        //Controller.notifyPasswordRemoved(password.getId());
    }

    @Override
    public void onDoubleClick(Stage parent) {

    }

    private EBook ebook;
}
