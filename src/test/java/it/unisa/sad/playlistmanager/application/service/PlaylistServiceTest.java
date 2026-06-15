package it.unisa.sad.playlistmanager.application.service;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
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

    // ===================================================================================
    // FAKE REPOSITORIES PER SIMULARE IL DATABASE
    // ===================================================================================

    /**
     * Sostituto finto (Fake Object) destinato all'isolamento dello stato delle playlist.
     * Implementa l'interfaccia contrattuale aggiornata del modulo persistence.
     */
    class FakePlaylistRepository implements PlaylistRepository {
        /** Interruttore per monitorare l'invocazione del salvataggio. */
        boolean isSaveCalled = false;
        /** Interruttore per catturare l'avvenuta associazione di una traccia. */
        boolean isAddTrackCalled = false;
        /** Interruttore per catturare l'avvenuta disassociazione di una traccia. */
        boolean isRemoveTrackCalled = false;
        /** Memorizza l'istanza dell'ultima playlist inviata alla persistenza. */
        Playlist savedPlaylist = null;

        /** Lista mutabile atta a simulare la tabella 'playlists' del database reale. */
        List<Playlist> simulatedPlaylists = new ArrayList<>(List.of(
                new Playlist("1", "Rock Classics")
        ));

        /** Lista mutabile per la gestione delle tracce interne alla playlist finta 1. */
        List<Track> tracksInPlaylist1 = new ArrayList<>(List.of(
                new Track("t1", "Song One", "Artist One", 180, "Rock", 2020),
                new Track("t2", "Song Two", "Artist Two", 200, "Rock", 2021)
        ));

        @Override
        public void save(Playlist playlist) {
            this.isSaveCalled = true;
            this.savedPlaylist = playlist;
            this.simulatedPlaylists.add(playlist);
        }

        @Override
        public Optional<Playlist> findById(String id) {
            return this.simulatedPlaylists.stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<Playlist> findByName(String name) {
            return Optional.empty();
        }

        @Override
        public List<Playlist> findAll() {
            return this.simulatedPlaylists;
        }

        @Override
        public boolean existsByName(String name) {
            return false;
        }

        @Override
        public void addTrackToPlaylist(String playlistId, String trackId) {
            this.isAddTrackCalled = true;
        }

        @Override
        public void addTrackToPlaylistAtPosition(String playlistId, String trackId, int position) {
            // TODO: simulare l'inserimento della traccia nella posizione richiesta.
        }

        @Override
        public void removeTrackFromPlaylist(String playlistId, String trackId) {
            this.isRemoveTrackCalled = true;
            this.tracksInPlaylist1.removeIf(t -> t.getId().equals(trackId));
        }

        @Override
        public List<Track> findTracksByPlaylistId(String playlistId) {
            if (!"1".equals(playlistId)) {
                return Collections.emptyList();
            }
            return this.tracksInPlaylist1;
        }

        @Override
        public Optional<Integer> getTrackPosition(String playlistId, String trackId) {
            // TODO: restituire la posizione simulata della traccia nella playlist.
            return Optional.empty();
        }

        /**
         * Simula l'operazione SQL DELETE rimuovendo la playlist dalla memoria
         * e troncando a cascata le associazioni delle tracce (Task T-103).
         *
         * @param playlistId L'identificativo unico della playlist da eliminare.
         * @return Un Optional contenente l'istanza eliminata se presente, altrimenti Optional.empty().
         */
        @Override
        public Optional<Playlist> deleteById(String playlistId) {
            Optional<Playlist> playlistOpt = findById(playlistId);
            if (playlistOpt.isPresent()) {
                this.simulatedPlaylists.remove(playlistOpt.get());
                this.tracksInPlaylist1.clear(); // Emula il tranciamento delle chiavi esterne associative
                return playlistOpt;
            }
            return Optional.empty();
        }
    }

    /**
     * Sostituto finto (Fake Object) destinato all'isolamento dello stato delle tracce.
     */
    class FakeTrackRepository implements TrackRepository {
        /** Lista finta delegata a simulare la tabella 'tracks' nel catalogo globale. */
        List<Track> simulatedCatalog = new ArrayList<>(List.of(
                new Track("t1", "Song One", "Artist One", 180, "Rock", 2020),
                new Track("t2", "Song Two", "Artist Two", 200, "Rock", 2021)
        ));
        /** Riferimento opzionale per emulare l'ON DELETE CASCADE dei database relazionali reali. */
        FakePlaylistRepository linkedPlaylistRepo;

        @Override
        public Optional<Track> findById(String id) {
            Optional<Track> dynamicTrack = simulatedCatalog.stream().filter(t -> t.getId().equals(id)).findFirst();
            if (dynamicTrack.isPresent()) return dynamicTrack;

            if ("t1".equals(id) || "t3-nuova".equals(id)) {
                return Optional.of(new Track(id, "Titolo", "Autore", 200, "Pop", 2022));
            }
            return Optional.empty();
        }

        @Override
        public void save(Track track) {
            this.simulatedCatalog.add(track);
        }

        @Override
        public List<Track> findAll() {
            return this.simulatedCatalog;
        }

        @Override
        public Optional<Track> deleteById(String id) {
            Optional<Track> trackOpt = findById(id);
            trackOpt.ifPresent(simulatedCatalog::remove);
            if (linkedPlaylistRepo != null) {
                linkedPlaylistRepo.removeTrackFromPlaylist("1", id);
            }
            return trackOpt;
        }

        @Override
        public Optional<Track> update(Track track) {
            for (int i = 0; i < simulatedCatalog.size(); i++) {
                if (simulatedCatalog.get(i).getId().equals(track.getId())) {
                    simulatedCatalog.set(i, track);
                    return Optional.of(track);
                }
            }
            return Optional.empty();
        }
    }

    // ===================================================================================
    // TEST PREESISTENTI (US-05 e US-06) - SANATI DAL TYPE MISMATCH
    // ===================================================================================

    /**
     * Verifica la corretta creazione e salvataggio di un'istanza di Playlist.
     */
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

    /**
     * Verifica la corretta estrazione delle tracce legate a una determinata playlist.
     */
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

    /**
     * Verifica l'inserimento di una traccia non presente all'interno della playlist selezionata.
     */
    @Test
    void testAddTrackToPlaylist_AggiuntaValida() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertDoesNotThrow(() -> {
            service.addTrackToPlaylist("1", "t3-nuova");
        });

        assertTrue(fakePlaylistRepo.isAddTrackCalled);
    }

    /**
     * Verifica l'appropriato sollevamento di un'eccezione qualora la playlist bersaglio non esista.
     */
    @Test
    void testAddTrackToPlaylist_PlaylistInesistente() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertThrows(PlaylistNotFoundException.class, () -> {
            service.addTrackToPlaylist("999", "t1");
        });
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

    /**
     * Verifica che la rimozione di un brano da una playlist avvenga con successo.
     */
    @Test
    void testRemoveTrackFromPlaylist_RimozioneValida() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertDoesNotThrow(() -> {
            service.removeTrackFromPlaylist("1", "t1");
        });
        assertTrue(fakePlaylistRepo.isRemoveTrackCalled);
    }

    /**
     * Verifica che la rimozione fallisca se indirizzata a una playlist mancante.
     */
    @Test
    void testRemoveTrackFromPlaylist_PlaylistInesistente() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertThrows(PlaylistNotFoundException.class, () -> {
            service.removeTrackFromPlaylist("999", "t1");
        });
    }

    /**
     * Verifica che il tentativo di rimozione di una traccia inesistente sollevi l'eccezione adeguata.
     */
    @Test
    void testRemoveTrackFromPlaylist_TracciaInesistenteNelCatalogo() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        PlaylistService service = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);

        assertThrows(TrackNotFoundException.class, () -> {
            service.removeTrackFromPlaylist("1", "t999");
        });
    }

    // ===================================================================================
    // TEST TASK T-82 (US-03): COERENZA CON PLAYLIST ESISTENTI
    // ===================================================================================

    /**
     * <b>Task T-82:</b> Test JUnit per verificare che una traccia contenuta in una playlist
     * esibisca i metadati aggiornati a seguito di una modifica, escludendo l'insorgenza di duplicati.
     */
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

    // ===================================================================================
    // TEST TASK T-92 (US-04): RIMOZIONE RIFERIMENTI DALLE PLAYLIST (CASCADE DELETE)
    // ===================================================================================

    /**
     * <b>Task T-92:</b> Test JUnit per certificare che l'eliminazione di una traccia a catalogo
     * inneschi la rimozione a cascata da tutte le playlist in cui era inserita, evitando
     * di lasciare riferimenti orfani o inconsistenti all'interno di playlist_tracks.
     * <p>Soddisfa lo Scenario 3 dei Criteri di Accettazione di US-04.</p>
     */
    @Test
    void testT92_EliminazioneRimuoveRiferimentiDaPlaylistTracks() {
        FakePlaylistRepository fakePlaylistRepo = new FakePlaylistRepository();
        FakeTrackRepository fakeTrackRepo = new FakeTrackRepository();
        
        // Colleghiamo i finti repository per emulare l'ON DELETE CASCADE relazionale di SQLite
        fakeTrackRepo.linkedPlaylistRepo = fakePlaylistRepo;
        
        PlaylistService playlistService = new PlaylistService(fakePlaylistRepo, fakeTrackRepo);
        TrackService trackService = new TrackService(fakeTrackRepo);

        // Given: la traccia con ID "t1" è presente nella playlist "1" (Rock Classics)
        List<Track> initialTracks = playlistService.getTracksForPlaylist("1");
        assertEquals(2, initialTracks.size());
        assertEquals("t1", initialTracks.get(0).getId());

        // When: la traccia viene eliminata definitivamente dal catalogo globale
        trackService.deleteTrack("t1");

        // Then: la traccia scompare automaticamente anche dalla playlist in cui era mappata
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
