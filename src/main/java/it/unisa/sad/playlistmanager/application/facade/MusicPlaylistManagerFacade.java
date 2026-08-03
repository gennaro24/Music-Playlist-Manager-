package it.unisa.sad.playlistmanager.application.facade;

import java.util.List;

import it.unisa.sad.playlistmanager.application.command.CommandFactory;
import it.unisa.sad.playlistmanager.application.command.UndoManager;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.AddTrackCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.CreatePlaylistCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.DeletePlaylistCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.DeleteTrackCommand;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.service.AutoPlaylistService;
import it.unisa.sad.playlistmanager.application.service.PlaybackService;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.application.service.TagService;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.AutoPlaylistCriteria;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.domain.model.Tag;

/**
 * Facciata principale dell'applicazione (Facade Pattern).
 * Fornisce un'interfaccia unificata e semplificata per il Presentation Layer,
 * centralizzando l'accesso a tutti i servizi del modulo Application.
 * 
 * @version 1.0
 */
public class MusicPlaylistManagerFacade {

    /** Riferimento al servizio applicativo per la gestione delle tracce. */
    private final TrackService trackService;
    private final PlaylistService playlistService;
    private final PlaybackService playbackService;
    private final CommandFactory commandFactory;
    private final UndoManager undoManager;
    private final TagService tagService;
    private final AutoPlaylistService autoPlaylistService;

    /**
     * Costruttore della Facade. Inietta le dipendenze dei servizi necessari.
     * Questo costruttore è pensato per essere usato nei test.
     *
     * @param trackService    Il servizio incaricato della logica di business delle
     *                        tracce.
     * @param playlistService Il servizio incaricato della logica di business delle
     *                        playlist.
     * @param playbackService Il servizio incaricato della logica di business del
     *                        playback.
     */
    public MusicPlaylistManagerFacade(
        TrackService trackService,
        PlaylistService playlistService,
        PlaybackService playbackService,
        TagService tagService) {
            this(
                    trackService,
                    playlistService,
                    playbackService,
                    tagService,
                    new CommandFactory(trackService, playlistService, tagService),
                    new UndoManager()
            );
    }

    /**
     * Costruttore con i servizi e l'infrastruttura command/undo.
     *
     * Mantiene la firma precedente: costruisce internamente un
     * {@link AutoPlaylistService} di default a partire dai service gia' iniettati,
     * cosi' i chiamatori esistenti non devono cambiare.
     *
     * @param trackService servizio delle tracce
     * @param playlistService servizio delle playlist
     * @param playbackService servizio di playback
     * @param tagService servizio dei tag
     * @param commandFactory factory condivisa dei command
     * @param undoManager cronologia globale della sessione
     */
    public MusicPlaylistManagerFacade(
            TrackService trackService,
            PlaylistService playlistService,
            PlaybackService playbackService,
            TagService tagService,
            CommandFactory commandFactory,
            UndoManager undoManager) {
        this(trackService, playlistService, playbackService, tagService, commandFactory, undoManager,
                new AutoPlaylistService(trackService, tagService, playlistService));
    }

    /**
     * Costruttore completo usato dal bootstrap applicativo.
     *
     * @param trackService servizio delle tracce
     * @param playlistService servizio delle playlist
     * @param playbackService servizio di playback
     * @param tagService servizio dei tag
     * @param commandFactory factory condivisa dei command
     * @param undoManager cronologia globale della sessione
     * @param autoPlaylistService servizio delle playlist automatiche
     */
    public MusicPlaylistManagerFacade(
            TrackService trackService,
            PlaylistService playlistService,
            PlaybackService playbackService,
            TagService tagService,
            CommandFactory commandFactory,
            UndoManager undoManager,
            AutoPlaylistService autoPlaylistService) {
        this.playbackService = playbackService;
        this.trackService = trackService;
        this.playlistService = playlistService;
        this.tagService = tagService;
        this.commandFactory = commandFactory;
        this.undoManager = undoManager;
        this.autoPlaylistService = autoPlaylistService;
    }

    /**
     * Espone al Presentation Layer la funzionalità di aggiunta di una nuova traccia
     * nel catalogo.
     * Svolge il ruolo di pass-through verso il servizio specializzato
     * {@link TrackService}.
     *
     * @param title    Il titolo della canzone da aggiungere.
     * @param author   L'artista della canzone.
     * @param duration La durata complessiva in secondi.
     * @param genre    Il genere della canzone.
     * @param year     L'anno di pubblicazione.
     * @return L'oggetto {@link Track} creato, validato e salvato.
     * @throws IllegalArgumentException Se i parametri violano le regole di
     *                                  validazione del dominio.
     */
    public Track addTrack(String title, String author, int duration, String genre, int year) {
        AddTrackCommand command = commandFactory.createAddTrackCommand(
                title, author, duration, genre, year);
        undoManager.executeAndPush(command);
        return command.getCreatedTrack();
    }

