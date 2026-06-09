package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeTrackRepository implements TrackRepository {
    public List<Track> simulatedCatalog = new ArrayList<>(List.of(
            new Track("t1", "Song One", "Artist One", 180, "Rock", 2020),
            new Track("t2", "Song Two", "Artist Two", 200, "Rock", 2021)
    ));
    public FakePlaylistRepository linkedPlaylistRepo;

    @Override
    public Optional<Track> findById(String id) {
        Optional<Track> dynamicTrack = simulatedCatalog.stream().filter(t -> t.getId().equals(id)).findFirst();
        if (dynamicTrack.isPresent()) return dynamicTrack;

        if ("t3-nuova".equals(id)) {
            return Optional.of(new Track(id, "Titolo", "Autore", 200, "Pop", 2022));
        }
        return Optional.empty();
    }

    @Override
    public void save(Track track) {
        this.simulatedCatalog.add(track);
    }

    @Override
    public List<Track> findAll() {
        return this.simulatedCatalog;
    }

    @Override
    public Optional<Track> deleteById(String id) {
        Optional<Track> trackOpt = findById(id);
        trackOpt.ifPresent(simulatedCatalog::remove);
        if (linkedPlaylistRepo != null) {
            linkedPlaylistRepo.removeTrackFromPlaylist("1", id);
        }
        return trackOpt;
    }

    @Override
    public Optional<Track> update(Track track) {
        for (int i = 0; i < simulatedCatalog.size(); i++) {
            if (simulatedCatalog.get(i).getId().equals(track.getId())) {
                simulatedCatalog.set(i, track);
                return Optional.of(track);
            }
        }
        return Optional.empty();
    }
}