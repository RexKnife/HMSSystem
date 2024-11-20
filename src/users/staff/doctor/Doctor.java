package users.staff.doctor;

import users.Users;
import utils.enums.Gender;
import utils.enums.Roles;

/**
 * Represents a doctor user with specific attributes and functionality.
 */
public class Doctor extends Users {

    private DoctorUI ui; // Reference to the UI class

    /**
     * Constructs a Doctor instance.
     *
     * @param userID   the unique ID of the doctor
     * @param name     the name of the doctor
     * @param gender   the gender of the doctor (as {@link Gender})
     * @param age      the age of the doctor
     * @param password the password for the doctor's account
     */
    public Doctor(String userID, String name, Gender gender, int age, String password) {
        super(userID, name, Roles.DOCTOR, gender, age, password);
    }

    /**
     * Lazily initializes the Administrator UI to avoid recursive dependency issues.
     */
    @Override
    public void initializeUI() {
        if(ui == null)ui = new DoctorUI(this.getUserID());
        ui.displayMenu(); // Delegate menu display to AdministratorUI
    }

    /**
     * Returns a string representation of the doctor's attributes.
     *
     * @return a string representation of the doctor
     */
    @Override
    public String toString() {
        return String.format("Doctor [ID: %s, Name: %s, Role: %s, Gender: %s, Age: %d]",
                getUserID(), getName(), getRole(), getGender(), getAge());
    }
}
