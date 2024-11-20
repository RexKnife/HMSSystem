package utils.medicalrecords;

import utils.CSVSerializable;
import utils.enums.PrescriptionStatus;

public class Prescription implements CSVSerializable {

    private String medicationName;
    private int quantity;
    private PrescriptionStatus status;

    /**
     * Constructor to initialize a Prescription with medication name and quantity.
     * Status is set to PENDING by default.
     *
     * @param medicationName the name of the medication
     * @param quantity       the quantity of the medication
     */
    public Prescription(String medicationName, int quantity) {
        if (medicationName == null || medicationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Medication name cannot be null or empty.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number.");
        }
        this.medicationName = medicationName;
        this.quantity = quantity;
        this.status = PrescriptionStatus.PENDING;
    }

    /**
     * Constructor to initialize a Prescription with medication name, quantity, and status.
     *
     * @param medicationName the name of the medication
     * @param quantity       the quantity of the medication
     * @param status         the status of the prescription
     */
    public Prescription(String medicationName, int quantity, PrescriptionStatus status) {
        if (medicationName == null || medicationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Medication name cannot be null or empty.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Prescription status cannot be null.");
        }
        this.medicationName = medicationName;
        this.quantity = quantity;
        this.status = status;
    }

    // Getters and Setters
    public String getMedicationName() {
        return medicationName;
    }

    public void setMedicationName(String medicationName) {
        if (medicationName == null || medicationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Medication name cannot be null or empty.");
        }
        this.medicationName = medicationName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number.");
        }
        this.quantity = quantity;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void updateStatus(PrescriptionStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Prescription status cannot be null.");
        }
        this.status = newStatus;
    }

    // Implementation of CSVSerializable interface

    /**
     * Converts the Prescription to a CSV-compatible string.
     *
     * @return the CSV string representation of the Prescription
     */
    @Override
    public String toCSV() {
        return String.join(",", medicationName, String.valueOf(quantity), status.name());
    }

    /**
     * Parses a CSV string to create a Prescription object.
     *
     * @param csvLine the CSV string
     * @return the parsed Prescription object
     * @throws IllegalArgumentException if the CSV string is invalid
     */
    public static Prescription fromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV line cannot be null or empty.");
        }
        String[] fields = csvLine.split(",", -1); // Allow empty fields
        if (fields.length != 3) {
            throw new IllegalArgumentException("Invalid CSV format for Prescription: " + csvLine);
        }

        String medicationName = fields[0];
        int quantity;
        try {
            quantity = Integer.parseInt(fields[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid quantity format: " + fields[1]);
        }
        PrescriptionStatus status;
        try {
            status = PrescriptionStatus.valueOf(fields[2]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid prescription status: " + fields[2]);
        }

        return new Prescription(medicationName, quantity, status);
    }

    @Override
    public String toString() {
        return String.format("%s (Quantity: %d, Status: %s)", medicationName, quantity, status);
    }
}
