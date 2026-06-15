package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.command.Command;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;

/**
 * Aggiunge una traccia esistente a una playlist.
 * L'undo elimina soltanto l'associazione appena creata, senza rimuovere la
 * traccia dal catalogo.
 *
 * Viene creato da CommandFactory per la facade ed eseguito da UndoManager.
 * Serve a rendere annullabile la sola relazione playlist-track.
 * 
 * @author Foschillo G.
 * @version 1.0
 */
public class AddTrackToPlaylistCommand implements Command {

    private final PlaylistService playlistService;
    private final String playlistId;
    private final String trackId;

    private boolean executed;

    public AddTrackToPlaylistCommand(
            PlaylistService playlistService,
            String playlistId,
            String trackId) {
        if (playlistService == null) {
            throw new IllegalArgumentException("PlaylistService non puo' essere null");
        }
        if (playlistId == null || playlistId.isBlank()) {
            throw new IllegalArgumentException("PlaylistId non puo' essere null o vuoto");
        }
        if (trackId == null || trackId.isBlank()) {
            throw new IllegalArgumentException("TrackId non puo' essere null o vuoto");
        }

        this.playlistService = playlistService;
        this.playlistId = playlistId;
        this.trackId = trackId;
    }

    @Override
    public void execute() {
        playlistService.addTrackToPlaylist(playlistId, trackId);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            return;
        }

        playlistService.removeTrackFromPlaylist(playlistId, trackId);
        executed = false;
    }
}
