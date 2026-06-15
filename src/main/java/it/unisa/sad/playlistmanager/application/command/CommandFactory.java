package it.unisa.sad.playlistmanager.application.command;

import it.unisa.sad.playlistmanager.application.command.concreteCommands.AddTrackCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.AddTrackToPlaylistCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.CreatePlaylistCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.DeletePlaylistCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.DeleteTrackCommand;
import it.unisa.sad.playlistmanager.application.command.concreteCommands.RemoveTrackFromPlaylistCommand;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.application.service.TrackService;

/**
 * Centralizza la costruzione dei command usando le istanze condivise dei
 * service applicativi.
 *
 * La factory costruisce e configura i command, ma non li esegue e non gestisce
 * la cronologia dell'undo. Viene chiamata da MusicPlaylistManagerFacade, che
 * consegna poi il command creato a {@link UndoManager}.
 *
 * @author Foschillo G.
 * @version 1.0
 */
public class CommandFactory {

    private final TrackService trackService;
    private final PlaylistService playlistService;

    public CommandFactory(TrackService trackService, PlaylistService playlistService) {
        if (trackService == null) {
            throw new IllegalArgumentException("TrackService cannot be null");
        }
        if (playlistService == null) {
            throw new IllegalArgumentException("PlaylistService cannot be null");
        }

        this.trackService = trackService;
        this.playlistService = playlistService;
    }

    public AddTrackCommand createAddTrackCommand(
            String title,
            String author,
            int duration,
            String genre,
            int year) {
        return new AddTrackCommand(trackService, title, author, duration, genre, year);
    }

    public DeleteTrackCommand createDeleteTrackCommand(String trackId) {
        return new DeleteTrackCommand(trackService, playlistService, trackId);
    }

    public CreatePlaylistCommand createCreatePlaylistCommand(String name) {
        return new CreatePlaylistCommand(name, playlistService);
    }

    public DeletePlaylistCommand createDeletePlaylistCommand(String playlistId) {
        return new DeletePlaylistCommand(playlistId, playlistService);
    }

    public AddTrackToPlaylistCommand createAddTrackToPlaylistCommand(
            String playlistId,
            String trackId) {
        return new AddTrackToPlaylistCommand(playlistService, playlistId, trackId);
    }

    public RemoveTrackFromPlaylistCommand createRemoveTrackFromPlaylistCommand(
            String playlistId,
            String trackId) {
        return new RemoveTrackFromPlaylistCommand(playlistService, playlistId, trackId);
    }
}
