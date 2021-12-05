package org.beynet.ebook.gui;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.beynet.ebook.EBook;
import org.beynet.ebook.model.EBookDatabase;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class EbookTree extends TreeView<EBookOrFolderTreeNode> {
    public EbookTree(Stage parent, Consumer<EBook> open) {
        

        setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    int selectedIndex = getSelectionModel().getSelectedIndex();
                    if (selectedIndex >= 0) {
                        TreeItem<EBookOrFolderTreeNode> itemSelected = getSelectionModel().getSelectedItem();
                        itemSelected.getValue().onDoubleClick(parent);
                    }
                }
            }
            else if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
                int selectedIndex = getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    TreeItem<EBookOrFolderTreeNode> itemSelected = getSelectionModel().getSelectedItem();
                    Optional<ContextMenu> contextMenu = itemSelected.getValue().getContextMenu();
                    contextMenu.ifPresent(m->m.show(this, mouseEvent.getScreenX(), mouseEvent.getScreenY()));
                }
            }
        });

        setCellFactory(fileCopiedTreeView -> {
            EBookTreeCell result = new EBookTreeCell();
            /*
             * result.setOnMouseClicked(event -> { ctxMenu.hide(); if
             * (MouseButton.SECONDARY.equals(event.getButton())) { result. if
             * (result.isEmpty() == false) { openNote.setDisable(false); } else {
             * openNote.setDisable(true); } ctxMenu.show(this, event.getScreenX(),
             * event.getScreenY()); } else if
             * (MouseButton.PRIMARY.equals(event.getButton())) { if (isFocused()==true) {
             * 
             * } } });
             */
            return result;
        });
        this.open = open ;
        query = "";
        display();
    }


    public void setQuery(String query) {
        this.query = query;
        Platform.runLater(()->{
            display();
        });
    }

    public void reloadEbook(EBook ebook) {
        TreeItem<EBookOrFolderTreeNode> eBookItem=cellsByPath.get(ebook.getPath());
        if (eBookItem!=null) eBookItem.getParent().getChildren().remove(eBookItem);
        addEBook(getRoot(),ebook);
    }

    public void deleteEBook(Path path) {
        TreeItem<EBookOrFolderTreeNode> eBookItem=cellsByPath.get(path);
        if (eBookItem!=null) eBookItem.getParent().getChildren().remove(eBookItem);
    }

    private void addEBook(TreeItem<EBookOrFolderTreeNode> rootTreeItem,EBook ebook) {
        TreeItem<EBookOrFolderTreeNode> eBookItem = new TreeItem<>(new EBookTreeNode(ebook,open));
        cellsByPath.put(ebook.getPath(),eBookItem);
        String subject = ebook.getSubjects().stream().findFirst().orElse("Undefined");
        TreeItem<EBookOrFolderTreeNode> subjectNode = subjectsCells.get(subject);
        if (subjectNode == null) {
            subjectNode = new TreeItem<>(new FolderTreeNode(subject));
            authorBySubjectCells.put(subjectNode, new HashMap<>());
            subjectsCells.put(subject, subjectNode);
            rootTreeItem.getChildren().add(subjectNode);
        }
        String author = ebook.getAuthor().orElse("Undefined");
        Map<String, TreeItem<EBookOrFolderTreeNode>> authorsMap = authorBySubjectCells.get(subjectNode);
        TreeItem<EBookOrFolderTreeNode> authorNode = authorsMap.get(author);
        if (authorNode == null) {
            authorNode = new TreeItem<>(new FolderTreeNode(author));
            authorsMap.put(author, authorNode);
            subjectNode.getChildren().add(authorNode);
        }
        authorNode.getChildren().add(eBookItem);
    }

    public void display() {

        subjectsCells = new HashMap<>();
        authorBySubjectCells = new HashMap<>();
        cellsByPath = new HashMap<>();

        TreeItem<EBookOrFolderTreeNode> rootTreeItem = new TreeItem<>(new FolderTreeNode("EBooks"));
        rootTreeItem.setExpanded(true);
        rootTreeItem.expandedProperty().addListener((observable, oldValue, newValue) -> {
            rootTreeItem.getValue().setExpanded(newValue);
        });

        try {
            List<EBook> ebooks = EBookDatabase.getInstance().list(query);
            for (EBook ebook : ebooks) {
                addEBook(rootTreeItem,ebook);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        setRoot(rootTreeItem);
        setShowRoot(true);
    }

    private Map<Path,TreeItem<EBookOrFolderTreeNode>>                                          cellsByPath ;
    private Map<String, TreeItem<EBookOrFolderTreeNode>>                                       subjectsCells;
    private Map<TreeItem<EBookOrFolderTreeNode>, Map<String, TreeItem<EBookOrFolderTreeNode>>> authorBySubjectCells;

    private Consumer<EBook> open;
    private String query;


}
