package it.unisa.sad.playlistmanager.ui.controller;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import javafx.event.ActionEvent;
public class PlaylistController {


    @FXML
    private ListView<Playlist> listPlaylists;
    @FXML
    private Button btnCreatePlaylist;
    @FXML
    private Button btnRemovePlaylist;


    private MusicPlaylistManagerFacade facade;


    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void handleCreatePlaylist(ActionEvent event) {
    }

    @FXML
    private void handleRemovePlaylist(ActionEvent event) {
    }
}
