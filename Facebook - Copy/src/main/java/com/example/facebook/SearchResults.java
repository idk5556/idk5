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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.File;

public class SearchResults {
    @FXML
    private VBox resultsContainer;
    @FXML
    private ImageView facebookIcon;

    public void initialize() {
        facebookIcon.setOnMouseClicked(event -> goToHomePage());

        String searchTerm = HomePage.searchTerm.trim();

        if (searchTerm.isEmpty()) {
            return;
        }

        String[] names = searchTerm.split("\\s+");

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);

            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement;

            if (names.length >= 2) {
                String firstName = names[0];
                String lastName = names[1];
                preparedStatement = connection.prepareStatement("select first_name, last_name, profile_picture, mobile_number_or_email from usersFacebook635265346 where first_name like ? and last_name like ?");
                preparedStatement.setString(1, "%" + firstName + "%");
                preparedStatement.setString(2, "%" + lastName + "%");
            }
            else if (names.length == 1) {
                String name = names[0];

                preparedStatement = connection.prepareStatement("select first_name, last_name, profile_picture, mobile_number_or_email from usersFacebook635265346 where first_name like ? or last_name like ?");
                preparedStatement.setString(1, "%" + name + "%");
                preparedStatement.setString(2, "%" + name + "%");
            }
            else {
                preparedStatement = connection.prepareStatement("select first_name, last_name, profile_picture, mobile_number_or_email from usersFacebook635265346");
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            resultsContainer.getChildren().clear();

            while (resultSet.next()) {
                String foundFirstName = resultSet.getString("first_name");
                String foundLastName = resultSet.getString("last_name");
                String profilePicturePath = resultSet.getString("profile_picture");
                String userIdentifier = resultSet.getString("mobile_number_or_email");

                HBox hBox = new HBox(10);
                hBox.setStyle("-fx-padding: 10; -fx-alignment: CENTER_LEFT;");

                ImageView profileImage = new ImageView();
                Image image = new Image(new File(profilePicturePath).toURI().toString());
                profileImage.setImage(image);
                profileImage.setFitHeight(70);
                profileImage.setFitWidth(70);
                profileImage.setPreserveRatio(true);

                Label nameLabel = new Label(foundFirstName + " " + foundLastName);
                nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                hBox.getChildren().addAll(profileImage, nameLabel);
                resultsContainer.getChildren().add(hBox);

                final String finalIdentifier = userIdentifier;

                hBox.setOnMouseClicked(event -> {
                    try {
                        String currentUserIdentifier = CreateNewAccount.loggedInEmailOrMobile;

                        if (finalIdentifier.equals(currentUserIdentifier)) {
                            Parent root = FXMLLoader.load(getClass().getResource("MyProfilePosts.fxml"));
                            Stage stage = (Stage) resultsContainer.getScene().getWindow();
                            stage.setScene(new Scene(root));
                            stage.setFullScreen(true);
                        }
                        else {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewUsersProfile.fxml"));
                            Parent root = loader.load();

                            ViewUsersProfile controller = loader.getController();
                            controller.setUser(finalIdentifier);

                            Stage stage = (Stage) resultsContainer.getScene().getWindow();
                            stage.setScene(new Scene(root));
                            stage.setFullScreen(true);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            connection.close();
        } catch (Exception e) {
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