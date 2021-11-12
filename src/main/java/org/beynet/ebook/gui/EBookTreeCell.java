package org.beynet.ebook.gui;

import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;


public class EBookTreeCell extends TreeCell<EBookOrFolderTreeNode> {
    @Override
    protected void updateItem(EBookOrFolderTreeNode item, boolean empty) {
        super.updateItem(item, empty);
        if (empty==true||item==null) {
            setGraphic(null);
            setText(null);
        }
        else {
            if (item!=null) {
                setGraphic(item.getImageView());
                setText(item.getText());
                item.getTooltip().ifPresentOrElse(s->setTooltip(new Tooltip(s)), ()->setTooltip(null));
            }
        }
    }
}
