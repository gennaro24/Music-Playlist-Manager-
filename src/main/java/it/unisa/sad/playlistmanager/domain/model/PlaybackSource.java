package it.unisa.sad.playlistmanager.domain.model;

/**
 * Definisce il contesto o la sorgente da cui viene riprodotta la musica.
 * - SINGLE: Riproduzione di una singola traccia.
 * - PLAYLIST: Riproduzione di una lista di tracce appartenenti a una playlist.
 * - CATALOG: Riproduzione dell'intero catalogo musicale.
 */
public enum PlaybackSource {
    SINGLE,
    PLAYLIST,
    CATALOG
}