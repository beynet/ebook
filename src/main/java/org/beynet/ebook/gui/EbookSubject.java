package org.beynet.ebook.gui;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.beynet.ebook.EBook;
import org.beynet.ebook.utils.I18NHelper;

/**
 * Created by beynet on 11/04/2015.
 */
public class EbookSubject extends DialogModal{

    public EbookSubject(EBook currentEBook, Stage parent, Double width, Double height) {
        super(parent, width, height);
        this.currentEBook = currentEBook;
        GridPane pane  = new GridPane();
        getRootGroup().getChildren().add(pane);

        Text label = new Text(I18NHelper.getLabelResourceBundle().getString("subject"));
        pane.add(label, 0, 0);

        name  = new TextField();
        name.setPromptText(I18NHelper.getLabelResourceBundle().getString("subject"));
        if (!this.currentEBook.getSubjects().isEmpty()) name.setText(this.currentEBook.getSubjects().get(0));
        name.setPrefWidth(getWidth() - label.getLayoutBounds().getWidth() - 5);
        widthProperty().addListener((observable, oldValue, newValue) -> {
                name.setPrefWidth(getWidth()-label.getLayoutBounds().getWidth()-5);
            }
        );
        pane.add(name, 1, 0);

        Button confirm = new Button("OK");
        pane.add(confirm,1,1);
        confirm.setOnAction(event -> {
            this.close();
            this.currentEBook.getSubjects().clear();
            if (!"".equals(name.getText()) && name.getText()!=null) {
                this.currentEBook.getSubjects().add(name.getText());
            }
            Service<Void> saveEBookService = new Service<Void>() {

                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {

                        @Override
                        protected Void call() throws Exception {
                            currentEBook.updateSubjects();
                            return null;
                        }

                    };
                }
            };
            saveEBookService.start();
        });
    }

    public String getName() {
        return name.getText();
    }

    private TextField name;
    private EBook     currentEBook;
}
