package it.unisa.sad.playlistmanager.ui.util;

import it.unisa.sad.playlistmanager.domain.model.Tag;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility per la formattazione testuale dei tag in tabella e dialoghi.
 */
public final class TrackTagTextFormatter {

    private TrackTagTextFormatter() {
    }

    /**
     * Formatta i tag su righe separate, un nome per riga.
     */
    public static String formatForTable(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.joining("\n"));
    }

    /** Formatta i nomi dei tag su una riga per tooltip o anteprime compatte. */
    public static String formatInline(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));
    }
}
