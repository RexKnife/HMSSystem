package utils.appointments.appointmentslots;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import utils.enums.WorkingDay;

/**
 * Represents an appointment slot for a specific doctor, including the doctor's ID, time range, and working days.
 */
public class AppointmentSlot {

    private final String doctorID;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final List<WorkingDay> workingDays; // List of working days as enums

    /**
     * Constructs an AppointmentSlot instance.
     *
     * @param doctorID    the ID of the doctor
     * @param startTime   the start time of the slot
     * @param endTime     the end time of the slot
     * @param workingDays the list of working days for the slot
     */
    public AppointmentSlot(String doctorID, LocalTime startTime, LocalTime endTime, List<WorkingDay> workingDays) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time.");
        }
        this.doctorID = doctorID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workingDays = List.copyOf(workingDays); // Ensure immutability
    }

    /**
     * Retrieves the doctor's ID associated with this slot.
     *
     * @return the doctor ID
     */
    public String getDoctorID() {
        return doctorID;
    }

    /**
     * Retrieves the start time of the slot.
     *
     * @return the start time
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Retrieves the end time of the slot.
     *
     * @return the end time
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Retrieves the working days for this slot.
     *
     * @return an immutable list of working days
     */
    public List<WorkingDay> getWorkingDays() {
        return workingDays;
    }

    /**
     * Converts the list of working days into a semicolon-separated string.
     *
     * @return the formatted string of working days
     */
    private String formatWorkingDays() {
        return workingDays.stream()
                .map(WorkingDay::name) // Convert each enum to its string representation
                .collect(Collectors.joining(";"));
    }

    /**
     * Provides a CSV-compatible string representation of the appointment slot.
     *
     * @return the CSV-compatible string
     */
    @Override
    public String toString() {
        return doctorID + "," + startTime + "," + endTime + "," + formatWorkingDays();
    }

    /**
     * Provides a human-readable string representation of the appointment slot.
     *
     * @return the human-readable string
     */
    public String displayData() {
        return "Doctor ID: " + doctorID +
                ", Start Time: " + startTime +
                ", End Time: " + endTime +
                ", Working Days: " + workingDays.stream()
                                                .map(WorkingDay::name)
                                                .collect(Collectors.joining(", "));
    }
}
