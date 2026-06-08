package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class TrackServiceTest {

    class FakeTrackRepository implements TrackRepository {
        boolean isSaveCalled = false;
        Track trackSavedInDb = null;
        List<Track> simulatedDatabase = new ArrayList<>();

        @Override
        public void save(Track track) {
            this.isSaveCalled = true;
            this.trackSavedInDb = track;
            this.simulatedDatabase.add(track);
        }

        @Override
        public Optional<Track> findById(String id){
            return Optional.empty();
        }

        @Override
        public List<Track> findAll() {
            return this.simulatedDatabase;
        }
        
        //TODO: Implementa casi di test per il metodo update
        @Override public Optional<Track> update(Track track){return Optional.empty();}
    }

    @Test
    void testAddTrackCreaESalvaCorrettamente() {
        FakeTrackRepository fakeRepo = new FakeTrackRepository();
        TrackService service = new TrackService(fakeRepo);

        Track result = service.addTrack("Bohemian Rhapsody", "Queen", 354, "Rock", 1975);

        assertNotNull(result);
        assertEquals("Bohemian Rhapsody", result.getTitle());
        assertEquals("Queen", result.getAuthor());
        assertTrue(fakeRepo.isSaveCalled);
        assertEquals(result, fakeRepo.trackSavedInDb);
    }

    @Test
    void testGetAllTracksRestituisceIlCatalogo() {
        FakeTrackRepository fakeRepo = new FakeTrackRepository();
        TrackService service = new TrackService(fakeRepo);

        Track track1 = new Track("1", "Song One", "Author", 100, "Pop", 2020);
        Track track2 = new Track("2", "Song Two", "Author", 200, "Rock", 2021);
        fakeRepo.simulatedDatabase.add(track1);
        fakeRepo.simulatedDatabase.add(track2);

        List<Track> result = service.getAllTracks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(track1));
        assertTrue(result.contains(track2));
    }
}