package com.example.facebook;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class Messanger {

    @FXML
    private VBox friendsVBox;
    @FXML
    private VBox messagesVBox;
    @FXML
    private TextField messageInput;
    @FXML
    private Label createGroupLabel;
    @FXML
    private ImageView chatProfileImage;
    @FXML
    private Label chatNameLabel;
    @FXML
    private ImageView facebookIcon;

    private String loggedInUser;
    private String selectedFriend;
    private String selectedGroup;

    public void initialize() {
        loggedInUser = CreateNewAccount.loggedInEmailOrMobile;
        createGroupLabel.setOnMouseClicked(e -> openCreateGroupPopup());
        loadAllChats();

        facebookIcon.setOnMouseClicked(e -> goToHomePage());
    }

    private void loadAllChats() {
        friendsVBox.getChildren().clear();

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select all_friends from friendsFacebook635265346 where logged_in_users_mobile_number_or_email = ?");
            preparedStatement.setString(1, loggedInUser);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                String allFriendsString = resultSet.getString("all_friends");
                if (allFriendsString == null) {
                    allFriendsString = "";
                }

                String[] friends = allFriendsString.split(",");

                for (int i = 0; i < friends.length; i++) {
                    String friend = friends[i];

                    if (!friend.isBlank())
                    {
                        addFriendToList(friend.trim(), connection);
                    }
                }
            }

            PreparedStatement preparedStatement1 = connection.prepareStatement("select distinct group_name from group_chatsFacebook635265346 where member_email_or_mobile = ?");
            preparedStatement1.setString(1, loggedInUser);

            ResultSet resultSet1 = preparedStatement1.executeQuery();

            while (resultSet1.next()) {
                String groupName = resultSet1.getString("group_name");
                addGroupToList(groupName);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addFriendToList(String friendEmail, Connection connection) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement("select first_name, last_name, profile_picture from usersFacebook635265346 where mobile_number_or_email = ?");
        preparedStatement.setString(1, friendEmail);

        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            String profilePic = resultSet.getString("profile_picture");

            HBox hBox = createUserHBox(profilePic, firstName, lastName);
            hBox.setOnMouseClicked(e -> {
                selectedFriend = friendEmail;
                selectedGroup = null;
                chatProfileImage.setImage(new Image("file:" + profilePic));
                chatNameLabel.setText(firstName + " " + lastName);
                loadMessages();
            });

            friendsVBox.getChildren().add(hBox);
        }
    }

    private void addGroupToList(String groupName) {
        HBox box = new HBox(10);
        Label name = new Label(groupName);
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label more = new Label("...");
        more.setOnMouseClicked(e -> openLeaveGroupPopup(groupName));

        box.getChildren().addAll(name, more);
        box.setOnMouseClicked(e -> {
            selectedGroup = groupName;
            selectedFriend = null;
            chatNameLabel.setText(groupName);
            loadGroupImage(groupName);
            loadMessages();
        });

        friendsVBox.getChildren().add(box);
    }

    private void openLeaveGroupPopup(String groupName) {
        Stage popup = new Stage();
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: white;");
        Label leaveLabel = new Label("leave group chat?");
        Button leaveBtn = new Button("leave");

        leaveBtn.setOnAction(e -> {
            try {
                Connection connection = DriverManager.getConnection(
                        HelloApplication.url, HelloApplication.username, HelloApplication.password);
                connection.createStatement().execute("use mydb");

                PreparedStatement preparedStatement = connection.prepareStatement("delete from group_chatsFacebook635265346 where group_name = ? and member_email_or_mobile = ?");
                preparedStatement.setString(1, groupName);
                preparedStatement.setString(2, loggedInUser);
                preparedStatement.executeUpdate();
                popup.close();
                loadAllChats();
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
        });

        vbox.getChildren().addAll(leaveLabel, leaveBtn);
        popup.setScene(new Scene(vbox));
        popup.initModality(Modality.NONE);
        popup.initOwner(facebookIcon.getScene().getWindow());
        popup.show();
        popup.toFront();
        popup.centerOnScreen();
    }

    private void loadGroupImage(String groupName) {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select image_path from group_chatsFacebook635265346 where group_name = ?");
            preparedStatement.setString(1, groupName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String path = resultSet.getString("image_path");
                chatProfileImage.setImage(new Image("file:" + path));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openCreateGroupPopup() {
        Stage popup = new Stage();
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-background-color: white;");

        Label title = new Label("select group members");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label nameLbl = new Label("group name:");
        TextField groupNameField = new TextField();

        Label imgLbl = new Label("group picture:");
        Button browseBtn = new Button("browse");

        ImageView previewImage = new ImageView();
        previewImage.setFitWidth(100);
        previewImage.setFitHeight(100);
        previewImage.setPreserveRatio(true);

        final File[] selectedImage = new File[1];

        browseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(popup);
            if (file != null) {
                selectedImage[0] = file;
                previewImage.setImage(new Image("file:" + file.getAbsolutePath()));
            }
        });

        VBox membersBox = new VBox(5);
        List<CheckBox> checkboxes = new ArrayList<>();

        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("select all_friends from friendsFacebook635265346 where logged_in_users_mobile_number_or_email = ?");
            preparedStatement.setString(1, loggedInUser);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                String allFriendsString = resultSet.getString("all_friends");

                if (allFriendsString == null) {
                    allFriendsString = "";
                }

                String[] friends = allFriendsString.split(",");

                for (int i = 0; i < friends.length; i++) {

                    String friend = friends[i];

                    if (!friend.isBlank()) {

                        PreparedStatement preparedStatement1 = connection.prepareStatement("select first_name, last_name, profile_picture from usersFacebook635265346 where mobile_number_or_email = ?");
                        preparedStatement1.setString(1, friend);

                        ResultSet resultSet1 = preparedStatement1.executeQuery();

                        if (resultSet1.next()) {
                            String firstName = resultSet1.getString("first_name");
                            String lastName = resultSet1.getString("last_name");
                            String profilePicture = resultSet1.getString("profile_picture");

                            ImageView img = new ImageView(new Image("file:" + profilePicture));
                            img.setFitWidth(30);
                            img.setFitHeight(30);
                            Label name = new Label(firstName + " " + lastName);
                            HBox hBox = new HBox(5);
                            CheckBox checkBox = new CheckBox();
                            checkboxes.add(checkBox);
                            hBox.getChildren().addAll(checkBox, img, name);
                            membersBox.getChildren().add(hBox);
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        Button cancel = new Button("cancel");
        Button create = new Button("create");

        cancel.setOnAction(e -> popup.close());
        create.setOnAction(e -> {
            String groupName = groupNameField.getText().trim();
            if (!groupName.isEmpty())
            {
                try {
                    Connection connection = DriverManager.getConnection(
                            HelloApplication.url, HelloApplication.username, HelloApplication.password);
                    connection.createStatement().execute("use mydb");
                    String imgPath;
                    if (selectedImage[0] != null) {
                        imgPath = selectedImage[0].getAbsolutePath();
                    }
                    else {
                        imgPath = "";
                    }

                    for (int i = 0; i < checkboxes.size(); i++) {
                        CheckBox checkBox = checkboxes.get(i);

                        if (checkBox.isSelected()) {
                            String fullName = ((Label) ((HBox) checkBox.getParent()).getChildren().get(2)).getText();
                            String[] nameParts = fullName.split(" ");

                            PreparedStatement preparedStatement = connection.prepareStatement("select mobile_number_or_email from usersFacebook635265346 where first_name = ? and last_name = ?");
                            preparedStatement.setString(1, nameParts[0]);
                            preparedStatement.setString(2, nameParts[1]);

                            ResultSet resultSet = preparedStatement.executeQuery();

                            if (resultSet.next()) {
                                String email = resultSet.getString("mobile_number_or_email");
                                PreparedStatement insert = connection.prepareStatement("insert into group_chatsFacebook635265346 (group_name, member_email_or_mobile, image_path) values (?, ?, ?)");
                                insert.setString(1, groupName);
                                insert.setString(2, email);
                                insert.setString(3, imgPath);
                                insert.executeUpdate();
                            }
                        }
                    }

                    PreparedStatement insertSelf = connection.prepareStatement("insert into group_chatsFacebook635265346 (group_name, member_email_or_mobile, image_path) values (?, ?, ?)");
                    insertSelf.setString(1, groupName);
                    insertSelf.setString(2, loggedInUser);
                    insertSelf.setString(3, imgPath);
                    insertSelf.executeUpdate();

                    popup.close();
                    loadAllChats();
                }
                catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        layout.getChildren().addAll(title, nameLbl, groupNameField, imgLbl, browseBtn, previewImage, membersBox, new HBox(10, cancel, create));
        popup.setScene(new Scene(layout));
        popup.initModality(Modality.NONE);
        popup.initOwner(facebookIcon.getScene().getWindow());
        popup.show();
        popup.toFront();
        popup.centerOnScreen();
    }

    private void loadMessages() {
        messagesVBox.getChildren().clear();
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement;

            if (selectedGroup != null) {
                preparedStatement = connection.prepareStatement("select sender_email_or_mobile, message_text from messagesFacebook635265346 where group_name = ? order by timestamp asc");
                preparedStatement.setString(1, selectedGroup);
            }
            else {
                preparedStatement = connection.prepareStatement("select sender_email_or_mobile, message_text from messagesFacebook635265346 where (sender_email_or_mobile = ? and receiver_email_or_mobile = ?) or (sender_email_or_mobile = ? and receiver_email_or_mobile = ?) order by timestamp asc");
                preparedStatement.setString(1, loggedInUser);
                preparedStatement.setString(2, selectedFriend);
                preparedStatement.setString(3, selectedFriend);
                preparedStatement.setString(4, loggedInUser);
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                String sender = resultSet.getString("sender_email_or_mobile");
                String message = resultSet.getString("message_text");

                PreparedStatement preparedStatement1 = connection.prepareStatement("select first_name, last_name, profile_picture from usersFacebook635265346 where mobile_number_or_email = ?");
                preparedStatement1.setString(1, sender);

                ResultSet resultSet1 = preparedStatement1.executeQuery();

                if (resultSet1.next()) {
                    String firstName = resultSet1.getString("first_name");
                    String lastName = resultSet1.getString("last_name");
                    String profilePicture = resultSet1.getString("profile_picture");

                    ImageView image = new ImageView(new Image("file:" + profilePicture));
                    image.setFitWidth(40);
                    image.setFitHeight(40);
                    String fullName = firstName + " " + lastName;

                    if (sender.equals(loggedInUser))
                    {
                        fullName = fullName + " (you)";
                    }
                    Label name = new Label(fullName);

                    name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                    Label body = new Label(message);
                    body.setWrapText(true);
                    body.setStyle("-fx-font-size: 14px;");
                    VBox vbox = new VBox(name, body);
                    HBox row = new HBox(10, image, vbox);
                    messagesVBox.getChildren().add(row);
                }
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onMessageInputKeyPressed() {
        messageInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !messageInput.getText().isBlank()) {
                sendMessage(messageInput.getText());
                messageInput.clear();
            }
        });
    }

    private void sendMessage(String message) {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);
            connection.createStatement().execute("use mydb");

            PreparedStatement preparedStatement = connection.prepareStatement("insert into messagesFacebook635265346 (sender_email_or_mobile, receiver_email_or_mobile, message_text, timestamp, group_name) values (?, ?, ?, ?, ?)");
            preparedStatement.setString(1, loggedInUser);
            preparedStatement.setString(2, selectedFriend);
            preparedStatement.setString(3, message);
            preparedStatement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setString(5, selectedGroup);
            preparedStatement.executeUpdate();
            loadMessages();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createUserHBox(String profilePic, String firstName, String lastName) {
        ImageView image = new ImageView(new Image("file:" + profilePic));
        image.setFitWidth(40);
        image.setFitHeight(40);
        Label name = new Label(firstName + " " + lastName);
        name.setStyle("-fx-font-size: 15px;");
        return new HBox(10, image, name);
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
