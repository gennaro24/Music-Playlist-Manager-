package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.exceptions.TagNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.domain.model.Track;
import it.unisa.sad.playlistmanager.ui.util.StyledAlertFactory;
import it.unisa.sad.playlistmanager.ui.util.TrackFormValues;
import it.unisa.sad.playlistmanager.ui.util.TrackTagTextFormatter;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Dialog modale per la modifica dei metadati e dei tag di una traccia.
 */
public class TrackEditDialog {

    private final MusicPlaylistManagerFacade facade;
    private final Consumer<String> onError;
    private final Runnable onTagsChanged;
    private final Consumer<TrackFormValues> formValidator;

    private final TextField editTitle = new TextField();
    private final TextField editAuthor = new TextField();
    private final TextField editDuration = new TextField();
    private final TextField editGenre = new TextField();
    private final TextField editYear = new TextField();
    private final Label tagsPreview = new Label();
    private final ComboBox<Tag> assignTagCombo = new ComboBox<>();
    private final ComboBox<Tag> removeTagCombo = new ComboBox<>();
    private final Button assignTagButton = new Button("Assegna");
    private final Button removeTagButton = new Button("Rimuovi");

    private Track track;

    public TrackEditDialog(
            MusicPlaylistManagerFacade facade,
            Consumer<String> onError,
            Runnable onTagsChanged,
            Consumer<TrackFormValues> formValidator) {
        this.facade = facade;
        this.onError = onError;
        this.onTagsChanged = onTagsChanged;
        this.formValidator = formValidator;
    }

    /**
     * Mostra il dialog e restituisce la traccia aggiornata se l'utente conferma con successo.
     */
    public Optional<Track> show(Track trackToEdit) {
        if (facade == null || trackToEdit == null) {
            return Optional.empty();
        }

        this.track = trackToEdit;
        populateMetadataFields(trackToEdit);
        configureTagControls();
        refreshTagState();

        Dialog<ButtonType> dialog = buildDialog(trackToEdit);
        return dialog.showAndWait()
                .filter(buttonType -> buttonType == ButtonType.OK)
                .flatMap(ignored -> saveMetadataChanges());
    }

    private Dialog<ButtonType> buildDialog(Track trackToEdit) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifica traccia");
        dialog.setHeaderText("Modifica i metadati di \"" + trackToEdit.getTitle() + "\"");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(buildForm());
        StyledAlertFactory.applyTheme(dialog.getDialogPane());
        return dialog;
    }

    private GridPane buildForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10, 20, 10, 10));
        form.add(new Label("Titolo *"), 0, 0);
        form.add(editTitle, 1, 0);
        form.add(new Label("Autore *"), 0, 1);
        form.add(editAuthor, 1, 1);
        form.add(new Label("Durata (sec) *"), 0, 2);
        form.add(editDuration, 1, 2);
        form.add(new Label("Genere *"), 0, 3);
        form.add(editGenre, 1, 3);
        form.add(new Label("Anno *"), 0, 4);
        form.add(editYear, 1, 4);
        form.add(new Label("Tag assegnati"), 0, 5, 2, 1);
        form.add(tagsPreview, 0, 6, 2, 1);
        form.add(new Label("Assegna tag"), 0, 7);
        form.add(new HBox(8, assignTagCombo, assignTagButton), 1, 7);
        form.add(new Label("Rimuovi tag"), 0, 8);
        form.add(new HBox(8, removeTagCombo, removeTagButton), 1, 8);

        editTitle.setPrefWidth(260);
        editAuthor.setPrefWidth(260);
        editDuration.setPrefWidth(260);
        editGenre.setPrefWidth(260);
        editYear.setPrefWidth(260);
        tagsPreview.setWrapText(true);
        tagsPreview.setPrefWidth(280);
        tagsPreview.setMaxWidth(280);
        assignTagCombo.setPrefWidth(200);
        removeTagCombo.setPrefWidth(200);
        return form;
    }

    private void populateMetadataFields(Track trackToEdit) {
        editTitle.setText(trackToEdit.getTitle());
        editAuthor.setText(trackToEdit.getAuthor());
        editDuration.setText(String.valueOf(trackToEdit.getDuration()));
        editGenre.setText(trackToEdit.getGenre());
        editYear.setText(String.valueOf(trackToEdit.getYear()));
    }

    private void configureTagControls() {
        configureTagCombo(assignTagCombo, "Tag da assegnare");
        configureTagCombo(removeTagCombo, "Tag da rimuovere");
        assignTagButton.setOnAction(event -> assignSelectedTag());
        removeTagButton.setOnAction(event -> removeSelectedTag());
    }

    private void configureTagCombo(ComboBox<Tag> comboBox, String emptyPrompt) {
        comboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? emptyPrompt : item.getName());
            }
        });
    }

    private void assignSelectedTag() {
        Tag tagToAssign = assignTagCombo.getSelectionModel().getSelectedItem();
        if (tagToAssign == null) {
            return;
        }
        try {
            facade.assignTagToTrack(track.getId(), tagToAssign.getId());
            refreshTagState();
            onTagsChanged.run();
        } catch (ValidationException | TrackNotFoundException | TagNotFoundException exception) {
            onError.accept(exception.getMessage());
        }
    }

    private void removeSelectedTag() {
        Tag tagToRemove = removeTagCombo.getSelectionModel().getSelectedItem();
        if (tagToRemove == null) {
            return;
        }
        try {
            facade.removeTagFromTrack(track.getId(), tagToRemove.getId());
            refreshTagState();
            onTagsChanged.run();
        } catch (ValidationException | TrackNotFoundException | TagNotFoundException exception) {
            onError.accept(exception.getMessage());
        }
    }

    private void refreshTagState() {
        List<Tag> trackTags = facade.getTagsForTrack(track.getId());
        tagsPreview.setText(TrackTagTextFormatter.formatForTable(trackTags));

        Set<String> assignedIds = trackTags.stream().map(Tag::getId).collect(Collectors.toSet());
        List<Tag> availableTags = facade.getAllTags().stream()
                .filter(tag -> !assignedIds.contains(tag.getId()))
                .toList();

        assignTagCombo.setItems(FXCollections.observableArrayList(availableTags));
        assignTagCombo.getSelectionModel().clearSelection();
        removeTagCombo.setItems(FXCollections.observableArrayList(trackTags));
        removeTagCombo.getSelectionModel().clearSelection();
        assignTagButton.setDisable(availableTags.isEmpty());
        removeTagButton.setDisable(trackTags.isEmpty());
    }

    private Optional<Track> saveMetadataChanges() {
        try {
            TrackFormValues values = TrackFormValues.fromRaw(
                    editTitle.getText(),
                    editAuthor.getText(),
                    editDuration.getText(),
                    editGenre.getText(),
                    editYear.getText());
            formValidator.accept(values);
            Track updatedTrack = new Track(
                    track.getId(),
                    values.title(),
                    values.author(),
                    values.duration(),
                    values.genre(),
                    values.year());
            facade.updateTrack(track.getId(), updatedTrack);
            return Optional.of(updatedTrack);
        } catch (NumberFormatException exception) {
            onError.accept("Durata e anno devono essere numeri validi.");
        } catch (ValidationException
                | it.unisa.sad.playlistmanager.domain.exceptions.ValidationException
                | IllegalArgumentException exception) {
            onError.accept(exception.getMessage());
        } catch (RuntimeException exception) {
            onError.accept("Errore durante la modifica della traccia.");
        }
        return Optional.empty();
    }
}
