module org.example.id3tageditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires jaudiotagger;
    requires javafx.swing;

    opens org.example.id3tageditor to javafx.fxml;
    exports org.example.id3tageditor;
}