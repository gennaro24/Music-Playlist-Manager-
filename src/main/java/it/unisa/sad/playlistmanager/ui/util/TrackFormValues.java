package it.unisa.sad.playlistmanager.ui.util;

/**
 * Valori del form traccia già normalizzati e pronti per la validazione UI.
 */
public record TrackFormValues(String title, String author, int duration, String genre, int year) {

    /**
     * Estrae e normalizza i campi testuali prima della validazione sintattica.
     */
    public static TrackFormValues fromRaw(
            String rawTitle,
            String rawAuthor,
            String rawDuration,
            String rawGenre,
            String rawYear) {
        return new TrackFormValues(
                UiTextFormatter.toSentenceCase(rawTitle),
                UiTextFormatter.toSentenceCase(rawAuthor),
                Integer.parseInt(rawDuration.trim()),
                UiTextFormatter.toSentenceCase(rawGenre),
                Integer.parseInt(rawYear.trim()));
    }
}
