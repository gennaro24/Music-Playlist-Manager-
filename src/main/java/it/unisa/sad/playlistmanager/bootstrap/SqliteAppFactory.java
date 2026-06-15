package it.unisa.sad.playlistmanager.bootstrap;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.persistence.db.DatabaseConnectionManager;
import it.unisa.sad.playlistmanager.persistence.db.DatabaseInitializer;
import it.unisa.sad.playlistmanager.persistence.repository.SqliteTrackRepository;
import it.unisa.sad.playlistmanager.persistence.repository.SqlitePlaylistRepository;
import it.unisa.sad.playlistmanager.persistence.repository.TrackRepository;
import it.unisa.sad.playlistmanager.persistence.repository.PlaylistRepository;
import it.unisa.sad.playlistmanager.application.service.TrackService;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.application.service.PlaybackService;
import it.unisa.sad.playlistmanager.application.command.CommandFactory;
import it.unisa.sad.playlistmanager.application.command.UndoManager;

/**
 * Factort responsabile della creazione della facade e del connection manager
 */
public class SqliteAppFactory implements AppFactory{
    //attributo per la connessione al database
    private DatabaseConnectionManager connectionManager;

    /**
     * metodo responsabile della creazione della facade e dell'inizializzazione del database
     * @return la facade creata
     */
    @Override
    public MusicPlaylistManagerFacade createFacade() {
        try {
            //creazione del connection manager
            DatabaseConnectionManager connectionManager = new DatabaseConnectionManager();
            //creazione del initializer
            DatabaseInitializer databaseInitializer = new DatabaseInitializer(connectionManager);
            //inizializzazione del database
            databaseInitializer.initializeDatabase();


            //creazione del repository delle tracce
            TrackRepository trackRepository = new SqliteTrackRepository(connectionManager);
            //creazione del repository delle playlist
            PlaylistRepository playlistRepository = new SqlitePlaylistRepository(connectionManager);

            //creazione del track service
            TrackService trackService = new TrackService(trackRepository);
            //creazione del playlist service
            PlaylistService playlistService = new PlaylistService(playlistRepository, trackRepository);
            //creazione del playback service
            PlaybackService playbackService = new PlaybackService(playlistRepository);
            //creazione dell'infrastruttura command con gli stessi service condivisi
            CommandFactory commandFactory = new CommandFactory(trackService, playlistService);
            UndoManager undoManager = new UndoManager();
            //creazione della facade
            return new MusicPlaylistManagerFacade(
                    trackService,
                    playlistService,
                    playbackService,
                    commandFactory,
                    undoManager);
        } catch (Exception e) {
            //TODO: gestire l'eccezione
            throw new RuntimeException("Errore nella creazione della facade", e);
        }
       
    }
}
