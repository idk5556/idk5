package com.example.facebook;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.sql.*;

public class MyProfileFriends {
    @FXML
    private ImageView facebookIcon;
    @FXML
    private ImageView profilePicture;
    @FXML
    private ImageView coverPhoto;
    @FXML
    private Button addACoverPhoto;
    @FXML
    private Label firstNameAndLastName;
    @FXML
    private Label postsLabel;
    @FXML
    private Label informationLabel;
    @FXML
    private ScrollPane friendsScrollPane;
    @FXML
    private VBox friendsVBox;
    @FXML
    private Button editUsername;

    private String profilePicturePath = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\black_user_profile_picture.png";
    private String coverPhotoPath = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\images.png";

    @FXML
    private void initialize() {
        setUserProfile();
        loadFriends();

        setupEditUsernameButton();

        addACoverPhoto.setOnMouseClicked(event -> chooseImage(coverPhoto));
        profilePicture.setOnMouseClicked(event -> chooseImage(profilePicture));
        facebookIcon.setOnMouseClicked(e -> goToHomePage());

        postsLabel.setOnMouseClicked(event -> posts());
        informationLabel.setOnMouseClicked(event -> information());
    }

    private void setupEditUsernameButton() {
        editUsername.setOnAction(e -> showEditUsernamePopup());
    }

    private void showEditUsernamePopup() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initStyle(StageStyle.UTILITY);
        popup.initOwner(editUsername.getScene().getWindow());

        VBox popupContent = new VBox(15);
        popupContent.setStyle("-fx-padding: 20;");
        popupContent.setPrefWidth(300);

        Label firstNameLabel = new Label("new first name");
        firstNameLabel.setStyle("-fx-font-weight: bold;");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("enter new first name");

        String[] currentNames = firstNameAndLastName.getText().split(" ");
        if (currentNames.length > 0)
        {
            firstNameField.setText(currentNames[0]);
        }

        Label lastNameLabel = new Label("new last name");
        lastNameLabel.setStyle("-fx-font-weight: bold;");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("enter new last name");

        if (currentNames.length > 1) {
            lastNameField.setText(currentNames[1]);
        }

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("cancel");
        cancelButton.setStyle("-fx-background-color: #e1e1e3; -fx-text-fill: black;");
        cancelButton.setOnAction(e -> popup.close());

        Button saveButton = new Button("save");
        saveButton.setStyle("-fx-background-color: blue; -fx-text-fill: white;");
        saveButton.setOnAction(e -> {
            String newFirstName = firstNameField.getText().trim();
            String newLastName = lastNameField.getText().trim();

            if (!newFirstName.isEmpty() && !newLastName.isEmpty()) {
                updateUsername(newFirstName, newLastName);
                firstNameAndLastName.setText(newFirstName + " " + newLastName);
                popup.close();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText("first name and last name can't be empty");
                alert.showAndWait();
            }
        });

        buttons.getChildren().addAll(cancelButton, saveButton);

        popupContent.getChildren().addAll(
                firstNameLabel, firstNameField,
                lastNameLabel, lastNameField,
                buttons
        );

        Scene popupScene = new Scene(popupContent);
        popup.setScene(popupScene);
        popup.showAndWait();
    }

    private void updateUsername(String newFirstName, String newLastName) {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("update usersFacebook635265346 set first_name = ?, last_name = ? where mobile_number_or_email = ?");
            preparedStatement.setString(1, newFirstName);
            preparedStatement.setString(2, newLastName);
            preparedStatement.setString(3, CreateNewAccount.loggedInEmailOrMobile);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setUserProfile() {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select first_name, last_name, profile_picture, cover_photo from usersFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, CreateNewAccount.loggedInEmailOrMobile);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                firstNameAndLastName.setText(firstName + " " + lastName);

                profilePicturePath = resultSet.getString("profile_picture");
                coverPhotoPath = resultSet.getString("cover_photo");

                if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                    profilePicture.setImage(new Image(new File(profilePicturePath).toURI().toString()));
                }

                if (coverPhotoPath != null && !coverPhotoPath.isEmpty()) {
                    coverPhoto.setImage(new Image(new File(coverPhotoPath).toURI().toString()));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chooseImage(ImageView imageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(imageView.getScene().getWindow());

        if (file != null)
        {
            imageView.setImage(new Image(file.toURI().toString()));
            if (imageView == profilePicture)
            {
                profilePicturePath = file.getAbsolutePath();
            }
            else if (imageView == coverPhoto) {
                coverPhotoPath = file.getAbsolutePath();
            }
            saveImageToDatabase();
        }
    }

    private void saveImageToDatabase() {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("update usersFacebook635265346 set profile_picture = ?, cover_photo = ? where mobile_number_or_email = ?");
            preparedStatement.setString(1, profilePicturePath);
            preparedStatement.setString(2, coverPhotoPath);
            preparedStatement.setString(3, CreateNewAccount.loggedInEmailOrMobile);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFriends() {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select all_friends from friendsFacebook635265346 where logged_in_users_mobile_number_or_email = ?");
            preparedStatement.setString(1, CreateNewAccount.loggedInEmailOrMobile);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String allFriends = resultSet.getString("all_friends");
                if (allFriends != null && !allFriends.isEmpty())
                {
                    String[] friendsArray = allFriends.split(",");
                    for (int i = 0; i < friendsArray.length; i++) {
                        String email = friendsArray[i];

                        if (email != null && !email.trim().isEmpty())
                        {
                            addFriendToVBox(connection, email.trim());
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addFriendToVBox(Connection connection, String friendEmail) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("select first_name, last_name, profile_picture from usersFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, friendEmail);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String fullName = resultSet.getString("first_name") + " " + resultSet.getString("last_name");
                String profilePath = resultSet.getString("profile_picture");

                Image image;
                image = new Image(new File(profilePath).toURI().toString());

                ImageView friendImage = new ImageView(image);
                friendImage.setFitWidth(50);
                friendImage.setFitHeight(50);

                Label nameLabel = new Label(fullName);
                nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");

                HBox friendBox = new HBox(10, friendImage, nameLabel);
                friendBox.setStyle("-fx-padding: 10px; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;");

                friendBox.setOnMouseClicked(e -> openUsersProfile(friendEmail));

                friendsVBox.getChildren().add(friendBox);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openUsersProfile(String mobileOrEmail) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewUsersProfile.fxml"));
            Parent root = loader.load();

            ViewUsersProfile viewUsersProfile = loader.getController();
            viewUsersProfile.setUser(mobileOrEmail);

            Stage stage = (Stage) friendsScrollPane.getScene().getWindow();
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
            e.printStackTrace();
        }
    }

    private void posts() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("MyProfilePosts.fxml"));
            Stage stage = (Stage) postsLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void information() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("MyProfileInformation.fxml"));
            Stage stage = (Stage) informationLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}