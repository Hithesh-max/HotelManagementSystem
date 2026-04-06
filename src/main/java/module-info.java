module com.example.hotelmanagement {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.hotelmanagement to javafx.fxml;
    exports com.example.hotelmanagement;
}