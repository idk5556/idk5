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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyProfilePosts {

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
    private VBox introductionBox;
    @FXML
    private Button addBiographyButton;
    @FXML
    private Button editDetailsButton;
    @FXML
    private Label informationLabel;
    @FXML
    private Label friendsLabel;
    @FXML
    private ScrollPane postsScrollPane;
    @FXML
    private VBox postsContainer;
    @FXML
    private Button editUsername;

    private String profilePicturePath = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\black_user_profile_picture.png";
    private String coverPhotoPath = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\images.png";
    private VBox biographyInputBox;
    private TextArea biographyTextArea;
    private Button saveBiographyButton;
    private Button cancelBiographyButton;

    private final String plusImagePath = "C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\facebook_plus_button.png";

    @FXML
    private void initialize() {
        setUserProfile();
        setBiography();
        loadPosts();

        setupEditUsernameButton();

        addBiographyButton.setOnAction(e -> showBiographyInput());
        editDetailsButton.setOnAction(e -> showEditDetailsPopup());

        addACoverPhoto.setOnMouseClicked(event -> chooseImage(coverPhoto));
        profilePicture.setOnMouseClicked(event -> chooseImage(profilePicture));
        facebookIcon.setOnMouseClicked(e -> goToHomePage());
        informationLabel.setOnMouseClicked(event -> information());
        friendsLabel.setOnMouseClicked(event -> friends());
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

    private void loadPosts() {
        postsContainer.getChildren().clear();

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select * from myPostsFacebook635265346 where user_email_or_mobile = ? order by post_id desc");
            preparedStatement.setString(1, CreateNewAccount.loggedInEmailOrMobile);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int postId = resultSet.getInt("post_id");
                String postText = resultSet.getString("post_text");
                String postImagePath = resultSet.getString("post_image_path");
                String likedPosts = resultSet.getString("liked_posts");
                String commentedPosts = resultSet.getString("commented_posts");
                String reactedPosts = resultSet.getString("reacted_posts");

                createPost(postId, postText, postImagePath, likedPosts, commentedPosts, reactedPosts);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createPost(int postId, String postText, String postImagePath, String likedPosts, String commentedPosts, String reactedPosts) {
        VBox postBox = new VBox(10);
        postBox.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-color: lightgray; -fx-border-radius: 5;");

        HBox headerBox = new HBox(10);
        ImageView postProfilePic = new ImageView();
        try {
            postProfilePic.setImage(new Image(new File(profilePicturePath).toURI().toString()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        postProfilePic.setFitWidth(40);
        postProfilePic.setFitHeight(40);

        String[] nameParts = firstNameAndLastName.getText().split(" ");
        Label firstNameLabel = new Label(nameParts[0]);
        firstNameLabel.setStyle("-fx-font-weight: bold;");

        String lastNameText = "";
        if (nameParts.length > 1) {
            lastNameText = nameParts[1];
        }
        Label lastNameLabel = new Label(lastNameText);

        lastNameLabel.setStyle("-fx-font-weight: bold;");

        HBox nameBox = new HBox(5, firstNameLabel, lastNameLabel);
        headerBox.getChildren().addAll(postProfilePic, nameBox);

        postBox.getChildren().add(headerBox);

        if (postText != null && !postText.isEmpty()) {
            Label textLabel = new Label(postText);
            textLabel.setWrapText(true);
            postBox.getChildren().add(textLabel);
        }

        if (postImagePath != null && !postImagePath.isEmpty()) {
            try {
                ImageView postImage = new ImageView(new Image(new File(postImagePath).toURI().toString()));
                postImage.setPreserveRatio(true);
                postImage.setFitWidth(500);
                postBox.getChildren().add(postImage);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        int likeCount = 0;
        if (likedPosts != null) {
            if (!likedPosts.isEmpty())
            {
                String[] likesArray = likedPosts.split(",");
                likeCount = likesArray.length;
            }
        }

        Label likesLabel = new Label(likeCount + " liked this post");

        Label reactionEmojiLabel = new Label("🙂");
        reactionEmojiLabel.setStyle("-fx-cursor: hand;");

        HBox likesBox = new HBox(10, likesLabel, reactionEmojiLabel);
        postBox.getChildren().add(likesBox);

        reactionEmojiLabel.setOnMouseClicked(e -> showReactionsPopup(postId));

        Label likeButton = new Label("👍");
        likeButton.setStyle("-fx-cursor: hand; " +
                (isPostLikedByUser(likedPosts) ? "-fx-text-fill: blue;" : "-fx-text-fill: black;"));

        Label commentButton = new Label("💬");
        commentButton.setStyle("-fx-cursor: hand;");

        HBox buttonsBox = new HBox(20, likeButton, commentButton);
        postBox.getChildren().add(buttonsBox);

        likeButton.setOnMouseClicked(e -> {
            boolean currentlyLiked = isPostLikedByUser(likedPosts);
            updatePostLike(postId, !currentlyLiked);
            loadPosts();
        });

        commentButton.setOnMouseClicked(e -> showCommentsPopup(postId, commentedPosts));

        HBox commentInputBox = new HBox(10);
        ImageView commenterProfilePic = new ImageView();
        commenterProfilePic.setImage(new Image(new File(profilePicturePath).toURI().toString()));
        commenterProfilePic.setFitWidth(30);
        commenterProfilePic.setFitHeight(30);

        TextField commentField = new TextField();
        commentField.setPromptText("write a comment...");

        Label commentEmojiLabel = new Label("🙂");
        commentEmojiLabel.setStyle("-fx-cursor: hand;");

        commentInputBox.getChildren().addAll(commenterProfilePic, commentField, commentEmojiLabel);
        postBox.getChildren().add(commentInputBox);

        commentEmojiLabel.setOnMouseClicked(e -> showEmojiPicker(commentField));

        commentField.setOnAction(e -> {
            if (!commentField.getText().isEmpty()) {
                addCommentToPost(postId, commentField.getText());
                commentField.clear();
                loadPosts();
            }
        });

        postsContainer.getChildren().add(postBox);
    }

    private boolean isPostLikedByUser(String likedPosts) {
        if (likedPosts == null || likedPosts.isEmpty())
        {
            return false;
        }
        return likedPosts.contains(CreateNewAccount.loggedInEmailOrMobile);
    }

    private void updatePostLike(int postId, boolean like) {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select liked_posts from myPostsFacebook635265346 where post_id = ?");
            preparedStatement.setInt(1, postId);

            ResultSet resultSet = preparedStatement.executeQuery();

            String currentLikes = "";
            if (resultSet.next()) {
                currentLikes = resultSet.getString("liked_posts");
            }

            String newLikes;
            if (like) {
                if (currentLikes == null || currentLikes.isEmpty())
                {
                    newLikes = CreateNewAccount.loggedInEmailOrMobile;
                }
                else {
                    newLikes = currentLikes + "," + CreateNewAccount.loggedInEmailOrMobile;
                }
            }
            else {
                if (currentLikes != null && currentLikes.contains(CreateNewAccount.loggedInEmailOrMobile)) {
                    newLikes = currentLikes.replace(CreateNewAccount.loggedInEmailOrMobile, "")
                            .replace(",,", ",")
                            .replaceAll("^,|,$", "");
                }
                else {
                    newLikes = currentLikes;
                }
            }

            PreparedStatement preparedStatement1 = connection.prepareStatement("update myPostsFacebook635265346 set liked_posts = ? where post_id = ?");
            preparedStatement1.setString(1, newLikes);
            preparedStatement1.setInt(2, postId);
            preparedStatement1.executeUpdate();

            PreparedStatement insertAction = connection.prepareStatement("insert into otherPostsFacebook635265346 (user_email_or_mobile, post_id, action, detail) values (?, ?, ?, ?)");
            insertAction.setString(1, CreateNewAccount.loggedInEmailOrMobile);
            insertAction.setInt(2, postId);

            String actionValue;
            if (like) {
                actionValue = "like";
            }
            else {
                actionValue = "unlike";
            }
            insertAction.setString(3, actionValue);

            insertAction.setString(4, "");
            insertAction.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showReactionsPopup(int postId) {
        Stage popup = new Stage();
        popup.initOwner(facebookIcon.getScene().getWindow());
        popup.initStyle(StageStyle.UTILITY);

        HBox emojiBox = new HBox(10);
        emojiBox.setStyle("-fx-padding: 15; -fx-background-color: white;");

        String[] emojis = {"👍", "❤", "😂", "😠"};

        for (int i = 0; i < emojis.length; i++) {
            String emoji = emojis[i];

            Label emojiLabel = new Label(emoji);
            emojiLabel.setStyle("-fx-font-size: 20; -fx-cursor: hand;");
            emojiLabel.setOnMouseClicked(e -> {
                addReactionToPost(postId, emoji);
                popup.close();
                loadPosts();
            });
            emojiBox.getChildren().add(emojiLabel);
        }

        Scene popupScene = new Scene(emojiBox);
        popup.setScene(popupScene);
        popup.show();
    }

    private void addReactionToPost(int postId, String emoji) {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select reacted_posts from myPostsFacebook635265346 where post_id = ?");
            preparedStatement.setInt(1, postId);
            ResultSet resultSet = preparedStatement.executeQuery();

            String currentReactions = "";
            if (resultSet.next()) {
                currentReactions = resultSet.getString("reacted_posts");
            }

            String newReaction = emoji + ":" + CreateNewAccount.loggedInEmailOrMobile;
            String newReactions;

            if (currentReactions == null || currentReactions.isEmpty()) {
                newReactions = newReaction;
            }
            else {
                String[] existingReactions = currentReactions.split(";");
                StringBuilder filteredReactions = new StringBuilder();
                for (int i = 0; i < existingReactions.length; i++)
                {
                    String r = existingReactions[i];

                    if (!r.endsWith(CreateNewAccount.loggedInEmailOrMobile))
                    {
                        if (filteredReactions.length() > 0) {
                            filteredReactions.append(";");
                        }
                        filteredReactions.append(r);
                    }
                }
                if (filteredReactions.length() > 0) {
                    filteredReactions.append(";");
                }
                filteredReactions.append(newReaction);
                newReactions = filteredReactions.toString();
            }

            PreparedStatement preparedStatement1 = connection.prepareStatement("update myPostsFacebook635265346 set reacted_posts = ? where post_id = ?");
            preparedStatement1.setString(1, newReactions);
            preparedStatement1.setInt(2, postId);
            preparedStatement1.executeUpdate();

            PreparedStatement preparedStatement2 = connection.prepareStatement("insert into otherPostsFacebook635265346 (user_email_or_mobile, post_id, action, detail) values (?, ?, ?, ?)");
            preparedStatement2.setString(1, CreateNewAccount.loggedInEmailOrMobile);
            preparedStatement2.setInt(2, postId);
            preparedStatement2.setString(3, "react");
            preparedStatement2.setString(4, emoji);
            preparedStatement2.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showCommentsPopup(int postId, String commentedPosts) {
        Stage popup = new Stage();
        popup.initOwner(facebookIcon.getScene().getWindow());
        popup.initStyle(StageStyle.UTILITY);

        VBox popupContent = new VBox(10);
        popupContent.setStyle("-fx-padding: 15; -fx-background-color: white;");

        VBox commentsBox = new VBox(10);
        ScrollPane commentsScroll = new ScrollPane(commentsBox);
        commentsScroll.setFitToWidth(true);
        commentsScroll.setPrefHeight(300);

        if (commentedPosts != null && !commentedPosts.isEmpty()) {
            String[] comments = commentedPosts.split(";");
            for (int i = 0; i < comments.length; i++) {
                String comment = comments[i];

                String[] parts = comment.split(":", 2);

                if (parts.length == 2)
                {
                    String userEmailOrMobile = parts[0];
                    String commentText = parts[1];

                    try {
                        Connection connection = DriverManager.getConnection(
                                HelloApplication.url, HelloApplication.username, HelloApplication.password);
                        connection.createStatement().execute("use mydb");

                        PreparedStatement preparedStatement = connection.prepareStatement("select first_name, last_name, profile_picture from usersFacebook635265346 where mobile_number_or_email = ?");
                        preparedStatement.setString(1, userEmailOrMobile);

                        ResultSet resultSet = preparedStatement.executeQuery();

                        if (resultSet.next()) {
                            HBox commentBox = new HBox(10);

                            ImageView userPic = new ImageView();
                            String picPath = resultSet.getString("profile_picture");

                            userPic.setImage(new Image(new File(picPath).toURI().toString()));
                            userPic.setFitWidth(30);
                            userPic.setFitHeight(30);

                            VBox textBox = new VBox(5);
                            Label nameLabel = new Label(resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                            nameLabel.setStyle("-fx-font-weight: bold;");
                            Label commentLabel = new Label(commentText);
                            commentLabel.setWrapText(true);

                            textBox.getChildren().addAll(nameLabel, commentLabel);
                            commentBox.getChildren().addAll(userPic, textBox);
                            commentsBox.getChildren().add(commentBox);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        HBox newCommentBox = new HBox(10);
        ImageView commenterPic = new ImageView();
        commenterPic.setImage(new Image(new File(profilePicturePath).toURI().toString()));
        commenterPic.setFitWidth(30);
        commenterPic.setFitHeight(30);

        TextField commentField = new TextField();
        commentField.setPromptText("write a comment...");
        commentField.setPrefWidth(300);

        Label emojiLabel = new Label("🙂");
        emojiLabel.setStyle("-fx-cursor: hand;");

        newCommentBox.getChildren().addAll(commenterPic, commentField, emojiLabel);

        emojiLabel.setOnMouseClicked(e -> showEmojiPicker(commentField));

        commentField.setOnAction(e -> {
            if (!commentField.getText().isEmpty()) {
                addCommentToPost(postId, commentField.getText());
                commentField.clear();
                popup.close();
                loadPosts();
            }
        });

        popupContent.getChildren().addAll(commentsScroll, newCommentBox);

        Scene popupScene = new Scene(popupContent, 400, 500);
        popup.setScene(popupScene);
        popup.show();
    }

    private void addCommentToPost(int postId, String comment) {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select commented_posts from myPostsFacebook635265346 where post_id = ?");
            preparedStatement.setInt(1, postId);
            ResultSet resultSet = preparedStatement.executeQuery();

            String currentComments = "";
            if (resultSet.next()) {
                currentComments = resultSet.getString("commented_posts");
            }

            String newComment = CreateNewAccount.loggedInEmailOrMobile + ":" + comment;
            String newComments;

            if (currentComments == null || currentComments.isEmpty()) {
                newComments = newComment;
            }
            else {
                newComments = currentComments + ";" + newComment;
            }

            PreparedStatement preparedStatement1 = connection.prepareStatement("update myPostsFacebook635265346 set commented_posts = ? where post_id = ?");
            preparedStatement1.setString(1, newComments);
            preparedStatement1.setInt(2, postId);
            preparedStatement1.executeUpdate();

            PreparedStatement preparedStatement2 = connection.prepareStatement("insert into otherPostsFacebook635265346 (user_email_or_mobile, post_id, action, detail) values (?, ?, ?, ?)");
            preparedStatement2.setString(1, CreateNewAccount.loggedInEmailOrMobile);
            preparedStatement2.setInt(2, postId);
            preparedStatement2.setString(3, "comment");
            preparedStatement2.setString(4, comment);
            preparedStatement2.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showEmojiPicker(TextField textField) {
        Stage emojiStage = new Stage();
        emojiStage.initOwner(facebookIcon.getScene().getWindow());
        emojiStage.initStyle(StageStyle.UTILITY);

        FlowPane emojiPane = new FlowPane();
        emojiPane.setHgap(5);
        emojiPane.setVgap(5);
        emojiPane.setStyle("-fx-padding: 10; -fx-background-color: white;");

        String[] emojis = {"👍", "👎", "😂", "😎", "❤", "😠", "😍"};

        for (int i = 0; i < emojis.length; i++) {
            String emoji = emojis[i];

            Label emojiLabel = new Label(emoji);
            emojiLabel.setStyle("-fx-font-size: 20; -fx-cursor: hand;");
            emojiLabel.setOnMouseClicked(e -> {
                textField.setText(textField.getText() + emoji);
                emojiStage.close();
            });
            emojiPane.getChildren().add(emojiLabel);
        }

        Scene scene = new Scene(emojiPane, 300, 200);
        emojiStage.setScene(scene);
        emojiStage.show();
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

        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
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

    private void goToHomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HomePage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) facebookIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBiography() {
        biographyInputBox = new VBox(5);
        biographyInputBox.setVisible(false);

        Label label = new Label("describe yourself");
        biographyTextArea = new TextArea();
        biographyTextArea.setPrefRowCount(3);

        HBox buttons = new HBox(10);
        cancelBiographyButton = new Button("cancel");
        saveBiographyButton = new Button("save");

        buttons.getChildren().addAll(cancelBiographyButton, saveBiographyButton);
        biographyInputBox.getChildren().addAll(label, biographyTextArea, buttons);

        cancelBiographyButton.setOnAction(e -> {
            introductionBox.getChildren().remove(biographyInputBox);
            addBiographyButton.setVisible(true);
        });

        saveBiographyButton.setOnAction(e -> {
            saveBioToDataBase(biographyTextArea.getText());
            introductionBox.getChildren().remove(biographyInputBox);
            addBiographyButton.setVisible(true);
        });

        introductionBox.getChildren().add(biographyInputBox);
    }

    private void showBiographyInput() {
        biographyTextArea.setText(loadBioFromDataBase());
        if (!introductionBox.getChildren().contains(biographyInputBox)) {
            introductionBox.getChildren().add(biographyInputBox);
        }
        biographyInputBox.setVisible(true);
        addBiographyButton.setVisible(false);
    }

    private String loadBioFromDataBase() {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select biography from introductionFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, CreateNewAccount.loggedInEmailOrMobile);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("biography");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void showEditDetailsPopup() {
        Stage popup = new Stage();
        popup.initOwner(editDetailsButton.getScene().getWindow());

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPrefSize(500, 600);
        anchorPane.setStyle("-fx-background-color: white; -fx-border-color: black;");

        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 20;");
        content.setPrefWidth(360);

        Label title = new Label("edit details");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        title.setAlignment(Pos.CENTER);
        Label label = new Label("the details you choose will be public and will appear at the top of your profile.");

        content.getChildren().addAll(title, label);

        createEditableField(content, "job", "job");
        createEditableField(content, "add a public school", "education_public_school");
        createEditableField(content, "add a higher education institution", "education_higher");
        createEditableField(content, "add an actual city", "actual_city");
        createEditableField(content, "add hometown", "hometown");
        createEditableField(content, "add a status about your relationship", "relationship");
        createEditableField(content, "add a family member", "family_members");

        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);

        AnchorPane.setTopAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 0.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);
        anchorPane.getChildren().add(scrollPane);

        Scene popupScene = new Scene(anchorPane);
        popup.setScene(popupScene);
        popup.centerOnScreen();
        popup.show();
    }

    private void createEditableField(VBox parent, String labelText, String column) {
        VBox vBox = new VBox(5);

        Label label = new Label(labelText);
        HBox fieldBox = new HBox(10);
        ImageView icon = new ImageView(new Image(new File(plusImagePath).toURI().toString()));
        icon.setFitWidth(20);
        icon.setFitHeight(20);
        fieldBox.getChildren().addAll(icon, label);

        TextField inputField = new TextField();
        inputField.setPromptText(labelText);

        HBox buttons = new HBox(10);
        Button saveButton = new Button("save");
        Button cancelButton = new Button("cancel");
        buttons.getChildren().addAll(cancelButton, saveButton);

        inputField.setVisible(false);
        buttons.setVisible(false);

        fieldBox.setOnMouseClicked(e -> {
            String previousValue = loadFieldFromDataBase(column);
            inputField.setText(previousValue);
            inputField.setVisible(true);
            buttons.setVisible(true);
            fieldBox.setVisible(false);
        });

        cancelButton.setOnAction(e -> {
            inputField.setVisible(false);
            buttons.setVisible(false);
            fieldBox.setVisible(true);
        });

        saveButton.setOnAction(e -> {
            String input = inputField.getText();
            if (!input.isEmpty())
            {
                saveFieldToDataBase(column, input);
                label.setText(labelText);
            }
            inputField.setVisible(false);
            buttons.setVisible(false);
            fieldBox.setVisible(true);
        });

        vBox.getChildren().addAll(fieldBox, inputField, buttons);
        parent.getChildren().add(vBox);
    }

    private String loadFieldFromDataBase(String column) {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select " + column + " from introduction where mobile_number_or_email = ?");
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

    private void saveBioToDataBase(String bio) {
        saveFieldToDataBase("biography", bio);
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

    private void information() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MyProfileInformation.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) informationLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void friends() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MyProfileFriends.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) friendsLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}