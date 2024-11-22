package datamgmt.retrievers;

import utils.env;
import utils.medicalrecords.MedicalRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the retrieval, parsing, and management of medical record data
 * from the file specified in the {@link env}.
 */
public class MedicalRecordData extends BaseDataHandler<MedicalRecord> {
    private final env environment;

    /**
     * Initializes the medical record data handler with an environment configuration.
     */
    public MedicalRecordData() {
        this.environment = new env();
    }

    /**
     * Imports medical record data from the file path specified in the {@link env}.
     */
    public void importData() {
        try {
            loadData(environment.getMedicalRecordPath());
        } catch (IOException e) {
            System.err.println("Error reading medical record data: " + e.getMessage());
        }
    }

    /**
     * Reloads all medical record data by clearing existing data and re-importing it.
     */
    public void reloadData() {
        clearData();
        importData();
    }

    @Override
    protected MedicalRecord parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            System.err.println("Blank line encountered in medical records file.");
            return null;
        }

        String[] parts = line.split(";", -1); // Use -1 to handle empty fields
        if (parts.length != 3) {
            System.err.println("Invalid data format: " + line);
            return null;
        }

        String patientID = parts[0].trim();
        if (patientID.isEmpty()) {
            System.err.println("Patient ID is missing in the line: " + line);
            return null;
        }

        // Handle diagnoses and treatments, defaulting to empty lists if blank
        List<String> diagnoses = parts[1].trim().isEmpty() ? new ArrayList<>() : Arrays.asList(parts[1].split(","));
        List<String> treatments = parts[2].trim().isEmpty() ? new ArrayList<>() : Arrays.asList(parts[2].split(","));

        // Create and populate the medical record
        MedicalRecord record = new MedicalRecord(patientID);
        record.setDiagnoses(new ArrayList<>(diagnoses.stream().map(String::trim).collect(Collectors.toList())));
        record.setTreatments(new ArrayList<>(treatments.stream().map(String::trim).collect(Collectors.toList())));

        return record;
    }


    @Override
    protected String formatItem(MedicalRecord record) {
        return record.getPatientID() + ";" +
                String.join(",", record.getDiagnoses()) + ";" +
                String.join(",", record.getTreatments());
    }

    @Override
    protected String getHeader() {
        return "PatientID;Diagnoses;Treatments";
    }

    /**
     * Retrieves a medical record by patient ID.
     *
     * @param patientID the patient ID to search for
     * @return the corresponding MedicalRecord, or null if not found
     */
    public MedicalRecord getMedicalRecordByPatientID(String patientID) {
        return getAllData().stream()
                .filter(record -> record.getPatientID().equalsIgnoreCase(patientID))
                .findFirst()
                .orElse(null);
    }

    /**
     * Updates or adds a medical record.
     *
     * @param updatedRecord the medical record to update or add
     */
    public void updateMedicalRecord(MedicalRecord updatedRecord) {
        List<MedicalRecord> records = getAllData();
        MedicalRecord existingRecord = records.stream()
                .filter(record -> record.getPatientID().equalsIgnoreCase(updatedRecord.getPatientID()))
                .findFirst()
                .orElse(null);

        if (existingRecord != null) {
            records.remove(existingRecord);
        }
        records.add(updatedRecord);

        try {
            writeData(environment.getMedicalRecordPath());
            System.out.println("Medical record updated successfully.");
        } catch (IOException e) {
            System.err.println("Error updating medical record: " + e.getMessage());
        }
    }

    /**
     * Retrieves all medical records associated with a list of patient IDs.
     *
     * @param patientIDs the list of patient IDs to filter by
     * @return the list of matching medical records
     */
    public List<MedicalRecord> getMedicalRecordsByPatientIDs(List<String> patientIDs) {
        return getAllData().stream()
                .filter(record -> patientIDs.contains(record.getPatientID()))
                .collect(Collectors.toList());
    }

    /**
     * Displays all medical records in a formatted way.
     */
    public void displayAllRecords() {
        List<MedicalRecord> records = getAllData();
        if (records.isEmpty()) {
            System.out.println("No medical records found.");
            return;
        }
        System.out.println("Medical Records:");
        records.forEach(record -> System.out.println(record));
    }
    /**
     * Adds a new medical record to the dataset.
     *
     * @param newRecord the medical record to add
     */
    public void addMedicalRecord(MedicalRecord newRecord) {
        if (newRecord == null || newRecord.getPatientID() == null || newRecord.getPatientID().isEmpty()) {
            throw new IllegalArgumentException("Invalid medical record. Patient ID cannot be null or empty.");
        }

        try {
            // Check for existing record
            MedicalRecord existingRecord = getMedicalRecordByPatientID(newRecord.getPatientID());
            if (existingRecord != null) {
                throw new IllegalArgumentException("Medical record for Patient ID " + newRecord.getPatientID() + " already exists.");
            }

            // Add the new record to in-memory data
            dataList.add(newRecord);

            // Append the new record to the file
            appendData(environment.getMedicalRecordPath(), newRecord);
            System.out.println("Medical record added successfully for Patient ID: " + newRecord.getPatientID());
        } catch (IOException e) {
            System.err.println("Error adding medical record: " + e.getMessage());
        }
    }
}
