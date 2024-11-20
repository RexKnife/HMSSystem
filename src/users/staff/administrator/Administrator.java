package users.staff.administrator;

import users.Users;
import utils.enums.Gender;
import utils.enums.Roles;

/**
 * Represents an Administrator in the system, extending the base {@link Users} class.
 * An Administrator has additional properties like age and functionality to manage the hospital.
 */
public class Administrator extends Users {
    private int age; // Age of the administrator
    private AdministratorUI ui = null; // Reference to the Administrator UI class for menu operations

    /**
     * Constructs an Administrator object with specified user details.
     *
     * @param userID   Unique identifier for the administrator.
     * @param name     Name of the administrator.
     * @param gender   Gender of the administrator.
     * @param age      Age of the administrator.
     * @param password Password for the administrator's account.
     */
    public Administrator(String userID, String name, Gender gender, int age, String password) {
        super(userID, name, Roles.ADMINISTRATOR, gender, age, password);
        this.age = age;
    }

    /**
     * Gets the age of the administrator.
     *
     * @return Age of the administrator.
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets the age of the administrator.
     *
     * @param age New age of the administrator.
     */
    public void setAge(int age) {
        if (age <= 0) {
            throw new IllegalArgumentException("Age must be a positive value.");
        }
        this.age = age;
    }

    /**
     * Lazily initializes the Administrator UI to avoid recursive dependency issues.
     */
    @Override
    public void initializeUI() {
        if(ui == null)ui = new AdministratorUI(this);
        ui.displayMenu(); // Delegate menu display to AdministratorUI
    }

    /**
     * Returns a string representation of the Administrator object.
     *
     * @return String representation of the administrator.
     */
    @Override
    public String toString() {
        return "Administrator{" +
               "userID='" + getUserID() + '\'' +
               ", name='" + getName() + '\'' +
               ", role='" + getRole() + '\'' +
               ", gender='" + getGender() + '\'' +
               ", age=" + age +
               '}';
    }
}
