package datamgmt.retrievers;

import users.patient.Patient;
import utils.ValidationUtils;
import utils.enums.Gender;
import utils.env;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

/**
 * Manages the retrieval, parsing, and management of patient data
 * from the file specified in the {@link env}.
 */
public class PatientData extends BaseDataHandler<Patient> {

    private final env environment;

    /**
     * Initializes the patient data handler with an environment configuration.
     */
    public PatientData() {
        this.environment = new env();
    }

    /**
     * Imports patient data from the file path specified in the {@link env}.
     */
    public void importData() {
        try {
            loadData(environment.getPatientDataPath());
        } catch (IOException e) {
            System.err.println("Error reading patient data: " + e.getMessage());
        }
    }

    /**
     * Reloads all patient data by clearing existing data and re-importing it.
     */
    public void reloadData() {
        clearData();
        importData();
    }

    @Override
    protected Patient parseLine(String line) {
        String[] data = line.split(",");
        if (data.length != 7) {
            System.err.println("Invalid data format: " + line);
            return null;
        }

        try {
            String userID = ValidationUtils.validateStringStartsWith(data[0].trim(), "P", "Patient ID");
            String name = ValidationUtils.validateString(data[1].trim(), "Patient Name");
            LocalDate dateOfBirth = ValidationUtils.validateDateOfBirth(
                    LocalDate.parse(data[2].trim(), createDateFormatter()),
                    "Patient Date of Birth"
            );
            Gender gender = Gender.fromString(data[3].trim());
            String bloodType = ValidationUtils.validateStringNotEmpty(data[4].trim(), "Patient Blood Type");
            String contactInfo = ValidationUtils.validateContactInfo(data[5].trim());
            String password = ValidationUtils.validateStringNotEmpty(data[6].trim(), "Patient Password");

            return new Patient(userID, name, dateOfBirth, gender, bloodType, contactInfo, password, false);
        } catch (Exception e) {
            System.err.println("Error parsing patient data: " + line + " - " + e.getMessage());
            return null;
        }
    }

    @Override
    protected String formatItem(Patient patient) {
        return String.join(",",
                patient.getUserID(),
                patient.getName(),
                patient.getDateOfBirth().toString(),
                patient.getGender().toString(),
                patient.getBloodType(),
                patient.getContactInfo(),
                patient.getPassword()
        );
    }
        /**
     * Generates a unique patient ID by finding the last used ID and incrementing it.
     *
     * @return a new unique patient ID
     */
    public String generateNextPatientID() {
        // Fetch all existing patient IDs
        List<Patient> patients = getAllData();
        if (patients.isEmpty()) {
            return "P1001"; // Default starting ID if no patients exist
        }

        // Find the maximum numeric part of the patient IDs
        int maxId = patients.stream()
                .map(patient -> {
                    try {
                        // Extract numeric part by removing the "P" prefix
                        return Integer.parseInt(patient.getUserID().substring(1));
                    } catch (NumberFormatException e) {
                        return 0; // Handle malformed IDs gracefully
                    }
                })
                .max(Integer::compareTo)
                .orElse(1000); // Default to 1000 if no valid IDs are found

        // Increment the maximum ID and prepend the "P" prefix
        return "P" + (maxId + 1);
    }
    /**
     * Updates an existing patient in the in-memory list and saves changes to the file.
     *
     * @param updatedPatient the patient object with updated details
     * @throws IOException if saving data fails
     */
    public void editPatient(Patient updatedPatient) throws IOException {
        Patient existingPatient = findPatientById(updatedPatient.getUserID());
        if (existingPatient == null) {
            throw new IllegalArgumentException("Patient not found.");
        }
        dataList.remove(existingPatient); // Remove the old entry
        dataList.add(updatedPatient); // Add the updated entry
        writeData(environment.getPatientDataPath()); // Persist changes
    }
    /**
     * Removes a patient from the in-memory list and saves changes to the file.
     *
     * @param patientID the ID of the patient to be removed
     * @throws IOException if saving data fails
     */
    public void removePatient(String patientID) throws IOException {
        Patient patient = findPatientById(patientID);
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found.");
        }
        dataList.remove(patient); // Remove from in-memory list
        writeData(environment.getPatientDataPath()); // Persist changes
    }
        /**
     * Adds a new patient to the in-memory list and saves it to the file.
     *
     * @param patient the patient to be added
     * @throws IOException if saving data fails
     */
    public void addPatient(Patient patient) throws IOException {
        if (findPatientById(patient.getUserID()) != null) {
            throw new IllegalArgumentException("A patient with the same ID already exists.");
        }
        dataList.add(patient); // Add to in-memory list
        writeData(environment.getPatientDataPath()); // Persist changes
    }

    @Override
    protected String getHeader() {
        return "UserID,Name,DateOfBirth,Gender,BloodType,ContactInfo,Password";
    }

    /**
     * Updates the password for a patient and saves the changes to the file.
     *
     * @param patient     the patient whose password needs updating
     * @param newPassword the new hashed password
     */
    public void updatePassword(Patient patient, String newPassword) {
        patient.setPassword(newPassword);
        try {
            writeData(environment.getPatientDataPath());
        } catch (IOException e) {
            System.err.println("Failed to update patient password: " + e.getMessage());
        }
    }

    /**
     * Finds a patient by their unique ID.
     *
     * @param patientID the patient ID to search for
     * @return the corresponding Patient object, or null if not found
     */
    public Patient findPatientById(String patientID) {
        return getAllData().stream()
                .filter(patient -> patient.getUserID().equalsIgnoreCase(patientID))
                .findFirst()
                .orElse(null);
    }

    /**
     * Creates a DateTimeFormatter to parse multiple date formats.
     *
     * @return the DateTimeFormatter
     */
    private DateTimeFormatter createDateFormatter() {
        return new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern("d/M/yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .toFormatter();
    }
}
