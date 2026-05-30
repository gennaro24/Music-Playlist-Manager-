package it.unisa.sad.playlistmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/it/unisa/sad/playlistmanager/ui/view/MainView.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 600);
        stage.setTitle("Music Playlist Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}