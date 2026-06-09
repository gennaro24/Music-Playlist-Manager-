package it.unisa.sad.playlistmanager.persistence.repository;

import it.unisa.sad.playlistmanager.domain.model.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FakeTrackRepository implements TrackRepository {
    public List<Track> simulatedTracks = new ArrayList<>();
    public FakePlaylistRepository linkedPlaylistRepo;

    @Override
    public Optional<Track> findById(String id) {
        Optional<Track> dynamicTrack = simulatedTracks.stream().filter(t -> t.getId().equals(id)).findFirst();
        if (dynamicTrack.isPresent()) return dynamicTrack;

        if ("t1".equals(id) || "t3-nuova".equals(id)) {
            return Optional.of(new Track(id, "Titolo", "Autore", 200, "Pop", 2022));
        }
        return Optional.empty();
    }

    @Override
    public void save(Track track) {
        this.simulatedTracks.add(track);
    }

    @Override
    public List<Track> findAll() {
        return Collections.emptyList();
    }

    @Override
    public Optional<Track> deleteById(String id) {
        Optional<Track> trackOpt = findById(id);
        trackOpt.ifPresent(simulatedTracks::remove);
        if (linkedPlaylistRepo != null) {
            linkedPlaylistRepo.removeTrackFromPlaylist("1", id);
        }
        return trackOpt;
    }

    @Override
    public Optional<Track> update(Track track) {
        for (int i = 0; i < simulatedTracks.size(); i++) {
            if (simulatedTracks.get(i).getId().equals(track.getId())) {
                simulatedTracks.set(i, track);
                return Optional.of(track);
            }
        }
        return Optional.empty();
    }
}