package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.util.Duration;

/**
 * Controllore principale della vista di dashboard (MainView).
 * Agisce come coordinatore (Mediator) di presentazione tra i sotto-controller.
 * * @version 3.0 (Sprint 3 - Undo Integration)
 */
public class MainViewController {

    /**
     * Riferimento immutabile alla facciata unificata del modulo applicativo.
     */
    private final MusicPlaylistManagerFacade facade;
    
    /**
     * Stato locale della playlist attualmente selezionata dall'utente.
     */
    private Playlist selectedPlaylist;
    private Timeline undoMonitorTimeline;

    @FXML private Label lblFeedback;
    @FXML private Label labelPageTitle;
    @FXML private VBox trackContainer;
    
    // Pulsante aggiunto per US-19/20 (T3-11)
    @FXML private Button btnUndo;
    
    @FXML private TrackController trackContainerController;
    
    /**
     * Sotto-controllore iniettato da JavaFX per il pannello di riproduzione.
     */
    @FXML private PlaybackController playbackViewController;
    
    /**
     * Sotto-controllore iniettato da JavaFX per la barra laterale delle playlist.
     */
    @FXML private PlaylistController playlistViewController;
    
    @FXML private Button btnShowTracks;
    
    /**
     * Costruttore conforme alle specifiche di Constructor Injection unificata (Task T-63).
     * Invocato a runtime dalla {@code ControllerFactory} impostata sul loader FXML.
     *
     * @param facade L'istanza dell'Application Facade pre-configurata nel Composition Root.
     */
    public MainViewController(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }
    
    /**
     * Metodo di ciclo di vita nativo di JavaFX. Inizializza i comportamenti
     * reattivi inter-controller e sincronizza lo stato geometrico iniziale della UI.
     */
    @FXML
    private void initialize() {
        configurePlaylistSelectionBehavior();
        configureTrackPlaybackBehavior();
        
        // INNESTATO SPRINT 3: Sincronizzazione della creazione playlist automatica
        configurePlaylistCreationBehavior();
        
        updateTitleLabel();
    
        // T3-11: Avvia il monitoraggio reattivo dello stato del pulsante Undo
        startUndoStateMonitoring();
    }
    
    /**
     * TASK T3-11: Configura un ciclo temporizzato leggero (ogni 300ms) per verificare 
     * lo stato dello stack dei comandi e disabilitare/abilitare il pulsante in tempo reale.
     */
    private void startUndoStateMonitoring() {
        undoMonitorTimeline = new Timeline(new KeyFrame(Duration.millis(300), event -> {
            if (btnUndo != null && facade != null) {
                btnUndo.setDisable(!facade.canUndo());
            }
        }));
        undoMonitorTimeline.setCycleCount(Timeline.INDEFINITE);
        undoMonitorTimeline.play();
    }
    
    /**
     * TASK T3-11, T3-12, T3-13: Gestore centralizzato dell'azione di Undo.
     * Innesca il rollback logico e ridistribuisce l'ordine di rinfresco ai sotto-moduli.
     */
    @FXML
    private void handleUndo(ActionEvent event) {
        if (facade == null) return;
        
        try {
            if (facade.canUndo()) {
                // 1. Esecuzione dell'Undo sul motore applicativo
                facade.undoLastAction();
                
                // 2. TASK T3-12: Sincronizzazione ed allineamento dell'interfaccia grafica
                if (playlistViewController != null) {
                    playlistViewController.loadPlaylists();
                }
                if (trackContainerController != null) {
                    trackContainerController.refresh();
                }
                if (playbackViewController != null) {
                    playbackViewController.refreshView();
                }
                
                updateTitleLabel();
                
                // 3. TASK T3-13: Feedback testuale chiaro di successo
                setUIFeedback("#1f7a1f", "↶ Successo: Ultima operazione annullata!");
            } else {
                // TASK T3-13: Operazione non annullabile o storico vuoto
                setUIFeedback("#b0413e", "Nessuna operazione da annullare nello storico.");
            }
        } catch (Exception e) {
            setUIFeedback("#b0413e", "Errore durante l'esecuzione dell'undo: " + e.getMessage());
        }
    }
    
    private void configurePlaylistSelectionBehavior() {
        if (playlistViewController == null) return;
    
        playlistViewController.setOnPlaylistSelected(playlist -> {
            if (playlist != null) {
                handlePlaylistSelected(playlist);
            } else {
                handlePlaylistDeselected();
            }
            updateTitleLabel();
        });
    }

    /**
     * Configura il canale di comunicazione asincrono tra il catalogo brani e il player.
     * All'atto della richiesta di riproduzione, delega l'operazione al PlaybackController.
     */
    private void configureTrackPlaybackBehavior(){
        if (trackContainerController == null || playbackViewController == null) return;
        trackContainerController.setOnTrackPlayRequested(track -> {
            playbackViewController.playTrack(track);
        });
    }

