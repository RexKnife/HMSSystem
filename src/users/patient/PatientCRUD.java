package users.patient;

import datamgmt.retrievers.PatientData;
import utils.enums.Gender;
import users.usermgmt.PasswordHasher;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides CRUD operations for managing patient data.
 */
public class PatientCRUD {

    private final PatientData data;

    /**
     * Constructs the PatientCRUD with the given PatientData handler.
     *
     * @param data the PatientData handler
     */
    public PatientCRUD(PatientData data) {
        this.data = data;
        data.reloadData(); // Load the latest patient data
    }

    /**
     * Creates a new patient with the provided details and displays the generated Patient ID.
     *
     * @param name        the patient's name
     * @param dateOfBirth the patient's date of birth
     * @param gender      the patient's gender
     * @param bloodType   the patient's blood type
     * @param contactInfo the patient's contact information (email)
     */
    public void createPatient(String name, LocalDate dateOfBirth, Gender gender, String bloodType, String contactInfo) {
        // Generate the next unique Patient ID
        String patientID = data.generateNextPatientID();
        // Hash the default password for the patient
        String hashedPassword = PasswordHasher.hashPassword("defaultPassword");

        // Create a new Patient object
        Patient newPatient = new Patient(patientID, name, dateOfBirth, gender, bloodType, contactInfo, hashedPassword, false);

        try {
            // Add the patient to the data repository
            data.addPatient(newPatient);
            // Display the generated Patient ID
            System.out.println("Patient added successfully. Generated Patient ID: " + patientID);
        } catch (IOException e) {
            // Handle any errors during the save process
            System.err.println("Error saving patient: " + e.getMessage());
        }
    }


    /**
     * Searches for patients by optional filters.
     *
     * @param patientID optional patient ID to filter by
     * @param name      optional patient name to filter by
     * @return a list of patients matching the criteria
     */
    public List<Patient> searchPatients(String patientID, String name) {
        List<Patient> patients = data.getAllData();

        if (patientID != null) {
            patients = patients.stream()
                    .filter(p -> p.getUserID().contains(patientID))
                    .collect(Collectors.toList());
        }

        if (name != null) {
            patients = patients.stream()
                    .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return patients;
    }

    /**
     * Updates a specific field of a patient.
     *
     * @param patientID the ID of the patient to update
     * @param field     the field to update (e.g., "name", "dateOfBirth")
     * @param value     the new value for the field
     */
    public void updatePatientField(String patientID, String field, Object value) {
        Patient patient = data.findPatientById(patientID);
        if (patient == null) {
            System.out.println("Patient not found.");
            return;
        }

        switch (field.toLowerCase()) {
            case "name":
                patient.setName((String) value);
                break;
            case "dateofbirth":
                patient.setDateOfBirth((LocalDate) value);
                break;
            case "contactinfo":
                patient.setContactInfo((String) value);
                break;
            case "bloodtype":
                patient.setBloodType((String) value);
                break;
            default:
                System.out.println("Invalid field: " + field);
                return;
        }

        try {
            data.editPatient(patient);
            System.out.println("Patient " + field + " updated successfully.");
        } catch (IOException e) {
            System.err.println("Error updating patient: " + e.getMessage());
        }
    }

    /**
     * Deletes a patient by their Patient ID.
     *
     * @param patientID the ID of the patient to delete
     */
    public void deletePatient(String patientID) {
        try {
            data.removePatient(patientID);
            System.out.println("Patient deleted successfully.");
        } catch (IOException e) {
            System.err.println("Error deleting patient: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
