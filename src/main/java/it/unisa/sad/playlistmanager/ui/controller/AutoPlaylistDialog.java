package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.AutoPlaylistCriteria;
import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.domain.model.Track;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog per la creazione di una playlist automatica (US-28, T3-28).
 *
 * Consente di selezionare i criteri (genere, anno, tag) e mostra in tempo reale
 * l'anteprima delle tracce del catalogo che li soddisfano, interrogando
 * {@link MusicPlaylistManagerFacade#previewAutoPlaylist}. I criteri sono
 * combinati in AND; lasciare una voce su "Qualsiasi" significa non filtrare su
 * quella dimensione. Se nessuna traccia corrisponde, viene mostrato un
 * messaggio di "nessun risultato" e non viene creata alcuna playlist.
 *
 * Il bottone "Crea" e il suo collegamento a createAutoPlaylist (T3-33) e la sua
 * disabilitazione a 0 risultati (T3-34) sono a carico di un'altra task: qui i
 * punti d'aggancio sono predisposti (campo nome, {@link #buildCriteria()},
 * {@link #lastPreview}).
 *
 * @author Adinolfi G.
 * @version 1.0
 */
public class AutoPlaylistDialog {

    private final MusicPlaylistManagerFacade facade;

    private final TextField txtName = new TextField();
    private final ComboBox<String> cmbGenre = new ComboBox<>();
    private final ComboBox<Integer> cmbYear = new ComboBox<>();
    private final ComboBox<Tag> cmbTag = new ComboBox<>();
    private final ListView<Track> listResults = new ListView<>();
    private final Label lblInfo = new Label();

    /** Ultima anteprima calcolata: utile per la creazione effettiva (T3-33/T3-34). */
    private List<Track> lastPreview = List.of();

    public AutoPlaylistDialog(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    /**
     * Costruisce e mostra la dialog di anteprima.
     */
    public void show() {
        if (facade == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Playlist automatica");
        dialog.setHeaderText("Scegli i criteri e visualizza l'anteprima delle tracce nel catalogo");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        txtName.setPromptText("Nome della playlist");
        txtName.setPrefWidth(260);

        configureGenreCombo();
        configureYearCombo();
        configureTagCombo();
        configureResultsList();

        // Anteprima reattiva a ogni cambio di criterio.
        cmbGenre.valueProperty().addListener((obs, oldV, newV) -> updatePreview());
        cmbYear.valueProperty().addListener((obs, oldV, newV) -> updatePreview());
        cmbTag.valueProperty().addListener((obs, oldV, newV) -> updatePreview());

        GridPane criteriaForm = new GridPane();
        criteriaForm.setHgap(10);
        criteriaForm.setVgap(10);
        criteriaForm.setPadding(new Insets(10, 10, 4, 10));
        criteriaForm.add(new Label("Nome"), 0, 0);
        criteriaForm.add(txtName, 1, 0);
        criteriaForm.add(new Label("Genere"), 0, 1);
        criteriaForm.add(cmbGenre, 1, 1);
        criteriaForm.add(new Label("Anno"), 0, 2);
        criteriaForm.add(cmbYear, 1, 2);
        criteriaForm.add(new Label("Tag"), 0, 3);
        criteriaForm.add(cmbTag, 1, 3);

        cmbGenre.setPrefWidth(260);
        cmbYear.setPrefWidth(260);
        cmbTag.setPrefWidth(260);

        Label lblPreviewTitle = new Label("Anteprima");
        lblPreviewTitle.setStyle("-fx-font-weight: bold;");
        VBox.setVgrow(listResults, Priority.ALWAYS);
        listResults.setPrefHeight(220);

        VBox content = new VBox(8, criteriaForm, lblPreviewTitle, listResults, lblInfo);
        content.setPadding(new Insets(6));
        content.setPrefWidth(420);
        dialog.getDialogPane().setContent(content);

        // Stato iniziale: nessun criterio selezionato.
        updatePreview();

        // TODO T3-33 (Di Marino): aggiungere un ButtonType "Crea" e, alla conferma,
        //   chiamare la creazione effettiva con il nome (txtName) e i criteri
        //   (buildCriteria()). La preview corrente e' disponibile in lastPreview.
        // TODO T3-34 (Di Marino): disabilitare il bottone "Crea" quando
        //   lastPreview e' vuota (vedi updatePreview()).

        dialog.showAndWait();
    }

    /**
     * Costruisce i criteri dalle selezioni correnti.
     *
     * @return i criteri scelti, oppure {@code null} se non e' stato selezionato
     *         alcun criterio (in tal caso non va costruito alcun
     *         {@link AutoPlaylistCriteria}, che richiede almeno un criterio).
     */
    private AutoPlaylistCriteria buildCriteria() {
        String genre = cmbGenre.getValue();
        Integer year = cmbYear.getValue();
        Tag tag = cmbTag.getValue();
        String tagId = (tag != null) ? tag.getId() : null;

        boolean nessunCriterio = (genre == null || genre.isBlank()) && year == null && tagId == null;
        if (nessunCriterio) {
            return null;
        }
        return AutoPlaylistCriteria.combined(genre, year, tagId);
    }

    /**
     * Ricalcola e mostra l'anteprima in base ai criteri correnti.
     */
    private void updatePreview() {
        AutoPlaylistCriteria criteria = buildCriteria();
        if (criteria == null) {
            lastPreview = List.of();
            listResults.getItems().clear();
            lblInfo.setText("Seleziona almeno un criterio (genere, anno o tag).");
            return;
        }

        try {
            lastPreview = facade.previewAutoPlaylist(criteria);
            listResults.setItems(FXCollections.observableArrayList(lastPreview));
            if (lastPreview.isEmpty()) {
                lblInfo.setText("Nessuna traccia corrisponde ai criteri selezionati.");
            } else {
                int n = lastPreview.size();
                lblInfo.setText(n + (n == 1 ? " traccia trovata." : " tracce trovate."));
            }
        } catch (RuntimeException exception) {
            lastPreview = List.of();
            listResults.getItems().clear();
            lblInfo.setText(exception.getMessage());
        }
    }

    private void configureGenreCombo() {
        List<String> genres = facade.getAllTracks().stream()
                .map(Track::getGenre)
                .filter(genre -> genre != null && !genre.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        List<String> items = new ArrayList<>();
        items.add(null); // "Qualsiasi genere"
        items.addAll(genres);
        cmbGenre.setItems(FXCollections.observableArrayList(items));
        cmbGenre.getSelectionModel().selectFirst();
        cmbGenre.setButtonCell(stringCell("Qualsiasi genere"));
        cmbGenre.setCellFactory(lv -> stringCell("Qualsiasi genere"));
    }

    private void configureYearCombo() {
        List<Integer> years = facade.getAllTracks().stream()
                .map(Track::getYear)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        List<Integer> items = new ArrayList<>();
        items.add(null); // "Qualsiasi anno"
        items.addAll(years);
        cmbYear.setItems(FXCollections.observableArrayList(items));
        cmbYear.getSelectionModel().selectFirst();
        cmbYear.setButtonCell(yearCell());
        cmbYear.setCellFactory(lv -> yearCell());
    }

    private void configureTagCombo() {
        List<Tag> items = new ArrayList<>();
        items.add(null); // "Qualsiasi tag"
        items.addAll(facade.getAllTags());
        cmbTag.setItems(FXCollections.observableArrayList(items));
        cmbTag.getSelectionModel().selectFirst();
        cmbTag.setButtonCell(tagCell());
        cmbTag.setCellFactory(lv -> tagCell());
    }

    private void configureResultsList() {
        listResults.setPlaceholder(new Label("Nessuna traccia da mostrare."));
        listResults.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Track item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + " — " + item.getAuthor()
                            + " (" + item.getYear() + ", " + item.getGenre() + ")");
                }
            }
        });
    }

    private ListCell<String> stringCell(String anyLabel) {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item == null ? anyLabel : item);
                }
            }
        };
    }

    private ListCell<Integer> yearCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item == null ? "Qualsiasi anno" : String.valueOf(item));
                }
            }
        };
    }

    private ListCell<Tag> tagCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item == null ? "Qualsiasi tag" : item.getName());
                }
            }
        };
    }
}