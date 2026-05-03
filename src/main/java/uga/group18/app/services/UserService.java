package uga.group18.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import uga.group18.app.models.User;

/**
 * Handles authentication and registration.
 * Session-scoped so each browser session gets its own instance
 * (and therefore its own loggedInUser).
 */
@Service
@SessionScope
public class UserService {

    private final DataSource dataSource;
    private final BCryptPasswordEncoder passwordEncoder;
    private User loggedInUser = null;

    @Autowired
    public UserService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Checks username + password against the database.
     * Stores the User in session if successful.
     */
    public boolean authenticate(String username, String password) throws SQLException {
        final String sql = "SELECT userId, username, firstName, lastName, password FROM user WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (passwordEncoder.matches(password, storedHash)) {
                        loggedInUser = new User(
                                rs.getString("userId"),
                                rs.getString("username"),
                                rs.getString("firstName"),
                                rs.getString("lastName")
                        );
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Registers a new user. Throws SQLException on duplicate username.
     */
    public boolean registerUser(String username, String password,
                                String firstName, String lastName) throws SQLException {
        final String sql = "INSERT INTO user (username, password, firstName, lastName) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, passwordEncoder.encode(password));
            pstmt.setString(3, firstName);
            pstmt.setString(4, lastName);

            return pstmt.executeUpdate() > 0;
        }
    }

    /** Logs the current user out. */
    public void unAuthenticate() {
        loggedInUser = null;
    }

    public boolean isAuthenticated() {
        return loggedInUser != null;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}