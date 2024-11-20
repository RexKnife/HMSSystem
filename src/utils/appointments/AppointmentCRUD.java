package utils.appointments;

import datamgmt.retrievers.AppointmentData;
import utils.enums.AppointmentStatus;
import utils.medicalrecords.OutcomeRecord;
import utils.ValidationUtils;

import java.io.IOException;
import java.util.List;
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
     * Confirms an appointment, marking its status as "CONFIRMED".
     *
     * @param appointmentID the ID of the appointment to confirm
     */
    public void confirmAppointment(String appointmentID) {
        // Retrieve the appointment by ID
        List<Appointment> appointments = data.getAppointments();
        Appointment appointmentToConfirm = appointments.stream()
                .filter(appt -> appt.getAppointmentID().equalsIgnoreCase(appointmentID))
                .findFirst()
                .orElse(null);

        if (appointmentToConfirm == null) {
            System.out.println("Appointment not found for ID: " + appointmentID);
            return;
        }

        // Check if the appointment is eligible for confirmation
        if (appointmentToConfirm.getStatus() != AppointmentStatus.PENDING) {
            System.out.println("Appointment with ID: " + appointmentID + " cannot be confirmed as it is not in PENDING status.");
            return;
        }

        // Update the appointment status
        appointmentToConfirm.updateStatus(AppointmentStatus.ACCEPTED);

        // Save the updated appointments list
        try {
            data.writeData();
            System.out.println("Appointment with ID: " + appointmentID + " has been confirmed successfully.");
        } catch (IOException e) {
            System.err.println("Error saving appointment data: " + e.getMessage());
        }
    }

    /**
     * Schedules a new appointment for a given patient and doctor.
     *
     * @param patientID the patient's ID
     * @param doctorID  the doctor's ID
     * @param date      the date of the appointment in "dd/MM/yyyy"
     * @param time      the time of the appointment in "HH:mm"
     * @param status    the status of the appointment
     */
    public void scheduleAppointment(String patientID, String doctorID, String date, String time, AppointmentStatus status) {
        if (!ValidationUtils.isFutureDate(date, time, "dd/MM/yyyy", "HH:mm")) {
            System.out.println("Error: The selected date and time have already passed.");
            return;
        }

        String appointmentID = "APPT" + System.currentTimeMillis();
        Appointment newAppointment = new Appointment(appointmentID, patientID, doctorID, date, time, status, null);

        try {
            data.getAppointments().add(newAppointment);
            data.writeData();
            System.out.println("Appointment scheduled successfully.");
        } catch (Exception e) {
            System.err.println("Error scheduling appointment: " + e.getMessage());
        }
    }

    /**
     * Updates an existing appointment's date and time.
     *
     * @param appointmentID the appointment ID
     * @param newDate       the new date in "dd/MM/yyyy"
     * @param newTime       the new time in "HH:mm"
     */
    public void rescheduleAppointment(String appointmentID, String newDate, String newTime) {
        List<Appointment> appointments = data.getAppointments();

        Appointment appointment = appointments.stream()
                .filter(a -> a.getAppointmentID().equals(appointmentID))
                .findFirst()
                .orElse(null);

        if (appointment == null) {
            System.out.println("Appointment not found.");
            return;
        }

        appointment.setDate(newDate);
        appointment.setTime(newTime);

        try {
            data.writeData();
            System.out.println("Appointment rescheduled successfully.");
        } catch (Exception e) {
            System.err.println("Error rescheduling appointment: " + e.getMessage());
        }
    }

    /**
     * Cancels an appointment by setting its status to "CANCELLED".
     *
     * @param appointmentID the appointment ID
     */
    public void cancelAppointment(String appointmentID) {
        List<Appointment> appointments = data.getAppointments();

        Appointment appointment = appointments.stream()
                .filter(a -> a.getAppointmentID().equals(appointmentID))
                .findFirst()
                .orElse(null);

        if (appointment == null) {
            System.out.println("Appointment not found.");
            return;
        }

        appointment.updateStatus(AppointmentStatus.CANCELLED);

        try {
            data.writeData();
            System.out.println("Appointment cancelled successfully.");
        } catch (Exception e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
        }
    }

    /**
     * Adds or updates an outcome record for an appointment.
     *
     * @param appointmentID the appointment ID
     * @param outcomeRecord the outcome record
     */
    public void addOutcomeRecord(String appointmentID, OutcomeRecord outcomeRecord) {
        List<Appointment> appointments = data.getAppointments();

        Appointment appointment = appointments.stream()
                .filter(a -> a.getAppointmentID().equals(appointmentID))
                .findFirst()
                .orElse(null);

        if (appointment == null) {
            System.out.println("Appointment not found.");
            return;
        }

        appointment.setOutcomeRecord(outcomeRecord);

        try {
            data.writeData();
            System.out.println("Outcome record added successfully.");
        } catch (Exception e) {
            System.err.println("Error adding outcome record: " + e.getMessage());
        }
    }

    /**
     * Retrieves a list of appointments based on filters.
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
     * Updates an existing appointment in the system.
     *
     * @param updatedAppointment the updated Appointment object
     * @throws IllegalArgumentException if the appointment is not found
     */
    public void updateAppointment(Appointment updatedAppointment) {
        if (updatedAppointment == null || updatedAppointment.getAppointmentID() == null) {
            throw new IllegalArgumentException("Updated appointment or its ID cannot be null.");
        }

        // Check if the appointment exists in the data
        List<Appointment> appointments = data.getAppointments();
        boolean appointmentFound = false;

        for (Appointment currentAppointment : appointments) {
            if (currentAppointment.getAppointmentID().equals(updatedAppointment.getAppointmentID())) {
                appointmentFound = true;
                break;
            }
        }

        if (!appointmentFound) {
            throw new IllegalArgumentException("Appointment with ID " + updatedAppointment.getAppointmentID() + " not found.");
        }

        // Update the appointment in the data
        try {
            List<Appointment> updatedAppointments = appointments.stream()
                    .map(appointment -> appointment.getAppointmentID().equals(updatedAppointment.getAppointmentID())
                            ? updatedAppointment
                            : appointment)
                    .collect(Collectors.toList());

            // Save the updated list
            data.getAppointments().clear();
            data.getAppointments().addAll(updatedAppointments);
            data.writeData(); // Persist changes
        } catch (IOException e) {
            System.err.println("Error updating appointment: " + e.getMessage());
        }
    }

}
