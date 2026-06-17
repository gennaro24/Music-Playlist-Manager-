package it.unisa.sad.playlistmanager.ui.util;

/**
 * Utility di formattazione testuale per il layer di presentazione.
 */
public final class UiTextFormatter {

    private UiTextFormatter() {
    }

    /**
     * Normalizza una stringa impostando la prima lettera maiuscola e il resto minuscolo.
     */
    public static String toSentenceCase(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}
