package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.ui.util.StyledAlertFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import java.util.function.Consumer;

/**
 * Sotto-controllore delegato alla gestione del ciclo di vita delle Playlist (Creazione,
 * rimozione ed eventi di selezione a griglia).
 * <p><b>Revisione Sprint 2:</b> Ristrutturato mediante Constructor Injection per adempiere 
 * alle specifiche DoD sul disaccoppiamento ed eliminazione dell'accoppiamento temporale.</p>
 * <p><b>Integrazione US-5.1 (Sprint 2):</b> Sfrutta l'esposizione diretta del metodo di 
 * rimozione della Facade aggiornata, vincolandolo a un dialogo di conferma nativo e rinfresco immediato.</p>
 * @version 2.7
 */
public class PlaylistController {

    /** Istanza centralizzata dell'Application Facade per il coordinamento dei casi d'uso. */
    private final MusicPlaylistManagerFacade facade;
    
    /** Routine di callback invocata quando viene selezionata o deselezionata una playlist. */
    private Consumer<Playlist> onPlaylistSelectedHandler;
    
    /** Routine di callback per richiedere la modifica asincrona del testo informativo delle tracce. */
    public Consumer<String> onShowTracksTextChangeHandler;

    /** Componente grafico ListView per la renderizzazione visiva dell'elenco delle playlist. */
    @FXML private ListView<Playlist> listPlaylists;
    
    /** Pulsante per attivare o confermare il workflow di creazione di una nuova playlist. */
    @FXML private Button btnCreatePlaylist;
    
    /** Pulsante per l'eliminazione permanente della playlist selezionata (US-5.1). */
    @FXML private Button btnRemovePlaylist;
    
    /** T-118: Pulsante per avviare la riproduzione dell'intera playlist selezionata. */
    @FXML private Button btnPlayPlaylist;
    
    /** Campo di testo a comparsa per digitare il nome della nuova playlist. */
    @FXML private TextField txtPlaylistName;
    
    /** Etichetta di feedback cromatico per le comunicazioni di stato all'utente. */
    @FXML private Label lblPlaylistFeedback;

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
    
    /**
     * Predispone la visibilità iniziale dei campi nascosti a startup per preservare l'integrità del layout.
     */
    private void configureInitialFieldsVisibility() {
        if (txtPlaylistName != null) {
            txtPlaylistName.setVisible(false);
            txtPlaylistName.setManaged(false);
        }
        // TASK T-100: Il pulsante rimuovi parte nascosto finché non viene selezionata una riga
        if (btnRemovePlaylist != null) {
            btnRemovePlaylist.setVisible(false);
            btnRemovePlaylist.setManaged(false);
        }
        // T-118: Il pulsante di riproduzione parte nascosto finché non selezioni una playlist
        if (btnPlayPlaylist != null) {
            btnPlayPlaylist.setVisible(false);
            btnPlayPlaylist.setManaged(false);
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
     * Sintonizza i listener reattivi sulla selezione delle celle, aggiornando dinamicamente
     * la visibilità dei comandi di rimozione e riproduzione (Task T-100, T-102 e T-118).
     */
    private void configurePlaylistSelectionListener() {
        listPlaylists.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                // TASK T-100 & T-118: Mostra i pulsanti quando una riga è attiva
                if (btnRemovePlaylist != null) {
                    btnRemovePlaylist.setVisible(true);
                    btnRemovePlaylist.setManaged(true);
                }
                if (btnPlayPlaylist != null) {
                    btnPlayPlaylist.setVisible(true);
                    btnPlayPlaylist.setManaged(true);
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
                if (onPlaylistSelectedHandler != null) {
                    onPlaylistSelectedHandler.accept(newSel);
                }
            } else {
                // TASK T-102 & T-118: Nasconde i pulsanti se non vi è selezione
                if (btnRemovePlaylist != null) {
                    btnRemovePlaylist.setVisible(false);
                    btnRemovePlaylist.setManaged(false);
                }
                if (btnPlayPlaylist != null) {
                    btnPlayPlaylist.setVisible(false);
                    btnPlayPlaylist.setManaged(false);
                }
                if (onPlaylistSelectedHandler != null) {
                    onPlaylistSelectedHandler.accept(null);
                }
            }
        });
    }

