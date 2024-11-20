package utils.medicalrecords;

import utils.CSVSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a medical record for a patient, including diagnoses and treatments.
 * Implements the {@link CSVSerializable} interface.
 */
public class MedicalRecord implements CSVSerializable {
    private String patientID;
    private List<String> diagnoses;
    private List<String> treatments;

    /**
     * Constructs a MedicalRecord for the specified patient ID.
     *
     * @param patientID the unique ID of the patient
     */
    public MedicalRecord(String patientID) {
        if (patientID == null || patientID.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty.");
        }
        this.patientID = patientID.trim();
        this.diagnoses = new ArrayList<>();
        this.treatments = new ArrayList<>();
    }

    // Diagnosis Methods
    public void addDiagnosis(String diagnosis) {
        if (diagnosis != null && !diagnosis.isBlank()) {
            diagnoses.add(diagnosis.trim());
        }
    }

    public void setDiagnoses(List<String> diagnoses) {
        this.diagnoses = diagnoses;
    }

    public List<String> getDiagnoses() {
        return diagnoses;
    }

    // Treatment Methods
    public void addTreatment(String treatment) {
        if (treatment != null && !treatment.isBlank()) {
            treatments.add(treatment.trim());
        }
    }

    public void setTreatments(List<String> treatments) {
        this.treatments = treatments;
    }

    public List<String> getTreatments() {
        return treatments;
    }

    // Getters and Setters
    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    /**
     * Converts the MedicalRecord to a CSV-compatible string.
     *
     * @return the CSV string representation of the medical record
     */
    @Override
    public String toCSV() {
        return patientID + ";" +
                String.join(",", diagnoses) + ";" +
                String.join(",", treatments);
    }

    /**
     * Parses a CSV string to create a MedicalRecord instance.
     *
     * @param csvLine the CSV string
     * @return the parsed MedicalRecord object
     * @throws IllegalArgumentException if the CSV string is invalid
     */
    public static MedicalRecord fromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV line cannot be null or empty.");
        }

        String[] parts = csvLine.split(";", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid CSV format for MedicalRecord: " + csvLine);
        }

        String patientID = parts[0].trim();
        List<String> diagnoses = Arrays.asList(parts[1].split(","));
        List<String> treatments = Arrays.asList(parts[2].split(","));

        MedicalRecord record = new MedicalRecord(patientID);
        record.setDiagnoses(new ArrayList<>(diagnoses));
        record.setTreatments(new ArrayList<>(treatments));

        return record;
    }

    @Override
    public String toString() {
        return "Patient ID: " + patientID +
                "\nDiagnoses: " + diagnoses +
                "\nTreatments: " + treatments;
    }
}
