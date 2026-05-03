package uga.group18.app.models;

/**
 * Represents a logged-in user. Stored in session scope by UserService.
 */
public class User {

    private final String userId;
    private final String username;
    private final String firstName;
    private final String lastName;

    public User(String userId, String username, String firstName, String lastName) {
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUserId()    { return userId; }
    public String getUsername()  { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
}