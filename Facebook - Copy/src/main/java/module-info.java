module com.example.facebook {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jdk.jdi;


    opens com.example.facebook to javafx.fxml;
    exports com.example.facebook;
}