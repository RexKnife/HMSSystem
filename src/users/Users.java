package users;

import utils.ValidationUtils;
import utils.enums.Roles;
import utils.enums.Gender;

/**
 * Abstract class representing a generic user in the system.
 * Provides shared attributes and methods for all user types.
 */
public abstract class Users {
    private String userID;
    private String name;
    private Roles role;
    private Gender gender;
    private String password;
    private int age;
    private Object ui;
    /**
     * Constructor for the Users class.
     *
     * @param userID   the unique ID of the user
     * @param name     the name of the user
     * @param role     the role of the user (e.g., DOCTOR, ADMINISTRATOR)
     * @param gender   the gender of the user
     * @param age      the age of the user
     * @param password the password for the user
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Users(String userID, String name, Roles role, Gender gender, int age, String password) {
        ValidationUtils.validateString(name, "Name");
        ValidationUtils.validatePassword(password);
        ValidationUtils.validateAge(age);

        this.userID = userID;
        this.name = name;
        this.role = role;
        this.gender = gender;
        this.age = age;
        this.password = password;
    }

    // Getters
    public String getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public Roles getRole() {
        return role;
    }

    public Gender getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setName(String name) {
        ValidationUtils.validateString(name, "Name");
        this.name = name;
    }

    public void setRole(Roles role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null.");
        }
        this.role = role;
    }

    public void setGender(Gender gender) {
        if (gender == null) {
            throw new IllegalArgumentException("Gender cannot be null.");
        }
        this.gender = gender;
    }

    public void setAge(int age) {
        ValidationUtils.validateAge(age);
        this.age = age;
    }

    public void setPassword(String password) {
        ValidationUtils.validatePassword(password);
        this.password = password;
    }
     /**
     * Lazy initialization for the UI.
     *
     * @return the UI object for the user
     */
    public Object getOrCreateUI() {
        if (ui == null) {
            initializeUI();
        }
        return ui;
    }

    /**
     * Initializes the UI specific to the user subclass.
     * This should be overridden in subclasses to create the appropriate UI.
     */
    protected abstract void initializeUI();

    // Utility Methods
    @Override
    public String toString() {
        return String.format("UserID: %s, Name: %s, Role: %s, Gender: %s, Age: %d",
                userID, name, role.name(), gender.name(), age);
    }
}
