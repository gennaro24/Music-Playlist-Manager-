package it.unisa.sad.playlistmanager.application.command.concreteCommands;

import it.unisa.sad.playlistmanager.application.command.Command;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.application.service.PlaylistService;

/**
 * Rimuove una singola associazione playlist-track.
 * Poiche' la traccia resta nel catalogo, per l'undo sono sufficienti gli ID e
 * la posizione originale dell'associazione.
 *
 * Viene creato da CommandFactory per la facade ed eseguito da UndoManager.
 * Serve a reinserire la traccia esattamente nella posizione occupata prima
 * della rimozione.
 *
 * @author Foschillo G.
 * @version 1.0
 */
public class RemoveTrackFromPlaylistCommand implements Command {

    private final PlaylistService playlistService;
    private final String playlistId;
    private final String trackId;

    private Integer originalPosition;

    public RemoveTrackFromPlaylistCommand(
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
        originalPosition = playlistService.getTrackPosition(playlistId, trackId)
                .orElseThrow(() ->
                        new ValidationException("La Track non esiste nella Playlist."));

        playlistService.removeTrackFromPlaylist(playlistId, trackId);
    }

    @Override
    public void undo() {
        if (originalPosition == null) {
            return;
        }

        playlistService.restoreTrackToPlaylist(playlistId, trackId, originalPosition);

        // Dopo un undo riuscito l'associazione non deve essere ripristinata due volte.
        originalPosition = null;
    }
}
