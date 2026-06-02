package it.unisa.sad.playlistmanager.application.service;

import java.util.List;

import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;

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
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist non trovata con l'ID specificato."));
                
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Traccia non trovata nel catalogo con l'ID microfilmato."));

        // Esegue la business rule di dominio (inclusa la verifica dei duplicati T-30)
        playlist.addTrack(track);

        // Invoca la persistenza. Anche se il metodo concreto non è ancora scritto dal collega, 
        // l'architettura compila correttamente e rispetta il DIP.
        playlistRepository.save(playlist);
    }
}
