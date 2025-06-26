package com.example.facebook;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class HelloApplication extends Application {
    public static String url = "jdbc:mysql://localhost:3306/sys";
    public static String username = "root";
    public static String password = "";

    @Override
    public void start(Stage stage) throws Exception {
        try {
            Connection connection = DriverManager.getConnection(
                    HelloApplication.url, HelloApplication.username, HelloApplication.password);

            connection.createStatement().execute("use mydb");
            Statement statement = connection.createStatement();
            statement.execute("create table if not exists usersFacebook635265346 (" +
                    "first_name varchar(20), " +
                    "last_name varchar(20), " +
                    "birthday varchar(50), " +
                    "gender varchar(20), " +
                    "mobile_number_or_email varchar(100), " +
                    "password varchar(20), " +
                    "profile_picture varchar(500), " +
                    "cover_photo varchar(500) )");

            statement.execute("create table if not exists friendsFacebook635265346 (" +
                    "logged_in_users_mobile_number_or_email varchar(100), " +
                    "friend_requests text, " +
                    "submitted_requests text, " +
                    "all_friends text, " +
                    "offers text" +
                    ")");

            statement.execute("create table if not exists introductionFacebook635265346 (" +
                    "mobile_number_or_email varchar(100) primary key, " +
                    "biography text, " +
                    "job text, " +
                    "education_public_school text, " +
                    "education_higher text, " +
                    "actual_city text, " +
                    "hometown text, " +
                    "relationship text, " +
                    "family_members text, " +
                    "visibility text)");

            statement.execute("create table if not exists myPostsFacebook635265346 (" +
                    "visibility varchar(20), " +
                    "post_id int primary key auto_increment, " +
                    "user_email_or_mobile varchar(100), " +
                    "post_text text, " +
                    "post_image_path varchar(500), " +
                    "liked_posts text, " +
                    "commented_posts text, " +
                    "reacted_posts text)");

            statement.execute("create table if not exists otherPostsFacebook635265346 (" +
                    "id int primary key auto_increment," +
                    "user_email_or_mobile varchar(100)," +
                    "post_id int, " +
                    "action varchar(20)," +
                    "detail text " +
                    ");");

            statement.execute("create table if not exists messagesFacebook635265346 (" +
                    "id int auto_increment primary key, " +
                    "sender_email_or_mobile varchar(100), " +
                    "receiver_email_or_mobile varchar(100), " +
                    "message_text text, " +
                    "timestamp datetime, " +
                    "group_name varchar(50) " +
                    ");");

            statement.execute("create table if not exists group_chatsFacebook635265346 (" +
                    "group_name varchar(50), " +
                    "member_email_or_mobile varchar(100), " +
                    "image_path text " +
                    ");");


            Parent root = FXMLLoader.load(getClass().getResource("logInOrSignUp.fxml"));

            Scene scene = new Scene(root);
            stage.setTitle("HomePage");

            stage.setFullScreen(true);
            stage.setScene(scene);
            stage.show();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}