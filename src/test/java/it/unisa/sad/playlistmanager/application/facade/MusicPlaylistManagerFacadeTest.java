package it.unisa.sad.playlistmanager.application.facade;

import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Track;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MusicPlaylistManagerFacadeTest {

    /**
     * 1. CREAZIONE DEL FAKE OBJECT
     */
    class FakeTrackService extends TrackService {
        
        public FakeTrackService() {
            super(null); 
        }

        // Variabili "spia"
        boolean isAddTrackCalled = false;
        boolean isGetAllTracksCalled = false;
        
        // Creiamo un finto catalogo da far restituire al metodo getAllTracks
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
            // Registriamo che il metodo è stato chiamato e restituiamo la lista finta
            this.isGetAllTracksCalled = true;
            return dummyCatalog;
        }
    }

    /**
     * 2. TEST PER L'AGGIUNTA DELLA TRACCIA
     */
    @Test
    void testAddTrackDelegaCorrettamente() {
        FakeTrackService fakeService = new FakeTrackService();
        // NOTA: uso il costruttore a 1 parametro come nel tuo ultimo codice
        MusicPlaylistManagerFacade facade = new MusicPlaylistManagerFacade(fakeService);

        facade.addTrack("Shape of You", "Ed Sheeran", 233, "Pop", 2017);

        assertTrue(fakeService.isAddTrackCalled, "Errore: La Facade non ha chiamato addTrack() del TrackService!");
    }

    /**
     * 3. NUOVO TEST PER IL RECUPERO DEL CATALOGO (Task T-11)
     */
    @Test
    void testGetAllTracksDelegaCorrettamente() {
        FakeTrackService fakeService = new FakeTrackService();
        MusicPlaylistManagerFacade facade = new MusicPlaylistManagerFacade(fakeService);

        // Chiamiamo il nuovo metodo della Facade
        List<Track> result = facade.getAllTracks();

        // Verifichiamo che abbia delegato correttamente la chiamata al servizio
        assertTrue(fakeService.isGetAllTracksCalled, "Errore: La Facade non ha chiamato getAllTracks() del TrackService!");
        
        // Verifichiamo che ci abbia restituito esattamente la lista generata dal servizio
        assertEquals(fakeService.dummyCatalog, result, "Errore: La lista restituita non è quella del TrackService!");
    }
}