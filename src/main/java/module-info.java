module MediaPlayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.xml;
    requires commons.io;
    requires commons.codec;
    requires org.apache.commons.lang3;
    requires jackson.core;
    requires jackson.databind;
    requires jackson.annotations;
    requires java.sql;
    requires java.desktop;

    opens mediaplayer to javafx.fxml;
    exports mediaplayer;
}