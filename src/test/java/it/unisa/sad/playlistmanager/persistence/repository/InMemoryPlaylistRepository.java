package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository di playlist in memoria per i test del layer Command.
 *
 * Implementazione completa del contratto {@link PlaylistRepository}: mantiene
 * per ogni playlist l'elenco ordinato degli id traccia, gestendo correttamente
 * inserimento in coda, inserimento in posizione (1-based) e calcolo della
 * posizione. Risolve gli id in {@link Track} tramite il {@link TrackRepository}
 * collegato, così che la delete a cascata di una traccia si rifletta anche qui.
 */
public class InMemoryPlaylistRepository implements PlaylistRepository {

    private final Map<String, Playlist> playlists = new LinkedHashMap<>();
    private final Map<String, List<String>> trackIdsByPlaylist = new LinkedHashMap<>();
    private final TrackRepository trackRepository;

    public InMemoryPlaylistRepository(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    @Override
    public void save(Playlist playlist) {
        playlists.put(playlist.getId(), playlist);
        trackIdsByPlaylist.putIfAbsent(playlist.getId(), new ArrayList<>());
    }

    @Override
    public void saveWithTracks(Playlist playlist, List<Track> tracks) {
        save(playlist);
        List<String> ids = trackIdsByPlaylist.get(playlist.getId());
        ids.clear();
        for (Track track : tracks) {
            ids.add(track.getId());
        }
    }

    @Override
    public Optional<Playlist> findById(String id) {
        return Optional.ofNullable(playlists.get(id));
    }

    @Override
    public List<Playlist> findAll() {
        return new ArrayList<>(playlists.values());
    }

    @Override
    public boolean existsByName(String name) {
        return playlists.values().stream().anyMatch(p -> p.getName().equals(name));
    }

    @Override
    public void addTrackToPlaylist(String playlistId, String trackId) {
        trackIdsByPlaylist.computeIfAbsent(playlistId, k -> new ArrayList<>()).add(trackId);
    }

    @Override
    public void addTrackToPlaylistAtPosition(String playlistId, String trackId, int position) {
        List<String> ids = trackIdsByPlaylist.computeIfAbsent(playlistId, k -> new ArrayList<>());
        int index = Math.max(0, Math.min(position - 1, ids.size()));
        ids.add(index, trackId);
    }

    @Override
    public void removeTrackFromPlaylist(String playlistId, String trackId) {
        List<String> ids = trackIdsByPlaylist.get(playlistId);
        if (ids != null) {
            ids.remove(trackId);
        }
    }

    @Override
    public List<Track> findTracksByPlaylistId(String playlistId) {
        List<String> ids = trackIdsByPlaylist.getOrDefault(playlistId, List.of());
        List<Track> result = new ArrayList<>();
        for (String id : ids) {
            trackRepository.findById(id).ifPresent(result::add);
        }
        return result;
    }

    @Override
    public Optional<Integer> getTrackPosition(String playlistId, String trackId) {
        List<String> ids = trackIdsByPlaylist.get(playlistId);
        if (ids == null) {
            return Optional.empty();
        }
        int index = ids.indexOf(trackId);
        return index < 0 ? Optional.empty() : Optional.of(index + 1);
    }

    @Override
    public Optional<Playlist> deleteById(String playlistId) {
        Playlist removed = playlists.remove(playlistId);
        trackIdsByPlaylist.remove(playlistId);
        return Optional.ofNullable(removed);
    }

    /** Rimuove la traccia da tutte le playlist (chiamato dalla delete a cascata). */
    void removeTrackFromAllPlaylists(String trackId) {
        for (List<String> ids : trackIdsByPlaylist.values()) {
            ids.remove(trackId);
        }
    }
}
