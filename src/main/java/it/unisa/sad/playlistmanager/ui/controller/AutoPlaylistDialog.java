package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.AutoPlaylistCriteria;
import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.ui.util.StyledAlertFactory;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
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
 * Dialog per la creazione di una playlist automatica (US-28).
 */
public class AutoPlaylistDialog {

    private final MusicPlaylistManagerFacade facade;

    private final TextField txtName = new TextField();
    private final ComboBox<String> cmbGenre = new ComboBox<>();
    private final ComboBox<Integer> cmbYear = new ComboBox<>();
    private final ComboBox<Tag> cmbTag = new ComboBox<>();
    private final ListView<Track> listResults = new ListView<>();
    private final Label lblInfo = new Label();

    private List<Track> lastPreview = List.of();

    private final ButtonType btnTypeCrea = new ButtonType("Crea Playlist", ButtonBar.ButtonData.OK_DONE);

    public AutoPlaylistDialog(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    public void show() {
        if (facade == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Playlist automatica");
        dialog.setHeaderText("Scegli i criteri e visualizza l'anteprima delle tracce nel catalogo");
        dialog.getDialogPane().getButtonTypes().addAll(btnTypeCrea, ButtonType.CLOSE);
        StyledAlertFactory.applyTheme(dialog.getDialogPane());

        txtName.setPromptText("Nome della playlist");
        txtName.setPrefWidth(260);

        configureGenreCombo();
        configureYearCombo();
        configureTagCombo();
        configureResultsList();

        txtName.textProperty().addListener((obs, oldV, newV) -> updatePreview(dialog));
        cmbGenre.valueProperty().addListener((obs, oldV, newV) -> updatePreview(dialog));
        cmbYear.valueProperty().addListener((obs, oldV, newV) -> updatePreview(dialog));
        cmbTag.valueProperty().addListener((obs, oldV, newV) -> updatePreview(dialog));

        GridPane criteriaForm = new GridPane();
        criteriaForm.setHgap(10);
        criteriaForm.setVgap(10);
        criteriaForm.setPadding(new Insets(10, 10, 4, 10));
        criteriaForm.add(new Label("Nome *"), 0, 0);
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

        updatePreview(dialog);
        dialog.setResultConverter(this::convertCreateResult);
        dialog.showAndWait();
    }

    private ButtonType convertCreateResult(ButtonType dialogButton) {
        if (dialogButton != btnTypeCrea) {
            return null;
        }

        String playlistName = txtName.getText() != null ? txtName.getText().trim() : "";
        try {
            AutoPlaylistCriteria criteria = buildCriteria();
            facade.createAutoPlaylist(playlistName, criteria);
            showPlaylistCreatedAlert(playlistName, lastPreview.size());
            return btnTypeCrea;
        } catch (RuntimeException ex) {
            showValidationErrorAlert(ex.getMessage());
            return null;
        }
    }

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

    private void updatePreview(Dialog<ButtonType> dialog) {
        AutoPlaylistCriteria criteria = buildCriteria();
        Button btnCrea = (Button) dialog.getDialogPane().lookupButton(btnTypeCrea);

        String nameInput = txtName.getText() != null ? txtName.getText().trim() : "";
        boolean isNameDuplicate = facade.getAllPlaylists().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(nameInput));

        if (criteria == null) {
            lastPreview = List.of();
            listResults.getItems().clear();
            lblInfo.setStyle("-fx-text-fill: #0066cc; -fx-font-weight: normal;");
            lblInfo.setText("Seleziona almeno un criterio (genere, anno o tag).");
            if (btnCrea != null) {
                btnCrea.setDisable(true);
            }
            return;
        }

        if (isNameDuplicate) {
            lastPreview = facade.previewAutoPlaylist(criteria);
            listResults.setItems(FXCollections.observableArrayList(lastPreview));
            lblInfo.setStyle("-fx-text-fill: #b0413e; -fx-font-weight: bold;");
            lblInfo.setText("Errore: Il nome '" + nameInput + "' è già utilizzato.");
            if (btnCrea != null) {
                btnCrea.setDisable(true);
            }
            return;
        }

        try {
            lastPreview = facade.previewAutoPlaylist(criteria);
            listResults.setItems(FXCollections.observableArrayList(lastPreview));

            if (lastPreview.isEmpty()) {
                lblInfo.setStyle("-fx-text-fill: #b0413e; -fx-font-weight: normal;");
                lblInfo.setText("Nessuna traccia corrisponde ai criteri selezionati.");
                if (btnCrea != null) {
                    btnCrea.setDisable(true);
                }
            } else {
                int n = lastPreview.size();
                lblInfo.setStyle("-fx-text-fill: green; -fx-font-weight: normal;");
                lblInfo.setText(n + (n == 1 ? " traccia trovata." : " tracce trovate."));
                if (btnCrea != null) {
                    btnCrea.setDisable(nameInput.isEmpty());
                }
            }
        } catch (RuntimeException exception) {
            lastPreview = List.of();
            listResults.getItems().clear();
            lblInfo.setStyle("-fx-text-fill: #b0413e; -fx-font-weight: normal;");
            lblInfo.setText(exception.getMessage());
            if (btnCrea != null) {
                btnCrea.setDisable(true);
            }
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
        items.add(null);
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
        items.add(null);
        items.addAll(years);
        cmbYear.setItems(FXCollections.observableArrayList(items));
        cmbYear.getSelectionModel().selectFirst();
        cmbYear.setButtonCell(yearCell());
        cmbYear.setCellFactory(lv -> yearCell());
    }

    private void configureTagCombo() {
        List<Tag> items = new ArrayList<>();
        items.add(null);
        items.addAll(facade.getAllTags());
        cmbTag.setItems(FXCollections.observableArrayList(items));
        cmbTag.getSelectionModel().selectFirst();
        cmbTag.setButtonCell(tagCell());
        cmbTag.setCellFactory(lv -> tagCell());
    }

    private void configureResultsList() {
        listResults.setPlaceholder(new Label("Nessuna traccia da mostrare."));
        listResults.setCellFactory(lv -> new ListCell<Track>() {
            @Override
            protected void updateItem(Track item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + " - " + item.getAuthor()
                            + " (" + item.getYear() + ", " + item.getGenre() + ")");
                }
            }
        });
    }

    private ListCell<String> stringCell(String anyLabel) {
        return new ListCell<String>() {
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
        return new ListCell<Integer>() {
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
        return new ListCell<Tag>() {
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

    private void showPlaylistCreatedAlert(String playlistName, int trackCount) {
        Alert alert = StyledAlertFactory.create(Alert.AlertType.INFORMATION);
        alert.setTitle("Playlist Creata");
        alert.setHeaderText(null);
        alert.setContentText(
                "La playlist automatica '" + playlistName
                        + "' è stata creata con successo con " + trackCount + " tracce!");
        alert.showAndWait();
    }

    private void showValidationErrorAlert(String message) {
        Alert alert = StyledAlertFactory.create(Alert.AlertType.ERROR);
        alert.setTitle("Errore di Validazione");
        alert.setHeaderText("Impossibile creare la playlist");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
