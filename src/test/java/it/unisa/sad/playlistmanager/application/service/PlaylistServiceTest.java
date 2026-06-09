package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.FakePlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.FakeTrackRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlaylistServiceTest {

    @Test
    void testCreatePlaylistCreaESalvaCorrettamente() {
        FakePlaylistRepository fakeRepo = new FakePlaylistRepository();
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

    @Test
    void testAddTrackToPlaylist_AggiuntaValida() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertDoesNotThrow(() -> service.addTrackToPlaylist("1", "t3-nuova"));
        assertTrue(fakePlaylistRepo.isAddTrackCalled);
    }

    @Test
    void testAddTrackToPlaylist_PlaylistInesistente() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertThrows(PlaylistNotFoundException.class, () -> service.addTrackToPlaylist("999", "t1"));
    }

    @Test
    void testAddTrackToPlaylist_Duplicato() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertThrows(ValidationException.class, () -> service.addTrackToPlaylist("1", "t1"));
    }

    @Test
    void testRemoveTrackFromPlaylist_RimozioneValida() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertDoesNotThrow(() -> service.removeTrackFromPlaylist("1", "t1"));
        assertTrue(fakePlaylistRepo.isRemoveTrackCalled);
    }

    @Test
    void testRemoveTrackFromPlaylist_PlaylistInesistente() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertThrows(PlaylistNotFoundException.class, () -> service.removeTrackFromPlaylist("999", "t1"));
    }

    @Test
    void testRemoveTrackFromPlaylist_TracciaInesistenteNelCatalogo() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertThrows(TrackNotFoundException.class, () -> service.removeTrackFromPlaylist("1", "t999"));
    }

    @Test
    void testT82_TracciaInPlaylistMostraMetadatiAggiornatiSenzaDuplicati() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService playlistService = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);
        TrackService trackService = new TrackService(fakeTrackRepo);

        Track track = new Track("t1", "Song One", "Artist One", 180, "Rock", 2020);
        fakeTrackRepo.save(track);

        List<Track> initialTracks = playlistService.getTracksForPlaylist("1");
        assertEquals(2, initialTracks.size());

        Track updatedData = new Track("t1", "Song One Updated", "Artist One", 180, "Rock", 2020);
        trackService.updateTrack("t1", updatedData);

        for (int i = 0; i < fakePlaylistRepo.tracksInPlaylist1.size(); i++) {
            if (fakePlaylistRepo.tracksInPlaylist1.get(i).getId().equals("t1")) {
                fakePlaylistRepo.tracksInPlaylist1.set(i, updatedData);
            }
        }

        List<Track> updatedTracks = playlistService.getTracksForPlaylist("1");
        assertEquals(2, updatedTracks.size());
        assertEquals("Song One Updated", updatedTracks.get(0).getTitle());
    }

    @Test
    void testT92_EliminazioneRimuoveRiferimentiDaPlaylistTracks() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        
        fakeTrackRepo.linkedPlaylistRepo = fakePlaylistRepo;
        
        PlaylistService playlistService = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);
        TrackService trackService = new TrackService(fakeTrackRepo);

        List<Track> initialTracks = playlistService.getTracksForPlaylist("1");
        assertEquals(2, initialTracks.size());
        assertEquals("t1", initialTracks.get(0).getId());

        trackService.deleteTrack("t1");

        List<Track> updatedTracks = playlistService.getTracksForPlaylist("1");
        assertEquals(1, updatedTracks.size());
        assertNotEquals("t1", updatedTracks.get(0).getId());
    }
}