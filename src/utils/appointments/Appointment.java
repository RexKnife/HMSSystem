package utils.appointments;

import utils.enums.AppointmentStatus;
import utils.medicalrecords.OutcomeRecord;

/**
 * Represents an appointment between a doctor and a patient.
 */
public class Appointment {
    private String appointmentID;
    private String patientID;
    private String doctorID;
    private String date;
    private String time;
    private AppointmentStatus status;
    private OutcomeRecord outcomeRecord;

    public Appointment(String appointmentID, String patientID, String doctorID, String date, String time,
                       AppointmentStatus status, OutcomeRecord outcomeRecord) {
        this.appointmentID = appointmentID;
        this.patientID = patientID;
        this.doctorID = doctorID;
        this.date = date;
        this.time = time;
        this.status = status;
        this.outcomeRecord = outcomeRecord;
    }

    public String getAppointmentID() {
        return appointmentID;
    }

    public String getPatientID() {
        return patientID;
    }

    public String getDoctorID() {
        return doctorID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void updateStatus(AppointmentStatus newStatus) {
        this.status = newStatus;
    }

    public OutcomeRecord getOutcomeRecord() {
        return outcomeRecord;
    }

    public void setOutcomeRecord(OutcomeRecord outcomeRecord) {
        this.outcomeRecord = outcomeRecord;
    }

    @Override
    public String toString() {
        return String.format(
                "Appointment ID: %s, Patient ID: %s, Doctor ID: %s, Date: %s, Time: %s, Status: %s%nOutcome Record:%n%s",
                appointmentID, patientID, doctorID, date, time, status,
                outcomeRecord != null ? outcomeRecord.toString() : "No outcome recorded yet.");
    }
}
