package com.example.facebook;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class HomePage {
    @FXML
    private Label friends;
    @FXML
    private TextField search;
    @FXML
    private HBox hBox;
    @FXML
    private ImageView profilePictureIcon;
    @FXML
    private ImageView notificationBell;
    @FXML
    private ImageView messanger;
    @FXML
    private Button shareThoughtsButton;
    @FXML
    private ScrollPane postsScrollpane;
    @FXML
    private VBox postsVbox;

    public static String searchTerm = "";
    public static String loggedInFirstName;
    public static String loggedInLastName;

    private final Popup profilePopup = new Popup();
    private boolean isProfilePopupVisible = false;

    private final Popup notificationPopup = new Popup();
    private boolean isNotificationPopupVisible = false;

    @FXML
    private void initialize() {
        loadUserProfile();
        loadAllPosts();

        friends.setOnMouseClicked(event -> Friends());

        search.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                goToSearchResults();
            }
        });

        hBox.setOnMouseClicked(event -> goToMyProfile());
        profilePictureIcon.setOnMouseClicked(event -> logOutPopup());
        notificationBell.setOnMouseClicked(event -> notificationsPopup());
        messanger.setOnMouseClicked(event -> goToMessenger());

        if (shareThoughtsButton != null) {
            shareThoughtsButton.setOnAction(e -> createPostPopup());
        }
    }

    private void logOutPopup() {
        if (isProfilePopupVisible) {
            profilePopup.hide();
            isProfilePopupVisible = false;
            return;
        }
        if (isNotificationPopupVisible) {
            notificationPopup.hide();
            isNotificationPopupVisible = false;
        }

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-color: black;");

        ImageView logoutImage = new ImageView(
                new Image(new File("C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\facebook_logout_button.png").toURI().toString())
        );
        logoutImage.setFitWidth(30);
        logoutImage.setFitHeight(30);

        Label logoutLabel = new Label("logout");
        logoutLabel.setStyle("-fx-font-size: 14px;");

        HBox logoutBox = new HBox(10, logoutImage, logoutLabel);
        logoutBox.setAlignment(Pos.CENTER_LEFT);
        logoutBox.setOnMouseClicked(e -> goToLogin());

        box.getChildren().add(logoutBox);
        profilePopup.getContent().clear();
        profilePopup.getContent().add(box);
        profilePopup.show(profilePictureIcon, profilePictureIcon.localToScreen(0, profilePictureIcon.getFitHeight()).getX(), profilePictureIcon.localToScreen(0, profilePictureIcon.getFitHeight()).getY());

        isProfilePopupVisible = true;
    }

    private void notificationsPopup() {
        if (isNotificationPopupVisible) {
            notificationPopup.hide();
            isNotificationPopupVisible = false;
            return;
        }

        if (isProfilePopupVisible) {
            profilePopup.hide();
            isProfilePopupVisible = false;
        }

        VBox notificationBox = new VBox(30);
        notificationBox.setStyle("-fx-background-color: white; -fx-padding: 10;");
        notificationBox.setAlignment(Pos.TOP_LEFT);
        notificationBox.setPrefWidth(280);

        String loggedInUser = CreateNewAccount.loggedInEmailOrMobile;

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select friend_requests from friendsFacebook635265346 where logged_in_users_mobile_number_or_email = ?");
            preparedStatement.setString(1, loggedInUser);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String requests = resultSet.getString("friend_requests");
                if (requests != null && !requests.isBlank())
                {
                    String[] splitRequests = requests.split(",");
                    for (int i = 0; i < splitRequests.length; i++) {
                        String email = splitRequests[i];
                        if (!email.isBlank())
                        {
                            String[] name = getNameFromEmail(email.trim(), connection);

                            if (name != null)
                            {
                                notificationBox.getChildren().add(createWrappedLabel(name[0], name[1], "sent you a friend request"));
                            }
                        }
                    }
                }
            }

            PreparedStatement preparedStatement1 = connection.prepareStatement("select submitted_requests, all_friends from friendsFacebook635265346 where logged_in_users_mobile_number_or_email = ?");
            preparedStatement1.setString(1, loggedInUser);

            ResultSet resultSet1 = preparedStatement1.executeQuery();

            if (resultSet1.next())
            {
                String submitted = resultSet1.getString("submitted_requests");
                if (submitted == null) {
                    submitted = "";
                }

                String[] submitted1Array = submitted.split(",");
                Set<String> submitted1 = new HashSet<>();

                for (int i = 0; i < submitted1Array.length; i++)
                {
                    submitted1.add(submitted1Array[i]);
                }

                String accepted = resultSet1.getString("all_friends");
                if (accepted == null)
                {
                    accepted = "";
                }

                String[] acceptedArray = accepted.split(",");
                Set<String> accepted1 = new HashSet<>();

                for (int i = 0; i < acceptedArray.length; i++) {
                    accepted1.add(acceptedArray[i]);
                }

                List<String> acceptedList = new ArrayList<>(accepted1);
                for (int i = 0; i < acceptedList.size(); i++) {

                    String acceptedUser = acceptedList.get(i);

                    if (!acceptedUser.isBlank() && submitted1.contains(acceptedUser))
                    {
                        String[] name = getNameFromEmail(acceptedUser.trim(), connection);

                        if (name != null)
                        {
                            notificationBox.getChildren().add(createWrappedLabel(name[0], name[1], "accepted your friend request"));
                        }
                    }
                }
            }

            PreparedStatement preparedStatement2 = connection.prepareStatement("select * from myPostsFacebook635265346 where user_email_or_mobile = ?");
            preparedStatement2.setString(1, loggedInUser);

            ResultSet resultSet2 = preparedStatement2.executeQuery();

            while (resultSet2.next()) {

                String liked = resultSet2.getString("liked_posts");
                if (liked != null && !liked.isBlank())
                {
                    String[] emails = liked.split(";");
                    for (int i = 0; i < emails.length; i++)
                    {
                        String email = emails[i];
                        if (!email.trim().isBlank())
                        {
                            String[] name = getNameFromEmail(email.trim(), connection);
                            if (name != null)
                            {
                                notificationBox.getChildren().add(createWrappedLabel(name[0], name[1], "liked your post"));
                            }
                        }
                    }
                }

                String comments = resultSet2.getString("commented_posts");
                if (comments != null && !comments.isBlank()) {

                    String[] entries = comments.split(";");
                    for (int i = 0; i < entries.length; i++) {
                        String entry = entries[i];

                        if (entry.contains(":"))
                        {
                            String[] parts = entry.split(":", 2);
                            String email = parts[0].trim();
                            String comment = parts[1].trim();

                            String[] name = getNameFromEmail(email, connection);

                            if (name != null)
                            {
                                notificationBox.getChildren().add(createWrappedLabel(name[0], name[1], "commented \"" + comment + "\" under your post"));
                            }
                        }
                    }
                }

                String reactions = resultSet2.getString("reacted_posts");

                if (reactions != null && !reactions.isBlank())
                {
                    String[] entries = reactions.split(";");

                    for (int i = 0; i < entries.length; i++)
                    {
                        String entry = entries[i];
                        if (entry.contains(":"))
                        {
                            String[] parts = entry.split(":", 2);
                            String emoji = parts[0].trim();
                            String email = parts[1].trim();

                            String[] name = getNameFromEmail(email, connection);
                            if (name != null)
                            {
                                notificationBox.getChildren().add(createWrappedLabel(name[0], name[1], "reacted with " + emoji + " under your post"));
                            }
                        }
                    }
                }
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        if (notificationBox.getChildren().isEmpty()) {
            notificationBox.getChildren().add(new Label("no new notifications"));
        }

        ScrollPane scrollPane = new ScrollPane(notificationBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(300, 500);

        notificationPopup.getContent().clear();
        notificationPopup.getContent().add(scrollPane);
        notificationPopup.show(notificationBell, notificationBell.localToScreen(0, notificationBell.getFitHeight()).getX(), notificationBell.localToScreen(0, notificationBell.getFitHeight()).getY());

        isNotificationPopupVisible = true;
    }

    private Label createWrappedLabel(String firstName, String lastName, String action) {

        Text boldName = new Text(firstName + " " + lastName);
        boldName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Text rest = new Text(" " + action);
        rest.setStyle("-fx-font-size: 14px;");

        TextFlow flow = new TextFlow(boldName, rest);
        flow.setPrefWidth(260);
        flow.setLineSpacing(5);
        flow.setStyle("-fx-background-color: transparent;");

        return new Label("", flow);
    }

    private String[] getNameFromEmail(String email, Connection conn) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement("select first_name, last_name from usersFacebook635265346 where mobile_number_or_email = ?");
        preparedStatement.setString(1, email);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return new String[]{
                    resultSet.getString("first_name"), resultSet.getString("last_name")};
        }
        return null;
    }


    private void goToMessenger() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Messanger.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) messanger.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("logInOrSignUp.fxml"));
            Stage stage = (Stage) profilePictureIcon.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToSearchResults() {
        searchTerm = search.getText();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SearchResults.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) search.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void Friends() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Friends.fxml"));
            Stage stage = (Stage) hBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void goToMyProfile() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("MyProfilePosts.fxml"));
            Stage stage = (Stage) hBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void loadUserProfile() {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select first_name, last_name, profile_picture from usersFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, CreateNewAccount.loggedInEmailOrMobile);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                loggedInFirstName = resultSet.getString("first_name");
                loggedInLastName = resultSet.getString("last_name");
                String profilePicturePath = resultSet.getString("profile_picture");

                File file = new File(profilePicturePath);
                Image image = new Image(file.toURI().toString());

                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);

                Label nameLabel = new Label(loggedInFirstName + " " + loggedInLastName);
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 0 0 10;");

                hBox.getChildren().clear();
                hBox.getChildren().addAll(imageView, nameLabel);
            }
            connection.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String getLoggedInUserProfilePicturePath() {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select profile_picture from usersFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, CreateNewAccount.loggedInEmailOrMobile);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("profile_picture");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void createPostPopup() {
        Stage popupStage = new Stage();
        popupStage.initOwner(friends.getScene().getWindow());

        VBox vBox = new VBox(15);
        vBox.setPadding(new Insets(20));
        vBox.setStyle("-fx-background-color: white;");
        vBox.setPrefSize(500, 650);

        Label title = new Label("create a post");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        VBox.setMargin(title, new Insets(0, 0, 0, 150));

        ImageView profileImage = new ImageView();
        profileImage.setFitWidth(50);
        profileImage.setFitHeight(50);
        profileImage.setPreserveRatio(true);

        Label nameLabel = new Label(loggedInFirstName + " " + loggedInLastName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        File profilePicFile = new File(getLoggedInUserProfilePicturePath());
        if (profilePicFile.exists()) {
            profileImage.setImage(new Image(profilePicFile.toURI().toString()));
        }

        HBox userBox = new HBox(10, profileImage, nameLabel);
        userBox.setAlignment(Pos.CENTER_LEFT);

        Button visibilityButton = new Button("public");
        Popup visibilityPopup = new Popup();

        VBox audienceBox = new VBox(10);
        audienceBox.setPadding(new Insets(10));
        audienceBox.setStyle("-fx-background-color: white; -fx-border-color: black;");

        Label audienceTitle = new Label("post audience");
        audienceTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        audienceTitle.setAlignment(Pos.CENTER);

        Label subtitle = new Label("who can see your post?");
        subtitle.setStyle("-fx-font-weight: bold;");

        ToggleGroup group = new ToggleGroup();
        RadioButton publicc = new RadioButton("public");
        publicc.setToggleGroup(group);
        publicc.setSelected(true);

        RadioButton friends = new RadioButton("friends");
        friends.setToggleGroup(group);

        RadioButton onlyMe = new RadioButton("only me");
        onlyMe.setToggleGroup(group);

        Button cancelButton = new Button("cancel");
        cancelButton.setStyle("-fx-background-color: lightgray; -fx-text-fill: black;");

        Button saveButton = new Button("save");
        saveButton.setStyle("-fx-background-color: blue; -fx-text-fill: white;");

        cancelButton.setOnAction(e -> visibilityPopup.hide());
        saveButton.setOnAction(e -> {
            RadioButton selected = (RadioButton) group.getSelectedToggle();
            if (selected != null) {
                visibilityButton.setText(selected.getText());
            }
            visibilityPopup.hide();
        });

        HBox actionButtons = new HBox(10, cancelButton, saveButton);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        audienceBox.getChildren().addAll(audienceTitle, subtitle, publicc, friends, onlyMe, actionButtons);
        visibilityPopup.getContent().add(audienceBox);

        visibilityButton.setOnAction(e -> {
            if (visibilityPopup.isShowing()) visibilityPopup.hide();
            else visibilityPopup.show(visibilityButton, visibilityButton.localToScreen(0, visibilityButton.getHeight()).getX(), visibilityButton.localToScreen(0, visibilityButton.getHeight()).getY());
        });

        TextField postText = new TextField();
        postText.setPromptText("would you mind sharing your thoughts with us?");
        postText.setPrefHeight(100);

        Label addLabel = new Label("add to your post");
        ImageView addImage = new ImageView(new Image(new File("C:\\Users\\user\\Desktop\\Facebook - Copy\\src\\main\\resources\\facebook_social_media_photo.png").toURI().toString()));
        addImage.setFitWidth(40);
        addImage.setFitHeight(30);

        HBox hBox1 = new HBox(10, addLabel, addImage);
        hBox1.setAlignment(Pos.CENTER_LEFT);

        ImageView selectedImageView = new ImageView();
        selectedImageView.setFitWidth(300);
        selectedImageView.setPreserveRatio(true);
        selectedImageView.setVisible(false);

        final File[] selectedFile = new File[1];
        addImage.setOnMouseClicked(e -> {
            FileChooser fileChooser = new FileChooser();
            selectedFile[0] = fileChooser.showOpenDialog(popupStage);
            if (selectedFile[0] != null) {
                selectedImageView.setImage(new Image(selectedFile[0].toURI().toString()));
                selectedImageView.setVisible(true);
            }
        });

        Button postButton = new Button("post");
        postButton.setStyle("-fx-background-color: blue; -fx-text-fill: white; -fx-font-size: 14px;");
        postButton.setPrefWidth(100);

        postButton.setOnAction(e -> {
            String text = postText.getText().trim();

            String imagePath;
            if (selectedFile[0] != null)
            {
                imagePath = selectedFile[0].getAbsolutePath();
            }
            else {
                imagePath = null;
            }

            String visibility = visibilityButton.getText();

            if (text.isEmpty() && imagePath == null) return;

            try {
                Connection connection = DriverManager.getConnection(
                        HelloApplication.url, HelloApplication.username, HelloApplication.password);
                connection.createStatement().execute("use mydb");

                PreparedStatement preparedStatement = connection.prepareStatement("insert into myPostsFacebook635265346 (visibility, user_email_or_mobile, post_text, post_image_path, liked_posts, commented_posts, reacted_posts) values (?, ?, ?, ?, '', '', '')");
                preparedStatement.setString(1, visibility);
                preparedStatement.setString(2, CreateNewAccount.loggedInEmailOrMobile);

                if (text.isEmpty())
                {
                    preparedStatement.setString(3, null);
                }
                else {
                    preparedStatement.setString(3, text);
                }
                preparedStatement.setString(4, imagePath);
                preparedStatement.executeUpdate();
                connection.close();
                popupStage.close();
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        vBox.getChildren().addAll(title, userBox, visibilityButton, postText, hBox1, selectedImageView, postButton);
        Scene scene = new Scene(vBox);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void loadAllPosts() {
        if (postsVbox == null) return;

        postsVbox.getChildren().clear();

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select * from myPostsFacebook635265346 order by post_id desc");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                String visibility = resultSet.getString("visibility");
                int postId = resultSet.getInt("post_id");
                String postText = resultSet.getString("post_text");
                String postImagePath = resultSet.getString("post_image_path");
                String likedPosts = resultSet.getString("liked_posts");
                String commentedPosts = resultSet.getString("commented_posts");
                String reactedPosts = resultSet.getString("reacted_posts");
                String postOwnerEmailOrMobile = resultSet.getString("user_email_or_mobile");

                if (canViewPost(visibility, CreateNewAccount.loggedInEmailOrMobile, postOwnerEmailOrMobile))
                {
                    createPost(postId, postText, postImagePath, likedPosts,
                            commentedPosts, reactedPosts, postOwnerEmailOrMobile);
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

                if (friendsList != null && friendsList.contains(user2))
                {
                    return true;
                }
            }

            preparedStatement = connection.prepareStatement("select all_friends from friendsFacebook635265346 where logged_in_users_mobile_number_or_email = ?");
            preparedStatement.setString(1, user2);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String friendsList = resultSet.getString("all_friends");

                if (friendsList != null && friendsList.contains(user1))
                {
                    return true;
                }
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createPost(int postId, String postText, String postImagePath, String likedPosts, String commentedPosts, String reactedPosts, String postOwnerEmailOrMobile) {
        VBox postBox = new VBox(10);
        postBox.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-color: lightgray; -fx-border-radius: 5;");

        String postOwnerName = "";
        String postOwnerProfilePic = "";
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select first_name, last_name, profile_picture from usersFacebook635265346 where mobile_number_or_email = ?");
            preparedStatement.setString(1, postOwnerEmailOrMobile);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
            {
                postOwnerName = resultSet.getString("first_name") + " " + resultSet.getString("last_name");
                postOwnerProfilePic = resultSet.getString("profile_picture");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        HBox headerBox = new HBox(10);
        ImageView postProfilePic = new ImageView();

        if (postOwnerProfilePic != null && !postOwnerProfilePic.isEmpty())
        {
            try {
                postProfilePic.setImage(new Image(new File(postOwnerProfilePic).toURI().toString()));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        postProfilePic.setFitWidth(40);
        postProfilePic.setFitHeight(40);

        Label nameLabel = new Label(postOwnerName);
        nameLabel.setStyle("-fx-font-weight: bold;");

        String visibility = "";

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select visibility from myPostsFacebook635265346 where post_id = ?");
            preparedStatement.setInt(1, postId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                visibility = resultSet.getString("visibility");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        Label visibilityLabel = new Label(getVisibilityIcon(visibility));
        visibilityLabel.setStyle("-fx-font-size: 12;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(postProfilePic, nameLabel, spacer, visibilityLabel);

        headerBox.setOnMouseClicked(e -> {
            if (!postOwnerEmailOrMobile.equals(CreateNewAccount.loggedInEmailOrMobile)) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("ViewUsersProfile.fxml"));
                    Parent root = loader.load();
                    ViewUsersProfile controller = loader.getController();
                    controller.setUser(postOwnerEmailOrMobile);
                    Stage stage = (Stage) postsVbox.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setFullScreen(true);
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        postBox.getChildren().add(headerBox);

        if (postText != null && !postText.isEmpty())
        {
            Label textLabel = new Label(postText);
            textLabel.setWrapText(true);
            postBox.getChildren().add(textLabel);
        }

        if (postImagePath != null && !postImagePath.isEmpty())
        {
            try
            {
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

        if (likedPosts != null && !likedPosts.isEmpty()) {
            String[] likesArray = likedPosts.split(",");
            likeCount = likesArray.length;
        }

        Label likesLabel = new Label(likeCount + " liked this post");

        Label reactionEmojiLabel = new Label("🙂");
        reactionEmojiLabel.setStyle("-fx-cursor: hand;");

        HBox likesBox = new HBox(10, likesLabel, reactionEmojiLabel);
        postBox.getChildren().add(likesBox);

        reactionEmojiLabel.setOnMouseClicked(e -> showReactionsPopup(postId));

        Label likeButton = new Label("👍");
        likeButton.setStyle("-fx-cursor: hand;");

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
            loadAllPosts();
        });

        commentButton.setOnMouseClicked(e -> showCommentsPopup(postId, commentedPosts));

        if (canViewPost(visibility, CreateNewAccount.loggedInEmailOrMobile, postOwnerEmailOrMobile)) {
            HBox commentInputBox = new HBox(10);
            ImageView commenterProfilePic = new ImageView();
            try {
                String currentUserPic = getCurrentUserProfilePic(CreateNewAccount.loggedInEmailOrMobile);
                if (currentUserPic != null && !currentUserPic.isEmpty())
                {
                    commenterProfilePic.setImage(new Image(new File(currentUserPic).toURI().toString()));
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
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
                    loadAllPosts();
                }
            });
        }

        postsVbox.getChildren().add(postBox);
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
            if (like)
            {
                if (currentLikes == null || currentLikes.isEmpty()) {
                    newLikes = CreateNewAccount.loggedInEmailOrMobile;
                }
                else {
                    newLikes = currentLikes + "," + CreateNewAccount.loggedInEmailOrMobile;
                }
            }
            else {
                if (currentLikes != null && currentLikes.contains(CreateNewAccount.loggedInEmailOrMobile))
                {
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

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showReactionsPopup(int postId) {

        Stage popup = new Stage();
        popup.initOwner(postsVbox.getScene().getWindow());
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
                loadAllPosts();
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

                for (int i = 0; i < existingReactions.length; i++) {
                    String r = existingReactions[i];

                    if (!r.endsWith(CreateNewAccount.loggedInEmailOrMobile))
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
        popup.initOwner(postsVbox.getScene().getWindow());
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
            commenterPic.setImage(new Image(new File(getCurrentUserProfilePic(CreateNewAccount.loggedInEmailOrMobile)).toURI().toString()));
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
                loadAllPosts();
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
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showEmojiPicker(TextField textField) {
        Stage emojiStage = new Stage();
        emojiStage.initOwner(postsVbox.getScene().getWindow());
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
}