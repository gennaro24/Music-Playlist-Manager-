package it.unisa.sad.playlistmanager.ui.controller;

import it.unisa.sad.playlistmanager.application.exceptions.PlaylistNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TagNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.TrackNotFoundException;
import it.unisa.sad.playlistmanager.application.exceptions.ValidationException;
import it.unisa.sad.playlistmanager.application.facade.MusicPlaylistManagerFacade;
import it.unisa.sad.playlistmanager.domain.model.Playlist;
import it.unisa.sad.playlistmanager.domain.model.Tag;
import it.unisa.sad.playlistmanager.domain.model.Track;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.beans.binding.Bindings;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Consumer;

/**
 * Sotto-controllore delegato alla visualizzazione, manipolazione e immissione dati
 * concernenti il catalogo globale delle tracce e le canzoni interne a una specifica playlist.
 * <p><b>Revisione Sprint 2 (US-04):</b> Integra la funzionalità di eliminazione di una traccia
 * dal catalogo globale con annesso dialogo di conferma e sincronizzazione in tempo reale delle viste.</p>
 * <p>La modifica dei metadati avviene tramite dialogo dal menu contestuale, non inline nella tabella.</p>
 * @version 4.0
 */
public class TrackController {

    /** Istanza centralizzata dell'Application Facade per il pass-through dei casi d'uso. */
    private final MusicPlaylistManagerFacade facade;
    
    /** Riferimento alla traccia attualmente selezionata nella TableView. */
    private Track selectedTrack;
    
    /** Routine di callback per notificare la selezione di una traccia al coordinatore. */
    private Consumer<Track> onTrackSelectedHandler;
    
    /** Routine di callback per intercettare le richieste di riproduzione forzata. */
    private Consumer<Track> onTrackPlayRequestedHandler;

    /** Campo di testo per l'immissione del titolo della traccia. */
    @FXML private TextField txtTitle;
    
    /** Campo di testo per l'immissione dell'autore della traccia. */
    @FXML private TextField txtAuthor;
    
    /** Campo di testo per l'immissione della durata in secondi. */
    @FXML private TextField txtDuration;
    
    /** Campo di testo per l'immissione del genere musicale. */
    @FXML private TextField txtGenre;
    
    /** Campo di testo per l'immissione dell'anno di pubblicazione. */
    @FXML private TextField txtYear;
    
    /** Etichetta informativa inferiore destinata ai feedback operativi per l'utente. */
    @FXML private Label lblFeedback;

    /** Tabella principale per la renderizzazione delle tracce musicali. */
    @FXML private TableView<Track> tableTracks;
    
    /** Colonna per la visualizzazione e l'editing del titolo. */
    @FXML private TableColumn<Track, String> colTitle;
    
    /** Colonna per la visualizzazione e l'editing dell'autore. */
    @FXML private TableColumn<Track, String> colAuthor;
    
    /** Colonna per la visualizzazione e l'editing della durata. */
    @FXML private TableColumn<Track, Integer> colDuration;
    
    /** Colonna per la visualizzazione e l'editing del genere. */
    @FXML private TableColumn<Track, String> colGenre;
    
    /** Colonna per la visualizzazione e l'editing dell'anno. */
    @FXML private TableColumn<Track, Integer> colYear;

    /** Colonna per la visualizzazione dei tag associati alla traccia. */
    @FXML private TableColumn<Track, String> colTags;
    
    /** Contenitore grafico del modulo di inserimento tracce. */
    @FXML private VBox formAddTrack;

    /** Riga superiore del catalogo con form traccia e gestione tag affiancati. */
    @FXML private HBox hboxCatalogForms;
    
    /** Menu a tendina per la scelta della playlist a cui associare il brano. */
    @FXML private ComboBox<Playlist> dropdownPlaylists;
    
    /** Contenitore dei comandi di aggiunta rapida a una playlist. */
    @FXML private HBox hboxAddtoPlaylist;
    
    /** Pulsante contestuale per escludere una traccia dalla playlist visualizzata. */
    @FXML private Button btnRemoveFromPlaylist;

    /** Pulsante contestuale per riprodurre tutto il catalogo. */
    @FXML private Button btnPlayCatalog;

    /** Pannello per la gestione dei tag nel catalogo. */
    @FXML private VBox formTagManagement;

    /** Campo per il nome di un nuovo tag. */
    @FXML private TextField txtNewTag;

    /** Elenco di tutti i tag disponibili nel sistema. */
    @FXML private ComboBox<Tag> dropdownAllTags;

    /** Tag disponibili da assegnare alla traccia selezionata. */
    @FXML private ComboBox<Tag> dropdownAssignTag;

