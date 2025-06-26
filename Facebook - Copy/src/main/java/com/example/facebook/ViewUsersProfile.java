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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.sql.*;

public class ViewUsersProfile {

    @FXML
    private ImageView profilePicture;
    @FXML
    private ImageView coverPhoto;
    @FXML
    private Label firstNameAndLastName;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private ImageView facebookIcon;
    @FXML
    private ScrollPane postsScrollPane;
    @FXML
    private VBox postsContainer;
    @FXML
    private AnchorPane scrollContent;
    @FXML
    private VBox infoVBox;

    private Button addAsFriendButton;
    private Button deleteRequestButton;
    private Button friendsButton;
    private Button requestConfirmationButton;

    static String secondUsersEmailOrMobileNumber;
    private String loggedInUsersEmailOrMobileNumber;
    private String secondUsersFullName;

    @FXML
    private void initialize() {
        facebookIcon.setOnMouseClicked(e -> goToHomePage());
        if (postsScrollPane != null) {
            postsScrollPane.setFitToWidth(true);
        }
    }

    public void setUser(String mobileNumberOrEmail) {
        this.secondUsersEmailOrMobileNumber = mobileNumberOrEmail;
        this.loggedInUsersEmailOrMobileNumber = CreateNewAccount.loggedInEmailOrMobile;

        addAsFriendButton = new Button();
        addAsFriendButton.setLayoutX(973);
        addAsFriendButton.setLayoutY(462);
        addAsFriendButton.setPrefWidth(268);
        addAsFriendButton.setPrefHeight(49);
        scrollContent.getChildren().add(addAsFriendButton);

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select first_name, last_name, profile_picture, cover_photo from usersFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, secondUsersEmailOrMobileNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                secondUsersFullName = firstName + " " + lastName;

                firstNameAndLastName.setText(secondUsersFullName);
                firstNameAndLastName.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

                String profilePicturePath = resultSet.getString("profile_picture");
                String coverPhotoPath = resultSet.getString("cover_photo");

                if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                    profilePicture.setImage(new Image(new File(profilePicturePath).toURI().toString()));
                }

