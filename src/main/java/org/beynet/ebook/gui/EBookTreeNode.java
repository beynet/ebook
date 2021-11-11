package org.beynet.ebook.gui;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookFactory;


public class EBookTreeNode implements EBookOrFolderTreeNode {

    public EBookTreeNode(EBook ebook,Consumer<EBook> open) {
        if (ebook==null) throw new IllegalArgumentException("password must not be null");
        this.ebook = ebook;
        this.open = open;
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


    @Override
    public Optional<ContextMenu> getContextMenu() {
        final ContextMenu ctxMenu = new ContextMenu();
        final MenuItem openNote = new MenuItem("open");
        ctxMenu.getItems().add(openNote);
        openNote.setOnAction(evt->{
            try {
                open.accept(EBookFactory.createEBook(ebook.getPath()));
            } catch (IOException e) {
               //
            }
        });

        return Optional.of(ctxMenu);
    }

    private EBook           ebook;
    private Consumer<EBook> open;
}
