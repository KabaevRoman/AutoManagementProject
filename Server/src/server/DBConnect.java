package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {
    private final String url;
    private final String user;
    private final String pass;

    DBConnect(String hostName, String dbName, String user, String pass) {
        if (hostName.equals("")) {
            hostName = "localhost";
        }
        if (dbName.equals("")) {
            dbName = "UMTSIK";
        }
        if (user.equals("")) {
            user = "postgres";
        }
        if (pass.equals("")) {
            pass = "2019";
        }
        this.url = "jdbc:postgresql://" + hostName + "/" + dbName;
        this.user = user;
        this.pass = pass;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }
}