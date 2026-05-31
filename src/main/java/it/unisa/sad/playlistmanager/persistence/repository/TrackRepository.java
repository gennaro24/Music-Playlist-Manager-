package it.unisa.sad.playlistmanager.persistence.repository;
import it.unisa.sad.playlistmanager.domain.model.Track;
public interface TrackRepository {
    void save(Track track);
}
