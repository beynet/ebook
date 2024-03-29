package org.beynet.ebook.gui;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import org.beynet.ebook.EBook;
import org.beynet.ebook.EBookFactory;
import org.beynet.ebook.utils.I18NHelper;

public class EBookTreeNode implements EBookOrFolderTreeNode {

    public EBookTreeNode(EBook ebook, Consumer<EBook> open) {
        if (ebook == null)
            throw new IllegalArgumentException("password must not be null");
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

    @Override
    public String getText() {
        return ebook.getTitle().orElse("Undefined");
    }

    @Override
    public Optional<String> getTooltip() {
        return Optional.of(ebook.getPath().toString());
    }

    @Override
    public boolean match(Object o) {
        return false;
    }

    @Override
    public void remove(TreeItem<EBookOrFolderTreeNode> parent, TreeItem<EBookOrFolderTreeNode> itemSelected) {
        // Controller.notifyPasswordRemoved(password.getId());
    }

    @Override
    public void onDoubleClick(Stage parent) {

    }

    @Override
    public Optional<ContextMenu> getContextMenu(Stage stage) {
        final ContextMenu ctxMenu = new ContextMenu();
        final MenuItem openNote = new MenuItem("open");
        final MenuItem copyPath = new MenuItem("copyPath");
        final MenuItem changeSubject = new MenuItem(I18NHelper.getLabelResourceBundle().getString("changeSubject"));
        ctxMenu.getItems().add(openNote);
        ctxMenu.getItems().add(copyPath);
        ctxMenu.getItems().add(changeSubject);
        openNote.setOnAction(evt -> {
            try {
                open.accept(EBookFactory.createEBook(ebook.getPath()));
            } catch (IOException e) {
                //
            }
        });
        copyPath.setOnAction(evt -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(ebook.getPath().toString());
            clipboard.setContent(content);
        });

        changeSubject.setOnAction(evt->{
            try {
                new EbookSubject(EBookFactory.createEBook(ebook.getPath()),stage,Double.valueOf(200),Double.valueOf(100)).show();
            } catch (IOException e) {

            }
        });

        return Optional.of(ctxMenu);
    }

    private EBook ebook;
    private Consumer<EBook> open;
}
