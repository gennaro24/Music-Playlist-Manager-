package it.unisa.sad.playlistmanager.ui.controller;
import it.unisa.sad.playlistmanager.persistence.db.DatabaseConnectionManager;
import it.unisa.sad.playlistmanager.persistence.db.DatabaseInitializer;
import it.unisa.sad.playlistmanager.persistence.repository.SqliteTrackRepository;
import it.unisa.sad.playlistmanager.persistence.repository.SqlitePlaylistRepository;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.application.service.PlaybackService;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;


public class MainViewController {

    private MusicPlaylistManagerFacade facade;
    private Playlist selectedPlaylist;
    

    @FXML
    private Label lblFeedback;
    @FXML
    private Label labelPageTitle;
    @FXML
    private VBox trackContainer;
    @FXML
    private TrackController trackContainerController;
    @FXML
    private PlaybackController playbackViewController;
    @FXML
    private PlaylistController playlistViewController;
    @FXML
    private Button btnShowTracks;
    
    
    @FXML
    private void initialize() {
        // 1. INIZIALIZZAZIONE INFRASTRUTTURA E BUSINESS LOGIC
        MusicPlaylistManagerFacade coreFacade = bootstrapApplicationContext();
        this.facade = coreFacade;
    
        // 2. INIEZIONE DELLE DIPENDENZE NEI SOTTO-CONTROLLER
        injectFacadeIntoSubControllers(coreFacade);
    
        // 3. CONFIGURAZIONE DEI COMPORTAMENTI INTER-CONTROLLER (EVENT LISTENERS)
        configurePlaylistSelectionBehavior();
        configureTrackPlaybackBehavior();
    
        // 4. AGGIORNAMENTO DELLO STATO INIZIALE DELLA UI
        updateTitleLabel();
    }
    
    /**
     * Inizializza l'intera infrastruttura dati (Connessione e tabelle SQLite), 
     * istanzia i repository e i servizi del Domain Layer, e incapsula il tutto 
     * all'interno dell'Application Facade.
     */
    private MusicPlaylistManagerFacade bootstrapApplicationContext() {
        
        DatabaseConnectionManager connectionManager = new DatabaseConnectionManager();
        DatabaseInitializer initializer = new DatabaseInitializer(connectionManager);
        
        try {
            initializer.initializeDatabase();
        } catch (Exception e) {
            handleInitializationError(e);
        }
    
        // Istanziazione del Data Access Layer (DAL)
        TrackRepository trackRepository = new SqliteTrackRepository(connectionManager);
        PlaylistRepository playlistRepository = new SqlitePlaylistRepository(connectionManager);
        
        // Istanziazione del Domain Service Layer
        TrackService trackService = new TrackService(trackRepository);
        PlaylistService playlistService = new PlaylistService(playlistRepository, trackRepository);
        // Aggiunta del PlaybackService a causa di aggiornamento del facade.
        PlaybackService playbackService = new PlaybackService();
        // Generazione del Mediator unificato (Facade Pattern)
        // Modificato il costruttore del facade a causa di aggiunta del PlaybackService.
        return new MusicPlaylistManagerFacade(trackService, playlistService, playbackService);
    }
    
    /**
     * Inietta la Facciata applicativa all'interno dei sotto-controller 
     * associati alle viste incluse nell'FXML principale.
     */
    private void injectFacadeIntoSubControllers(MusicPlaylistManagerFacade coreFacade) {
        if (trackContainerController != null) {
            trackContainerController.setFacade(coreFacade);
        }
        if (playlistViewController != null) {
            playlistViewController.setFacade(coreFacade);
        }
        if (playbackViewController != null) {
            playbackViewController.setFacade(coreFacade);
        }
    }
    