    /**
     * Registra il consumatore delegato a notificare i mutamenti testuali delle descrizioni.
     *
     * @param handler Routine di callback esposta dal coordinatore principale.
     */
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
                    
                    // Ripristina lo stato grafico originale del form
                    txtPlaylistName.setVisible(false);
                    txtPlaylistName.setManaged(false);
                    btnCreatePlaylist.setText("Nuova Playlist");
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
     * TASK T-101 e T-102: Intercetta la richiesta di eliminazione permanente.
     * Mostra un pop-up di conferma e, in caso di esito positivo, richiede la rimozione
     * alla Facade aggiornata rinfrescando istantaneamente ed in tempo reale l'interfaccia.
     *
     * @param event Evento di click associato al pulsante di cancellazione.
     */
    @FXML
    private void handleRemovePlaylist(ActionEvent event) {
        if (listPlaylists == null || lblPlaylistFeedback == null) return;
        Playlist selected = listPlaylists.getSelectionModel().getSelectedItem();
        if (selected != null) {
            
            // TASK T-101: Creazione e configurazione del dialogo di conferma nativo
            Alert alert = StyledAlertFactory.create(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Conferma Eliminazione Playlist");
            alert.setHeaderText("Eliminare la playlist selezionata?");
            alert.setContentText("Sei sicuro di voler rimuovere '" + selected.getName() + "'? Le canzoni rimarranno inalterate nel catalogo.");

            java.util.Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    if (facade != null) {
                        // TASK T-102: Invocazione del metodo esposto dalla Facade per l'allineamento su SQLite
                        facade.deletePlaylist(selected.getId());
                    }

                    // TASK T-102: Svuotamento della selezione e aggiornamento istantaneo UI (Scenario 1)
                    listPlaylists.getSelectionModel().clearSelection();
                    listPlaylists.getItems().remove(selected);

                    if (onPlaylistSelectedHandler != null) {
                        onPlaylistSelectedHandler.accept(null); // Azzera il pannello delle tracce a destra
                    }
                    if (listPlaylists.getItems().isEmpty()) {
                        listPlaylists.setPlaceholder(new Label("Nessuna playlist disponibile."));
                    }
                    lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
                    lblPlaylistFeedback.setText("Playlist rimossa.");
                } catch (PlaylistNotFoundException | ValidationException e) {
                    lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
                    lblPlaylistFeedback.setText(e.getMessage());
                } catch (Exception e) {
                    lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
                    lblPlaylistFeedback.setText("Errore durante l'eliminazione della playlist.");
                }
            } else {
                lblPlaylistFeedback.setStyle("-fx-text-fill: #0066cc;");
                lblPlaylistFeedback.setText("Eliminazione annullata.");
            }
        } else {
            lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
            lblPlaylistFeedback.setText("Seleziona una playlist da rimuovere.");
        }
    }

    /**
     * US-12 / T-118: Comanda l'avvio della riproduzione dell'intera playlist selezionata
     * sfruttando il metodo esposto dalla Facade centralizzata.
     *
     * @param event Evento di click del mouse sul bottone Play Playlist.
     */
    @FXML
    private void handlePlayPlaylist(ActionEvent event) {
        if (listPlaylists == null || lblPlaylistFeedback == null) return;
        Playlist selected = listPlaylists.getSelectionModel().getSelectedItem();
        
        if (selected != null) {
            try {
                if (facade != null) {
                    facade.playPlaylist(selected.getId());
                    lblPlaylistFeedback.setStyle("-fx-text-fill: green;");
                    lblPlaylistFeedback.setText("Riproduzione playlist avviata.");
                }
            } catch (PlaylistNotFoundException | ValidationException | IllegalArgumentException e) {
                lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
                lblPlaylistFeedback.setText(e.getMessage());
            } catch (Exception e) {
                lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
                lblPlaylistFeedback.setText("Errore imprevisto durante l'avvio della playlist.");
            }
        } else {
            lblPlaylistFeedback.setStyle("-fx-text-fill: red;");
            lblPlaylistFeedback.setText("Seleziona una playlist per avviarla.");
        }
    }

    /**
     * Invia una richiesta sincrona alla Facade estraendo tutte le playlist salvate
     * e le riversa all'interno della lista grafica globale.
     */
    public void loadPlaylists() {
        if (facade == null || listPlaylists == null) return;
        listPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists()));
    }
}