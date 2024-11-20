package datamgmt.retrievers;

import utils.env;
import utils.medicalrecords.MedicalRecord;

import java.io.IOException;
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
        String[] parts = line.split(";");
        if (parts.length < 3) {
            System.err.println("Invalid data format: " + line);
            return null;
        }
        String patientID = parts[0].trim();
        String[] diagnoses = parts[1].split(",");
        String[] treatments = parts[2].split(",");

        MedicalRecord record = new MedicalRecord(patientID);
        for (String diagnosis : diagnoses) {
            record.addDiagnosis(diagnosis.trim());
        }
        for (String treatment : treatments) {
            record.addTreatment(treatment.trim());
        }
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
}
