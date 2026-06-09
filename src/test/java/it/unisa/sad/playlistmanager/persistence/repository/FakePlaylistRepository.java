package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FakePlaylistRepository implements PlaylistRepository {
    public boolean isSaveCalled = false;
    public boolean isAddTrackCalled = false;
    public boolean isRemoveTrackCalled = false;
    public Playlist savedPlaylist = null;

    public List<Playlist> simulatedPlaylists = new ArrayList<>(List.of(
            new Playlist("1", "Rock Classics"),
            new Playlist("2", "Empty Playlist")
    ));

    public List<Track> tracksInPlaylist1 = new ArrayList<>(List.of(
            new Track("t1", "Song One", "Artist One", 180, "Rock", 2020),
            new Track("t2", "Song Two", "Artist Two", 200, "Rock", 2021)
    ));
    public List<Track> tracksInPlaylist2 = new ArrayList<>();

    @Override
    public void save(Playlist playlist) {
        this.isSaveCalled = true;
        this.savedPlaylist = playlist;
        this.simulatedPlaylists.add(playlist);
    }

    @Override
    public Optional<Playlist> findById(String id) {
        return this.simulatedPlaylists.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Playlist> findByName(String name) { return Optional.empty(); }

    @Override
    public List<Playlist> findAll() { return this.simulatedPlaylists; }

    @Override
    public boolean existsByName(String name) { return false; }

    @Override
    public void addTrackToPlaylist(String playlistId, String trackId) {
        if ("1".equals(playlistId) && "t1".equals(trackId)) {
            throw new IllegalArgumentException("Errore DB: Traccia già presente nella playlist.");
        }
        this.isAddTrackCalled = true;
    }

    @Override
    public void removeTrackFromPlaylist(String playlistId, String trackId) {
        this.isRemoveTrackCalled = true;
        this.tracksInPlaylist1.removeIf(t -> t.getId().equals(trackId));
    }

    @Override
    public List<Track> findTracksByPlaylistId(String playlistId) {
        if ("1".equals(playlistId)) return this.tracksInPlaylist1;
        if ("2".equals(playlistId)) return this.tracksInPlaylist2;
        return Collections.emptyList();
    }

    @Override
    public Optional<Playlist> deleteById(String playlistId) {
        Optional<Playlist> playlistOpt = findById(playlistId);
        if (playlistOpt.isPresent()) {
            this.simulatedPlaylists.remove(playlistOpt.get());
            if ("1".equals(playlistId)) {
                this.tracksInPlaylist1.clear();
            }
            return playlistOpt;
        }
        return Optional.empty();
    }
}