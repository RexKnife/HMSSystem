package utils.appointments;

import datamgmt.retrievers.AppointmentData;
import utils.enums.AppointmentStatus;
import utils.medicalrecords.OutcomeRecord;
import utils.ValidationUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides CRUD operations for managing appointment data.
 */
public class AppointmentCRUD {
    private final AppointmentData data;

    /**
     * Constructs the AppointmentCRUD with the given AppointmentData handler.
     *
     * @param data the AppointmentData handler
     */
    public AppointmentCRUD(AppointmentData data) {
        this.data = data;
        this.data.reloadData();
    }

    /**
     * Confirms an appointment by updating its status to "CONFIRMED".
     *
     * @param appointmentID the ID of the appointment to confirm
     */
    public void confirmAppointment(String appointmentID) {
        Optional<Appointment> appointmentOpt = findAppointmentById(appointmentID);

        if (appointmentOpt.isEmpty()) {
            System.out.println("Appointment not found for ID: " + appointmentID);
            return;
        }

        Appointment appointment = appointmentOpt.get();
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            System.out.println("Appointment with ID: " + appointmentID + " cannot be confirmed as it is not in PENDING status.");
            return;
        }

        appointment.updateStatus(AppointmentStatus.ACCEPTED);
        saveAppointments("Appointment confirmed successfully.");
    }

    /**
     * Schedules a new appointment and saves it to the system.
     *
     * @param patientID the patient's ID
     * @param doctorID  the doctor's ID
     * @param date      the appointment date in "dd/MM/yyyy"
     * @param time      the appointment time in "HH:mm"
     * @param status    the status of the appointment
     */
    public void scheduleAppointment(String patientID, String doctorID, String date, String time, AppointmentStatus status) {
        if (!ValidationUtils.isFutureDate(date, time, "dd/MM/yyyy", "HH:mm")) {
            System.out.println("Error: The selected date and time have already passed.");
            return;
        }

        String appointmentID = "APPT" + System.currentTimeMillis();
        Appointment newAppointment = new Appointment(appointmentID, patientID, doctorID, date, time, status, null);

        data.getAppointments().add(newAppointment);
        saveAppointments("Appointment scheduled successfully.");
    }
    /**
     * Updates an existing appointment in the system.
     *
     * @param updatedAppointment the updated Appointment object
     * @throws IllegalArgumentException if the appointment is not found
     */
    public void updateAppointment(Appointment updatedAppointment) {
        if (updatedAppointment == null || updatedAppointment.getAppointmentID() == null) {
            throw new IllegalArgumentException("Updated appointment or its ID cannot be null.");
        }

        List<Appointment> appointments = data.getAppointments();
        Optional<Appointment> existingAppointmentOpt = appointments.stream()
                .filter(appointment -> appointment.getAppointmentID().equals(updatedAppointment.getAppointmentID()))
                .findFirst();

        if (existingAppointmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Appointment with ID " + updatedAppointment.getAppointmentID() + " not found.");
        }

        // Replace the existing appointment with the updated one
        appointments.replaceAll(appointment ->
                appointment.getAppointmentID().equals(updatedAppointment.getAppointmentID())
                        ? updatedAppointment
                        : appointment);

        // Persist the changes
        try {
            data.writeData();
            System.out.println("Appointment updated successfully.");
        } catch (Exception e) {
            System.err.println("Error saving updated appointment: " + e.getMessage());
        }
        data.reloadData();
    }
    /**
     * Reschedules an existing appointment by updating its date and time.
     *
     * @param appointmentID the ID of the appointment to reschedule
     * @param newDate       the new date in "dd/MM/yyyy"
     * @param newTime       the new time in "HH:mm"
     */
    public void rescheduleAppointment(String appointmentID, String newDate, String newTime) {
        Optional<Appointment> appointmentOpt = findAppointmentById(appointmentID);

        if (appointmentOpt.isEmpty()) {
            System.out.println("Appointment not found.");
            return;
        }

        Appointment appointment = appointmentOpt.get();
        appointment.setDate(newDate);
        appointment.setTime(newTime);

        saveAppointments("Appointment rescheduled successfully.");
    }

    /**
     * Cancels an appointment by setting its status to "CANCELLED".
     *
     * @param appointmentID the ID of the appointment to cancel
     */
    public void cancelAppointment(String appointmentID) {
        Optional<Appointment> appointmentOpt = findAppointmentById(appointmentID);

        if (appointmentOpt.isEmpty()) {
            System.out.println("Appointment not found.");
            return;
        }

        Appointment appointment = appointmentOpt.get();
        appointment.updateStatus(AppointmentStatus.CANCELLED);

        saveAppointments("Appointment cancelled successfully.");
    }

    /**
     * Adds or updates an outcome record for an appointment.
     *
     * @param appointmentID the ID of the appointment
     * @param outcomeRecord the outcome record to be added
     */
    public void addOutcomeRecord(String appointmentID, OutcomeRecord outcomeRecord) {
        Optional<Appointment> appointmentOpt = findAppointmentById(appointmentID);

        if (appointmentOpt.isEmpty()) {
            System.out.println("Appointment not found.");
            return;
        }

        Appointment appointment = appointmentOpt.get();
        appointment.setOutcomeRecord(outcomeRecord);

        saveAppointments("Outcome record added successfully.");
    }

    /**
     * Retrieves appointments based on the specified filters.
     *
     * @param patientID the patient ID (optional)
     * @param doctorID  the doctor ID (optional)
     * @param status    the appointment status (optional)
     * @return a list of filtered appointments
     */
    public List<Appointment> getAppointments(String patientID, String doctorID, AppointmentStatus status) {
        return data.getAppointments().stream()
                .filter(a -> (patientID == null || a.getPatientID().equals(patientID)) &&
                             (doctorID == null || a.getDoctorID().equals(doctorID)) &&
                             (status == null || a.getStatus() == status))
                .collect(Collectors.toList());
    }

    /**
     * Displays all appointments matching the given filters.
     *
     * @param patientID the patient ID (optional)
     * @param doctorID  the doctor ID (optional)
     * @param status    the appointment status (optional)
     */
    public void viewAppointments(String patientID, String doctorID, AppointmentStatus status) {
        List<Appointment> appointments = getAppointments(patientID, doctorID, status);

        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
        } else {
            appointments.forEach(System.out::println);
        }
    }

    /**
     * Finds an appointment by its ID.
     *
     * @param appointmentID the ID of the appointment
     * @return an Optional containing the found appointment, or empty if not found
     */
    private Optional<Appointment> findAppointmentById(String appointmentID) {
        return data.getAppointments().stream()
                .filter(a -> a.getAppointmentID().equalsIgnoreCase(appointmentID))
                .findFirst();
    }

    /**
     * Persists all appointments to the file and displays a success message.
     *
     * @param successMessage the message to display upon successful save
     */
    private void saveAppointments(String successMessage) {
        try {
            data.writeData();
            System.out.println(successMessage);
        } catch (Exception e) {
            System.err.println("Error saving appointment data: " + e.getMessage());
        }
        data.reloadData();
    }
}
