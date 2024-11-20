package users.patient;

import users.Users;
import utils.ValidationUtils;
import utils.enums.Roles;
import utils.enums.Gender;

import java.time.LocalDate;
import java.time.Period;

/**
 * Represents a Patient in the system with specific attributes and functionality.
 */
public class Patient extends Users {

    private LocalDate dateOfBirth;
    private String bloodType;
    private String contactInfo;
    private PatientUI ui = null; // Reference to the PatientUI class

    /**
     * Constructs a Patient instance.
     *
     * @param userID      the unique ID of the patient
     * @param name        the name of the patient
     * @param dateOfBirth the date of birth of the patient
     * @param gender      the gender of the patient
     * @param bloodType   the blood type of the patient
     * @param contactInfo the contact information of the patient
     * @param password    the password for the patient's account
     */
    public Patient(String userID, String name, LocalDate dateOfBirth, Gender gender, String bloodType, String contactInfo, String password, boolean UI) {
        super(userID, name, Roles.PATIENT, gender, calculateAge(dateOfBirth), password);

        this.dateOfBirth = ValidationUtils.validateDateOfBirth(dateOfBirth, "Patient Date of Birth");
        this.bloodType = ValidationUtils.validateStringNotEmpty(bloodType, "Patient Blood Type");
        this.contactInfo = ValidationUtils.validateContactInfo(contactInfo);

    }

    // Getters and Setters

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = ValidationUtils.validateDateOfBirth(dateOfBirth, "Patient Date of Birth");
        setAge(calculateAge(dateOfBirth)); // Update age based on new date of birth
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = ValidationUtils.validateStringNotEmpty(bloodType, "Patient Blood Type");
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = ValidationUtils.validateContactInfo(contactInfo);
    }

    /**
     * Calculates the age of the patient based on their date of birth.
     *
     * @param dateOfBirth the date of birth of the patient
     * @return the calculated age in years
     */
    private static int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null.");
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Displays the menu for the patient using the {@link PatientUI}.
     */
    @Override
    public void initializeUI() {
        if(ui == null)ui = new PatientUI(this);
        ui.displayMenu(); // Delegate menu display to PatientUI
    }

    /**
     * Returns a string representation of the patient's details.
     */
    @Override
    public String toString() {
        return String.format(
                "Role: %s, Patient ID: %s, Name: %s, Date of Birth: %s, Gender: %s, Blood Type: %s, Contact Info: %s, Age: %d",
                getRole(), getUserID(), getName(), dateOfBirth, getGender(), bloodType, contactInfo, getAge()
        );
    }
}
