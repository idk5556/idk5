package com.example.facebook;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class ProfileCustomization {

    @FXML
    private ImageView facebookIcon;
    @FXML
    private TextField search;
    @FXML
    private ImageView coverPhoto;
    @FXML
    private Button addACoverPhoto;
    @FXML
    private ImageView profilePicture;
    @FXML
    private Text firstNameAndLastName;

    String currentUser = CreateNewAccount.loggedInEmailOrMobile;

    @FXML
    private void initialize() {
        search.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                openSearchScene();
            }
        });

        addACoverPhoto.setOnMouseClicked(event -> chooseImage(coverPhoto));
        profilePicture.setOnMouseClicked(event -> chooseImage(profilePicture));

        facebookIcon.setOnMouseClicked(e -> goToHomePage());
    }

    public void FirstNameAndLastName(String firstName, String lastName) {
        firstNameAndLastName.setText(firstName + " " + lastName);
    }

    private String profilePicture1 = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\black_user_profile_picture.png";
    private String coverPhoto1 = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\images.png";

    private void chooseImage(ImageView imageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(imageView.getScene().getWindow());

        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);

            if (imageView == profilePicture) {
                profilePicture1 = file.getAbsolutePath();
                saveImageToDatabase();
            }
            else if (imageView == coverPhoto) {
                coverPhoto1 = file.getAbsolutePath();
                saveImageToDatabase();
            }
        }
    }

    private void saveImageToDatabase() {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement update = connection.prepareStatement("update usersFacebook635265346 set profile_picture = ?, cover_photo = ? where mobile_number_or_email = ?");
            update.setString(1, profilePicture1);
            update.setString(2, coverPhoto1);
            update.setString(3, CreateNewAccount.loggedInEmailOrMobile);
            update.execute();

            connection.close();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    private void goToHomePage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
            Stage stage = (Stage) facebookIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void openSearchScene() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("SearchResults.fxml"));
            Stage stage = (Stage) search.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}