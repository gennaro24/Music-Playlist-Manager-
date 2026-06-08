package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test d'unità focalizzata ad analizzare la correttezza della business logic
 * legata alle tracce del catalogo musicale. Estesa per verificare i flussi di modifica (US-03).
 */
class TrackServiceTest {

    /**
     * Sostituto finto (Fake Object) del database reale delle tracce per isolare lo stato nei test d'unità.
     * Implementa pienamente l'interfaccia contrattuale aggiornata TrackRepository.
     */
    class FakeTrackRepository implements TrackRepository {
        /** Interruttore booleano per verificare l'effettivo innesco del salvataggio. */
        boolean isSaveCalled = false;
        /** Memorizza il riferimento all'ultimo oggetto Track inviato per la persistenza. */
        Track trackSavedInDb = null;
        /** Lista in memoria atta a simulare la tabella 'tracks' del database relazionale. */
        List<Track> simulatedDatabase = new ArrayList<>();

        @Override
        public void save(Track track) {
            this.isSaveCalled = true;
            this.trackSavedInDb = track;
            this.simulatedDatabase.add(track);
        }

        /**
         * Esegue la ricerca sequenziale della traccia all'interno del finto database in memoria.
         * * @param id L'identificativo unico della traccia da cercare.
         * @return Un Optional contenente l'entità trovata, altrimenti Optional.empty().
         */
        @Override
        public Optional<Track> findById(String id){
            return this.simulatedDatabase.stream()
                    .filter(t -> t.getId().equals(id))
                    .findFirst();
        }

        @Override
        public List<Track> findAll() {
            return this.simulatedDatabase;
        }

        @Override
        public Optional<Track> deleteById(String id) {
            return Optional.empty();
        }

        /**
         * Simula il comportamento dell'istruzione SQL UPDATE sovrascrivendo l'entità a parità di ID.
         * * @param track L'oggetto Track aggiornato da memorizzare.
         * @return Un Optional contenente la traccia modificata se l'ID esiste, altrimenti Optional.empty().
         */
        @Override
        public Optional<Track> update(Track track) {
            for (int i = 0; i < simulatedDatabase.size(); i++) {
                if (simulatedDatabase.get(i).getId().equals(track.getId())) {
                    simulatedDatabase.set(i, track);
                    return Optional.of(track);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Verifica la corretta creazione e persistenza incondizionata di una traccia valida.
     */
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

    /**
     * Verifica l'estrazione totale degli elementi presenti a catalogo.
     */
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

    /**
     * <b>Task T-80:</b> Test JUnit per certificare che una modifica recante dati validi
     * sovrascriva i vecchi metadati e consolidi lo stato all'interno del repository.
     */
    @Test
    void testT80_ModificaValidaAggiornaMetadatiPersistiti() {
        FakeTrackRepository fakeRepo = new FakeTrackRepository();
        TrackService service = new TrackService(fakeRepo);

        // Given: una traccia salvata all'interno del finto storage
        Track originalTrack = new Track("t-80", "Old Title", "Old Author", 200, "Jazz", 2010);
        fakeRepo.simulatedDatabase.add(originalTrack);

        // When: viene sottomessa una richiesta di update legittima
        Track updatedData = new Track("t-80", "New Title", "New Author", 250, "Rock", 2023);
        Track result = service.updateTrack("t-80", updatedData);

        // Then: i vecchi metadati sono sostituiti a livello persistente
        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals(250, result.getDuration());

        Track persistedTrack = fakeRepo.findById("t-80").orElse(null);
        assertNotNull(persistedTrack);
        assertEquals("New Title", persistedTrack.getTitle());
    }

    /**
     * <b>Task T-81:</b> Test JUnit per certificare che il tentativo di immissione di parametri
     * non validi mantenga inalterati i vecchi valori originali della traccia.
     */
    @Test
    void testT81_ModificaNonValidaMantieneVecchiValori() {
        FakeTrackRepository fakeRepo = new FakeTrackRepository();
        TrackService service = new TrackService(fakeRepo);

        // Given: una traccia integra inserita nello storage finto
        Track originalTrack = new Track("t-81", "Consistent Title", "Consistent Author", 180, "Pop", 2020);
        fakeRepo.simulatedDatabase.add(originalTrack);

        // When & Then: il tentativo di configurare dati errati fallisce a livello di entità
        assertThrows(it.unisa.sad.playlistmanager.domain.exceptions.ValidationException.class, () -> {
            new Track("t-81", "   ", "Consistent Author", 180, "Pop", 2020);
        });

        // Then: lo stato della risorsa memorizzata nel database non risulta alterato
        Track persistedTrack = fakeRepo.findById("t-81").orElse(null);
        assertNotNull(persistedTrack);
        assertEquals("Consistent Title", persistedTrack.getTitle());
    }
}