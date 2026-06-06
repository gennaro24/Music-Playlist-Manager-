package it.unisa.sad.playlistmanager.application.facade;

import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Track;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MusicPlaylistManagerFacadeTest {

    class FakeTrackService extends TrackService {
        
        public FakeTrackService() {
            super(null); 
        }

        boolean isAddTrackCalled = false;
        boolean isGetAllTracksCalled = false;
        
        List<Track> dummyCatalog = Arrays.asList(
            new Track("1", "Song One", "Artist", 100, "Pop", 2020),
            new Track("2", "Song Two", "Artist", 200, "Rock", 2021)
        );

        @Override
        public Track addTrack(String title, String author, int duration, String genre, int year) {
            this.isAddTrackCalled = true;
            return new Track("test-id", title, author, duration, genre, year);
        }

        @Override
        public List<Track> getAllTracks() {
            this.isGetAllTracksCalled = true;
            return dummyCatalog;
        }
    }

    @Test
    void testAddTrackDelegaCorrettamente() {
        FakeTrackService fakeService = new FakeTrackService();
        
        MusicPlaylistManagerFacade facade = new MusicPlaylistManagerFacade(fakeService, null, null);

        facade.addTrack("Shape of You", "Ed Sheeran", 233, "Pop", 2017);

        assertTrue(fakeService.isAddTrackCalled);
    }

    @Test
    void testGetAllTracksDelegaCorrettamente() {
        FakeTrackService fakeService = new FakeTrackService();
        
        MusicPlaylistManagerFacade facade = new MusicPlaylistManagerFacade(fakeService, null, null);

        List<Track> result = facade.getAllTracks();

        assertTrue(fakeService.isGetAllTracksCalled);
        assertEquals(fakeService.dummyCatalog, result);
    }
}