package it.unisa.sad.playlistmanager.application.service;

import java.util.List;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
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
     * @param name Il nome della playlist da creare.
     * @return L'oggetto {@link Playlist} registrato nel sistema.
     * @throws ValidationException se il nome della Playlist è nullo o vuoto.
     */
    public Playlist createPlaylist(String name) {
        if (null == name) throw new ValidationException("Il nome della playlist non può essere nullo.");
        if (name.trim().isEmpty()) throw new ValidationException("Il nome della playlist non può essere vuoto.");
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
     * @return L'istanza di {@link Playlist} individuata.
     * @throws ValidationException se l'id da cercare è nullo.
     * @throws PlaylistNotFoundException se la playlist non viene trovata. (Optional non vuoto)
     */
    public Playlist getPlaylistById(String id) {
        if (null == id){
            throw new ValidationException("L'ID della Playlist da cercare è nullo");
        }
        // Estrae l'oggetto dall'Optional; restituisce null se l'Optional è vuoto (.orElse(null))
        return this.playlistRepository.findById(id).orElseThrow(() -> new PlaylistNotFoundException("Playlist non trovata."));
    }

    /**
     * Coordina il caso d'uso di aggiunta di una traccia esistente a una playlist .
     *
     * @param playlistId L'identificativo della playlist.
     * @param trackId    L'identificativo della traccia da recuperare dal catalogo.
     * @throws PlaylistNotFoundException se la Playlist non viene trovata. (Optional non vuoto)
     * @throws TrackNotFoundException se la Track non viene trovata. (Optional non vuoto)
     * @throws ValidationException nel caso in cui l'id della track o della playlist sia nullo.
     */
    public void addTrackToPlaylist(String playlistId, String trackId) {

        if (null == trackId) throw new ValidationException("L'id della Track da aggiungere è nullo.");
        if (null == playlistId) throw new ValidationException("L'id della Playlist in cui aggiungere la Track è nullo.");
        playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist non trovata con l'ID specificato."));

        trackRepository.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException("Traccia non trovata nel catalogo con l'ID specificato."));

        boolean alreadyInPlaylist = playlistRepository.findTracksByPlaylistId(playlistId)
                .stream()
                .anyMatch(track -> track.getId().equals(trackId));
        if (alreadyInPlaylist) {
            throw new IllegalArgumentException("La traccia è già presente nella playlist.");
        }

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
     * @throws PlaylistNotFoundException se la Playlist non viene trovata. (Optional non vuoto)
     * @throws TrackNotFoundException se la Track non viene trovata. (Optional non vuoto)
     * @throws ValidationException nel caso in cui l'id della track o della playlist sia nullo.
     */
    public void removeTrackFromPlaylist(String playlistId, String trackId) {

        if (null == trackId) throw new ValidationException("L'id della Track da rimuovere è nullo.");
        if (null == playlistId) throw new ValidationException("L'id della Playlist in cui rimuovere la Track è nullo.");
        // Recupero sicuro tramite l'Optional esposto dal repository reale
        playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist non trovata con l'ID specificato."));

        trackRepository.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException("Traccia non trovata nel catalogo con l'ID specificato."));

        // Rimozione fisica sulla tabella ponte playlist_tracks.
        playlistRepository.removeTrackFromPlaylist(playlistId, trackId);
    }

    
    /**
     * Ritorna le Track presenti in una Playlist.
     * @throws PlaylistNotFoundException se la playlist non viene trovata.
     * @param playlistId
     * @return
     */
    public List<Track> getTracksForPlaylist(String playlistId) {
        if (null == playlistId) throw new ValidationException("L'id della Track da rimuovere è nullo.");
        playlistRepository.findById(playlistId)
            .orElseThrow(() -> new PlaylistNotFoundException("Playlist non trovata con l'ID specificato."));
        return playlistRepository.findTracksByPlaylistId(playlistId);
    }
}
