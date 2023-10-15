module com.games.jezzball.games {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires com.google.common;

    opens com.games.jezzball.games to javafx.fxml;
    exports com.games.jezzball.games;
}