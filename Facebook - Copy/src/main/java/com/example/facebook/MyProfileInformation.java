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

public class MyProfileInformation {

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
    private Label friendsLabel;
    @FXML
    private VBox infoContainer;
    @FXML
    private Label mobileOrEmailLabel;
    @FXML
    private ImageView visibilityIcon;
    @FXML
    private Button editUsername;

    private String profilePicturePath = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\black_user_profile_picture.png";
    private String coverPhotoPath = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\images.png";

    private final String plusImagePath = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\facebook_plus_button.png";
    private final String lockedImagePath = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\facebook_locked_image.png";
    private final String earthImagePath = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\earth_image.png";
    private final String peopleImagePath = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\emoji_of_people.png";

    private String currentVisibility = "only me";

    @FXML
    private void initialize() {
        setUserProfile();
        setEditableFields();

        setupEditUsernameButton();

        addACoverPhoto.setOnMouseClicked(event -> chooseImage(coverPhoto));
        profilePicture.setOnMouseClicked(event -> chooseImage(profilePicture));
        facebookIcon.setOnMouseClicked(e -> goToHomePage());

        postsLabel.setOnMouseClicked(e -> loadScene("MyProfilePosts.fxml"));
        friendsLabel.setOnMouseClicked(e -> loadScene("MyProfileFriends.fxml"));
        visibilityIcon.setOnMouseClicked(e -> showVisibilityPopup());
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
                firstNameAndLastName.setText(resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                profilePicturePath = resultSet.getString("profile_picture");
                coverPhotoPath = resultSet.getString("cover_photo");

                if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                    profilePicture.setImage(new Image(new File(profilePicturePath).toURI().toString()));
                }
                if (coverPhotoPath != null && !coverPhotoPath.isEmpty()) {
                    coverPhoto.setImage(new Image(new File(coverPhotoPath).toURI().toString()));
                }
            }

