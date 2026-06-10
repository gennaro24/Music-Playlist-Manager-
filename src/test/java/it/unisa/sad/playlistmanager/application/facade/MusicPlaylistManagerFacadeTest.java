package it.unisa.sad.playlistmanager.application.facade;

import it.unisa.sad.playlistmanager.application.service.PlaybackService;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.PlaybackMode;
import it.unisa.sad.playlistmanager.domain.model.PlaybackSnapshot;
import it.unisa.sad.playlistmanager.domain.model.PlaybackState;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.FakePlaylistRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MusicPlaylistManagerFacadeTest {

    // ===================================================================================
    // FAKE SERVICES PER ISOLARE I TEST
    // ===================================================================================

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

    class FakePlaybackService extends PlaybackService {
        boolean isSetPlaybackModeCalled = false;
        PlaybackMode lastModeSet = null;

        public FakePlaybackService() {
            // Usiamo il FakePlaylistRepository creato in precedenza per soddisfare il costruttore
            super(new FakePlaylistRepository()); 
        }

        @Override
        public void setPlaybackMode(PlaybackMode mode) {
            this.isSetPlaybackModeCalled = true;
            this.lastModeSet = mode;
        }

        @Override
        public PlaybackSnapshot getSnapshot() {
            // Ritorna uno snapshot fittizio con l'ultima modalità impostata
            return new PlaybackSnapshot(PlaybackState.STOPPED, null, lastModeSet != null ? lastModeSet : PlaybackMode.SEQUENTIAL, 0);
        }
    }

    // ===================================================================================
    // TEST METODI FACADE
    // ===================================================================================

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

    /**
     * T-144: Verifica che la Facade inoltri correttamente il cambio modalità al PlaybackService
     * e restituisca uno snapshot aggiornato.
     */
    @Test
    void testSetPlaybackModeDelegaCorrettamente() {
        FakePlaybackService fakePlaybackService = new FakePlaybackService();
        
        // Inizializziamo la Facade passando il PlaybackService finto
        MusicPlaylistManagerFacade facade = new MusicPlaylistManagerFacade(null, null, fakePlaybackService);

        // Chiamiamo il nuovo metodo
        PlaybackSnapshot result = facade.setPlaybackMode(PlaybackMode.SHUFFLE);

        // Verifiche
        assertTrue(fakePlaybackService.isSetPlaybackModeCalled, "Il metodo setPlaybackMode del service deve essere invocato.");
        assertEquals(PlaybackMode.SHUFFLE, fakePlaybackService.lastModeSet, "La modalità passata deve essere SHUFFLE.");
        assertNotNull(result, "Deve restituire uno snapshot valido.");
    }
}