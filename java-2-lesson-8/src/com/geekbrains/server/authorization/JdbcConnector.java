package com.geekbrains.server.authorization;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class JdbcConnector {
    private static Connection connection;
    private static Statement statement;
    static Map<String, UserData> users = new HashMap<>();

    public JdbcConnector() {

    }

    public static void connection() {
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // disconnect();
        }
    }

    private static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:C:/Users/Aleh/Desktop/new_folder/java-2-lesson-8/src/com/geekbrains/users.db");
        statement = connection.createStatement();
    }


    public static void disconnect() {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


//    public static void main(String[] args) throws SQLException {
//        connection();
//        logIn();
//    }

    public static Map<String, UserData> logIn() throws SQLException {
        if (statement == null) {
            statement = connection.createStatement();
        }
        try (ResultSet resultSet = statement.executeQuery("select * from  chat_users")) {
            while (resultSet.next()) {
                users.put(resultSet.getString("login"), new UserData(resultSet.getString("login"), resultSet.getString("password"), resultSet.getString("nickName")));
            }
        }
        return users;

    }
}


