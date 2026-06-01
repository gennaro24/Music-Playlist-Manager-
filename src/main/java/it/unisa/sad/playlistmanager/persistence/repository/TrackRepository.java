package it.unisa.sad.playlistmanager.persistence.repository;

import java.util.List;
import java.util.Optional;

import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Interfaccia contrattuale per le operazioni di persistenza della Track (DIP).
 */
public interface TrackRepository {
    /**
     * Salva una nuova track nel sistema di persistenza.
     * @param track L'oggetto Track da salvare.
     */
    void save(Track track);
    /**
     * Trova una track nel sistema di persistenza in base al suo id.
     * @param id da trovare
     * @return un Optional contenente la traccia se presente, altrimenti Optional.empty()
     */
    Optional<Track> findById(String id);
    /**
     * Trova tutte le track nel sistema di persistenza.
     * 
     * @return una List di track trovate. La lista può essere vuota.
     */
    List<Track> findAll();
    
}