            mobileOrEmailLabel.setText(CreateNewAccount.loggedInEmailOrMobile);
            currentVisibility = getVisibilitySetting(connection);
            updateVisibilityIcon(currentVisibility);

        }
        catch (SQLException e) {
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

    private String getVisibilitySetting(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select visibility from introductionFacebook635265346 where mobile_number_or_email = ?");
        preparedStatement.setString(1, CreateNewAccount.loggedInEmailOrMobile);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            String v = resultSet.getString("visibility");
            if (v != null)
            {
                return v;
            }
            else {
                return "only me";
            }
        }
        return "only me";
    }

    private void updateVisibilityIcon(String visibility) {

        if (visibility.equals("only me"))
        {
            visibilityIcon.setImage(new Image(new File(lockedImagePath).toURI().toString()));
        }
        else if (visibility.equals("friends")) {
            visibilityIcon.setImage(new Image(new File(peopleImagePath).toURI().toString()));
        }
        else {
            visibilityIcon.setImage(new Image(new File(earthImagePath).toURI().toString()));
        }
    }

    private void showVisibilityPopup() {
        Stage popup = new Stage();
        popup.initOwner(visibilityIcon.getScene().getWindow());

        VBox layout = new VBox(15);
        layout.setStyle("-fx-padding: 20; -fx-background-color: white;");
        layout.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("choosing an audience");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        ToggleGroup group = new ToggleGroup();
        RadioButton publicc = new RadioButton("public");
        RadioButton friends = new RadioButton("friends");
        RadioButton onlyMe = new RadioButton("only me");
        publicc.setToggleGroup(group);
        friends.setToggleGroup(group);
        onlyMe.setToggleGroup(group);

        switch (currentVisibility) {
            case "public": group.selectToggle(publicc); break;
            case "friends": group.selectToggle(friends); break;
            default: group.selectToggle(onlyMe);
        }

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        Button cancel = new Button("cancel");
        Button save = new Button("save");
        buttons.getChildren().addAll(cancel, save);

        cancel.setOnAction(e -> popup.close());

        save.setOnAction(e -> {
            String newVisibility = "only me";
            if (group.getSelectedToggle() == publicc) {
                newVisibility = "public";
            }
            else if (group.getSelectedToggle() == friends)
            {
                newVisibility = "friends";
            }

            saveVisibilityToDataBase(newVisibility);
            currentVisibility = newVisibility;
            updateVisibilityIcon(newVisibility);
            popup.close();
        });

        layout.getChildren().addAll(title, publicc, friends, onlyMe, buttons);
        Scene scene = new Scene(layout, 300, 250);
        popup.setScene(scene);
        popup.setResizable(false);
        popup.centerOnScreen();
        popup.show();
    }

    private void saveVisibilityToDataBase(String visibility) {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select * from introductionFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, CreateNewAccount.loggedInEmailOrMobile);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                PreparedStatement preparedStatement1 = connection.prepareStatement("update introductionFacebook635265346 set visibility = ? where mobile_number_or_email = ?");
                preparedStatement1.setString(1, visibility);
                preparedStatement1.setString(2, CreateNewAccount.loggedInEmailOrMobile);
                preparedStatement1.executeUpdate();
            }
            else {
                PreparedStatement preparedStatement1 = connection.prepareStatement("insert into introductionFacebook635265346 (mobile_number_or_email, visibility) values (?, ?)");
                preparedStatement1.setString(1, CreateNewAccount.loggedInEmailOrMobile);
                preparedStatement1.setString(2, visibility);
                preparedStatement1.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chooseImage(ImageView imageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(imageView.getScene().getWindow());

        if (file != null)
        {
            Image img = new Image(file.toURI().toString());
            imageView.setImage(img);
            if (imageView == profilePicture) {
                profilePicturePath = file.getAbsolutePath();
            }
            else {
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

    private void setEditableFields() {
        addField("add a workplace", "job");
        addField("add a public school", "education_public_school");
        addField("add a higher education institution", "education_higher");
        addField("add an actual city", "actual_city");
        addField("add hometown", "hometown");
        addField("add a status about your relationship", "relationship");
    }

    private void addField(String labelText, String dbColumn) {
        VBox container = new VBox(8);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 15px;");

        ImageView icon = new ImageView(new Image(new File(plusImagePath).toURI().toString()));
        icon.setFitWidth(20);
        icon.setFitHeight(20);

        HBox hBox = new HBox(15, icon, label);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setStyle("-fx-padding: 5 0 5 0;");

        TextField inputField = new TextField();
        inputField.setPromptText(labelText);
        inputField.setVisible(false);

        HBox buttons = new HBox(10);
        Button cancel = new Button("cancel");
        Button save = new Button("save");
        buttons.getChildren().addAll(cancel, save);
        buttons.setVisible(false);

        hBox.setOnMouseClicked(e -> {
            String previous = loadFieldFromDataBase(dbColumn);
            inputField.setText(previous);
            inputField.setVisible(true);
            buttons.setVisible(true);
            hBox.setVisible(false);
        });

        cancel.setOnAction(e -> {
            inputField.setVisible(false);
            buttons.setVisible(false);
            hBox.setVisible(true);
        });

        save.setOnAction(e -> {
            String input = inputField.getText();
            if (!input.isEmpty()) {
                saveFieldToDataBase(dbColumn, input);
                label.setText(labelText);
            }
            inputField.setVisible(false);
            buttons.setVisible(false);
            hBox.setVisible(true);
        });

        container.getChildren().addAll(hBox, inputField, buttons);
        infoContainer.getChildren().add(container);
    }

    private String loadFieldFromDataBase(String column) {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select " + column + " from introductionFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, CreateNewAccount.loggedInEmailOrMobile);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
            {
                return resultSet.getString(column);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void saveFieldToDataBase(String column, String value) {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select * from introductionFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, CreateNewAccount.loggedInEmailOrMobile);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                PreparedStatement preparedStatement1 = connection.prepareStatement("update introductionFacebook635265346 set " + column + " = ? where mobile_number_or_email = ?");
                preparedStatement1.setString(1, value);
                preparedStatement1.setString(2, CreateNewAccount.loggedInEmailOrMobile);
                preparedStatement1.executeUpdate();
            }
            else {
                PreparedStatement preparedStatement1 = connection.prepareStatement("insert into introductionFacebook635265346 (mobile_number_or_email, " + column + ") values (?, ?)");
                preparedStatement1.setString(1, CreateNewAccount.loggedInEmailOrMobile);
                preparedStatement1.setString(2, value);
                preparedStatement1.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) profilePicture.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}