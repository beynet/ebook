package org.beynet.ebook.gui;

import java.util.Optional;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Created by beynet on 09/11/14.
 */
public class FolderTreeNode implements EBookOrFolderTreeNode {
    public FolderTreeNode(String folderName) {
        this.folderName = folderName;
        imageView = new ImageView(folderOpen);
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
    }

    public ImageView getImageView() {
        return imageView;
    }

    @Override
    public void setExpanded(Boolean newValue) {
        if (newValue==true) {
            imageView.setImage(folderOpen);
        }
        else {
            imageView.setImage(folder);
        }
    }

    @Override
    public String getText() {
        return folderName;
    }

    @Override
    public Optional<String> getTooltip() {
        return Optional.empty();
    }

    @Override
    public boolean match(Object o) {
        return folderName.equals(o);
    }

    @Override
    public void remove(TreeItem<EBookOrFolderTreeNode> parent, TreeItem<EBookOrFolderTreeNode> itemSelected) {

    }

    @Override
    public void onDoubleClick(Stage parent) {

    }

    @Override
    public Optional<ContextMenu> getContextMenu(Stage stage) {
        return Optional.empty();
    }

    private String folderName;
    private ImageView imageView;
    private static final Image folder = new Image(EBookOrFolderTreeNode.class.getResourceAsStream("/Folder.png"));
    private static final Image folderOpen = new Image(EBookOrFolderTreeNode.class.getResourceAsStream("/Folder_Open.png"));
    
}
