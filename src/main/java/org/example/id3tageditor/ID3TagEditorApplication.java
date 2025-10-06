package org.example.id3tageditor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ID3TagEditorApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ID3TagEditorApplication.class.getResource("/id3-tag-editor-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800); // Start with a good default size
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/modern-style.css")).toExternalForm());


        ID3TagEditorController controller = fxmlLoader.getController();
        controller.setPrimaryStage(stage);

        stage.getIcons().add(new Image(Objects.requireNonNull(ID3TagEditorApplication.class.getResourceAsStream("/icon.png"))));

        stage.setTitle("TuneLabel - ID3 Tag Editor");
        stage.setScene(scene);

        stage.setMinWidth(1000);
        stage.setMinHeight(700);


        stage.setResizable(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}