package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Track;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /** Collega il repository delle playlist per propagare la delete a cascata. */
    public void linkPlaylistRepository(InMemoryPlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
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
}