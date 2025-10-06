package org.example.id3tageditor;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public class ID3TagService {

    public ID3Tag readTags(File file) throws Exception {
        ID3Tag id3Tag = new ID3Tag();
        id3Tag.setFilePath(file.getAbsolutePath());

        AudioFile audioFile = AudioFileIO.read(file);
        Tag audioTag = audioFile.getTag();
        AudioHeader audioHeader = audioFile.getAudioHeader();


        if (audioTag != null) {
            id3Tag.setTitle(audioTag.getFirst(FieldKey.TITLE));
            id3Tag.setArtist(audioTag.getFirst(FieldKey.ARTIST));
            id3Tag.setAlbum(audioTag.getFirst(FieldKey.ALBUM));
            id3Tag.setYear(audioTag.getFirst(FieldKey.YEAR));
            id3Tag.setGenre(audioTag.getFirst(FieldKey.GENRE));
            id3Tag.setComment(audioTag.getFirst(FieldKey.COMMENT));
            id3Tag.setTrack(audioTag.getFirst(FieldKey.TRACK));
            id3Tag.setTrackTotal(audioTag.getFirst(FieldKey.TRACK_TOTAL));
            id3Tag.setDisk(audioTag.getFirst(FieldKey.DISC_NO));
            id3Tag.setDiskTotal(audioTag.getFirst(FieldKey.DISC_TOTAL));
            id3Tag.setLyrics(audioTag.getFirst(FieldKey.LYRICS));
            id3Tag.setEncoder(audioTag.getFirst(FieldKey.ENCODER));


            id3Tag.setComposer(audioTag.getFirst(FieldKey.COMPOSER));
            id3Tag.setLyricist(audioTag.getFirst(FieldKey.LYRICIST));
            id3Tag.setPublisher(audioTag.getFirst(FieldKey.RECORD_LABEL));
            id3Tag.setCopyright(audioTag.getFirst(FieldKey.COPYRIGHT));
            id3Tag.setBpm(audioTag.getFirst(FieldKey.BPM));
            id3Tag.setIsrc(audioTag.getFirst(FieldKey.ISRC));



            Artwork artwork = audioTag.getFirstArtwork();
            if (artwork != null) {
                byte[] imageData = artwork.getBinaryData();
                id3Tag.setArtwork(new Image(new ByteArrayInputStream(imageData)));
                id3Tag.setArtworkBinaryData(imageData);
            }
        }


        id3Tag.setFileSize(String.format(Locale.US, "%.2f MiB", file.length() / (1024.0 * 1024.0)));
        id3Tag.setDuration(formatTrackLength(audioHeader.getTrackLength()));
        id3Tag.setBitrate(audioHeader.getBitRate() + " kbps");
        id3Tag.setMimeType(audioHeader.getFormat());


        return id3Tag;
    }

    public void saveTags(ID3Tag tag, File outputFile) throws Exception {
        File inputFile = new File(tag.getFilePath());

        if (!inputFile.getCanonicalPath().equals(outputFile.getCanonicalPath())) {
            Files.copy(inputFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        AudioFile audioFile = AudioFileIO.read(outputFile);
        Tag audioTag = audioFile.getTagOrCreateAndSetDefault();

        java.util.function.BiConsumer<FieldKey, String> updateOrDeleteField = (key, value) -> {
            try {
                if (value != null && !value.trim().isEmpty()) {
                    audioTag.setField(key, value);
                } else {
                    audioTag.deleteField(key);
                }
            } catch (Exception e) {
                System.err.println("Could not process field " + key + ": " + e.getMessage());
            }
        };

        updateOrDeleteField.accept(FieldKey.TITLE, tag.getTitle());
        updateOrDeleteField.accept(FieldKey.ARTIST, tag.getArtist());
        updateOrDeleteField.accept(FieldKey.ALBUM, tag.getAlbum());
        updateOrDeleteField.accept(FieldKey.YEAR, tag.getYear());
        updateOrDeleteField.accept(FieldKey.GENRE, tag.getGenre());
        updateOrDeleteField.accept(FieldKey.COMMENT, tag.getComment());
        updateOrDeleteField.accept(FieldKey.TRACK, tag.getTrack());
        updateOrDeleteField.accept(FieldKey.TRACK_TOTAL, tag.getTrackTotal());
        updateOrDeleteField.accept(FieldKey.DISC_NO, tag.getDisk());
        updateOrDeleteField.accept(FieldKey.DISC_TOTAL, tag.getDiskTotal());
        updateOrDeleteField.accept(FieldKey.LYRICS, tag.getLyrics());
        updateOrDeleteField.accept(FieldKey.ENCODER, tag.getEncoder());


        updateOrDeleteField.accept(FieldKey.COMPOSER, tag.getComposer());
        updateOrDeleteField.accept(FieldKey.LYRICIST, tag.getLyricist());
        updateOrDeleteField.accept(FieldKey.RECORD_LABEL, tag.getPublisher());
        updateOrDeleteField.accept(FieldKey.COPYRIGHT, tag.getCopyright());
        updateOrDeleteField.accept(FieldKey.BPM, tag.getBpm());
        updateOrDeleteField.accept(FieldKey.ISRC, tag.getIsrc());

        if (tag.getArtworkBinaryData() != null && tag.getArtworkBinaryData().length > 0) {
            Artwork newArtwork = ArtworkFactory.getNew();
            newArtwork.setBinaryData(tag.getArtworkBinaryData());
            newArtwork.setMimeType("image/png");
            audioTag.deleteArtworkField();
            audioTag.setField(newArtwork);
        } else {
            audioTag.deleteArtworkField();
        }

        audioFile.commit();
    }

    public Image loadImageFromFile(File imageFile) throws IOException {
        return new Image(imageFile.toURI().toString());
    }

    public byte[] imageToByteArray(Image image, String format) throws IOException {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        ImageIO.write(bImage, format, s);
        return s.toByteArray();
    }

    private String formatTrackLength(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d min %d s", minutes, remainingSeconds);
    }
}