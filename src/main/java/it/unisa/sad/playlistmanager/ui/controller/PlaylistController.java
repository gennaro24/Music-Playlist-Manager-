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
        // 1. CONFIGURAZIONE INIZIALE DEI COMPONENTI GRAFICI
        configureInitialFieldsVisibility();
    
        // 2. CONFIGURAZIONE REATTIVA E EVENT-DRIVEN DELLA LISTVIEW
        if (listPlaylists != null) {
            loadPlaylists();
            configurePlaylistCellFactory();
            configurePlaylistSelectionListener();
        }
    }
    
    /**
     * Gestisce la visibilità e il dimensionamento iniziale dei campi di testo
     * dedicati alla creazione delle playlist (Workflow a comparsa).
     */
    private void configureInitialFieldsVisibility() {
        if (txtPlaylistName != null) {
            txtPlaylistName.setVisible(false);
            txtPlaylistName.setManaged(false);
        }
    }
    
    /**
     * Configura la CellFactory per la ListView delle Playlist, implementando sia
     * il rendering testuale custom che il meccanismo di "Toggle Deselection".
     */
    private void configurePlaylistCellFactory() {
        listPlaylists.setCellFactory(lv -> {
            ListCell<Playlist> cell = new ListCell<>() {
                @Override
                protected void updateItem(Playlist item, boolean empty) {
                    super.updateItem(item, empty);
                    setText((empty || item == null) ? null : item.getName());
                }
            };
    
            // Filtro degli eventi per intercettare il secondo click (Deselezione)
            cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (!cell.isEmpty() && cell.isSelected()) {
                    // Svuota lo stato di selezione della ListView
                    listPlaylists.getSelectionModel().clearSelection();
                    System.out.println("[UI TOGGLE PLAYLIST] Playlist deselezionata correttamente.");
    
                    if (lblPlaylistFeedback != null) {
                        lblPlaylistFeedback.setText("");
                    }
    
                    // Propaga 'null' al MainViewController per comandare il ripristino del catalogo
                    if (onPlaylistSelectedHandler != null) {
                        onPlaylistSelectedHandler.accept(null);
                    }
    
                    // Consuma l'evento per impedire a JavaFX di riassegnare la selezione
                    event.consume();
                }
            });
    
            return cell;
        });
    }
    
    /**
     * Configura il listener sulla proprietà di selezione della ListView per gestire
     * il recupero delle tracce e la notifica al MainViewController.
     */
    private void configurePlaylistSelectionListener() {
        listPlaylists.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            // Se la selezione diventa null (es. a causa del Toggle), la logica viene ignorata
            // poiché già gestita dall'event filter della cella
            if (newSel == null || onPlaylistSelectedHandler == null) {
                return;
            }
    
            if (facade != null) {
                // Gestione dei messaggi di feedback all'utente basata sul contenuto
                if (newSel.getTracks() == null || newSel.getTracks().isEmpty()) {
                    if (lblPlaylistFeedback != null) {
                        lblPlaylistFeedback.setStyle("-fx-text-fill: #b0413e;");
                        lblPlaylistFeedback.setText("La playlist selezionata non contiene tracce.");
                    }
                } else {
                    if (lblPlaylistFeedback != null) {
                        lblPlaylistFeedback.setText("");
                    }
                }
            }
    
            // Propaga l'oggetto Playlist selezionato al MainViewController
            onPlaylistSelectedHandler.accept(newSel);
        });
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

        // WORKFLOW A COMPARSA: Se il campo di testo è nascosto, lo mostriamo al primo
        // click
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
        }
        if (facade != null) {
             
            try {
                Playlist newPlaylist = facade.createPlaylist(name);
                if (newPlaylist != null) {
                    listPlaylists.getItems().add(newPlaylist);
                    listPlaylists.getSelectionModel().select(newPlaylist);
                    txtPlaylistName.clear();
                    lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
                    lblPlaylistFeedback.setText("Playlist creata con successo.");
                }
            }catch (IllegalArgumentException e) {
                lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
                lblPlaylistFeedback.setText(e.getMessage());
            } catch (Exception e) {
                lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
                lblPlaylistFeedback.setText("Errore durante la creazione della playlist.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRemovePlaylist(ActionEvent event) {
        if (listPlaylists == null || lblPlaylistFeedback == null) {
            return;
        }
        Playlist selected = listPlaylists.getSelectionModel().getSelectedItem();
        if (selected != null) {
            listPlaylists.getSelectionModel().clearSelection();
            listPlaylists.getItems().remove(selected);

            // Deseleziona la playlist selezionata e notifica l'handler per nascondere la tabella canzoni
            listPlaylists.getSelectionModel().clearSelection();

            // Segnala la deselezione al MainViewController (o chi ascolta) per far nascondere la tabella dei brani
            if (onPlaylistSelectedHandler != null) {
                onPlaylistSelectedHandler.accept(null);
            }

            if (listPlaylists.getItems().isEmpty()) {
                // Messaggio placeholder quando non ci sono più playlist
                listPlaylists.setPlaceholder(new Label("Nessuna playlist disponibile."));
            }
            lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
            lblPlaylistFeedback.setText("Playlist rimossa.");
        } else {
            lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
            lblPlaylistFeedback.setText("Seleziona una playlist da rimuovere.");
        }
    }

    private void loadPlaylists() {
        // se il facade o la tabella non sono inizializzati, non faccio nulla
        if (facade == null || listPlaylists == null) {
            return;
        }
        // interrogo il facade per ottenere tutte le tracce e le setto nella tabella
        // quanto invoco il setItems, la tabella si aggiorna con i dati della facade
        // i dati vengono inseriti nella colonna corretta tramite PropertyValueFactory
        // definito in initialize
        //listPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists()));

    }
}
