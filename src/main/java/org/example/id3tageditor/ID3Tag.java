package org.example.id3tageditor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

import java.io.File;

public class ID3Tag {


    private final StringProperty filePath = new SimpleStringProperty();
    private final StringProperty fileName = new SimpleStringProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty artist = new SimpleStringProperty();
    private final StringProperty album = new SimpleStringProperty();
    private final StringProperty year = new SimpleStringProperty();
    private final StringProperty genre = new SimpleStringProperty();
    private final StringProperty comment = new SimpleStringProperty();
    private final StringProperty track = new SimpleStringProperty();
    private final StringProperty trackTotal = new SimpleStringProperty();
    private final StringProperty disk = new SimpleStringProperty();
    private final StringProperty diskTotal = new SimpleStringProperty();
    private final StringProperty lyrics = new SimpleStringProperty();
    private final StringProperty encoder = new SimpleStringProperty();


    private final StringProperty composer = new SimpleStringProperty();
    private final StringProperty lyricist = new SimpleStringProperty();
    private final StringProperty publisher = new SimpleStringProperty();
    private final StringProperty copyright = new SimpleStringProperty();
    private final StringProperty bpm = new SimpleStringProperty();
    private final StringProperty isrc = new SimpleStringProperty();



    private final StringProperty fileSize = new SimpleStringProperty();
    private final StringProperty duration = new SimpleStringProperty();
    private final StringProperty bitrate = new SimpleStringProperty();
    private final StringProperty mimeType = new SimpleStringProperty();

    private final ObjectProperty<Image> artwork = new SimpleObjectProperty<>();
    private byte[] artworkBinaryData;


    public StringProperty fileNameProperty() { return fileName; }
    public StringProperty titleProperty() { return title; }
    public StringProperty artistProperty() { return artist; }
    public StringProperty albumProperty() { return album; }
    public StringProperty trackProperty() { return track; }
    public StringProperty yearProperty() { return year; }
    public StringProperty genreProperty() { return genre; }



    public String getFilePath() { return filePath.get(); }
    public void setFilePath(String filePath) {
        this.filePath.set(filePath);
        if (filePath != null) {
            this.fileName.set(new File(filePath).getName());
        } else {
            this.fileName.set("");
        }
    }

    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }

    public String getArtist() { return artist.get(); }
    public void setArtist(String artist) { this.artist.set(artist); }

    public String getAlbum() { return album.get(); }
    public void setAlbum(String album) { this.album.set(album); }

    public String getYear() { return year.get(); }
    public void setYear(String year) { this.year.set(year); }

    public String getGenre() { return genre.get(); }
    public void setGenre(String genre) { this.genre.set(genre); }

    public String getComment() { return comment.get(); }
    public void setComment(String comment) { this.comment.set(comment); }

    public String getTrack() { return track.get(); }
    public void setTrack(String track) { this.track.set(track); }

    public String getTrackTotal() { return trackTotal.get(); }
    public void setTrackTotal(String trackTotal) { this.trackTotal.set(trackTotal); }

    public String getDisk() { return disk.get(); }
    public void setDisk(String disk) { this.disk.set(disk); }

    public String getDiskTotal() { return diskTotal.get(); }
    public void setDiskTotal(String diskTotal) { this.diskTotal.set(diskTotal); }

    public String getLyrics() { return lyrics.get(); }
    public void setLyrics(String lyrics) { this.lyrics.set(lyrics); }

    public String getEncoder() { return encoder.get(); }
    public void setEncoder(String encoder) { this.encoder.set(encoder); }

    public String getFileSize() { return fileSize.get(); }
    public void setFileSize(String fileSize) { this.fileSize.set(fileSize); }

    public String getDuration() { return duration.get(); }
    public void setDuration(String duration) { this.duration.set(duration); }

    public String getBitrate() { return bitrate.get(); }
    public void setBitrate(String bitrate) { this.bitrate.set(bitrate); }

    public String getMimeType() { return mimeType.get(); }
    public void setMimeType(String mimeType) { this.mimeType.set(mimeType); }

    public Image getArtwork() { return artwork.get(); }
    public void setArtwork(Image artwork) { this.artwork.set(artwork); }

    public byte[] getArtworkBinaryData() { return artworkBinaryData; }
    public void setArtworkBinaryData(byte[] artworkBinaryData) { this.artworkBinaryData = artworkBinaryData; }


    public String getComposer() { return composer.get(); }
    public void setComposer(String composer) { this.composer.set(composer); }

    public String getLyricist() { return lyricist.get(); }
    public void setLyricist(String lyricist) { this.lyricist.set(lyricist); }

    public String getPublisher() { return publisher.get(); }
    public void setPublisher(String publisher) { this.publisher.set(publisher); }

    public String getCopyright() { return copyright.get(); }
    public void setCopyright(String copyright) { this.copyright.set(copyright); }

    public String getBpm() { return bpm.get(); }
    public void setBpm(String bpm) { this.bpm.set(bpm); }

    public String getIsrc() { return isrc.get(); }
    public void setIsrc(String isrc) { this.isrc.set(isrc); }
}