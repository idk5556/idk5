package com.example.facebook;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class logInOrSignUp {
    @FXML
    private TextField emailOrPhoneNumber;
    @FXML
    private TextField password;
    @FXML
    private Button logIn;
    @FXML
    private Button createNewAcc;
    @FXML
    private Label label;

    public static String loggedInEmailOrMobile;

    @FXML
    private void CreateNewAccount(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CreateNewAccount.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) createNewAcc.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
    }

    @FXML
    private void LogIn(ActionEvent actionEvent) {
        String emailOrPhoneNumber1 = emailOrPhoneNumber.getText();
        String password1 = password.getText();

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement statement = connection.prepareStatement("select * from usersFacebook635265346 where mobile_number_or_email = ? and password = ?");
            statement.setString(1, emailOrPhoneNumber1);
            statement.setString(2, password1);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next())
            {
                CreateNewAccount.loggedInEmailOrMobile = emailOrPhoneNumber1;

                HomePage.loggedInFirstName = resultSet.getString("first_name");
                HomePage.loggedInLastName = resultSet.getString("last_name");

                Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
                Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setFullScreen(true);
                stage.show();
            }
            else {
                label.setText("mobile number, email or password is incorrect");
            }

            HomePage.loggedInFirstName = resultSet.getString("first_name");
            HomePage.loggedInLastName = resultSet.getString("last_name");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}