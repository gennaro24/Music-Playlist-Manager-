package it.unisa.sad.playlistmanager.ui.controller;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import javafx.event.ActionEvent;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;


public class PlaylistController {


    private MusicPlaylistManagerFacade facade;
    private Consumer<Playlist> onPlaylistSelectedHandler;
    public Consumer<String> onShowTracksTextChangeHandler;




    @FXML
    private ListView<Playlist> listPlaylists;
    @FXML
    private Button btnCreatePlaylist;
    @FXML
    private Button btnRemovePlaylist;
    @FXML
    private TextField txtPlaylistName;
    @FXML
    private Label lblPlaylistFeedback;


    @FXML
    private void initialize() {
        if (txtPlaylistName != null) {
            txtPlaylistName.setVisible(false);
            txtPlaylistName.setManaged(false);
        }
        if (listPlaylists != null) {
            loadPlaylists();
            // Cattura il cambio di selezione sulla ListView delle playlist
            listPlaylists.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Playlist item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            listPlaylists.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null && onPlaylistSelectedHandler != null) {
                    // Propaga l'evento al MainViewController
                    onPlaylistSelectedHandler.accept(newSelection);
                    //modifico btnShowTracks in mainviewcontroller
                    
                }
            });
            // Se si riclicca la stessa playlist gia' selezionata, riattiva comunque la vista playlist.
            listPlaylists.setOnMouseClicked(mouseEvent -> {
                Playlist selected = listPlaylists.getSelectionModel().getSelectedItem();
                if (selected != null && onPlaylistSelectedHandler != null) {
                    onPlaylistSelectedHandler.accept(selected);
                }
            });
        }

    }

    public void setOnShowTracksTextChange(Consumer<String> handler) {
        this.onShowTracksTextChangeHandler = handler;
    }

    public void setOnPlaylistSelected(Consumer<Playlist> handler) {
        this.onPlaylistSelectedHandler = handler;
    }

    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
        loadPlaylists();
    }

    @FXML

    private void handleCreatePlaylist(ActionEvent event) {
        if (listPlaylists == null || txtPlaylistName == null || lblPlaylistFeedback == null) {
            return;
        }

        // WORKFLOW A COMPARSA: Se il campo di testo è nascosto, lo mostriamo al primo click
        if (!txtPlaylistName.isVisible()) {
            txtPlaylistName.setVisible(true);
            txtPlaylistName.setManaged(true);
            txtPlaylistName.requestFocus(); // Richiede il focus per facilitare l'immissione di testo
            lblPlaylistFeedback.setStyle("-fx-text-fill: #0066cc;");
            lblPlaylistFeedback.setText("Digita il nome e salva la playlist");
            btnCreatePlaylist.setText("Salva la playlist");
            return;
        }
        String rawName = txtPlaylistName.getText();
        String name = rawName != null ? rawName.trim() : "";
        if (name.isEmpty()) {
            lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
            lblPlaylistFeedback.setText("Inserisci un nome playlist.");
            return;
        }else{
            System.out.println("\n[TEST UI] Nome playlist: " + name);
            lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
            lblPlaylistFeedback.setText("Playlist " + name + " creata con successo.");
            btnCreatePlaylist.setText("Nuova Playlist");
            txtPlaylistName.setVisible(false);
            txtPlaylistName.setManaged(false);
            txtPlaylistName.clear();
        }
        /*
        boolean duplicate = listPlaylists.getItems().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));
        if (duplicate) {
            lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
            lblPlaylistFeedback.setText("Esiste gia una playlist con questo nome.");
            return;
        }
        /*     
        /*   
        Playlist newPlaylist = new Playlist(name);
        listPlaylists.getItems().add(newPlaylist);
        listPlaylists.getSelectionModel().select(newPlaylist);
        txtPlaylistName.clear();
        lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
        lblPlaylistFeedback.setText("Playlist creata con successo.");
        */
        if (facade != null) {
            facade.addPlaylist(name);
            // Sincronizzazione atomica dei nodi della ListView
            listPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists2()));
            
            lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
            lblPlaylistFeedback.setText("Playlist '" + name + "' creata con successo.");
        } else {
            lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
            lblPlaylistFeedback.setText("Errore di sistema: Facciata non disponibile.");
        }
    }

    @FXML
    private void handleRemovePlaylist(ActionEvent event) {
        if (listPlaylists == null || lblPlaylistFeedback == null) {
            return;
        }
        Playlist selected = listPlaylists.getSelectionModel().getSelectedItem();
        if (selected != null) {
            listPlaylists.getItems().remove(selected);
            lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
            lblPlaylistFeedback.setText("Playlist rimossa.");
        } else {
            lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
            lblPlaylistFeedback.setText("Seleziona una playlist da rimuovere.");
        }
    }


    private void loadPlaylists() {
        //se il facade o la tabella non sono inizializzati, non faccio nulla
        if (facade == null || listPlaylists == null) {
            return;
        }
        //interrogo il facade per ottenere tutte le tracce e le setto nella tabella
        //quanto invoco il setItems, la tabella si aggiorna con i dati della facade
        //i dati vengono inseriti nella colonna corretta tramite PropertyValueFactory definito in initialize
        listPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists2()));


    }
}
