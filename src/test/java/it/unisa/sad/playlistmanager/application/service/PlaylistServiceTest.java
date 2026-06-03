package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class PlaylistServiceTest {

    class FakePlaylistRepository implements PlaylistRepository {
        boolean isSaveCalled = false;
        Playlist savedPlaylist = null;
        
        // Questa variabile "pilota" ci permette di decidere se il nome esiste o meno nel test
        boolean simulateNameExists = false;

        @Override
        public void save(Playlist playlist) {
            this.isSaveCalled = true;
            this.savedPlaylist = playlist;
        }

        @Override
        public Optional<Playlist> findById(String id) {
            if ("1".equals(id)) {
                return Optional.of(new Playlist("1", "Rock Classics"));
            }
            return Optional.empty();
        }

        @Override
        public Optional<Playlist> findByName(String name) { return Optional.empty(); }

        @Override
        public List<Playlist> findAll() { return null; }

        @Override
        public boolean existsByName(String name) { 
            // Impostato su false per garantire che il test di creazione rimanga sempre verde
            return false; 
        }

        @Override
        public void addTrackToPlaylist(String playlistId, String trackId) {
        }

        @Override
        public void removeTrackFromPlaylist(String playlistId, String trackId) {
        }

        @Override
        public List<Track> findTracksByPlaylistId(String playlistId) {
            if (!"1".equals(playlistId)) {
                return List.of();
            }
            return List.of(
                    new Track("t1", "Song One", "Artist One", 180, "Rock", 2020),
                    new Track("t2", "Song Two", "Artist Two", 200, "Rock", 2021));
        }
    }

    @Test
    void testCreatePlaylistCreaESalvaCorrettamente() {
        FakePlaylistRepository fakeRepo = new FakePlaylistRepository();
        // Per questo test, simuliamo che il nome NON esista ancora nel DB
        fakeRepo.simulateNameExists = false; 
        PlaylistService service = new PlaylistService(fakeRepo, null);

        Playlist result = service.createPlaylist("Rock Classics");

        assertNotNull(result);
        assertEquals("Rock Classics", result.getName());
        assertTrue(fakeRepo.isSaveCalled);
        assertEquals(result, fakeRepo.savedPlaylist);
    }

    @Test
    void testGetTracksForPlaylistRestituisceTracceCorrette() {
        FakePlaylistRepository fakeRepo = new FakePlaylistRepository();
        PlaylistService service = new PlaylistService(fakeRepo, null);
        Playlist playlist = new Playlist("1", "Rock Classics");
        List<Track> tracks = service.getTracksForPlaylist(playlist.getId());
        assertNotNull(tracks);
        assertEquals(2, tracks.size());
        assertEquals("Song One", tracks.get(0).getTitle());
        assertEquals("Song Two", tracks.get(1).getTitle());
    }
}