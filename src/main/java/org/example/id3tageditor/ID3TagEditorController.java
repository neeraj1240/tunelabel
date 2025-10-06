package org.example.id3tageditor;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import java.util.function.Function;

public class ID3TagEditorController {


    @FXML private Button saveButton, saveAsButton;
    @FXML private ImageView artworkImageView;
    @FXML private TextField titleField, artistField, albumField, yearField, genreField, trackField, trackTotalField, diskField, diskTotalField, commentField;
    @FXML private TextArea lyricsArea;
    @FXML private Label statusLabel, sizeLabel, durationLabel, bitrateLabel, mimeTypeLabel, encoderLabel;


    @FXML private TextField composerField, lyricistField, publisherField, copyrightField, bpmField, isrcField;



    @FXML private TableView<ID3Tag> filesTableView;
    @FXML private TableColumn<ID3Tag, String> fileNameColumn, titleColumn, artistColumn, albumColumn, trackColumn, yearColumn, genreColumn;


    private final ObservableList<ID3Tag> loadedTags = FXCollections.observableArrayList();
    private final ID3TagService tagService = new ID3TagService();
    private Stage primaryStage;
    private boolean artworkChanged = false;
    private final Image defaultArtwork = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/no-cover-art.png")));


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        setupTableView();
        setupTagFieldListeners();
        saveAsButton.disableProperty().bind(
                Bindings.size(filesTableView.getSelectionModel().getSelectedItems()).isNotEqualTo(1)
        );
    }

    private void setupTableView() {
        filesTableView.setItems(loadedTags);
        filesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
        trackColumn.setCellValueFactory(new PropertyValueFactory<>("track"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));

        filesTableView.setRowFactory(tv -> {
            TableRow<ID3Tag> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem removeItem = new MenuItem("Remove Selected");
            removeItem.setOnAction(event -> {
                List<ID3Tag> selectedItems = filesTableView.getSelectionModel().getSelectedItems();
                loadedTags.removeAll(selectedItems);
                statusLabel.setText(selectedItems.size() + " file(s) removed.");
                filesTableView.getSelectionModel().clearSelection();
            });

            MenuItem renameItem = new MenuItem("Rename File");
            renameItem.setOnAction(event -> handleRenameFile());
            renameItem.disableProperty().bind(
                    Bindings.size(filesTableView.getSelectionModel().getSelectedItems()).isNotEqualTo(1)
            );

            contextMenu.getItems().addAll(removeItem, renameItem);
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });

        filesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFieldsFromSelection();
            } else {
                clearAllFields();
            }
        });
    }

    private void setupTagFieldListeners() {
        List<Control> fields = Arrays.asList(titleField, artistField, albumField, yearField, genreField, trackField,
                trackTotalField, diskField, diskTotalField, commentField, lyricsArea,
                composerField, lyricistField, publisherField, copyrightField, bpmField, isrcField);

        fields.forEach(field -> {
            if (field instanceof TextInputControl) {
                ((TextInputControl) field).textProperty().addListener((obs, ov, nv) -> markAsChanged());
            }
        });
    }

    @FXML
    private void handleOpenFilesButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open MP3 Files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);
        if (selectedFiles != null) {
            loadFiles(selectedFiles);
        }
    }

    @FXML
    private void handleOpenFolderButton() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Folder Containing MP3 Files");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            File[] files = selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
            if (files != null) {
                loadFiles(Arrays.asList(files));
            }
        }
    }

    private void loadFiles(List<File> files) {
        for (File file : files) {
            try {
                if (loadedTags.stream().noneMatch(tag -> tag.getFilePath().equals(file.getAbsolutePath()))) {
                    ID3Tag tag = tagService.readTags(file);
                    loadedTags.add(tag);
                }
            } catch (Exception e) {
                showError("Error Loading File", "Could not read tags from: " + file.getName());
            }
        }
        statusLabel.setText(loadedTags.size() + " files loaded.");
        saveButton.setDisable(loadedTags.isEmpty());
    }

    @FXML
    private void handleSaveButton() {
        ObservableList<ID3Tag> selectedItems = filesTableView.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showError("Nothing to Save", "Please select one or more files to save.");
            return;
        }
        updateTagsFromFields(selectedItems);
        int savedCount = 0;
        for (ID3Tag tag : selectedItems) {
            try {
                tagService.saveTags(tag, new File(tag.getFilePath()));
                savedCount++;
            } catch (Exception e) {
                showError("Error Saving File", "Could not save tags for: " + tag.fileNameProperty().get());
                e.printStackTrace();
            }
        }
        statusLabel.setText(savedCount + " of " + selectedItems.size() + " files saved successfully!");
        filesTableView.refresh();
    }

    @FXML
    private void handleSaveAsButton() {
        ID3Tag selectedTag = filesTableView.getSelectionModel().getSelectedItem();
        if (selectedTag == null) {
            showError("Nothing to Save", "Please select a file to save as a copy.");
            return;
        }
        updateTagsFromFields(FXCollections.singletonObservableList(selectedTag));
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save MP3 File As...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));
        fileChooser.setInitialFileName(selectedTag.fileNameProperty().get());
        File newFile = fileChooser.showSaveDialog(primaryStage);
        if (newFile != null) {
            try {
                tagService.saveTags(selectedTag, newFile);
                statusLabel.setText("File saved successfully to: " + newFile.getName());
            } catch (Exception e) {
                showError("Error Saving File", "Could not save a copy of the file.");
                e.printStackTrace();
            }
        }
    }

    private void handleRenameFile() {
        ID3Tag selectedTag = filesTableView.getSelectionModel().getSelectedItem();
        if (selectedTag == null) return;
        String currentFileName = selectedTag.fileNameProperty().get();
        String currentNameWithoutExt = currentFileName.replaceFirst("[.][^.]+$", "");
        TextInputDialog dialog = new TextInputDialog(currentNameWithoutExt);
        dialog.setTitle("Rename File");
        dialog.setHeaderText("Enter a new name for the file '" + currentFileName + "'.");
        dialog.setContentText("New name (without extension):");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            try {
                if (newName.isEmpty() || newName.equals(currentNameWithoutExt)) return;
                Path sourcePath = Paths.get(selectedTag.getFilePath());
                String extension = sourcePath.toString().substring(sourcePath.toString().lastIndexOf("."));
                Path targetPath = sourcePath.resolveSibling(newName + extension);
                if (Files.exists(targetPath)) {
                    showError("Rename Error", "A file with that name already exists.");
                    return;
                }
                Files.move(sourcePath, targetPath);
                selectedTag.setFilePath(targetPath.toString());
                statusLabel.setText("File renamed to " + targetPath.getFileName());
                filesTableView.refresh();
            } catch (Exception e) {
                showError("Rename Error", "Could not rename the file. It may be in use.");
            }
        });
    }

    private void populateFieldsFromSelection() {
        ObservableList<ID3Tag> selectedItems = filesTableView.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            clearAllFields();
            return;
        }

        titleField.setText(getCommonValue(selectedItems, ID3Tag::getTitle));
        artistField.setText(getCommonValue(selectedItems, ID3Tag::getArtist));
        albumField.setText(getCommonValue(selectedItems, ID3Tag::getAlbum));
        yearField.setText(getCommonValue(selectedItems, ID3Tag::getYear));
        genreField.setText(getCommonValue(selectedItems, ID3Tag::getGenre));
        commentField.setText(getCommonValue(selectedItems, ID3Tag::getComment));
        trackField.setText(getCommonValue(selectedItems, ID3Tag::getTrack));
        trackTotalField.setText(getCommonValue(selectedItems, ID3Tag::getTrackTotal));
        diskField.setText(getCommonValue(selectedItems, ID3Tag::getDisk));
        diskTotalField.setText(getCommonValue(selectedItems, ID3Tag::getDiskTotal));
        lyricsArea.setText(getCommonValue(selectedItems, ID3Tag::getLyrics));

        composerField.setText(getCommonValue(selectedItems, ID3Tag::getComposer));
        lyricistField.setText(getCommonValue(selectedItems, ID3Tag::getLyricist));
        publisherField.setText(getCommonValue(selectedItems, ID3Tag::getPublisher));
        copyrightField.setText(getCommonValue(selectedItems, ID3Tag::getCopyright));
        bpmField.setText(getCommonValue(selectedItems, ID3Tag::getBpm));
        isrcField.setText(getCommonValue(selectedItems, ID3Tag::getIsrc));

        if (selectedItems.size() == 1) {
            ID3Tag tag = selectedItems.get(0);
            encoderLabel.setText(tag.getEncoder());
            sizeLabel.setText(tag.getFileSize());
            durationLabel.setText(tag.getDuration());
            bitrateLabel.setText(tag.getBitrate());
            mimeTypeLabel.setText(tag.getMimeType());
            artworkImageView.setImage(tag.getArtwork() != null ? tag.getArtwork() : defaultArtwork);
        } else {
            encoderLabel.setText("<various>");
            sizeLabel.setText("-");
            durationLabel.setText("-");
            bitrateLabel.setText("-");
            mimeTypeLabel.setText("-");
            artworkImageView.setImage(defaultArtwork);
        }
        artworkChanged = false;
    }

    private String getCommonValue(ObservableList<ID3Tag> tags, Function<ID3Tag, String> extractor) {
        if (tags == null || tags.isEmpty()) return "";
        String firstValue = extractor.apply(tags.get(0));
        for (int i = 1; i < tags.size(); i++) {
            if (!Objects.equals(firstValue, extractor.apply(tags.get(i)))) {
                return "";
            }
        }
        return firstValue == null ? "" : firstValue;
    }


    private void updateTagsFromFields(ObservableList<ID3Tag> tagsToUpdate) {
        for (ID3Tag tag : tagsToUpdate) {
            if (!titleField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getTitle).isEmpty()) {
                tag.setTitle(titleField.getText());
            }
            if (!artistField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getArtist).isEmpty()) {
                tag.setArtist(artistField.getText());
            }
            if (!albumField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getAlbum).isEmpty()) {
                tag.setAlbum(albumField.getText());
            }
            if (!yearField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getYear).isEmpty()) {
                tag.setYear(yearField.getText());
            }
            if (!genreField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getGenre).isEmpty()) {
                tag.setGenre(genreField.getText());
            }
            if (!commentField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getComment).isEmpty()) {
                tag.setComment(commentField.getText());
            }
            if (!trackField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getTrack).isEmpty()) {
                tag.setTrack(trackField.getText());
            }
            if (!trackTotalField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getTrackTotal).isEmpty()) {
                tag.setTrackTotal(trackTotalField.getText());
            }
            if (!diskField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getDisk).isEmpty()) {
                tag.setDisk(diskField.getText());
            }
            if (!diskTotalField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getDiskTotal).isEmpty()) {
                tag.setDiskTotal(diskTotalField.getText());
            }
            if (!lyricsArea.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getLyrics).isEmpty()) {
                tag.setLyrics(lyricsArea.getText());
            }
            if (!composerField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getComposer).isEmpty()) {
                tag.setComposer(composerField.getText());
            }
            if (!lyricistField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getLyricist).isEmpty()) {
                tag.setLyricist(lyricistField.getText());
            }
            if (!publisherField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getPublisher).isEmpty()) {
                tag.setPublisher(publisherField.getText());
            }
            if (!copyrightField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getCopyright).isEmpty()) {
                tag.setCopyright(copyrightField.getText());
            }
            if (!bpmField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getBpm).isEmpty()) {
                tag.setBpm(bpmField.getText());
            }
            if (!isrcField.getText().isEmpty() || getCommonValue(tagsToUpdate, ID3Tag::getIsrc).isEmpty()) {
                tag.setIsrc(isrcField.getText());
            }

            if (artworkChanged) {
                if (artworkImageView.getImage() != null && artworkImageView.getImage() != defaultArtwork) {
                    try {
                        byte[] imageData = tagService.imageToByteArray(artworkImageView.getImage(), "png");
                        tag.setArtworkBinaryData(imageData);
                        tag.setArtwork(artworkImageView.getImage());
                    } catch (Exception e) { /* ignore */ }
                } else {
                    tag.setArtworkBinaryData(null);
                    tag.setArtwork(null);
                }
            }
        }
    }

    @FXML
    private void handleAutoNumberTracks() {
        ObservableList<ID3Tag> selectedItems = filesTableView.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            showInfo("Auto-numbering", "Please select the files you wish to number.");
            return;
        }
        int totalTracks = selectedItems.size();
        for (int i = 0; i < totalTracks; i++) {
            ID3Tag tag = selectedItems.get(i);
            tag.setTrack(String.valueOf(i + 1));
            tag.setTrackTotal(String.valueOf(totalTracks));
        }
        statusLabel.setText("Track numbers assigned. Click Save to apply.");
        filesTableView.refresh();
        populateFieldsFromSelection();
    }

    @FXML
    private void handleChangeArtworkButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Album Artwork");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                Image newArtwork = tagService.loadImageFromFile(selectedFile);
                artworkImageView.setImage(newArtwork);
                artworkChanged = true;
                markAsChanged();
            } catch (Exception e) {
                showError("Artwork Error", "Could not load the selected image.");
            }
        }
    }

    @FXML
    private void handleRemoveArtworkButton() {
        artworkImageView.setImage(defaultArtwork);
        artworkChanged = true;
        markAsChanged();
    }

    private void clearAllFields() {
        titleField.clear();
        artistField.clear();
        albumField.clear();
        yearField.clear();
        genreField.clear();
        trackField.clear();
        trackTotalField.clear();
        diskField.clear();
        diskTotalField.clear();
        commentField.clear();
        lyricsArea.clear();

        composerField.clear();
        lyricistField.clear();
        publisherField.clear();
        copyrightField.clear();
        bpmField.clear();
        isrcField.clear();

        artworkImageView.setImage(defaultArtwork);
        encoderLabel.setText("-");
        sizeLabel.setText("-");
        durationLabel.setText("-");
        bitrateLabel.setText("-");
        mimeTypeLabel.setText("-");
    }

    private void markAsChanged() {
        if (!loadedTags.isEmpty()) {
            statusLabel.setText("Unsaved changes...");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}