package it.unisa.sad.playlistmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import it.unisa.sad.playlistmanager.bootstrap.AppFactory;
import it.unisa.sad.playlistmanager.bootstrap.SqliteAppFactory;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.bootstrap.ControllerFactory;
import javafx.scene.Parent;




public class Main extends Application {
    private MusicPlaylistManagerFacade facade;

    public void init() throws Exception {
        // Istanziazione polimorfica della Factory
        AppFactory appFactory = new SqliteAppFactory();

        //chiama il metedo per creare il facade, creare il database e inizializzarlo
        this.facade = appFactory.createFacade();
    }


    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/it/unisa/sad/playlistmanager/ui/view/MainView.fxml")
        );

        // Istanziazione della factory per i controller, iniettandovi la Facade
        ControllerFactory controllerFactory = new ControllerFactory(this.facade);
            
        loader.setControllerFactory(controllerFactory);

        //Ogni volta che il loader incontra un controller, richiama il metodo call della ControllerFactory per istanziare il controller
        Parent root = loader.load();

        // Configurazione della finestra principale (Stage)
        Scene scene = new Scene(root);
        stage.setTitle("Music Playlist Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}