    /** Tag già associati alla traccia selezionata. */
    @FXML private ComboBox<Tag> dropdownTrackTags;

    /** Pulsante per assegnare un tag alla traccia selezionata. */
    @FXML private Button btnAssignTag;

    /** Pulsante per rimuovere un tag dalla traccia selezionata. */
    @FXML private Button btnRemoveTag;

    /** Flag discriminante per comprendere se la UI mostra il catalogo o una playlist. */
    private boolean playlistViewMode = false;
    
    /** Riferimento alla playlist attualmente visualizzata nella TableView. */
    private Playlist currentPlaylist;
    
    /** Lista interna contenente la copia speculare delle tracce della playlist attiva. */
    private java.util.List<Track> currentPlaylistTracks = new java.util.ArrayList<>();

    /**
     * Costruttore per Constructor Injection (Task T-63).
     *
     * @param facade L'istanza centralizzata dell'Application Facade.
     */
    public TrackController(MusicPlaylistManagerFacade facade) {
        this.facade = facade;
    }

    /**
     * Inizializza i componenti grafici della TableView, effettua il data-binding
     * ed esegue il caricamento dei dati di catalogo a startup.
     */
    @FXML
    private void initialize() {
        if (tableTracks != null) {
            tableTracks.setEditable(false);
        }

        initializeTableColumns();
        configureDropdownPlaylistsRendering();
        configureTagComboRendering();
        if (tableTracks != null) {
            configureTableToggleDeselection();
            configureTableSelectionListener();
        }

        if (this.facade != null) {
            loadCatalog();
            refreshTagCombos();
            if (dropdownPlaylists != null) {
                dropdownPlaylists.setItems(FXCollections.observableArrayList(this.facade.getAllPlaylists()));
            }
        }
    }

    /**
     * Associa le colonne della TableView ai campi dati del Domain Model (Track).
     * La modifica dei metadati avviene solo tramite il menu contestuale.
     */
    private void initializeTableColumns() {
        if (colTitle != null) {
            colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
            colTitle.setEditable(false);
        }
        if (colAuthor != null) {
            colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
            colAuthor.setEditable(false);
        }
        if (colDuration != null) {
            colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
            colDuration.setEditable(false);
        }
        if (colGenre != null) {
            colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
            colGenre.setEditable(false);
        }
        if (colYear != null) {
            colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
            colYear.setEditable(false);
        }
        configureTagsColumn();
        configureResponsiveColumnWidths();
    }