    /**
     * Coordina l'aggiornamento visivo della dashboard all'atto della selezione di una playlist.
     * Forza la transizione del TrackController verso la vista di dettaglio playlist.
     *
     * @param playlist L'oggetto di dominio Playlist selezionato.
     */
    private void handlePlaylistSelected(Playlist playlist) {
        this.selectedPlaylist = playlist;

        if (trackContainer != null) {
            trackContainer.setVisible(true);
            trackContainer.setManaged(true);
        }

        if (trackContainerController != null) {
            trackContainerController.showCatalogView();
            Platform.runLater(() -> trackContainerController.displayPlaylistTracks(playlist));
        }

        if (btnShowTracks != null) {
            btnShowTracks.setText("Visualizza Catalogo");
        }
    
        setUIFeedback("#1f7a1f", "Visualizzazione tracce playlist: " + playlist.getName());
    }
    
    /**
     * Ripristina la topologia visiva asettica della dashboard quando la selezione
     * della playlist decade o viene annullata.
     */
    private void handlePlaylistDeselected() {
        this.selectedPlaylist = null;
    
        if (trackContainerController != null) {
            trackContainerController.clearPlaylistView();
        }

        if (trackContainer != null) {
            trackContainer.setVisible(false);
            trackContainer.setManaged(false);
        }

        if (btnShowTracks != null) {
            btnShowTracks.setText("Visualizza Catalogo");
        }

        setUIFeedback("#1f7a1f", "Playlist deselezionata.");
    }
    
    /**
     * Intercettore grafico centralizzato per la stampa di messaggi diagnostici 
     * e di feedback utente sulla barra di stato inferiore.
     *
     * @param colorHex Il codice esadecimale del colore del testo (es. "red", "#1f7a1f").
     * @param text     Il messaggio testuale da renderizzare.
     */
    private void setUIFeedback(String colorHex, String text) {
        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: " + colorHex + ";");
            lblFeedback.setText(text);
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante di visualizzazione o chiusura del catalogo completo.
     * Implementa un meccanismo di toggle logico basato sullo stato di visibilità corrente.
     *
     * @param event L'evento di azione generato dal click sul bottone.
     */
    @FXML
    private void handleShowTracks(ActionEvent event) {
        if (trackContainer == null || btnShowTracks == null || lblFeedback == null) {
            return;
        }
        if (selectedPlaylist != null) {
            selectedPlaylist = null;
            if (playlistViewController != null) {
                playlistViewController.clearCurrentSelection();
            }
            trackContainer.setVisible(true);
            trackContainer.setManaged(true);
            if (trackContainerController != null) {
                trackContainerController.clearForm();
                trackContainerController.showCatalogView();
            }
            btnShowTracks.setText("Chiudi Catalogo");
            setUIFeedback("#1f7a1f", "Catalogo completo visualizzato.");
            updateTitleLabel();
            return;
        }
        
        boolean show = !trackContainer.isVisible();
        trackContainer.setVisible(show);
        trackContainer.setManaged(show);

        if (show) {
            if (trackContainerController != null) {
                trackContainerController.clearForm();
                trackContainerController.showCatalogView();
            }
            btnShowTracks.setText("Chiudi Catalogo");
            setUIFeedback("#1f7a1f", "Catalogo completo visualizzato.");
            updateTitleLabel();
        } else {
            btnShowTracks.setText("Visualizza Catalogo");
            setUIFeedback("#1f7a1f", "Catalogo chiuso.");
            updateTitleLabel();
        }
    }

    /**
     * Aggiorna dinamicamente la Label del titolo della pagina principale 
     * per riflettere il contesto operativo corrente.
     */
    private void updateTitleLabel() {
        if (labelPageTitle == null) {
            return;
        }
        if (selectedPlaylist != null) {
            labelPageTitle.setText("Tracce " + selectedPlaylist.getName());
        } else if (trackContainer != null && trackContainer.isVisible()) {
            labelPageTitle.setText("Tracce Catalogo");
        } else {
            labelPageTitle.setText("Home");
        }
    }
    /**
     * Sintonizza il listener reattivo sul TrackController. Quando viene generata una 
     * playlist automatica, ordina al PlaylistController di riallineare la barra laterale.
     */
    private void configurePlaylistCreationBehavior() {
        if (trackContainerController == null || playlistViewController == null) return;
        
        trackContainerController.setOnPlaylistCreated(() -> {
            // Comanda il ricaricamento istantaneo delle playlist a sinistra
            playlistViewController.loadPlaylists();
        });
    }
}