    /**
     * Centralizza l'accesso al caso d'uso di creazione di una playlist.
     * 
     * @param name Il nome della playlist.
     * @return La playlist creata.
     */
    public Playlist createPlaylist(String name) {
        CreatePlaylistCommand command = commandFactory.createCreatePlaylistCommand(name);
        undoManager.executeAndPush(command);
        return command.getCreatedPlaylist();
    }

    /**
     * Espone al Presentation Layer la funzionalità di eliminazione di una playlist.
     * 
     * @param playlistId L'identificativo unico della playlist da eliminare.
     * @return La playlist eliminata.
     */
    public Playlist deletePlaylist(String playlistId) {
        DeletePlaylistCommand command = commandFactory.createDeletePlaylistCommand(playlistId);
        undoManager.executeAndPush(command);
        playbackService.handleDeletedPlaylist(playlistId);
        return command.getDeletedPlaylist();
    }

    /**
     * Espone al Presentation Layer l'elenco completo di tutte le tracce presenti
     * nel catalogo.
     * Risolve il Task T-11 della prima sprint.
     *
     * @return Una lista contenente tutte le tracce musicali disponibili.
     */
    public List<Track> getAllTracks() {
        // Delega del pass-through verso il servizio di competenza
        return this.trackService.getAllTracks();
    }

    /**
     * Espone al Presentation Layer l'elenco completo di tutte le playlist
     * configurate.
     * Risolve il Task T-25 della prima sprint.
     *
     * @return Una lista contenente tutte le playlist caricate dal modulo
     *         persistence.
     */
    public List<Playlist> getAllPlaylists() {
        // Delega del pass-through verso il servizio di competenza
        return this.playlistService.getAllPlaylists();
    }

    /**
     * Fornisce l'accesso al dettaglio di una specifica playlist identificata da ID.
     * Consente alla UI di verificare la presenza di elementi e l'ordine delle
     * tracce.
     * Risolve il Task T-25 della prima sprint.
     *
     * @param id L'identificativo univoco della risorsa.
     * @return La playlist corrispondente, o null se non trovata.
     */
    public Playlist getPlaylistById(String id) {
        // Delega del pass-through verso il servizio di competenza
        return this.playlistService.getPlaylistById(id);
    }

    /**
     * Centralizza ed espone alla UI il caso d'uso di aggiunta traccia a una
     * playlist.
     *
     * @param playlistId Identificativo della playlist di destinazione.
     * @param trackId    Identificativo della traccia da aggiungere.
     */
    public void addTrackToPlaylist(String playlistId, String trackId) {
        undoManager.executeAndPush(
                commandFactory.createAddTrackToPlaylistCommand(playlistId, trackId));
    }

    /**
     * Espone al Presentation Layer la funzionalità di rimozione di una traccia da
     * una playlist.
     * Agisce da puro pass-through verso il servizio applicativo competente.
     *
     * @param playlistId L'identificativo unico della playlist di riferimento.
     * @param trackId    L'identificativo unico della traccia da cancellare dalla
     *                   playlist.
     * @throws IllegalArgumentException Se i parametri o le regole di business
     *                                  vengono violate.
     */
    public void removeTrackFromPlaylist(String playlistId, String trackId) {
        undoManager.executeAndPush(
                commandFactory.createRemoveTrackFromPlaylistCommand(playlistId, trackId));
    }

    /**
     * Espone al Presentation Layer la funzionalità di recupero delle tracce
     * associate a una playlist.
     * 
     * @param playlistId L'identificativo unico della playlist di riferimento.
     * @return Una lista di tracce associate alla playlist.
     */
    public List<Track> getTracksForPlaylist(String playlistId) {
        return playlistService.getTracksForPlaylist(playlistId);
    }
    // =====================METODI PER IL PLAYBACK=====================:

    /**
     * Espone al Presentation Layer la funzionalità di avvio del playback di una
     * traccia specifica.
     * Recupera la traccia tramite il servizio TrackService e delega l'operazione al
     * PlaybackService.
     * 
     * @return Una fotografia dello stato corrente del playback dopo l'avvio
     *         (PlaybackSnapshot).
     * @param trackId L'identificativo della traccia da riprodurre.
     * @throws TrackNotFoundException Se la traccia non esiste (propagata dal
     *                                Service).
     */
    public PlaybackSnapshot playTrack(String trackId) {
        Track track = trackService.getTrackById(trackId);
        playbackService.playTrack(track);
        return getPlaybackSnapshot();
    }

