package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;

/**
 * Servizio applicativo responsabile del coordinamento dei casi d'uso legati alle playlist.
 * @version 1.0
 */
/**
 * TODO: Necessario un cambiamento di playlistRepository. Deve essere concretizzato da SqlitePlaylistRepository
 */
public class PlaylistService {
    
    private final PlaylistRepository playlistRepository;

    /**
     * Costruttore con Dependency Injection.
     * * @param playlistRepository L'astrazione della persistenza.
     */
    public PlaylistService(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
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
}
