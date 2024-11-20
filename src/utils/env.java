package utils;

/**
 * Represents the environment configuration for file paths used in the Hospital Management System.
 * Provides centralized management of file paths for various data resources.
 */
public class env {
    private final String appointmentSlotDataPath;
    private final String appointmentDataPath;
    private final String medicalRecordPath;
    private final String medicinePath;
    private final String patientDataPath;
    private final String replenishmentRequestDataPath;
    private final String staffListPath;

    /**
     * Initializes the environment configuration with predefined file paths.
     */
    public env() {
        this.appointmentSlotDataPath = "src/datamgmt/datastores/AppointmentSlotData.csv";
        this.appointmentDataPath = "src/datamgmt/datastores/AppointmentData.csv";
        this.medicalRecordPath = "src/datamgmt/datastores/MedicalRecordsData.csv";
        this.medicinePath = "src/datamgmt/datastores/MedicineData.csv";
        this.patientDataPath = "src/datamgmt/datastores/PatientData.csv";
        this.replenishmentRequestDataPath = "src/datamgmt/datastores/ReplenishmentRequestData.csv";
        this.staffListPath = "src/datamgmt/datastores/StaffData.csv";
    }

    /**
     * Gets the file path for appointment slots data.
     * 
     * @return the file path for appointment slots data
     */
    public String getAppointmentSlotDataPath() {
        return appointmentSlotDataPath;
    }

    /**
     * Gets the file path for appointments data.
     * 
     * @return the file path for appointments data
     */
    public String getAppointmentDataPath() {
        return appointmentDataPath;
    }

    /**
     * Gets the file path for medical records data.
     * 
     * @return the file path for medical records data
     */
    public String getMedicalRecordPath() {
        return medicalRecordPath;
    }

    /**
     * Gets the file path for medicines data.
     * 
     * @return the file path for medicines data
     */
    public String getMedicinePath() {
        return medicinePath;
    }

    /**
     * Gets the file path for patient data.
     * 
     * @return the file path for patient data
     */
    public String getPatientDataPath() {
        return patientDataPath;
    }

    /**
     * Gets the file path for replenishment requests data.
     * 
     * @return the file path for replenishment requests data
     */
    public String getReplenishmentRequestDataPath() {
        return replenishmentRequestDataPath;
    }

    /**
     * Gets the file path for staff list data.
     * 
     * @return the file path for staff list data
     */
    public String getStaffListPath() {
        return staffListPath;
    }
}
