package it.unisa.sad.playlistmanager.persistence.repository;
import java.util.List;
import java.util.Optional;

import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.domain.model.Track;

/**
 * Interfaccia per la gestione della persistenza dei tag visuali assegnabili alle tracce.
 * Fornisce l'astrazione necessaria per isolare il Domain/Application Layer
 * dalle tecnologie specifiche di storage (es. SQLite).
 * * @version 1.0
 */
public interface TagRepository {
    /**
     * Salva una nuovo tag nel sistema di persistenza.
     * @param tag L'oggetto Tag da salvare.
     */
    void save(Tag tag);

    /**
     * Trova un tag nel sistema di persistenza in base al suo id.
     * @param id da trovare
     * @return un Optional contenente il tag se presente, altrimenti Optional.empty()
     */
    Optional<Tag> findById(String id);

    /**
     * Trova tutti i tag nel sistema di persistenza.
     * @return una List di tag trovati. La lista può essere vuota.
     */
    List<Tag> findAll();

    /**
     * Elimina un tag nel sistema di persistenza in base al suo id.
     * @param id dell'oggetto Tag da eliminare
     * 
     */
    Optional<Tag> deleteById(String id);

    /**
     * Trova tutti i tag nel sistema di persistenza assegnati a una traccia.
     * @param trackId id della traccia
     * @return una List di tag trovati. La lista può essere vuota.
     */
    List<Tag> findByTrackId(String trackId);

    /**
     * Trova un tag nel sistema di persistenza in base al suo nome.
     * @param name nome del tag da trovare
     * @return un Optional contenente il tag se presente, altrimenti Optional.empty()
     */
    Optional<Tag> findByName(String name);
    /**
     * Verifica se un tag è assegnato a una traccia.
     * @param trackId id della traccia
     * @param tagId id del tag
     * @return true se il tag è assegnato alla traccia, false altrimenti
     */
    boolean isAttached(String trackId, String tagId);

    /**
     * Assegna un tag a una traccia.
     * @param trackId id della traccia
     * @param tagId id del tag
     */
    void attach(String trackId, String tagId);

    /**
     * Rimuove un tag da una traccia.
     * @param trackId id della traccia
     * @param tagId
     */
    void detach(String trackId, String tagId);

    /**
     * Trova tutte le tracce associate a un tag.
     * @param tagId id del tag
     * @return una List di tracce trovate. La lista può essere vuota.
     */
    List<Track> findTracksByTagId(String tagId);
    
    
}
