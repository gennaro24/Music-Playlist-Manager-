package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

/**
 * Controllore principale della vista di dashboard (MainView).
 * Agisce come coordinatore (Mediator) di presentazione tra i sotto-controller
 * inclusi nell'interfaccia grafica (Viste nidificate via {@code <fx:include>}).
 * * <p><b>Revisione Sprint 2 (US-Tech 01):</b> È stato rimosso l'intero blocco legacy
 * di bootstrap infrastrutturale. La classe non detiene alcuna dipendenza verso
 * {@code DatabaseConnectionManager}, repository concrete o pacchetti di servizio,
 * delegando la risoluzione del grafo delle dipendenze alla {@code ControllerFactory}
 * mediante Constructor Injection.</p>
 * @version 2.0
 * @see MusicPlaylistManagerFacade
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

    @FXML private Label lblFeedback;
    @FXML private Label labelPageTitle;
    @FXML private VBox trackContainer;
    
    /**
     * Sotto-controllore iniettato da JavaFX per la gestione del catalogo tracce.
     */
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
        // CONFIGURAZIONE DEI COMPORTAMENTI INTER-CONTROLLER (EVENT LISTENERS)
        configurePlaylistSelectionBehavior();
        configureTrackPlaybackBehavior();
    
        // AGGIORNAMENTO DELLO STATO INIZIALE DELLA UI
        updateTitleLabel();
    }
    
    /**
     * Configura la callback reattiva sulla ListView del PlaylistController.
     * Gestisce il layout geometrico della dashboard a seconda che una playlist
     * venga selezionata o deselezionata (Toggle Behavior).
     */
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
            // Workaround per prevenire difetti di rendering geometrico al primo rendering
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
}