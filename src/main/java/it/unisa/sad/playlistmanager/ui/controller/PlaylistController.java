package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import java.util.function.Consumer;

/**
 * Sotto-controllore delegato alla gestione del ciclo di vita delle Playlist (Creazione,
 * rimozione ed eventi di selezione a griglia).
 * * <p><b>Revisione Sprint 2:</b> Ristrutturato mediante Constructor Injection per adempiere 
 * alle specifiche DoD sul disaccoppiamento ed eliminazione dell'accoppiamento temporale.</p>
 * @version 2.0
 */
public class PlaylistController {

    private final MusicPlaylistManagerFacade facade;
    private Consumer<Playlist> onPlaylistSelectedHandler;
    public Consumer<String> onShowTracksTextChangeHandler;

    @FXML private ListView<Playlist> listPlaylists;
    @FXML private Button btnCreatePlaylist;
    @FXML private Button btnRemovePlaylist;
    @FXML private TextField txtPlaylistName;
    @FXML private Label lblPlaylistFeedback;
    @FXML private Button btnPlayPlaylist;

    /**
     * Costruttore uniforme per Constructor Injection (Task T-63).
     *
     * @param facade L'istanza dell'Application Facade iniettata dal bootstrap.
     */
    public PlaylistController(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    /**
     * Inizializza i componenti grafici, configura la CellFactory personalizzata
     * e richiede il caricamento delle playlist attive sul database.
     */
    @FXML
    private void initialize() {
        // 1. CONFIGURAZIONE INIZIALE DEI COMPONENTI GRAFICI
        configureInitialFieldsVisibility();
    
        // 2. CONFIGURAZIONE REATTIVA E EVENT-DRIVEN DELLA LISTVIEW
        if (listPlaylists != null) {
            if (facade != null) {
                loadPlaylists();
            }
            configurePlaylistCellFactory();
            configurePlaylistSelectionListener();
        }
    }
    
    private void configureInitialFieldsVisibility() {
        if (txtPlaylistName != null) {
            txtPlaylistName.setVisible(false);
            txtPlaylistName.setManaged(false);
        }
    }
    
    /**
     * Personalizza il rendering delle righe della ListView implementando
     * l'intercettazione degli eventi mouse per catturare la deselezione utente (Toggle).
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
    
            cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (!cell.isEmpty() && cell.isSelected()) {
                    listPlaylists.getSelectionModel().clearSelection();
                    if (lblPlaylistFeedback != null) {
                        lblPlaylistFeedback.setText("");
                    }
                    if (onPlaylistSelectedHandler != null) {
                        onPlaylistSelectedHandler.accept(null);
                    }
                    event.consume();
                }
            });
            return cell;
        });
    }
    
    /**
     * Sintonizza i listener reattivi sulla selezione delle celle, notificando
     * tempestivamente il coordinatore globale in caso di modifica del brano evidenziato.
     */
    private void configurePlaylistSelectionListener() {
        listPlaylists.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel == null || onPlaylistSelectedHandler == null) {
                if (newSel == null) return;
            }
            if (facade != null) {
                if (newSel.getTracks() == null || newSel.getTracks().isEmpty()) {
                    if (lblPlaylistFeedback != null && onShowTracksTextChangeHandler != null) {
                        lblPlaylistFeedback.setStyle("-fx-text-fill: #b0413e;");
                        onShowTracksTextChangeHandler.accept("La playlist selezionata non contiene tracce.");
                    }
                } else {
                    if (lblPlaylistFeedback != null) {
                        lblPlaylistFeedback.setText("");
                    }
                }
            }
            onPlaylistSelectedHandler.accept(newSel);
        });
    }

    public void setOnShowTracksTextChange(Consumer<String> handler) {
        this.onShowTracksTextChangeHandler = handler;
    }

    /**
     * Specifica il gestore eventi da lanciare non appena una playlist viene cliccata.
     *
     * @param handler Routine di callback esposta dal controller contenitore.
     */
    public void setOnPlaylistSelected(Consumer<Playlist> handler) {
        this.onPlaylistSelectedHandler = handler;
    }

    /**
     * Forza l'azzeramento dello stato di selezione della ListView.
     */
    public void clearCurrentSelection() {
        if (listPlaylists != null) {
            listPlaylists.getSelectionModel().clearSelection();
        }
        if (lblPlaylistFeedback != null) {
            lblPlaylistFeedback.setText("");
        }
    }

    /**
     * Gestisce la logica di creazione di una playlist mediante workflow a comparsa.
     * Al secondo click, valida l'input ed invoca l'Application Service tramite la Facade.
     *
     * @param event Evento di click del mouse sul bottone.
     */
    @FXML
    private void handleCreatePlaylist(ActionEvent event) {
        if (listPlaylists == null || txtPlaylistName == null || lblPlaylistFeedback == null) return;

        if (!txtPlaylistName.isVisible()) {
            txtPlaylistName.setVisible(true);
            txtPlaylistName.setManaged(true);
            txtPlaylistName.requestFocus();
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
            } catch (ValidationException | IllegalArgumentException e) {
                lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
                lblPlaylistFeedback.setText(e.getMessage());
            } catch (Exception e) {
                lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
                lblPlaylistFeedback.setText("Errore durante la creazione della playlist.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Intercetta la richiesta di eliminazione permanente della playlist correntemente selezionata.
     *
     * @param event Evento di click associato al pulsante di cancellazione.
     */
    @FXML
    private void handleRemovePlaylist(ActionEvent event) {
        if (listPlaylists == null || lblPlaylistFeedback == null) return;
        Playlist selected = listPlaylists.getSelectionModel().getSelectedItem();
        if (selected != null) {
            listPlaylists.getSelectionModel().clearSelection();
            listPlaylists.getItems().remove(selected);
            listPlaylists.getSelectionModel().clearSelection();

            if (onPlaylistSelectedHandler != null) {
                onPlaylistSelectedHandler.accept(null);
            }
            if (listPlaylists.getItems().isEmpty()) {
                listPlaylists.setPlaceholder(new Label("Nessuna playlist disponibile."));
            }
            lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
            lblPlaylistFeedback.setText("Playlist rimossa.");
        } else {
            lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
            lblPlaylistFeedback.setText("Seleziona una playlist da rimuovere.");
        }
    }

    /**
     * Invia una richiesta sincrona alla Facade estraendo tutte le playlist salvate
     * e le riversa all'interno della lista grafica observable.
     */
    private void loadPlaylists() {
        if (facade == null || listPlaylists == null) return;
        listPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists()));
    }

    /**
     * T-118 (US-12): Intercetta la richiesta di avvio di una playlist.
     * Recupera l'ID della playlist selezionata e invoca il PlaybackService tramite la Facade.
     *
     * @param event Evento di click associato al pulsante Play della Playlist.
     */
    @FXML
    private void handlePlayPlaylist(ActionEvent event) {
        if (listPlaylists == null || lblPlaylistFeedback == null || facade == null) return;
        
        Playlist selected = listPlaylists.getSelectionModel().getSelectedItem();
        
        if (selected != null) {
            try {
                // Invoca la facade per avviare la riproduzione della playlist
                facade.playPlaylist(selected.getId());
                
                lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
                lblPlaylistFeedback.setText("In riproduzione: " + selected.getName());
                
            } catch (ValidationException e) {
                // T-120: Cattura l'errore della playlist vuota
                lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
                lblPlaylistFeedback.setText(e.getMessage()); // "Impossibile avviare una playlist vuota."
            } catch (Exception e) {
                lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
                lblPlaylistFeedback.setText("Errore durante l'avvio della playlist.");
            }
        } else {
            lblPlaylistFeedback.setStyle("-fx-text-fill: #b0413e;");
            lblPlaylistFeedback.setText("Seleziona una playlist per avviare la riproduzione.");
        }
    }
}