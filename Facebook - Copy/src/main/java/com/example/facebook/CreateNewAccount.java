package com.example.facebook;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.sql.*;

public class CreateNewAccount {

    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private ChoiceBox<String> month;
    @FXML
    private ChoiceBox<Integer> day;
    @FXML
    private ChoiceBox<Integer> year;
    @FXML
    private RadioButton femaleButton;
    @FXML
    private RadioButton maleButton;
    @FXML
    private TextField mobileNumberOrEmail;
    @FXML
    private PasswordField password;
    @FXML
    private Button signUpButton;
    @FXML
    private Label label;

    public static String loggedInEmailOrMobile;

    @FXML
    private void SignUp(ActionEvent actionEvent) {
        String firstName1 = firstName.getText();
        String lastName1 = lastName.getText();
        String month1 = month.getValue();
        Integer day1 = day.getValue();
        Integer year1 = year.getValue();
        String gender1 = "";

        if (femaleButton.isSelected())
        {
            gender1 = femaleButton.getText();
        }
        else if (maleButton.isSelected()) {
            gender1 = maleButton.getText();
        }

        String mobileNumberOrEmail1 = mobileNumberOrEmail.getText();
        String password1 = password.getText();

        if (firstName1.isBlank() || lastName1.isBlank() || month1 == null || day1 == null || year1 == null ||
                gender1.isBlank() || mobileNumberOrEmail1.isBlank() || password1.isBlank())
        {
            label.setText("fill everything");
            return;
        }

        if (password1.length() < 3 || password1.length() > 20)
        {
            label.setText("password must be between 3 and 20 characters");
            return;
        }

        if (!password1.matches(".*[a-zA-Z].*") || !password1.matches(".*\\d.*") || !password1.matches(".*[!@#$%*_.].*"))
        {
            label.setText("password must contain letters, numbers and special characters");
            return;
        }

        String birthday1 = month1 + " - " + day1 + " - " + year1;
        loggedInEmailOrMobile = mobileNumberOrEmail1;

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select * from usersFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, mobileNumberOrEmail1);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                label.setText("user with this email or mobile number already exists");
            }
            else {
                PreparedStatement preparedStatement1 = connection.prepareStatement("insert into usersFacebook635265346 (first_name, last_name, birthday, gender, mobile_number_or_email, password, profile_picture, cover_photo) values (?, ?, ?, ?, ?, ?, ?, ?)");
                preparedStatement1.setString(1, firstName1);
                preparedStatement1.setString(2, lastName1);
                preparedStatement1.setString(3, birthday1);
                preparedStatement1.setString(4, gender1);
                preparedStatement1.setString(5, mobileNumberOrEmail1);
                preparedStatement1.setString(6, password1);
                preparedStatement1.setString(7, "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\black_user_profile_picture.png");
                preparedStatement1.setString(8, "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\images.png");
                preparedStatement1.executeUpdate();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("profileCustomization.fxml"));
                Parent root = loader.load();

                ProfileCustomization controller = loader.getController();
                controller.FirstNameAndLastName(firstName1, lastName1);

                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.show();
            }

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        month.getItems().addAll("January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );

        for (int i = 1; i <= 31; i++) {
            day.getItems().add(i);
        }

        for (int i = 2025; i >= 1905; i--) {
            year.getItems().add(i);
        }
    }
}