    /**
     * Espone al Presentation Layer la funzionalità di pausa del playback.
     * Delega l'operazione al PlaybackService.
     * 
     * @return Una fotografia dello stato corrente del playback dopo la pausa
     *         (PlaybackSnapshot).
     */
    public PlaybackSnapshot pausePlayback() {
        playbackService.pause();
        return getPlaybackSnapshot();
    }

    /**
     * Espone al presentation layer la funzionalità di avvio del playback di una
     * playlist specifica.
     *
     * @param playlistId identificativo della playlist da riprodurre
     * @return snapshot aggiornato del playback
     */
    public PlaybackSnapshot playPlaylist(String playlistId) {
        playbackService.playPlaylist(playlistId);
        return playbackService.getSnapshot();
    }

    /**
     * Avvia il playback di tutto il catalogo musicale.
     * Risolve la richiesta di riproduzione globale e abilita lo shuffle sul catalogo.
     */
    public PlaybackSnapshot playCatalog() {
        //recupero tutte le tracce
        List<Track> allTracks = this.trackService.getAllTracks();
        //avvio la coda di riproduzione
        this.playbackService.playCatalog(allTracks);
        return getPlaybackSnapshot();
    }

    /**
     * Espone al Presentation Layer la funzionalità di skip alla traccia successiva.
     * 
     * @return Una fotografia dello stato corrente del playback dopo lo skip
     *         (PlaybackSnapshot).
     */
    public PlaybackSnapshot skipToNext() {
        playbackService.skipToNext();
        return getPlaybackSnapshot();
    }

    /**
     * Espone al Presentation Layer il cambio modalità verso single-track-loop
     * (REPEAT_ONE).
     *
     * @return snapshot aggiornato del playback
     */
    public PlaybackSnapshot enableSingleTrackLoopMode() {
        playbackService.enableSingleTrackLoopMode();
        return getPlaybackSnapshot();
    }

    /**
     * Espone al Presentation Layer la disattivazione del single-track-loop.
     *
     * @return snapshot aggiornato del playback
     */
    public PlaybackSnapshot disableSingleTrackLoopMode() {
        playbackService.disableSingleTrackLoopMode();
        return getPlaybackSnapshot();
    }

    public PlaybackSnapshot enableRepeatAllMode() {
        playbackService.enableRepeatAllMode();
        return getPlaybackSnapshot();
    }

    public PlaybackSnapshot disableRepeatAllMode() {
        playbackService.disableRepeatAllMode();
        return getPlaybackSnapshot();
    }

    /**
     * Restituisce l'unico DTO letto dalla UI per conoscere lo stato del player.
     * * @return Una fotografia dello stato corrente del playback
     * (PlaybackSnapshot).
     */
    public PlaybackSnapshot getPlaybackSnapshot() {
        return playbackService.getSnapshot();
    }

    /**
     * Indica se la sorgente corrente supporta la modalità shuffle.
     */
    public boolean isShuffleAvailable() {
        return playbackService.isShuffleAvailable();
    }

    /**
     * Avanza di un "tick" il playback e restituisce lo snapshot aggiornato.
     *
     * @return snapshot aggiornato del playback
     */
    public PlaybackSnapshot tickPlayback() {
        //avanzamento del playback di un secondo
        playbackService.tick();
        //restituisco lo snapshot aggiornato
        return playbackService.getSnapshot();
    }

    /**
     * Espone al presentation layer la funzionalità di modifica di una traccia.
     * 
     * @return la traccia modificata da ritornare alla UI.
     */
    public Track updateTrack(String trackId, Track updatedTrack) {
        return trackService.updateTrack(trackId, updatedTrack);
    }

    /**
     * Espone al presentation layer la funzionalità di eliminazione di una traccia.
     * Notifica il PlayBackService se la traccia corrente è in playback.
     * 
     * @return la traccia eliminata da ritornare alla UI.
     */
    public Track deleteTrack(String trackId) {
        DeleteTrackCommand command = commandFactory.createDeleteTrackCommand(trackId);
        undoManager.executeAndPush(command);
        playbackService.handleDeletedTrack(trackId);
        return command.getDeletedTrack();
    }

    /**
     * Annulla l'ultima operazione mutativa registrata nella sessione.
     */
    public void undoLastAction() {
        undoManager.undoLast();
    }

