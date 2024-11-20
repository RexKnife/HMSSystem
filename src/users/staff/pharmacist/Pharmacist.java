package users.staff.pharmacist;

import users.Users;
import utils.enums.Roles;
import utils.enums.Gender;

/**
 * Represents a Pharmacist in the system with specific attributes and functionality.
 */
public class Pharmacist extends Users {

    private PharmacistUI ui; // Reference to the PharmacistUI class

    /**
     * Constructs a Pharmacist instance.
     *
     * @param userID   the unique ID of the pharmacist
     * @param name     the name of the pharmacist
     * @param gender   the gender of the pharmacist
     * @param age      the age of the pharmacist
     * @param password the password for the pharmacist's account
     */
    public Pharmacist(String userID, String name, Gender gender, int age, String password) {
        super(userID, name, Roles.PHARMACIST, gender, age, password);
    }
    /**
     * Lazily initializes the Pharmacist UI to avoid recursive dependency issues.
     */
    @Override
    public void initializeUI() {
        if(ui == null)ui = new PharmacistUI(this);
        ui.displayMenu(); // Delegate menu display to PharmacistUI
    }

    /**
     * Returns a string representation of the pharmacist's details.
     */
    @Override
    public String toString() {
        return String.format(
                "Staff ID: %s, Name: %s, Role: %s, Gender: %s, Age: %d",
                getUserID(), getName(), getRole(), getGender(), getAge()
        );
    }
}
