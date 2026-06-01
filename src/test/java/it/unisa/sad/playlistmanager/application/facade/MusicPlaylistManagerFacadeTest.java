package it.unisa.sad.playlistmanager.application.facade;

import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.domain.model.Track;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MusicPlaylistManagerFacadeTest {

    /**
     * 1. CREAZIONE DEL FAKE OBJECT
     */
    class FakeTrackService extends TrackService {
        
        public FakeTrackService() {
            super(null); 
        }

        boolean isCalled = false;
        String passedTitle;
        String passedAuthor;
        int passedDuration;
        String passedGenre;
        int passedYear;

        @Override
        public Track addTrack(String title, String author, int duration, String genre, int year) {
            this.isCalled = true;
            this.passedTitle = title;
            this.passedAuthor = author;
            this.passedDuration = duration;
            this.passedGenre = genre;
            this.passedYear = year;
            
            return new Track("test-id", title, author, duration, genre, year);
        }
    }

    /**
     * 2. IL TEST VERO E PROPRIO
     */
    @Test
    void testAddTrackDelegaCorrettamente() {
        // Prepariamo la controfigura del TrackService
        FakeTrackService fakeService = new FakeTrackService();
        
        // CORREZIONE: Passiamo null come secondo parametro (PlaylistService) 
        // perché in questo test specifico non viene utilizzato
        MusicPlaylistManagerFacade facade = new MusicPlaylistManagerFacade(fakeService, null);

        // Chiamiamo la Facade con dei dati di prova
        facade.addTrack("Shape of You", "Ed Sheeran", 233, "Pop", 2017);

        // Verifichiamo che la Facade abbia effettivamente girato i dati alla nostra controfigura
        assertTrue(fakeService.isCalled, "Errore: La Facade non ha chiamato il TrackService!");
        assertEquals("Shape of You", fakeService.passedTitle);
        assertEquals("Ed Sheeran", fakeService.passedAuthor);
        assertEquals(233, fakeService.passedDuration);
        assertEquals("Pop", fakeService.passedGenre);
        assertEquals(2017, fakeService.passedYear);
    }
}