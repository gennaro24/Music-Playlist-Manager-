package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.domain.model.AutoPlaylistCriteria;
import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.domain.model.Track;
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
 * * @version 2.0 (Task T3-32 & T3-33)
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
    
    // T3-32: Definizione del tipo di bottone personalizzato per confermare la creazione
    private final ButtonType btnTypeCrea = new ButtonType("Crea Playlist", ButtonBar.ButtonData.OK_DONE);

    public AutoPlaylistDialog(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    /**
     * Costruisce e mostra la dialog di anteprima e creazione.
     */
    public void show() {
        if (facade == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Playlist automatica");
        dialog.setHeaderText("Scegli i criteri e visualizza l'anteprima delle tracce nel catalogo");
        
        // T3-32: Aggiunge sia il pulsante "Crea Playlist" sia il pulsante "Chiudi" alla Dialog
        dialog.getDialogPane().getButtonTypes().addAll(btnTypeCrea, ButtonType.CLOSE);

        txtName.setPromptText("Nome della playlist");
        txtName.setPrefWidth(260);

        configureGenreCombo();
        configureYearCombo();
        configureTagCombo();
        configureResultsList();

        txtName.textProperty().addListener((obs, oldV, newV) -> updatePreview(dialog));

        // Anteprima reattiva a ogni cambio di criterio.
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

        // Calcola lo stato di partenza iniziale
        updatePreview(dialog);

        // T3-32: Intercetta la pressione del tasto Crea e ne valida le condizioni operative
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnTypeCrea) {
                String playlistName = txtName.getText() != null ? txtName.getText().trim() : "";
                AutoPlaylistCriteria criteria = buildCriteria();
                
                try {
                    // Esegue la creazione effettiva passando i dati alla Facade
                    facade.createAutoPlaylist(playlistName, criteria);
                    
                    // Mostra un feedback di successo all'utente
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Playlist Creata");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("La playlist automatica '" + playlistName + "' è stata creata con successo con " + lastPreview.size() + " tracce!");
                    successAlert.showAndWait();
                    
                    return btnTypeCrea;
                } catch (ValidationException | it.unisa.sad.playlistmanager.domain.exceptions.ValidationException ex) {
                    // Mostra un messaggio di validazione chiaro in caso di errore (es. nome vuoto)
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Errore di Validazione");
                    errorAlert.setHeaderText("Impossibile creare la playlist");
                    errorAlert.setContentText(ex.getMessage());
                    errorAlert.showAndWait();
                    return null; // Consuma l'evento impedendo la chiusura automatica della dialog
                }
            }
            return null;
        });

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
     * <p><b>TASK T3-33:</b> Disabilita in tempo reale il pulsante Crea se l'anteprima produce 0 risultati.</p>
     */
    /**
     * Ricalcola e mostra l'anteprima in base ai criteri correnti.
     * <p><b>TASK T3-33:</b> Disabilita reattivamente il pulsante se l'anteprima è vuota, 
     * se il nome è vuoto o se il nome inserito è un duplicato già presente nel database.</p>
     */
    private void updatePreview(Dialog<ButtonType> dialog) {
        AutoPlaylistCriteria criteria = buildCriteria();
        Button btnCrea = (Button) dialog.getDialogPane().lookupButton(btnTypeCrea);
        
        // Preleva e normalizza il testo inserito dall'utente per il nome
        String nameInput = txtName.getText() != null ? txtName.getText().trim() : "";

        // Verifica in tempo reale se il nome inserito collide con una playlist esistente
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

        // Se il nome è un duplicato, blocca preventivamente l'interfaccia dando un feedback visivo immediato
        if (isNameDuplicate) {
            lastPreview = facade.previewAutoPlaylist(criteria);
            listResults.setItems(FXCollections.observableArrayList(lastPreview));
            lblInfo.setStyle("-fx-text-fill: #b0413e; -fx-font-weight: bold;");
            lblInfo.setText("Errore: Il nome '" + nameInput + "' è già utilizzato.");
            if (btnCrea != null) {
                btnCrea.setDisable(true); // Disabilita il pulsante
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
                
                // Il pulsante si abilita SOLO se ci sono tracce E il campo nome non è vuoto
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