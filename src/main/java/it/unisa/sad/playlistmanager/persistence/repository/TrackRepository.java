package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Track;
import java.util.List;

/**
 * Interfaccia per la gestione della persistenza delle tracce musicali.
 * Fornisce l'astrazione necessaria per isolare il Domain/Application Layer
 * dalle tecnologie specifiche di storage (es. SQLite).
 * * @version 1.1
 */
public interface TrackRepository {
    
    /**
     * Salva una nuova traccia musicale nel sistema di persistenza.
     *
     * @param track La traccia di dominio da memorizzare.
     */
    void save(Track track);

    /**
     * Recupera tutte le tracce musicali memorizzate nel database.
     * Richiesto per l'adempimento dei casi d'uso di visualizzazione catalogo.
     *
     * @return Una {@link List} contenente tutte le tracce presenti, o una lista vuota se il catalogo è vuoto.
     */
    List<Track> findAll();
}