    /**
     * Configura la colonna dei tag con badge visivi per ogni etichetta associata alla traccia.
     */
    private void configureTagsColumn() {
        if (colTags == null) {
            return;
        }

        colTags.setEditable(false);
        colTags.setCellValueFactory(cellData -> {
            Track track = cellData.getValue();
            if (track == null || facade == null) {
                return new ReadOnlyObjectWrapper<>("");
            }
            return new ReadOnlyObjectWrapper<>(formatTagsForDisplay(facade.getTagsForTrack(track.getId())));
        });
        colTags.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Track track = getTableRow().getItem();
                FlowPane badges = createTagBadges(track);
                if (badges.getChildren().isEmpty()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(badges);
                }
            }
        });
    }

    private FlowPane createTagBadges(Track track) {
        FlowPane container = new FlowPane();
        container.setHgap(4);
        container.setVgap(4);

        if (track == null || facade == null) {
            return container;
        }

        for (Tag tag : facade.getTagsForTrack(track.getId())) {
            Label badge = new Label(tag.getName());
            badge.setStyle(
                    "-fx-background-color: #dbeafe;"
                            + "-fx-text-fill: #1e3a8a;"
                            + "-fx-padding: 2 8;"
                            + "-fx-background-radius: 10;"
                            + "-fx-font-size: 11px;");
            container.getChildren().add(badge);
        }
        return container;
    }

    private String formatTagsForDisplay(java.util.List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return tags.stream()
                .map(Tag::getName)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    /**
     * Apre una finestra di dialogo per modificare i metadati della traccia selezionata.
     * I tag non sono modificabili da questa finestra.
     */
    private void showEditTrackDialog(Track track) {
        if (facade == null || track == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifica traccia");
        dialog.setHeaderText("Modifica i metadati di \"" + track.getTitle() + "\"");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField editTitle = new TextField(track.getTitle());
        TextField editAuthor = new TextField(track.getAuthor());
        TextField editDuration = new TextField(String.valueOf(track.getDuration()));
        TextField editGenre = new TextField(track.getGenre());
        TextField editYear = new TextField(String.valueOf(track.getYear()));

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

        editTitle.setPrefWidth(260);
        editAuthor.setPrefWidth(260);
        editDuration.setPrefWidth(260);
        editGenre.setPrefWidth(260);
        editYear.setPrefWidth(260);

        dialog.getDialogPane().setContent(form);
        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return;
            }
            try {
                String title = toSentenceCase(editTitle.getText());
                String author = toSentenceCase(editAuthor.getText());
                String genre = toSentenceCase(editGenre.getText());
                int duration = Integer.parseInt(editDuration.getText().trim());
                int year = Integer.parseInt(editYear.getText().trim());
                validateDataInput(title, author, genre, duration, year);

                Track updatedTrack = new Track(track.getId(), title, author, duration, genre, year);
                facade.updateTrack(track.getId(), updatedTrack);
                selectedTrack = updatedTrack;
                labelFeedback("Traccia modificata con successo.", "#1f7a1f");
                loadCatalog();
                if (playlistViewMode && currentPlaylist != null) {
                    loadPlaylistTracks(currentPlaylist);
                }
                if (tableTracks != null) {
                    tableTracks.refresh();
                }
            } catch (NumberFormatException e) {
                labelFeedback("Durata e anno devono essere numeri validi.", "red");
            } catch (ValidationException | it.unisa.sad.playlistmanager.domain.exceptions.ValidationException e) {
                labelFeedback(e.getMessage(), "red");
            } catch (IllegalArgumentException e) {
                labelFeedback(e.getMessage(), "red");
            } catch (Exception e) {
                labelFeedback("Errore durante la modifica della traccia.", "red");
            }
        });
    }

    /**
     * Imposta il dimensionamento proporzionale e vincolato (Responsive) delle colonne.
     */
    private void configureResponsiveColumnWidths() {
        if (tableTracks == null || colTitle == null || colAuthor == null
                || colDuration == null || colGenre == null || colYear == null || colTags == null) {
            return;
        }
        tableTracks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final double weightTitle = 0.24;
        final double weightAuthor = 0.24;
        final double weightDuration = 0.10;
        final double weightGenre = 0.14;
        final double weightYear = 0.08;
        final double weightTags = 0.20;
        double scrollbarOffset = 15.0;

        colTitle.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightTitle));
        colAuthor.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightAuthor));
        colDuration.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightDuration));
        colGenre.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightGenre));
        colYear.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightYear));
        colTags.prefWidthProperty().bind(tableTracks.widthProperty().subtract(scrollbarOffset).multiply(weightTags));

        colTitle.setMinWidth(120);
        colAuthor.setMinWidth(110);
        colDuration.setMinWidth(70);
        colGenre.setMinWidth(90);
        colYear.setMinWidth(60);
        colTags.setMinWidth(100);
    }

    /**
     * Configura il rendering custom all'interno del menu a tendina delle playlist,
     * assicurando l'estrazione pulita della proprietà stringa del nome dell'oggetto.
     */
    private void configureDropdownPlaylistsRendering() {
        if (dropdownPlaylists == null) return;
        dropdownPlaylists.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });
        dropdownPlaylists.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Playlist item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "Seleziona una playlist" : item.getName());
            }
        });
    }

    /**
     * Configura il rendering dei menu a tendina che mostrano oggetti {@link Tag}.
     */
    private void configureTagComboRendering() {
        configureTagCombo(dropdownAllTags, "Seleziona un tag");
        configureTagCombo(dropdownAssignTag, "Tag da assegnare");
        configureTagCombo(dropdownTrackTags, "Tag da rimuovere");
    }

    private void configureTagCombo(ComboBox<Tag> comboBox, String emptyPrompt) {
        if (comboBox == null) {
            return;
        }
        comboBox.setCellFactory(lv -> new ListCell<>() {
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

    /**
     * Aggiorna gli elenchi dei tag globali e quelli legati alla traccia selezionata.
     */
    private void refreshTagCombos() {
        if (facade == null) {
            return;
        }

        List<Tag> allTags = facade.getAllTags();
        if (dropdownAllTags != null) {
            Tag selectedGlobalTag = dropdownAllTags.getSelectionModel().getSelectedItem();
            dropdownAllTags.setItems(FXCollections.observableArrayList(allTags));
            if (selectedGlobalTag != null) {
                allTags.stream()
                        .filter(tag -> tag.getId().equals(selectedGlobalTag.getId()))
                        .findFirst()
                        .ifPresentOrElse(
                                dropdownAllTags.getSelectionModel()::select,
                                () -> dropdownAllTags.getSelectionModel().clearSelection());
            }
        }

        if (selectedTrack == null) {
            if (dropdownAssignTag != null) {
                dropdownAssignTag.getItems().clear();
                dropdownAssignTag.setDisable(true);
            }
            if (dropdownTrackTags != null) {
                dropdownTrackTags.getItems().clear();
                dropdownTrackTags.setDisable(true);
            }
            if (btnAssignTag != null) {
                btnAssignTag.setDisable(true);
            }
            if (btnRemoveTag != null) {
                btnRemoveTag.setDisable(true);
            }
            return;
        }

        List<Tag> trackTags = facade.getTagsForTrack(selectedTrack.getId());
        Set<String> assignedIds = trackTags.stream().map(Tag::getId).collect(Collectors.toSet());
        List<Tag> availableTags = allTags.stream()
                .filter(tag -> !assignedIds.contains(tag.getId()))
                .toList();

        if (dropdownAssignTag != null) {
            dropdownAssignTag.setItems(FXCollections.observableArrayList(availableTags));
            dropdownAssignTag.getSelectionModel().clearSelection();
            dropdownAssignTag.setDisable(availableTags.isEmpty());
        }
        if (dropdownTrackTags != null) {
            dropdownTrackTags.setItems(FXCollections.observableArrayList(trackTags));
            dropdownTrackTags.getSelectionModel().clearSelection();
            dropdownTrackTags.setDisable(trackTags.isEmpty());
        }
        if (btnAssignTag != null) {
            btnAssignTag.setDisable(availableTags.isEmpty());
        }
        if (btnRemoveTag != null) {
            btnRemoveTag.setDisable(trackTags.isEmpty());
        }
    }

    @FXML
    private void handleCreateTag(ActionEvent event) {
        if (facade == null || txtNewTag == null) {
            return;
        }

        try {
            String tagName = txtNewTag.getText() != null ? txtNewTag.getText().trim() : "";
            Tag createdTag = facade.addTag(tagName);
            txtNewTag.clear();
            refreshTagCombos();
            if (dropdownAllTags != null) {
                dropdownAllTags.getSelectionModel().select(createdTag);
            }
            if (tableTracks != null) {
                tableTracks.refresh();
            }
            labelFeedback("Tag '" + createdTag.getName() + "' creato con successo.", "#1f7a1f");
        } catch (ValidationException e) {
            labelFeedback(e.getMessage(), "red");
        } catch (RuntimeException e) {
            labelFeedback("Errore durante la creazione del tag.", "red");
        }
    }

    @FXML
    private void handleDeleteTag(ActionEvent event) {
        if (facade == null || dropdownAllTags == null) {
            return;
        }

        Tag tagToDelete = dropdownAllTags.getSelectionModel().getSelectedItem();
        if (tagToDelete == null) {
            labelFeedback("Seleziona un tag da eliminare.", "red");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma eliminazione");
        alert.setHeaderText("Eliminare il tag '" + tagToDelete.getName() + "'?");
        alert.setContentText("Il tag verrà rimosso da tutte le tracce associate.");

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return;
            }
            try {
                facade.deleteTag(tagToDelete.getId());
                refreshTagCombos();
                if (tableTracks != null) {
                    tableTracks.refresh();
                }
                labelFeedback("Tag '" + tagToDelete.getName() + "' eliminato.", "#1f7a1f");
            } catch (ValidationException | TagNotFoundException e) {
                labelFeedback(e.getMessage(), "red");
            }
        });
    }

    @FXML
    private void handleAssignTag(ActionEvent event) {
        if (facade == null || selectedTrack == null || dropdownAssignTag == null) {
            labelFeedback("Seleziona una traccia e un tag da assegnare.", "red");
            return;
        }

        Tag tagToAssign = dropdownAssignTag.getSelectionModel().getSelectedItem();
        if (tagToAssign == null) {
            labelFeedback("Seleziona un tag da assegnare.", "red");
            return;
        }

        try {
            facade.assignTagToTrack(selectedTrack.getId(), tagToAssign.getId());
            refreshTagCombos();
            if (tableTracks != null) {
                tableTracks.refresh();
            }
            labelFeedback(
                    "Tag '" + tagToAssign.getName() + "' assegnato a '" + selectedTrack.getTitle() + "'.",
                    "#1f7a1f");
        } catch (ValidationException | TrackNotFoundException | TagNotFoundException e) {
            labelFeedback(e.getMessage(), "red");
        }
    }

    @FXML
    private void handleRemoveTagFromTrack(ActionEvent event) {
        if (facade == null || selectedTrack == null || dropdownTrackTags == null) {
            labelFeedback("Seleziona una traccia e un tag da rimuovere.", "red");
            return;
        }

        Tag tagToRemove = dropdownTrackTags.getSelectionModel().getSelectedItem();
        if (tagToRemove == null) {
            labelFeedback("Seleziona un tag da rimuovere.", "red");
            return;
        }

        try {
            facade.removeTagFromTrack(selectedTrack.getId(), tagToRemove.getId());
            refreshTagCombos();
            if (tableTracks != null) {
                tableTracks.refresh();
            }
            labelFeedback(
                    "Tag '" + tagToRemove.getName() + "' rimosso da '" + selectedTrack.getTitle() + "'.",
                    "#1f7a1f");
        } catch (ValidationException | TrackNotFoundException | TagNotFoundException e) {
            labelFeedback(e.getMessage(), "red");
        }
    }

    /**
     * Configura la riga della tabella iniettando un menu contestuale per il comando "Play",
     * "Modifica" e il nuovo comando di eliminazione dal catalogo globale (Task T-89).
     */
    private void configureTableToggleDeselection() {
        tableTracks.setRowFactory(tv -> {
            final TableRow<Track> row = new TableRow<>();
            MenuItem playItem = new MenuItem("Play");
            MenuItem editItem = new MenuItem("Modifica"); 
            MenuItem deleteItem = new MenuItem("Elimina dal catalogo"); // Iniezione Task T-89
            ContextMenu contextMenu = new ContextMenu(playItem, editItem, deleteItem);
            
            playItem.setOnAction(event -> {
                Track track = row.getItem();
                if (track == null) return;
                tableTracks.getSelectionModel().select(track);
                selectedTrack = track;
                if (onTrackPlayRequestedHandler != null)
                    onTrackPlayRequestedHandler.accept(track);
            });

            editItem.setOnAction(event -> {
                Track track = row.getItem();
                if (track == null) return;
                tableTracks.getSelectionModel().select(track);
                selectedTrack = track;
                showEditTrackDialog(track);
            });

            // TASK T-89 e T-90: Finestra di dialogo di conferma ed eliminazione traccia
            deleteItem.setOnAction(event -> {
                Track track = row.getItem();
                if (track == null) return;

                // T-89: Configurazione e apertura del dialogo di conferma nativo
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Conferma Eliminazione");
                alert.setHeaderText("Eliminare la traccia dal catalogo?");
                alert.setContentText("L'operazione rimuoverà definitivamente la traccia '" + track.getTitle() + "' anche da tutte le playlist.");

                java.util.Optional<ButtonType> confirmationResult = alert.showAndWait();
                if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.OK) {
                    try {
                        facade.deleteTrack(track.getId());
                        labelFeedback("Traccia eliminata con successo.", "green");
                        
                        // T-90: Aggiornamento reattivo in tempo reale del catalogo e della playlist corrente
                        loadCatalog();
                        if (playlistViewMode && currentPlaylist != null) {
                            loadPlaylistTracks(currentPlaylist);
                        }
                        tableTracks.getSelectionModel().clearSelection();
                        selectedTrack = null;
                    } catch (Exception e) {
                        labelFeedback("Errore durante l'eliminazione della traccia.", "red");
                    }
                } else {
                    labelFeedback("Eliminazione annullata.", "#0066cc"); // Scenario 2
                }
            });

            row.contextMenuProperty().bind(
                Bindings.when(row.emptyProperty())
                        .then((ContextMenu) null)
                        .otherwise(contextMenu)
            );

            row.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1 && row.isEmpty()) {
                    tableTracks.getSelectionModel().clearSelection();
                    selectedTrack = null;
                    if (hboxAddtoPlaylist != null) {
                        hboxAddtoPlaylist.setVisible(false);
                        hboxAddtoPlaylist.setManaged(false);
                    }
                }
            });
            return row;
        });
    }

    /**
     * Gestisce i mutamenti di stato della riga selezionata, allineando dinamicamente
     * la visibilità dei pannelli d'azione contestuali (Barra d'aggiunta vs Rimozione).
     */
    private void configureTableSelectionListener() {
        tableTracks.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedTrack = newSel;
                if (onTrackSelectedHandler != null) {
                    onTrackSelectedHandler.accept(newSel);
                }
                if (facade != null && dropdownPlaylists != null) {
                    dropdownPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists()));
                }
                if (playlistViewMode) {
                    if (btnRemoveFromPlaylist != null) {
                        btnRemoveFromPlaylist.setVisible(true);
                        btnRemoveFromPlaylist.setManaged(true);
                    }
                    if (hboxAddtoPlaylist != null) {
                        hboxAddtoPlaylist.setVisible(false);
                        hboxAddtoPlaylist.setManaged(false);
                    }
                } else {
                    if (btnRemoveFromPlaylist != null) {
                        btnRemoveFromPlaylist.setVisible(false);
                        btnRemoveFromPlaylist.setManaged(false);
                    }
                    if (hboxAddtoPlaylist != null) {
                        hboxAddtoPlaylist.setVisible(true);
                        hboxAddtoPlaylist.setManaged(true);
                    }
                    if (dropdownPlaylists != null) {
                        dropdownPlaylists.getSelectionModel().clearSelection();
                        dropdownPlaylists.setValue(null);
                        dropdownPlaylists.setPromptText("Seleziona una playlist");
                    }
                }
            } else {
                selectedTrack = null;
                if (hboxAddtoPlaylist != null) {
                    hboxAddtoPlaylist.setVisible(false);
                    hboxAddtoPlaylist.setManaged(false);
                }
                if (btnRemoveFromPlaylist != null) {
                    btnRemoveFromPlaylist.setVisible(false);
                    btnRemoveFromPlaylist.setManaged(false);
                }
            }
            refreshTagCombos();
            tableTracks.refresh();
        });
    }

    /**
     * Registra il consumatore delegato a catturare gli eventi di selezione traccia.
     * * @param handler Routine di callback esposta dal coordinatore principale.
     */
    public void setOnTrackSelected(Consumer<Track> handler) {
        this.onTrackSelectedHandler = handler;
    }

    /**
     * Registra il consumatore delegato a catturare gli eventi di riproduzione forzata.
     *
     * @param handler Routine di callback esposta dal coordinatore principale.
     */
    public void setOnTrackPlayRequested(Consumer<Track> handler){
        this.onTrackPlayRequestedHandler = handler;
    }

    @FXML
    private void handlePlayCatalog(ActionEvent event) {
        if (facade == null) {
            labelFeedback("Errore: Facade non inizializzata.", "red");
            return;
        }
        try {
            facade.playCatalog();
            labelFeedback("Riproduzione del catalogo globale avviata.", "green");
        } catch (Exception e) {
            labelFeedback("Errore durante l'avvio del catalogo: " + e.getMessage(), "red");
        }
    }

    /**
     * Intercetta la richiesta di inserimento di una nuova traccia nel sistema.
     * Valida sintatticamente i campi e delega la persistenza all'Application Facade.
     *
     * @param event Evento di click associato al pulsante d'aggiunta.
     */
    @FXML
    private void handleTrackAddition(ActionEvent event) {
        lblFeedback.setText("");
        if (facade == null) {
            labelFeedback("Errore interno: facade non inizializzata.", "red");
            return;
        }
        try {
            String title = toSentenceCase(txtTitle.getText());
            String author = toSentenceCase(txtAuthor.getText());
            String genre = toSentenceCase(txtGenre.getText());
            int duration = Integer.parseInt(txtDuration.getText().trim());
            int year = Integer.parseInt(txtYear.getText().trim());
            validateDataInput(title, author, genre, duration, year);

            Track newTrack = facade.addTrack(title, author, duration, genre, year);
            if (newTrack != null) {
                tableTracks.getSelectionModel().clearSelection();
                clearForm();
                labelFeedback("Traccia aggiunta con successo.", "green");
                loadCatalog();
            }
        } catch (NumberFormatException e) {
            labelFeedback("Durata e anno devono essere numeri validi.", "red");
        } catch (ValidationException | it.unisa.sad.playlistmanager.domain.exceptions.ValidationException e) {
            labelFeedback(e.getMessage(), "red");
        } catch (IllegalArgumentException e) {
            labelFeedback(e.getMessage(), "red");
        } catch (Exception e) {
            labelFeedback("Errore durante il salvataggio della traccia.", "red");
            e.printStackTrace();
        }
    }

    /**
     * Valida la correttezza dei dati di input della traccia prima dell'invio ai servizi.
     */
    private void validateDataInput(String title, String author, String genre, int duration, int year) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Errore di inserimento titolo: il titolo della traccia è obbligatorio.");
        if (author == null || author.trim().isEmpty()) throw new IllegalArgumentException("Errore di inserimento autore: l'autore della traccia è obbligatorio.");
        if (genre == null || genre.trim().isEmpty()) throw new IllegalArgumentException("Errore di inserimento genere: il genere della traccia è obbligatorio.");
        if (duration <= 0) throw new IllegalArgumentException("Errore di inserimento durata: la durata della traccia deve essere maggiore di zero.");
        if (year <= 0 || year > java.time.Year.now().getValue()) throw new IllegalArgumentException("Errore di inserimento anno: l'anno della traccia non è valido.");
    }

    /**
     * Lega la traccia selezionata alla playlist bersaglio scelta all'interno del menu a tendina.
     *
     * @param event Evento di click del bottone di salvataggio.
     */
    @FXML
    private void handleSaveAddPlaylist(ActionEvent event) {
        if (selectedTrack != null && dropdownPlaylists != null && dropdownPlaylists.getSelectionModel().getSelectedItem() != null) {
            Playlist targetPlaylist = dropdownPlaylists.getSelectionModel().getSelectedItem();
            try {
                facade.addTrackToPlaylist(targetPlaylist.getId(), selectedTrack.getId());
            } catch (ValidationException | PlaylistNotFoundException | TrackNotFoundException | IllegalArgumentException e) {
                labelFeedback(e.getMessage(), "red");
                return;
            }

            labelFeedback("Traccia '" + selectedTrack.getTitle() + "' aggiunta alla playlist '" + targetPlaylist.getName() + "' con successo.", "green");
            if (playlistViewMode && currentPlaylist != null && currentPlaylist.getId().equals(targetPlaylist.getId())) {
                loadPlaylistTracks(currentPlaylist);
            }
            dropdownPlaylists.setItems(FXCollections.observableArrayList(facade.getAllPlaylists()));
            if (hboxAddtoPlaylist != null) {
                hboxAddtoPlaylist.setVisible(false);
                hboxAddtoPlaylist.setManaged(false);
            }
        } else {
            labelFeedback("Seleziona una playlist valida dal menu a tendina.", "red");
        }
    }

    /**
     * Intercetta la sottomissione del pulsante grafico per rimuovere il brano dalla playlist corrente.
     * * @param event Evento di click associato al pulsante.
     */
    @FXML
    private void handleRemoveFromPlaylist(ActionEvent event) {
        removeSelectedTrackFromCurrentPlaylist();
    }

    /**
     * Rimuove la traccia selezionata dalla playlist corrente interrogando la Facade.
     */
    private void removeSelectedTrackFromCurrentPlaylist() {
        if (!playlistViewMode || currentPlaylist == null || selectedTrack == null) {
            labelFeedback("Seleziona una traccia della playlist da rimuovere.", "red");
            return;
        }

        try {
            facade.removeTrackFromPlaylist(currentPlaylist.getId(), selectedTrack.getId());
        } catch (ValidationException | PlaylistNotFoundException | TrackNotFoundException | IllegalArgumentException e) {
            labelFeedback(e.getMessage(), "red");
            return;
        }
        loadPlaylistTracks(currentPlaylist);
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }

        if (lblFeedback != null) {
            if (currentPlaylistTracks.isEmpty()) {
                labelFeedback("Playlist '" + currentPlaylist.getName() + "' vuota.", "#1f7a1f");
            } else {
                labelFeedback("Traccia rimossa da '" + currentPlaylist.getName() + "'.", "#1f7a1f");
            }
        }
    }

    /**
     * Interroga la Facade per caricare tutte le tracce presenti nel sistema all'interno della TableView.
     */
    private void loadCatalog() {
        if (facade == null || tableTracks == null) return;
        tableTracks.setItems(FXCollections.observableArrayList(facade.getAllTracks()));
        tableTracks.setPlaceholder(new Label("Catalogo vuoto. Aggiungi una traccia."));
        tableTracks.refresh();
    }

    /**
     * Commuta lo stato visivo abilitando il form di immissione dati per la visualizzazione catalogo.
     */
    public void showCatalogView() {
        playlistViewMode = false;
        currentPlaylist = null;
        currentPlaylistTracks.clear();
        
        if (btnPlayCatalog != null) {
            btnPlayCatalog.setVisible(true);
            btnPlayCatalog.setManaged(true);
        }
        if (hboxCatalogForms != null) {
            hboxCatalogForms.setVisible(true);
            hboxCatalogForms.setManaged(true);
        }
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }
        loadCatalog();
        refreshTagCombos();
        if (tableTracks != null) tableTracks.refresh();
        if (lblFeedback != null) {
            if (tableTracks != null && tableTracks.getItems() != null && tableTracks.getItems().isEmpty()) {
                labelFeedback("Catalogo vuoto. Aggiungi una traccia.", "#b0413e");
            } else {
                labelFeedback("Visualizzazione catalogo completo.", "#1f7a1f");
            }
        }
    }

    /**
     * Annulla lo stato dei dati e svuota la tabella quando non vi è selezione attiva.
     */
    public void clearPlaylistView() {
        playlistViewMode = false;
        currentPlaylist = null;
        currentPlaylistTracks.clear();

        if (btnPlayCatalog != null) {
            btnPlayCatalog.setVisible(false);
            btnPlayCatalog.setManaged(false);
        }
        if (tableTracks != null) {
            tableTracks.getSelectionModel().clearSelection();
            tableTracks.setItems(FXCollections.observableArrayList());
            tableTracks.refresh();
        }
        if (hboxAddtoPlaylist != null) {
            hboxAddtoPlaylist.setVisible(false);
            hboxAddtoPlaylist.setManaged(false);
        }
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }
        if (hboxCatalogForms != null) {
            hboxCatalogForms.setVisible(false);
            hboxCatalogForms.setManaged(false);
        }
        labelFeedback("Nessuna playlist selezionata.", "#1f7a1f");
    }

    /**
     * Predispone la tabella per accogliere esclusivamente i brani della playlist sotto analisi.
     *
     * @param playlist L'istanza di Playlist da ispezionare.
     */
    public void displayPlaylistTracks(Playlist playlist) {
        if (tableTracks == null || playlist == null) return;
        playlistViewMode = true;
        currentPlaylist = playlist;
        currentPlaylistTracks = new java.util.ArrayList<>(facade != null ? facade.getTracksForPlaylist(playlist.getId()) : playlist.getTracks());
        tableTracks.setPlaceholder(new Label("Questa playlist non contiene tracce."));
        
        
        if (btnPlayCatalog != null) {
            btnPlayCatalog.setVisible(false);
            btnPlayCatalog.setManaged(false);
        }
        if (hboxCatalogForms != null) {
            hboxCatalogForms.setVisible(false);
            hboxCatalogForms.setManaged(false);
        }
        if (hboxAddtoPlaylist != null) {
            hboxAddtoPlaylist.setVisible(false);
            hboxAddtoPlaylist.setManaged(false);
        }
        if (btnRemoveFromPlaylist != null) {
            btnRemoveFromPlaylist.setVisible(false);
            btnRemoveFromPlaylist.setManaged(false);
        }
        loadPlaylistTracks(playlist);
        
        Platform.runLater(() -> {
            tableTracks.applyCss();
            tableTracks.layout();
            tableTracks.refresh();
        });
        labelFeedback("Contenuto playlist: " + playlist.getName(), "#0066cc");
    }

    /**
     * Esegue il recupero sincrono dei brani di una playlist aggiornando gli elementi della TableView.
     * * @param playlist L'istanza di Playlist da aggiornare a livello grafico.
     */
    public void loadPlaylistTracks(Playlist playlist) {
        if (tableTracks == null || playlist == null || facade == null) return;
        try {
            currentPlaylistTracks = facade.getTracksForPlaylist(playlist.getId());
        } catch (ValidationException | PlaylistNotFoundException | IllegalArgumentException e) {
            labelFeedback(e.getMessage(), "red");
            currentPlaylistTracks = new java.util.ArrayList<>();
        }
        tableTracks.setItems(FXCollections.observableArrayList(currentPlaylistTracks));
        tableTracks.getSelectionModel().clearSelection();
        tableTracks.refresh();
    }

    /**
     * Svuota i campi testuali del modulo di inserimento.
     */
    public void clearForm() {
        txtTitle.clear();
        txtAuthor.clear();
        txtDuration.clear();
        txtGenre.clear();
        txtYear.clear();
    }

    /**
     * Uniforma la formattazione di una stringa impostando in maiuscolo il primo carattere.
     */
    private String toSentenceCase(String value) {
        if (value == null) return "";
        String trimmed = value.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) return "";
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }

    /**
     * Modifica lo stile cromatico e il testo della etichetta informativa inferiore.
     * * @param text  Il testo descrittivo da stampare.
     * @param color La specifica CSS per la colorazione del font.
     */
    public void labelFeedback(String text, String color) {
        if (lblFeedback != null) {
            lblFeedback.setStyle("-fx-text-fill: " + color + ";");
            lblFeedback.setText(text);
        }
    }

    /**
     * TASK T3-12: Rinfresca in tempo reale lo stato dei dati visibili (Catalogo o Playlist)
     * per riallineare la Tabella a seguito di operazioni mutative o comandi di Undo.
     */
    public void refresh() {
        if (playlistViewMode && currentPlaylist != null) {
            loadPlaylistTracks(currentPlaylist);
        } else {
            loadCatalog();
        }
    }
}