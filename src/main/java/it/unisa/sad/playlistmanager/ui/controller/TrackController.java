package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Track;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

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
    @FXML
    private VBox formAddTrack;

    @FXML
    private void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
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

            facade.addTrack2(
                    title,
                    author,
                    duration,
                    genre,
                    year);
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
            System.out.println("Errore durante il salvataggio della traccia: " + e.getClass().getSimpleName()
                    + (e.getMessage() != null ? " - " + e.getMessage() : ""));
            e.printStackTrace(); // Per debugging a console
        }

    }

    /**
     * metodo che richiama il metodo getAllTracks del facade e setta i dati nella
     * tabella catalogo
     */
    private void loadCatalog() {
        // se il facade o la tabella non sono inizializzati, non faccio nulla
        if (facade == null || tableTracks == null) {
            System.out.println("facade o tabella non inizializzati");
            return;
        }
        // interrogo il facade per ottenere tutte le tracce e le setto nella tabella
        // quanto invoco il setItems, la tabella si aggiorna con i dati della facade
        // i dati vengono inseriti nella colonna corretta tramite PropertyValueFactory
        // definito in initialize
        tableTracks.setItems(FXCollections.observableArrayList(facade.getAllTracks2()));

    }

    /**
     * Mostra il catalogo completo, anche se prima era visibile una playlist.
     */
    public void showCatalogView() {
        if (formAddTrack != null) {
            formAddTrack.setVisible(true);
            formAddTrack.setManaged(true);
        }
        loadCatalog();
        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: #1f7a1f;");
            lblFeedback.setText("Visualizzazione catalogo completo.");
        }
    }

    public void displayPlaylistTracks(Playlist playlist) {
        if (tableTracks == null || playlist == null) {
            return;
        }

        // nascondiamo il form in modalità visualizzazione playlist
        if (formAddTrack != null) {
            formAddTrack.setVisible(false);
            formAddTrack.setManaged(false);
        }
        tableTracks.setItems(FXCollections.observableArrayList(playlist.getTracks()));
        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: #0066cc;");
            lblFeedback.setText("Contenuto playlist: " + playlist.getName());
        }

    }

    /**
     * metodo che pulisce i campi del form
     */
    public void clearForm() {
        txtTitle.clear();
        txtAuthor.clear();
        txtDuration.clear();
        txtGenre.clear();
        txtYear.clear();
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
