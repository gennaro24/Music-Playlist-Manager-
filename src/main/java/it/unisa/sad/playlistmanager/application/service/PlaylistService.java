package it.unisa.sad.playlistmanager.application.service;

import java.util.List;

import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;

/**
 * Servizio applicativo responsabile del coordinamento dei casi d'uso legati alle playlist.
 * * Funge da intermediario tra il Presentation Layer e lo strato di persistenza.
 * @version 1.1
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
}
