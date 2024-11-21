package utils.medicalrecords;

import utils.CSVSerializable;
import utils.enums.PrescriptionStatus;
import utils.ValidationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the outcome record of an appointment, including prescriptions,
 * service type, and consultation notes.
 */
public class OutcomeRecord implements CSVSerializable {
    private String dateOfAppointment;
    private String serviceType;
    private List<Prescription> prescriptions;
    private String consultationNotes;

    /**
     * Constructs an OutcomeRecord with the specified details.
     *
     * @param dateOfAppointment the date of the appointment
     * @param serviceType       the type of service provided
     * @param consultationNotes the consultation notes
     */
    public OutcomeRecord(String dateOfAppointment, String serviceType, String consultationNotes) {
        this.dateOfAppointment = ValidationUtils.validateString(dateOfAppointment, "Date of Appointment");
        this.serviceType = ValidationUtils.validateString(serviceType, "Service Type");
        this.consultationNotes = ValidationUtils.validateString(consultationNotes, "Consultation Notes");
        this.prescriptions = new ArrayList<>();
    }

    /**
     * Updates the status of a prescription by its medication name.
     *
     * @param medicationName the name of the medication
     * @param newStatus      the new status for the prescription
     */
    public void updatePrescriptionStatus(String medicationName, PrescriptionStatus newStatus) {
        for (Prescription prescription : prescriptions) {
            if (prescription.getMedicationName().equalsIgnoreCase(medicationName)) {
                prescription.updateStatus(newStatus);
                break;
            }
        }
    }

    public String getDateOfAppointment() {
        return dateOfAppointment;
    }

    public void setDateOfAppointment(String dateOfAppointment) {
        this.dateOfAppointment = ValidationUtils.validateString(dateOfAppointment, "Date of Appointment");
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = ValidationUtils.validateString(serviceType, "Service Type");
    }

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public void addPrescription(Prescription prescription) {
        prescriptions.add(ValidationUtils.validateObject(prescription, "Prescription"));
    }

    public String getConsultationNotes() {
        return consultationNotes;
    }

    public void setConsultationNotes(String consultationNotes) {
        this.consultationNotes = ValidationUtils.validateString(consultationNotes, "Consultation Notes");
    }

    /**
     * Serializes the OutcomeRecord to a CSV-compatible string.
     *
     * @return the CSV string representation of the outcome record
     */
    @Override
    public String toCSV() {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append(dateOfAppointment).append(","); // Date of appointment
        csvBuilder.append(serviceType).append(",");       // Service type
        csvBuilder.append(consultationNotes).append(","); // Consultation notes

        // Serialize prescriptions
        if (!prescriptions.isEmpty()) {
            csvBuilder.append("[");
            for (int i = 0; i < prescriptions.size(); i++) {
                if (i > 0) csvBuilder.append(";");
                csvBuilder.append(prescriptions.get(i).toCSV());
            }
            csvBuilder.append("]");
        } else {
            csvBuilder.append("[]"); // Empty list indicator
        }

        return csvBuilder.toString();
    }


    /**
     * Deserializes a CSV string to an OutcomeRecord object.
     *
     * @param csv the CSV string
     * @return the OutcomeRecord object
     * @throws IllegalArgumentException if parsing fails
     */
    public static OutcomeRecord fromCSV(String csv) {
        try {
            String[] parts = csv.split(",", 4);
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid OutcomeRecord CSV format.");
            }

            String dateOfAppointment = ValidationUtils.validateString(parts[0], "Date of Appointment");
            String serviceType = ValidationUtils.validateString(parts[1], "Service Type");
            String consultationNotes = ValidationUtils.validateString(parts[2], "Consultation Notes");

            OutcomeRecord outcomeRecord = new OutcomeRecord(dateOfAppointment, serviceType, consultationNotes);

            String prescriptionsData = parts[3].substring(1, parts[3].length() - 1); // Remove brackets
            if (!ValidationUtils.isNullOrEmpty(prescriptionsData)) {
                String[] prescriptionsArray = prescriptionsData.split(";");
                for (String prescriptionCSV : prescriptionsArray) {
                    outcomeRecord.addPrescription(Prescription.fromCSV(prescriptionCSV));
                }
            }

            return outcomeRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse OutcomeRecord from CSV: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        StringBuilder prescriptionDetails = new StringBuilder();
        for (Prescription prescription : prescriptions) {
            prescriptionDetails.append("\n  - ").append(prescription.toString());
        }
        return "Date: " + dateOfAppointment + ", Service: " + serviceType +
                "\nPrescriptions:" + prescriptionDetails +
                "\nConsultation Notes: " + consultationNotes;
    }
}
