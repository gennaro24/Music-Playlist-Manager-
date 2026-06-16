package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.command.Command;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;

import java.util.List;

/**
 * Elimina una playlist conservando il suo stato minimo: entita' e tracce
 * ordinate. L'undo ricrea la playlist con lo stesso ID e ripristina le tracce
 * nello stesso ordine.
 *
 * Viene creato da CommandFactory per la facade ed eseguito da UndoManager.
 * Serve a non perdere identita' e contenuto ordinato della playlist eliminata.
 *
 * @author Foschillo G.
 * @version 1.0
 */
public class DeletePlaylistCommand implements Command {

    private final String playlistId;
    private final PlaylistService playlistService;

    private Playlist deletedPlaylist;
    private List<Track> deletedTracks = List.of();

    public DeletePlaylistCommand(String playlistId, PlaylistService playlistService) {
        if (playlistService == null) {
            throw new IllegalArgumentException("PlaylistService non puo' essere null");
        }
        if (playlistId == null || playlistId.isBlank()) {
            throw new IllegalArgumentException("PlaylistId non puo' essere null o vuoto");
        }

        this.playlistId = playlistId;
        this.playlistService = playlistService;
    }

    @Override
    public void execute() {
        deletedPlaylist = playlistService.getPlaylistById(playlistId);
        // La copia impedisce che la delete modifichi indirettamente lo snapshot.
        deletedTracks = List.copyOf(playlistService.getTracksForPlaylist(playlistId));
        playlistService.deletePlaylist(playlistId);
    }

    @Override
    public void undo() {
        if (deletedPlaylist == null) {
            return;
        }

        playlistService.createPlaylist(deletedPlaylist.getId(), deletedPlaylist.getName());
        playlistService.populatePlaylist(deletedPlaylist.getId(), deletedTracks);

        // Lo stato corrente del command viene eliminato solo dopo il ripristino.
        deletedPlaylist = null;
        deletedTracks = List.of();
    }

    public Playlist getDeletedPlaylist() {
        return deletedPlaylist;
    }
}
