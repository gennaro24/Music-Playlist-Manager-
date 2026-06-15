package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Track;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Repository di tracce in memoria per i test del layer Command.
 *
 * Implementazione completa e fedele del contratto {@link TrackRepository}:
 * a differenza dei fake "leggeri", supporta davvero salvataggio, update e
 * cancellazione. Sulla delete propaga la rimozione delle associazioni alla
 * {@link InMemoryPlaylistRepository} collegata, riproducendo il comportamento
 * a cascata atteso dal layer di persistenza reale (necessario per testare
 * l'undo di {@code DeleteTrackCommand}).
 */
public class InMemoryTrackRepository implements TrackRepository {

    private final Map<String, Track> store = new LinkedHashMap<>();
    private InMemoryPlaylistRepository playlistRepository;

    /**
     * Hook di test: se impostato, viene invocato durante
     * {@link #restoreWithPlaylistPositions} dopo l'inserimento della track e
     * delle associazioni gia' processate. Se restituisce un'eccezione non
     * nulla, questa viene lanciata simulando un fallimento a meta' transazione
     * (es. un vincolo FK violato in SQLite), per testare il rollback e il
     * comportamento di retry di {@code DeleteTrackCommand}.
     */
    private Supplier<RuntimeException> restoreFailure;

    /** Collega il repository delle playlist per propagare la delete a cascata. */
    public void linkPlaylistRepository(InMemoryPlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    /**
     * Imposta un fallimento simulato per il prossimo (e ogni successivo)
     * {@link #restoreWithPlaylistPositions}. Passare {@code null} per
     * disattivare e tornare al comportamento normale (utile per testare un
     * retry riuscito dopo un primo fallimento).
     */
    public void setRestoreFailure(Supplier<RuntimeException> restoreFailure) {
        this.restoreFailure = restoreFailure;
    }

    @Override
    public Optional<Track> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(Track track) {
        store.put(track.getId(), track);
    }

    @Override
    public List<Track> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Track> deleteById(String id) {
        Track removed = store.remove(id);
        if (removed != null && playlistRepository != null) {
            playlistRepository.removeTrackFromAllPlaylists(id);
        }
        return Optional.ofNullable(removed);
    }

    @Override
    public Optional<Track> update(Track track) {
        if (!store.containsKey(track.getId())) {
            return Optional.empty();
        }
        store.put(track.getId(), track);
        return Optional.of(track);
    }

    /**
     * Simula il ripristino atomico usato dalla persistenza SQLite.
     * Se un'associazione fallisce (o se e' stato impostato un fallimento
     * simulato tramite {@link #setRestoreFailure}), elimina quelle gia'
     * reinserite e rimuove la traccia, riportando lo storage allo stato
     * precedente al tentativo.
     */
    @Override
    public void restoreWithPlaylistPositions(
            Track track,
            Map<String, Integer> playlistPositions) {
        List<String> restoredPlaylistIds = new ArrayList<>();
        store.put(track.getId(), track);

        try {
            if (playlistRepository != null) {
                for (Map.Entry<String, Integer> entry : playlistPositions.entrySet()) {
                    playlistRepository.addTrackToPlaylistAtPosition(
                            entry.getKey(),
                            track.getId(),
                            entry.getValue());
                    restoredPlaylistIds.add(entry.getKey());
                }
            }

            if (restoreFailure != null) {
                RuntimeException simulated = restoreFailure.get();
                if (simulated != null) {
                    throw simulated;
                }
            }
        } catch (RuntimeException exception) {
            if (playlistRepository != null) {
                for (String playlistId : restoredPlaylistIds) {
                    playlistRepository.removeTrackFromPlaylist(playlistId, track.getId());
                }
            }
            store.remove(track.getId());
            throw exception;
        }
    }
}