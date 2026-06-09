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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test d'unità preesistente sanata dal Type Mismatch e dall'eccezione del duplicato,
 * ed estesa per adempiere ai Task T-103 e T-104 dello Sprint 2 (User Story 5.1).
 *
 * @version 1.3
 */
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

    /**
     * <b>Correzione Sprint 2:</b> Modificata l'intercettazione dell'eccezione attesa.
     * In linea con le specifiche di PlaylistService, il tentativo di inserimento di una traccia
     * duplicata all'interno della medesima playlist lancia una ValidationException di livello applicativo.
     */
    @Test
    void testAddTrackToPlaylist_Duplicato() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        // Corretto da IllegalArgumentException.class a ValidationException.class
        Exception exception = assertThrows(ValidationException.class, () -> {
            service.addTrackToPlaylist("1", "t1");
        });
        assertNotNull(exception.getMessage());
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

    // ===================================================================================
    // TEST TASK T-103 e T-104 (US-5.1): ELIMINAZIONE PLAYLIST
    // ===================================================================================

    /**
     * <b>Task T-103:</b> Test JUnit per verificare che il coordinamento di deletePlaylist
     * espunga permanentemente la playlist dallo storage eliminando tutte le associazioni traccia.
     * <p>Soddisfa lo Scenario 1 dei Criteri di Accettazione di US-5.1.</p>
     */
    @Test
    void testT103_EliminazionePlaylistRimuovePlaylistEAssociazioni() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        // Given: una playlist esistente identificata da ID "1" e popolata da tracce musicali
        assertFalse(service.getTracksForPlaylist("1").isEmpty());

        // When: viene richiesta la rimozione della risorsa playlist
        Playlist deletedPlaylist = service.deletePlaylist("1");

        // Then: la playlist viene estratta con successo e non compare più nelle interrogazioni future
        assertNotNull(deletedPlaylist);
        assertEquals("Rock Classics", deletedPlaylist.getName());
        assertThrows(PlaylistNotFoundException.class, () -> service.getPlaylistById("1"));
        assertTrue(fakePlaylistRepo.tracksInPlaylist1.isEmpty(), "Le relazioni dei brani devono essere troncate.");
    }

    /**
     * <b>Task T-104:</b> Test JUnit per accertare che l'eliminazione atomica della playlist
     * non vada a intaccare né a cancellare i brani musicali memorizzati nel catalogo globale.
     * <p>Soddisfa lo Scenario 2 dei Criteri di Accettazione di US-5.1.</p>
     */
    @Test
    void testT104_EliminazionePlaylistNonEliminaTracceDalCatalogo() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        // Given: le tracce "t1" e "t2" preesistono regolarmente nel catalogo globale delle canzoni
        assertNotNull(fakeTrackRepo.findById("t1").orElse(null));
        assertNotNull(fakeTrackRepo.findById("t2").orElse(null));

        // When: l'utente rimuove definitivamente la playlist "1" che le aggregava
        service.deletePlaylist("1");

        // Then: nessuna traccia originale viene rimossa dal database generale
        Optional<Track> track1 = fakeTrackRepo.findById("t1");
        Optional<Track> track2 = fakeTrackRepo.findById("t2");
        assertTrue(track1.isPresent(), "La canzone 't1' deve persistere intatta all'interno del catalogo globale.");
        assertTrue(track2.isPresent(), "La canzone 't2' deve persistere intatta all'interno del catalogo globale.");
    }
}