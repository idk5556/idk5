package com.example.facebook;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;
import java.util.*;

public class Friends {

    @FXML
    private ImageView facebookIcon;
    @FXML
    private Label friendRequests;
    @FXML
    private Label allFriends;
    @FXML
    private VBox vBox;

    private String loggedInUser;

    @FXML
    private void initialize() {
        loggedInUser = CreateNewAccount.loggedInEmailOrMobile;

        friendRequests.setOnMouseClicked(event -> loadUsers("friend_requests"));
        allFriends.setOnMouseClicked(event -> loadUsers("all_friends"));
        facebookIcon.setOnMouseClicked(event -> goToHomePage());
    }

    private void loadUsers(String category) {
        vBox.getChildren().clear();

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select " + category + " from friendsFacebook635265346 where logged_in_users_mobile_number_or_email = ?");
            preparedStatement.setString(1, loggedInUser);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
            {
                String data = resultSet.getString(category);

                if (data != null && !data.isBlank())
                {
                    String[] emails = data.split(",");

                    for (int i = 0; i < emails.length; i++)
                    {
                        String email = emails[i];

                        if (!email.trim().isBlank())
                        {
                            addUserToVBox(email.trim(), connection);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addUserToVBox(String email, Connection connection) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("select first_name, last_name, profile_picture from usersFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String profilePath = resultSet.getString("profile_picture");

                ImageView imageView = new ImageView();
                File file = new File(profilePath);

                imageView.setImage(new Image(file.toURI().toString()));
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);

                Label nameLabel = new Label(firstName + " " + lastName);
                nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                HBox box = new HBox(10, imageView, nameLabel);
                box.setStyle("-fx-padding: 5; -fx-alignment: center-left;");
                box.setOnMouseClicked(event -> goToViewProfile(email));

                vBox.getChildren().add(box);
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void goToViewProfile(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewUsersProfile.fxml"));
            Parent root = loader.load();

            ViewUsersProfile controller = loader.getController();
            controller.setUser(email);

            Stage stage = (Stage) facebookIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToHomePage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
            Stage stage = (Stage) facebookIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}