package org.beynet.ebook.gui;

import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

public interface EBookOrFolderTreeNode {
    Node getImageView() ;

    String getText();

    Optional<String> getTooltip();

    boolean match(Object o);

    void remove(TreeItem<EBookOrFolderTreeNode> parent, TreeItem<EBookOrFolderTreeNode> itemSelected);

    void onDoubleClick(Stage parent);

    //void display(Consumer<Password> selectedPasswordChange);

    void setExpanded(Boolean newValue);

    Optional<ContextMenu> getContextMenu();
}