                if (coverPhotoPath != null && !coverPhotoPath.isEmpty()) {
                    coverPhoto.setImage(new Image(new File(coverPhotoPath).toURI().toString()));
                }
            }

            displayUserInfo();

            String submitted = getColumnValue(connection, loggedInUsersEmailOrMobileNumber, "submitted_requests");
            String received = getColumnValue(connection, loggedInUsersEmailOrMobileNumber, "friend_requests");
            String friends = getColumnValue(connection, loggedInUsersEmailOrMobileNumber, "all_friends");

            if (friends != null && friends.contains(secondUsersEmailOrMobileNumber)) {
                showFriendsButton();
            }
            else {
                if (submitted != null && submitted.contains(secondUsersEmailOrMobileNumber)) {
                    ImageView addIcon = new ImageView(new Image("C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\cancel_request_black_button.png"));
                    addIcon.setFitWidth(35);
                    addIcon.setFitHeight(35);

                    setupButtonState(" cancel request", false, "#e1e1e3", "black", addIcon);
                }
                else if (received != null && received.contains(secondUsersEmailOrMobileNumber)) {
                    File file = new File("C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\add-friends-icon-free-vector-removebg-preview.png");
                    ImageView addIcon = new ImageView(new Image(file.toURI().toString()));
                    addIcon.setFitWidth(35);
                    addIcon.setFitHeight(35);

                    requestConfirmationButton = new Button(" request confirmation");
                    requestConfirmationButton.setGraphic(addIcon);
                    requestConfirmationButton.setStyle("-fx-background-color: blue; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
                    requestConfirmationButton.setPrefWidth(231);
                    requestConfirmationButton.setPrefHeight(45);
                    requestConfirmationButton.setLayoutX(855);
                    requestConfirmationButton.setLayoutY(464);

                    requestConfirmationButton.setOnAction(e -> acceptFriendRequest());

                    scrollContent.getChildren().add(requestConfirmationButton);
                    createDeleteRequestButton();

                    addAsFriendButton.setVisible(false);
                }
                else {
                    File file = new File("C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\add-friends-icon-free-vector-removebg-preview.png");
                    ImageView addIcon = new ImageView(new Image(file.toURI().toString()));
                    addIcon.setFitWidth(35);
                    addIcon.setFitHeight(35);

                    setupButtonState(" add as friend", false, "#e1e1e3", "black", addIcon);
                }
            }

            loadUserPosts();
        } catch (Exception e) {
            e.printStackTrace();
        }

        addAsFriendButton.setOnAction(e -> {
            String text = addAsFriendButton.getText();
            if (text.equals(" add as friend")) {
                sendFriendRequest();
            }
            else if (text.equals(" request confirmation")) {
                acceptFriendRequest();
            }
            else if (text.equals(" cancel request")) {
                cancelFriendRequest();
            }
        });
    }

    private void loadUserPosts() {
        if (postsContainer == null) return;

        postsContainer.getChildren().clear();

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select * from myPostsFacebook635265346 where user_email_or_mobile = ? order by post_id desc");
            preparedStatement.setString(1, secondUsersEmailOrMobileNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String visibility = resultSet.getString("visibility");
                int postId = resultSet.getInt("post_id");
                String postText = resultSet.getString("post_text");
                String postImagePath = resultSet.getString("post_image_path");
                String likedPosts = resultSet.getString("liked_posts");
                String commentedPosts = resultSet.getString("commented_posts");
                String reactedPosts = resultSet.getString("reacted_posts");

                if (canViewPost(visibility, loggedInUsersEmailOrMobileNumber, secondUsersEmailOrMobileNumber)) {
                    createPost(postId, postText, postImagePath, likedPosts, commentedPosts, reactedPosts, visibility);
                }
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private boolean canViewPost(String visibility, String viewerEmailOrMobile, String postOwnerEmailOrMobile) {
        if (visibility == null)
        {
            return true;
        }
        if (viewerEmailOrMobile.equals(postOwnerEmailOrMobile))
        {
            return true;
        }

        switch (visibility.toLowerCase()) {
            case "public": return true;
            case "friends": return areFriends(viewerEmailOrMobile, postOwnerEmailOrMobile);
            case "only me": return false;
            default: return true;
        }
    }

    private boolean areFriends(String user1, String user2) {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select all_friends from friendsFacebook635265346 where logged_in_users_mobile_number_or_email = ?");
            preparedStatement.setString(1, user1);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String friendsList = resultSet.getString("all_friends");
                if (friendsList != null && friendsList.contains(user2)) {
                    return true;
                }
            }

            preparedStatement = connection.prepareStatement("select all_friends from friendsFacebook635265346 where logged_in_users_mobile_number_or_email = ?");
            preparedStatement.setString(1, user2);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String friendsList = resultSet.getString("all_friends");
                if (friendsList != null && friendsList.contains(user1)) {
                    return true;
                }
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createPost(int postId, String postText, String postImagePath, String likedPosts, String commentedPosts, String reactedPosts, String visibility) {

        if (postsContainer == null)
        {
            return;
        }

        VBox postBox = new VBox(10);
        postBox.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-color: lightgray; -fx-border-radius: 5;");

        HBox headerBox = new HBox(10);
        ImageView postProfilePic = new ImageView();
        try {
            String profilePicPath = getCurrentUserProfilePic(secondUsersEmailOrMobileNumber);
            if (profilePicPath != null && !profilePicPath.isEmpty()) {
                postProfilePic.setImage(new Image(new File(profilePicPath).toURI().toString()));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        postProfilePic.setFitWidth(40);
        postProfilePic.setFitHeight(40);

        Label nameLabel = new Label(secondUsersFullName);
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label visibilityLabel = new Label(getVisibilityIcon(visibility));
        visibilityLabel.setStyle("-fx-font-size: 12;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(postProfilePic, nameLabel, spacer, visibilityLabel);
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
            if (!likedPosts.isEmpty()) {
                String[] likesArray = likedPosts.split(",");
                likeCount = likesArray.length;
            }
            else {
                likeCount = 0;
            }
        }
        else {
            likeCount = 0;
        }

        Label likesLabel = new Label(likeCount + " liked this post");

        Label reactionEmojiLabel = new Label("🙂");
        reactionEmojiLabel.setStyle("-fx-cursor: hand;");

        HBox likesBox = new HBox(10, likesLabel, reactionEmojiLabel);
        postBox.getChildren().add(likesBox);

        reactionEmojiLabel.setOnMouseClicked(e -> showReactionsPopup(postId));

        Label likeButton = new Label("👍");
        likeButton.setStyle("-fx-cursor: hand; ");

        if (isPostLikedByUser(likedPosts)) {
            likeButton.setStyle(likeButton.getStyle() + "-fx-text-fill: blue;");
        }
        else {
            likeButton.setStyle(likeButton.getStyle() + "-fx-text-fill: black;");
        }

        Label commentButton = new Label("💬");
        commentButton.setStyle("-fx-cursor: hand;");

        HBox buttonsBox = new HBox(20, likeButton, commentButton);
        postBox.getChildren().add(buttonsBox);

        likeButton.setOnMouseClicked(e -> {
            boolean currentlyLiked = isPostLikedByUser(likedPosts);
            updatePostLike(postId, !currentlyLiked);
            loadUserPosts();
        });

        commentButton.setOnMouseClicked(e -> showCommentsPopup(postId, commentedPosts));

        if (canViewPost(visibility, loggedInUsersEmailOrMobileNumber, secondUsersEmailOrMobileNumber)) {
            HBox commentInputBox = new HBox(10);
            ImageView commenterProfilePic = new ImageView();
            try {
                String currentUserPic = getCurrentUserProfilePic(loggedInUsersEmailOrMobileNumber);
                if (currentUserPic != null && !currentUserPic.isEmpty()) {
                    commenterProfilePic.setImage(new Image(new File(currentUserPic).toURI().toString()));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
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
                    loadUserPosts();
                }
            });
        }

        postsContainer.getChildren().add(postBox);
    }

    private String getCurrentUserProfilePic(String emailOrMobile) {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select profile_picture from usersFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, emailOrMobile);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("profile_picture");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getVisibilityIcon(String visibility) {
        if (visibility == null) return "🌍";
        switch (visibility.toLowerCase()) {
            case "public": return "🌍";
            case "friends": return "👥";
            case "only me": return "🔒";
            default: return "🌍";
        }
    }

    private boolean isPostLikedByUser(String likedPosts) {
        if (likedPosts == null || likedPosts.isEmpty())
        {
            return false;
        }
        return likedPosts.contains(loggedInUsersEmailOrMobileNumber);
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
                if (currentLikes == null || currentLikes.isEmpty()) {
                    newLikes = loggedInUsersEmailOrMobileNumber;
                }
                else {
                    newLikes = currentLikes + "," + loggedInUsersEmailOrMobileNumber;
                }
            }
            else {
                if (currentLikes != null && currentLikes.contains(loggedInUsersEmailOrMobileNumber)) {
                    newLikes = currentLikes.replace(loggedInUsersEmailOrMobileNumber, "")
                            .replace(",,", ",")
                            .replaceAll("^,|,$", "");
                }
                else {
                    newLikes = currentLikes;
                }
            }

            PreparedStatement updateLikes = connection.prepareStatement("update myPostsFacebook635265346 set liked_posts = ? where post_id = ?");
            updateLikes.setString(1, newLikes);
            updateLikes.setInt(2, postId);
            updateLikes.executeUpdate();

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
                loadUserPosts();
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

            String newReaction = emoji + ":" + loggedInUsersEmailOrMobileNumber;
            String newReactions;

            if (currentReactions == null || currentReactions.isEmpty()) {
                newReactions = newReaction;
            }
            else {
                String[] existingReactions = currentReactions.split(";");
                StringBuilder filteredReactions = new StringBuilder();

                for (int i = 0; i < existingReactions.length; i++) {

                    String r = existingReactions[i];

                    if (!r.endsWith(loggedInUsersEmailOrMobileNumber))
                    {
                        if (filteredReactions.length() > 0)
                        {
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

        if (commentedPosts != null && !commentedPosts.isEmpty())
        {
            String[] comments = commentedPosts.split(";");

            for (int i = 0; i < comments.length; i++)
            {
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
                            if (picPath != null && !picPath.isEmpty()) {
                                userPic.setImage(new Image(new File(picPath).toURI().toString()));
                            }
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

        try {
            commenterPic.setImage(new Image(new File(getCurrentUserProfilePic(loggedInUsersEmailOrMobileNumber)).toURI().toString()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
                loadUserPosts();
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

            String newComment = loggedInUsersEmailOrMobileNumber + ":" + comment;
            String newComments;

            if (currentComments == null || currentComments.isEmpty()) {
                newComments = newComment;
            }
            else {
                newComments = currentComments + ";" + newComment;
            }

            PreparedStatement updateComments = connection.prepareStatement("update myPostsFacebook635265346 set commented_posts = ? where post_id = ?");
            updateComments.setString(1, newComments);
            updateComments.setInt(2, postId);
            updateComments.executeUpdate();

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

    private void sendFriendRequest() {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            updateOrInsertColumn(connection, loggedInUsersEmailOrMobileNumber, "submitted_requests", secondUsersEmailOrMobileNumber);
            updateOrInsertColumn(connection, secondUsersEmailOrMobileNumber, "friend_requests", loggedInUsersEmailOrMobileNumber);

            File file = new File("C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\cancel_request_black_button.png");
            ImageView addIcon = new ImageView(new Image(file.toURI().toString()));
            addIcon.setFitWidth(35);
            addIcon.setFitHeight(35);

            setupButtonState(" cancel request", false, "#e1e1e3", "black", addIcon);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void acceptFriendRequest() {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            removeFromColumn(connection, loggedInUsersEmailOrMobileNumber, "friend_requests", secondUsersEmailOrMobileNumber);
            removeFromColumn(connection, secondUsersEmailOrMobileNumber, "submitted_requests", loggedInUsersEmailOrMobileNumber);

            updateOrInsertColumn(connection, loggedInUsersEmailOrMobileNumber, "all_friends", secondUsersEmailOrMobileNumber);
            updateOrInsertColumn(connection, secondUsersEmailOrMobileNumber, "all_friends", loggedInUsersEmailOrMobileNumber);

            scrollContent.getChildren().remove(deleteRequestButton);
            scrollContent.getChildren().remove(requestConfirmationButton);
            showFriendsButton();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelFriendRequest() {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            removeFromColumn(connection, loggedInUsersEmailOrMobileNumber, "submitted_requests", secondUsersEmailOrMobileNumber);
            removeFromColumn(connection, secondUsersEmailOrMobileNumber, "friend_requests", loggedInUsersEmailOrMobileNumber);

            ImageView addIcon = new ImageView(
                    new Image(new File("C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\add-friends-icon-free-vector-removebg-preview.png").toURI().toString())
            );
            addIcon.setFitWidth(35);
            addIcon.setFitHeight(35);

            setupButtonState(" add as friend", false, "#e1e1e3", "black", addIcon);
            addAsFriendButton.setVisible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonState(String text, boolean disabled, String bgColor, String textColor, ImageView icon) {
        addAsFriendButton.setText(text);
        addAsFriendButton.setDisable(disabled);
        addAsFriendButton.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        if (icon != null) {
            addAsFriendButton.setGraphic(icon);
        }
        else {
            addAsFriendButton.setGraphic(null);
        }
    }

    private void createDeleteRequestButton() {
        deleteRequestButton = new Button("delete request");
        deleteRequestButton.setLayoutX(1099);
        deleteRequestButton.setLayoutY(467);
        deleteRequestButton.setStyle("-fx-background-color: #e1e1e3; -fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;");
        deleteRequestButton.setPrefWidth(155);
        deleteRequestButton.setPrefHeight(38);

        deleteRequestButton.setOnAction(e -> {
            try {
                Connection connection = DriverManager.getConnection(
                        HelloApplication.url, HelloApplication.username, HelloApplication.password);
                connection.createStatement().execute("use mydb");

                removeFromColumn(connection, loggedInUsersEmailOrMobileNumber, "friend_requests", secondUsersEmailOrMobileNumber);
                removeFromColumn(connection, secondUsersEmailOrMobileNumber, "submitted_requests", loggedInUsersEmailOrMobileNumber);

                scrollContent.getChildren().remove(deleteRequestButton);
                scrollContent.getChildren().remove(requestConfirmationButton);

                ImageView addIcon = new ImageView(new Image("C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\add-friends-icon-free-vector-removebg-preview.png"));
                addIcon.setFitWidth(35);
                addIcon.setFitHeight(35);

                setupButtonState(" add as friend", false, "#e1e1e3", "black", addIcon);
                addAsFriendButton.setVisible(true);
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        scrollContent.getChildren().add(deleteRequestButton);
    }

    private void showFriendsButton() {
        addAsFriendButton.setVisible(false);

        if (requestConfirmationButton != null) rootPane.getChildren().remove(requestConfirmationButton);
        if (deleteRequestButton != null) rootPane.getChildren().remove(deleteRequestButton);

        friendsButton = new Button("👤✔ friends");

        friendsButton.setLayoutX(850);
        friendsButton.setLayoutY(468);
        friendsButton.setStyle("-fx-background-color: #e1e1e3; -fx-text-fill: black; -fx-font-size: 16px; -fx-font-weight: bold;");
        friendsButton.setPrefWidth(150);
        friendsButton.setPrefHeight(38);

        friendsButton.setOnAction(e -> unfriending());

        scrollContent.getChildren().add(friendsButton);
    }

    private String getColumnValue(Connection connection, String userMobileNumberOrEmail, String column) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select " + column + " from friendsFacebook635265346 where logged_in_users_mobile_number_or_email = ?");
        preparedStatement.setString(1, userMobileNumberOrEmail);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            String data = resultSet.getString(column);
            if (data != null) {
                return data;
            }
            else {
                return "";
            }
        }
        return "";
    }

    private void updateOrInsertColumn(Connection connection, String userMobileNumberOrEmail, String column, String value) throws SQLException {

        String existing = getColumnValue(connection, userMobileNumberOrEmail, column);

        if (!existing.contains(value))
        {
            String newValue;

            if (existing.isEmpty())
            {
                newValue = value;
            }
            else {
                newValue = existing + "," + value;
            }

            PreparedStatement preparedStatement = connection.prepareStatement("select * from friendsFacebook635265346 where logged_in_users_mobile_number_or_email = ?");
            preparedStatement.setString(1, userMobileNumberOrEmail);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                PreparedStatement preparedStatement1 = connection.prepareStatement("update friendsFacebook635265346 set " + column + " = ? where logged_in_users_mobile_number_or_email = ?");
                preparedStatement1.setString(1, newValue);
                preparedStatement1.setString(2, userMobileNumberOrEmail);
                preparedStatement1.executeUpdate();
            }
            else {
                PreparedStatement preparedStatement1 = connection.prepareStatement("insert into friendsFacebook635265346 (logged_in_users_mobile_number_or_email, " + column + ") values (?, ?)");
                preparedStatement1.setString(1, userMobileNumberOrEmail);
                preparedStatement1.setString(2, value);
                preparedStatement1.executeUpdate();
            }
        }
    }

    private void removeFromColumn(Connection connection, String userMobileNumberOrEmail, String column, String value) throws SQLException {
        String existing = getColumnValue(connection, userMobileNumberOrEmail, column);
        if (existing == null || existing.isEmpty()) return;

        StringBuilder updated = new StringBuilder();

        String[] parts = existing.split(",");

        for (int i = 0; i < parts.length; i++) {

            String part = parts[i].trim();

            if (!part.equals(value))
            {
                if (updated.length() > 0)
                {
                    updated.append(",");
                }
                updated.append(part);
            }
        }

        PreparedStatement preparedStatement = connection.prepareStatement("update friendsFacebook635265346 set " + column + " = ? where logged_in_users_mobile_number_or_email = ?");
        preparedStatement.setString(1, updated.toString());
        preparedStatement.setString(2, userMobileNumberOrEmail);
        preparedStatement.executeUpdate();
    }

    private void unfriending() {
        Stage unfriendConfirmation = new Stage();
        unfriendConfirmation.initOwner(scrollContent.getScene().getWindow());

        AnchorPane dialogPane = new AnchorPane();
        dialogPane.setPrefSize(300, 150);
        dialogPane.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1;");

        Label label = new Label("do you want to unfriend this person?");
        label.setLayoutX(30);
        label.setLayoutY(30);
        label.setStyle("-fx-font-size: 15px;");

        Button confirm = new Button("confirm");
        confirm.setLayoutX(50);
        confirm.setLayoutY(90);
        confirm.setPrefWidth(80);

        Button cancel = new Button("cancel");
        cancel.setLayoutX(170);
        cancel.setLayoutY(90);
        cancel.setPrefWidth(80);

        confirm.setOnAction(e -> {
            try {
                Connection connection = DriverManager.getConnection(
                        HelloApplication.url, HelloApplication.username, HelloApplication.password);
                connection.createStatement().execute("use mydb");

                removeFromColumn(connection, loggedInUsersEmailOrMobileNumber, "all_friends", secondUsersEmailOrMobileNumber);
                removeFromColumn(connection, secondUsersEmailOrMobileNumber, "all_friends", loggedInUsersEmailOrMobileNumber);

                unfriendConfirmation.close();

                ImageView addIcon = new ImageView(new Image("C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\add-friends-icon-free-vector-removebg-preview.png"));
                addIcon.setFitWidth(35);
                addIcon.setFitHeight(35);
                setupButtonState(" add as friend", false, "#e1e1e3", "black", addIcon);
                addAsFriendButton.setVisible(true);

                scrollContent.getChildren().remove(friendsButton);
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        cancel.setOnAction(e -> unfriendConfirmation.close());

        dialogPane.getChildren().addAll(label, confirm, cancel);

        Scene scene = new Scene(dialogPane);
        unfriendConfirmation.setScene(scene);
        unfriendConfirmation.show();
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

    private void displayUserInfo() {

        if (infoVBox == null) return;

        infoVBox.getChildren().clear();
        infoVBox.setSpacing(10);
        infoVBox.setStyle("-fx-padding: 15;");

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select * from usersFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, secondUsersEmailOrMobileNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            String birthday = null;
            String gender = null;

            if (resultSet.next()) {
                birthday = resultSet.getString("birthday");
                gender = resultSet.getString("gender");

                PreparedStatement introStmt = connection.prepareStatement("select * from introductionFacebook635265346 where mobile_number_or_email = ?");
                introStmt.setString(1, secondUsersEmailOrMobileNumber);
                ResultSet introRs = introStmt.executeQuery();

                if (introRs.next()) {
                    addInfoItem("bio", introRs.getString("biography"));
                    addInfoItem("job", introRs.getString("job"));
                    addInfoItem("education (School)", introRs.getString("education_public_school"));
                    addInfoItem("education (Higher)", introRs.getString("education_higher"));
                    addInfoItem("current City", introRs.getString("actual_city"));
                    addInfoItem("hometown", introRs.getString("hometown"));
                    addInfoItem("relationship", introRs.getString("relationship"));
                    addInfoItem("family Members", introRs.getString("family_members"));
                    addInfoItem("birthday", birthday);
                    addInfoItem("gender", gender);

                    String visibility = introRs.getString("visibility");
                    boolean canSeeContact = canViewContactInfo(visibility);

                    if (canSeeContact) {
                        addInfoItem("contact", secondUsersEmailOrMobileNumber);
                    }
                    else {
                        addInfoItem("contact", "not available based on privacy settings");
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addInfoItem(String label, String value) {
        if (value == null || value.trim().isEmpty()) return;

        HBox itemBox = new HBox(10);
        itemBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(label + ": ");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 14px;");
        valueLabel.setWrapText(true);

        itemBox.getChildren().addAll(titleLabel, valueLabel);
        infoVBox.getChildren().add(itemBox);
    }

    private boolean canViewContactInfo(String visibility) {
        if (visibility == null) return true;

        if (loggedInUsersEmailOrMobileNumber.equals(secondUsersEmailOrMobileNumber)) {
            return true;
        }

        switch (visibility.toLowerCase()) {
            case "public":
                return true;
            case "friends":
                return areFriends(loggedInUsersEmailOrMobileNumber, secondUsersEmailOrMobileNumber);
            case "only me":
                return false;
            default:
                return true;
        }
    }
}