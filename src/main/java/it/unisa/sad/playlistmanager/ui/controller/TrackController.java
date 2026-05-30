package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Track;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;

public class TrackController {

    private MusicPlaylistManagerFacade facade;
    @FXML
    private TextField txtTitle;
    @FXML
    private TextField txtAuthor;
    @FXML
    private TextField txtDuration;
    @FXML
    private TextField txtGenre;
    @FXML
    private TextField txtYear;
    @FXML
    private Label lblFeedback;

    @FXML
    private TableView<Track> tableTracks;
    @FXML
    private TableColumn<Track, String> colTitle;
    @FXML
    private TableColumn<Track, String> colAuthor;
    @FXML
    private TableColumn<Track, Integer> colDuration;
    @FXML
    private TableColumn<Track, String> colGenre;
    @FXML
    private TableColumn<Track, Integer> colYear;
    private Track selectedTrack;

    @FXML
    private void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        if (tableTracks != null) {
            tableTracks.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                selectedTrack = newSelection;
            });
        }

    }

    public void setFacade(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
        loadCatalog();
    }

    @FXML
    private void handleTrackAddition(ActionEvent event) {
        lblFeedback.setText(""); // pulisce messaggi precedenti
        if (facade == null) {
            lblFeedback.setText("Errore interno: facade non inizializzata.");
            return;
        }
        try {
            String title = toSentenceCase(txtTitle.getText());
            String author = toSentenceCase(txtAuthor.getText());
            String genre = toSentenceCase(txtGenre.getText());
            int duration = Integer.parseInt(txtDuration.getText().trim());
            int year = Integer.parseInt(txtYear.getText().trim());
            /*
             * facade.addTrack(
             * title,
             * author,
             * duration,
             * genre,
             * year,
             * );
             */
            clearForm();
            lblFeedback.setStyle("-fx-text-fill: green;");
            lblFeedback.setText("Traccia aggiunta con successo.");
            loadCatalog();

        } catch (NumberFormatException e) {
            lblFeedback.setStyle("-fx-text-fill: red;");
            lblFeedback.setText("Durata e anno devono essere numeri validi.");
        } catch (IllegalArgumentException e) {
            lblFeedback.setStyle("-fx-text-fill: red;");
            lblFeedback.setText(e.getMessage()); // errore dal service (campi mancanti, ecc.)
        } catch (Exception e) {
            lblFeedback.setStyle("-fx-text-fill: red;");
            lblFeedback.setText("Errore durante il salvataggio della traccia.");
        }
    }


    //rimuove la traccia selezionata dalla tabella Catalogo
    private void handleTrackRemoval(ActionEvent event) {
        lblFeedback.setText(""); // pulisce messaggi precedenti
        if (facade == null) {
            lblFeedback.setText("Errore interno: facade non inizializzata.");
            return;
        }
        try {
            if (selectedTrack != null) {
                // facade.removeTrack(selectedTrack);
                loadCatalog();
                lblFeedback.setStyle("-fx-text-fill: green;");
                lblFeedback.setText("Traccia eliminata con successo.");
            } else {
                lblFeedback.setStyle("-fx-text-fill: red;");
                lblFeedback.setText("Nessuna traccia selezionata.");
            }
            // facade.removeTrack(tableTracks.getSelectionModel().getSelectedItem());
        } catch (Exception e) {
            lblFeedback.setStyle("-fx-text-fill: red;");
            lblFeedback.setText("Errore durante l'eliminazione della traccia.");
        }
    }

    public void clearForm() {
        txtTitle.clear();
        txtAuthor.clear();
        txtDuration.clear();
        txtGenre.clear();
        txtYear.clear();
    }

    private void loadCatalog() {
        if (facade == null || tableTracks == null) {
            return;
        }
        // tableTracks.getItems().setAll(facade.getAllTracks());

    }

    // funzione per convertire la prima lettera di ogni parola in maiuscolo
    // vale anche per i numeri " track99 " -> "Track99" e "123abc" -> "123abc"
    private String toSentenceCase(String value) {
        if (value == null)
            return "";
        String trimmed = value.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty())
            return "";
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}
