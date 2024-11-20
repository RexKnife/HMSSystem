package users.authorization;

import users.*;
import users.usermgmt.PasswordHasher;

import java.util.HashMap;
import java.util.Map;

import datamgmt.retrievers.PatientData;
import datamgmt.retrievers.StaffData;

/**
 * Manages user authentication, authorization, and session control.
 */
public class AuthorizationControl {

    private final Map<String, Users> activeSessions; // Stores active user sessions
    private final Map<String, Users> users; // Stores all loaded users for authentication
    private static String currentUserId; // Stores the ID of the currently logged-in user

    /**
     * Initializes the AuthorizationControl with empty user and session maps.
     */
    public AuthorizationControl() {
        this.activeSessions = new HashMap<>();
        this.users = new HashMap<>();
    }

    /**
     * Attempts to log in a user with the provided user ID and password.
     *
     * @param userID   the user ID to log in
     * @param password the password to authenticate
     * @return the authenticated user object, or null if authentication fails
     */
    public Users login(String userID, String password) {
        System.out.println("Attempting login for user: " + userID);

        Users user = users.get(userID);
        if (user == null) {
            System.out.println("User not found: " + userID);
            return null;
        }

        String hashedInputPassword = PasswordHasher.hashPassword(password);
        if (!user.getPassword().equals(hashedInputPassword)) {
            System.out.println("Invalid password for user: " + userID);
            return null;
        }

        createSession(user);
        System.out.println("Login successful for user: " + userID);
        return user;
    }

    /**
     * Creates a session for the logged-in user.
     *
     * @param user the authenticated user
     */
    private void createSession(Users user) {
        activeSessions.put(user.getUserID(), user);
        currentUserId = user.getUserID();
    }

    /**
     * Logs out a user by removing their session.
     *
     * @param userID the user ID to log out
     */
    public void logout(String userID) {
        activeSessions.remove(userID);
        if (userID.equals(currentUserId)) {
            currentUserId = null;
        }
        System.out.println("User logged out: " + userID);
    }

    /**
     * Checks if a user's role matches the expected role for an operation.
     *
     * @param userID       the user ID to check
     * @param expectedRole the required role for authorization
     * @return true if the user's role matches the expected role, false otherwise
     */
    public boolean authorize(String userID, String expectedRole) {
        Users user = activeSessions.get(userID);
        if (user == null) {
            System.out.println("Authorization failed: No active session for user ID " + userID);
            return false;
        }
        return user.getRole().name().equalsIgnoreCase(expectedRole);
    }

    /**
     * Loads users from the provided staff and patient data managers.
     *
     * @param staffData   the staff data handler
     * @param patientData the patient data handler
     */
    public void loadCredentials(StaffData staffData, PatientData patientData) {
        // Load patients
        patientData.getAllData().forEach(patient -> users.put(patient.getUserID(), patient));

        // Load doctors, administrators, and pharmacists
        staffData.getAllData().forEach(staff -> users.put(staff.getUserID(), staff));

        System.out.println("Staff and patient credentials loaded successfully.");
    }

    /**
     * Gets the currently logged-in user's ID.
     *
     * @return the current user ID
     */
    public static String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Finds the currently logged-in user by their ID.
     *
     * @return the current user object, or null if no user is logged in
     */
    public Users getCurrentUser() {
        if (currentUserId == null) {
            System.out.println("No user is currently logged in.");
            return null;
        }
        return activeSessions.get(currentUserId);
    }
}