    /**
     * Indica se la cronologia contiene almeno un'operazione annullabile.
     *
     * @return true se e' disponibile un undo
     */
    public boolean canUndo() {
        return undoManager.canUndo();
    }

    // il controller lo deve chiamare quando la traccia corrente termina.
    public PlaybackSnapshot handleTrackCompleted() {
        return playbackService.handleTrackCompleted();
    }

    /**
     * T-144: Espone al Presentation Layer la funzionalità di cambio modalità di riproduzione.
     * Consente alla UI di attivare lo Shuffle o di tornare alla riproduzione sequenziale.
     *
     * @param mode La modalità di playback da impostare (es. PlaybackMode.SHUFFLE)
     * @return Lo snapshot aggiornato del playback.
     */
    public PlaybackSnapshot setPlaybackMode(it.unisa.sad.playlistmanager.domain.model.PlaybackMode mode) {
        this.playbackService.setPlaybackMode(mode);
        return this.playbackService.getSnapshot();
    }

    //====================METODI PER IL TAG=====================:

    public Tag addTag(String name) {
        return tagService.addTag(name);
    }

    public Tag deleteTag(String tagId) {
        return tagService.deleteTag(tagId);
    }

    public void assignTagToTrack(String trackId, String tagId) {
        tagService.assignTagToTrack(trackId, tagId);
    }

    public void removeTagFromTrack(String trackId, String tagId) {
        tagService.removeTagFromTrack(trackId, tagId);
    }

    public List<Track> getTracksByTag(String tagId) {
        return tagService.getTracksByTag(tagId);
    }

    public List<Tag> getAllTags() {
        return tagService.getAllTags();
    }

    public List<Tag> getTagsForTrack(String trackId) {
        return tagService.getTagsForTrack(trackId);
    }

    //====================METODI PER LE PLAYLIST AUTOMATICHE=====================:

    /**
     * Espone al Presentation Layer l'anteprima di una playlist automatica:
     * restituisce le tracce del catalogo che soddisfano i criteri indicati, senza
     * creare alcuna playlist. Pass-through verso {@link AutoPlaylistService}.
     *
     * @param criteria criteri di genere, anno e tag scelti dall'utente
     * @return le tracce corrispondenti, eventualmente lista vuota
     */
    public List<Track> previewAutoPlaylist(AutoPlaylistCriteria criteria) {
        return autoPlaylistService.previewAutoPlaylist(criteria);
    }

    /**
     * TASK T3-30: Espone al Presentation Layer il caso d'uso di creazione effettiva
     * di una playlist automatica basata sui criteri specificati.
     *
     * @param name     Il nome da assegnare alla playlist.
     * @param criteria I criteri di filtraggio (genere, anno, tag).
     * @return La playlist generata e salvata su database.
     */
    /**
     * TASK SPRINT 3: Gestione transazionale e annullabile della playlist automatica.
     * Include validazione preventiva anti-duplicazione e iniezione nell'UndoManager.
     */
    public Playlist createAutoPlaylist(String name, AutoPlaylistCriteria criteria) {
        // 1. Recupera l'anteprima delle tracce dal servizio di Adinolfi
        List<Track> matchingTracks = autoPlaylistService.previewAutoPlaylist(criteria);
        
        // 2. VALIDAZIONE: Verifica che il nome della playlist non sia vuoto
        if (name == null || name.trim().isEmpty()) {
            throw new it.unisa.sad.playlistmanager.application.exceptions.ValidationException("Il nome della playlist automatica è obbligatorio.");
        }
        
        // 3. VALIDAZIONE BUG UNIQUE: Controlla preventivamente se esiste già una playlist con lo stesso nome, evitando duplicati
        boolean nameExists = playlistService.getAllPlaylists().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name.trim()));
        if (nameExists) {
            throw new it.unisa.sad.playlistmanager.application.exceptions.ValidationException("Esiste già una playlist denominata '" + name + "'. Scegli un nome univoco.");
        }
        
        // 4. VALIDAZIONE: Impedisce la creazione di playlist vuote
        if (matchingTracks == null || matchingTracks.isEmpty()) {
            throw new it.unisa.sad.playlistmanager.application.exceptions.ValidationException("Nessuna traccia soddisfa i criteri scelti. Impossibile creare la playlist.");
        }

        // 5. INTEGRAZIONE UNDO: Incapsula nel comando e registra nella cronologia della sessione
        it.unisa.sad.playlistmanager.application.command.concreteCommands.CreateAutoPlaylistCommand command = 
                commandFactory.createCreateAutoPlaylistCommand(name.trim(), matchingTracks);
        
        undoManager.executeAndPush(command);
        
        return command.getCreatedPlaylist();
    }
}