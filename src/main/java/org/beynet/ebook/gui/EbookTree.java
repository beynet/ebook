package org.beynet.ebook.gui;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.beynet.ebook.EBook;
import org.beynet.ebook.database.EBookDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EbookTree extends TreeView<EBookOrFolderTreeNode> {
    public EbookTree(Stage parent, Pane passwordContentPane) {
        setCellFactory(fileCopiedTreeView -> new EBookTreeCell());

        subjectsCells  = new HashMap<>();

        TreeItem<EBookOrFolderTreeNode> rootTreeItem = new TreeItem<>(new FolderTreeNode("EBooks"));
        rootTreeItem.setExpanded(true);
        rootTreeItem.expandedProperty().addListener((observable, oldValue, newValue) -> {
            rootTreeItem.getValue().setExpanded(newValue);
        });

        try {
            List<EBook> ebooks = EBookDatabase.getInstance().list();
            for (EBook ebook : ebooks) {
                TreeItem<EBookOrFolderTreeNode> eBookItem = new TreeItem<>(new EBookTreeNode(ebook));
                String subject = ebook.getSubjects().stream().findFirst().orElse("Undefined");
                TreeItem<EBookOrFolderTreeNode> subjectNode = subjectsCells.get(subject);
                if (subjectNode==null) {
                    subjectNode = new TreeItem<>(new FolderTreeNode(subject));
                    subjectsCells.put(subject,subjectNode);
                    rootTreeItem.getChildren().add(subjectNode);
                }
                subjectNode.getChildren().add(eBookItem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        setRoot(rootTreeItem);
        setShowRoot(true);
    }


    private Map<String,TreeItem<EBookOrFolderTreeNode>> subjectsCells ;
}