    /**
     * Configura la callback reattiva sulla ListView del PlaylistController, definendo
     * il comportamento del layout sia in caso di selezione che di toggle-deselezione.
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
     * Configura la callback reattiva sulla ListView del Catalogo, definendo
     * il comportamento del layout quando viene richiesta la riproduzione di una traccia.
     */
    private void configureTrackPlaybackBehavior(){
        if (trackContainerController == null || playbackViewController == null) return;
        trackContainerController.setOnTrackPlayRequested(track ->{
            playbackViewController.playTrack(track);
        });
    }
    /**
     * Gestisce il flusso visivo e logico all'atto della selezione di una playlist.
     */
    private void handlePlaylistSelected(Playlist playlist) {
        this.selectedPlaylist = playlist;

        //selezionata la playlist, mostro la tabella delle tracce di essa
        if (trackContainer != null) {
            trackContainer.setVisible(true);
            trackContainer.setManaged(true);
        }


        // Evita rendering "deformato" al primo accesso:
        // prima allinea il layout del catalogo (stesso workaround manuale che funziona quando apri il catalogo),
        // poi mostra i dati playlist nel ciclo UI successivo.
        if (trackContainerController != null) {
            trackContainerController.showCatalogView();
            Platform.runLater(() -> trackContainerController.displayPlaylistTracks(playlist));
        }

        //modifico il bottone, dando la possibilità di visualizzare il catalogo completo
        if (btnShowTracks != null) {
            btnShowTracks.setText("Visualizza Catalogo");
        }
    
        setUIVeedback("#1f7a1f", "Visualizzazione tracce playlist: " + playlist.getName());
    }
    
    /**
     * Gestisce il ripristino dello stato del layout all'atto della deselezione (toggle).
     */
    private void handlePlaylistDeselected() {
        this.selectedPlaylist = null;
    
        //rimuovo la visualizzazine degli elementi di trackController
        if (trackContainerController != null) {
            trackContainerController.clearPlaylistView();
        }

        //rimuovo la visualizzazine della trackContainer
        if (trackContainer != null) {
            trackContainer.setVisible(false);
            trackContainer.setManaged(false);
        }

        //modifico il bottone, dando la possibilità di visualizzare il catalogo completo
        if (btnShowTracks != null) {
            btnShowTracks.setText("Visualizza Catalogo");
        }

        //modifico il feedback in basso
        setUIVeedback("#1f7a1f", "Playlist deselezionata.");
    }
    
    /**
     * Utility method centralizzato per la propagazione dei messaggi diagnostici sulla UI.
     */
    private void setUIVeedback(String colorHex, String text) {
        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: " + colorHex + ";");
            lblFeedback.setText(text);
        }
    }
    
    /**
     * Gestisce e logga i fallimenti critici durante il bootstrap del database.
     */
    private void handleInitializationError(Exception e) {
        setUIVeedback("red", "Errore durante l'inizializzazione del database: " + e.getMessage());
        System.err.println("[CRITICAL] Fallimento inizializzazione DB: " + e.getMessage());
    }
    

    /**
     * metodo per visualizzare il catalogo completo, cliccando sul bottone "Visualizza Catalogo",
     * sia quando la playlist è selezionata che quando non lo è.
     * @param event
     */
    @FXML
    private void handleShowTracks(ActionEvent event) {
        if (trackContainer == null || btnShowTracks == null || lblFeedback == null) {
            return;
        }
        //se la playlist è selezionata, la deseleziono e mostro la trackContainer
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
            //modifico il bottone, dando la possibilità di chiudere il catalogo
            btnShowTracks.setText("Chiudi Catalogo");
            setUIVeedback("#1f7a1f", "Catalogo completo visualizzato.");
            updateTitleLabel();
            return;
        }
        // Toggle apertura/chiusura sezione catalogo.
        boolean show = !trackContainer.isVisible();
        trackContainer.setVisible(show);
        trackContainer.setManaged(show);

        if (show) {
            // Quando riapro dal bottone, forzo sempre la vista catalogo completo.
            if (trackContainerController != null) {
                trackContainerController.clearForm();
                trackContainerController.showCatalogView();
            }
            btnShowTracks.setText("Chiudi Catalogo");
            setUIVeedback("#1f7a1f", "Catalogo completo visualizzato.");
            if (selectedPlaylist != null) {
                setUIVeedback("#1f7a1f", "Catalogo completo visualizzato (playlist selezionata: " + selectedPlaylist.getName() + ").");
            } else {
                setUIVeedback("#1f7a1f", "Catalogo completo visualizzato.");
            }
            updateTitleLabel();
        } else {
            btnShowTracks.setText("Visualizza Catalogo");
            setUIVeedback("#1f7a1f", "Catalogo chiuso.");
            updateTitleLabel();
        }
    }

    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

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