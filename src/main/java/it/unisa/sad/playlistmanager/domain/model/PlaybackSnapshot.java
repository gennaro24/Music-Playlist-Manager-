package it.unisa.sad.playlistmanager.domain.model;

/**
 * DTO immutabile dello stato corrente del playback.
 *
 * @param state stato corrente
 * @param currentTrack traccia corrente, oppure {@code null} se assente
 */

public record PlaybackSnapshot(
        PlaybackState state,
        Track currentTrack
) {}