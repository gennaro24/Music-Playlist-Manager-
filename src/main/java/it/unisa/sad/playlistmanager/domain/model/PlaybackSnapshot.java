package it.unisa.sad.playlistmanager.domain.model;

/**
 * DTO immutabile dello stato corrente del playback.
 *
 * @param state stato corrente
 * @param currentTrack traccia corrente, oppure {@code null} se assente
 * @param mode modalità di playback corrente
 * @param elapsedSeconds secondi trascorsi nella traccia corrente
 */

public record PlaybackSnapshot(
        PlaybackState state,
        Track currentTrack,
        PlaybackMode mode,
        int elapsedSeconds

) {}