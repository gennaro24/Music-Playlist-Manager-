package it.unisa.sad.playlistmanager.application.service;

import java.util.List;

import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Servizio applicativo responsabile del coordinamento dei casi d'uso legati alle playlist.
 * * Funge da intermediario tra il Presentation Layer e lo strato di persistenza.
 * @version 1.1
 */
public class PlaylistService {
    
    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;

    /**
     * Costruttore con Dependency Injection delle astrazioni di persistenza.
     *
     * @param playlistRepository L'astrazione della persistenza delle playlist.
     * @param trackRepository    L'astrazione della persistenza delle tracce.
     */
    public PlaylistService(PlaylistRepository playlistRepository, TrackRepository trackRepository) {
        this.playlistRepository = playlistRepository;
        this.trackRepository = trackRepository;
    }

    /**
     * Coordina la creazione e il salvataggio di una nuova playlist vuota.
     * * @param name Il nome della playlist da creare.
     * @return L'oggetto {@link Playlist} registrato nel sistema.
     */
    public Playlist createPlaylist(String name) {

        Playlist newPlaylist = new Playlist(null, name);
        playlistRepository.save(newPlaylist);
        return newPlaylist;
        
    }

    /**
     * Coordina il recupero di tutte le playlist memorizzate nel sistema.     *
     * @return La lista completa di tutte le playlist salvate. La lista può essere vuota.
     */
    public List<Playlist> getAllPlaylists() {
        // Richiama il metodo findAll() definito nel repository reale
        return this.playlistRepository.findAll();
    }

    /**
     * Coordina la ricerca di una singola playlist a partire dal suo identificativo univoco.
     * Gestisce l'astrazione dell'Optional restituito dal livello di persistenza.
     *
     * @param id L'identificativo unico della playlist da cercare.
     * @return L'istanza di {@link Playlist} individuata, oppure null se non è presente nel database.
     */
    public Playlist getPlaylistById(String id) {
        // Estrae l'oggetto dall'Optional; restituisce null se l'Optional è vuoto (.orElse(null))
        return this.playlistRepository.findById(id).orElse(null);
    }

    /**
     * Coordina il caso d'uso di aggiunta di una traccia esistente a una playlist (Task T-32).
     *
     * @param playlistId L'identificativo della playlist.
     * @param trackId    L'identificativo della traccia da recuperare dal catalogo.
     * @throws IllegalArgumentException Se la playlist o la traccia non esistono, o se viola le regole di dominio.
     */
    public void addTrackToPlaylist(String playlistId, String trackId) {
        playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist non trovata con l'ID specificato."));

        trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Traccia non trovata nel catalogo con l'ID specificato."));

        // Persistenza sulla tabella ponte playlist_tracks.
        playlistRepository.addTrackToPlaylist(playlistId, trackId);
    }

    /**
     * Coordina il caso d'uso di rimozione di una traccia da una specifica playlist.
     * Recupera l'entità dal repository, ne modifica lo stato interno tramite il modello di dominio
     * e infine persiste l'aggiornamento.
     *
     * @param playlistId L'identificativo unico della playlist.
     * @param trackId    L'identificativo unico della traccia da estromettere.
     * @throws IllegalArgumentException Se la playlist non esiste o se la traccia non era presente.
     */
    public void removeTrackFromPlaylist(String playlistId, String trackId) {
        // Recupero sicuro tramite l'Optional esposto dal repository reale
        playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist non trovata con l'ID specificato."));

        trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Traccia non trovata nel catalogo con l'ID specificato."));

        // Rimozione fisica sulla tabella ponte playlist_tracks.
        playlistRepository.removeTrackFromPlaylist(playlistId, trackId);
    }

    
    /**
     * 
     * @param playlistId
     * @return
     */
    public List<Track> getTracksForPlaylist(String playlistId) {
        playlistRepository.findById(playlistId)
            .orElseThrow(() -> new IllegalArgumentException("Playlist non trovata con l'ID specificato."));
        return playlistRepository.findTracksByPlaylistId(playlistId);
    }
}
