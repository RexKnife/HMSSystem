package utils.appointments;

import datamgmt.retrievers.AppointmentData;
import datamgmt.retrievers.AppointmentSlotData;
import utils.appointments.appointmentslots.AppointmentSlot;
import utils.enums.AppointmentStatus;
import utils.enums.WorkingDay;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Provides functionality for scheduling appointments,
 * dynamically calculating available slots based on doctor's availability and existing appointments.
 */
public class AppointmentScheduler {
    private final AppointmentCRUD appointmentCRUD;
    private final AppointmentSlotData slotData;

    /**
     * Constructs the AppointmentScheduler with necessary data handlers.
     *
     * @param appointmentData the data handler for appointments
     * @param slotData        the data handler for appointment slots
     */
    public AppointmentScheduler(AppointmentData appointmentData, AppointmentSlotData slotData) {
        this.appointmentCRUD = new AppointmentCRUD(appointmentData);
        this.slotData = slotData;
        slotData.reloadData();
    }

   public void scheduleAppointment(Scanner scanner, String patientID) {
        System.out.println("\n--- Schedule a New Appointment ---");

        // Prompt for doctor ID
        String doctorID = promptInput(scanner, "Enter Doctor ID (e.g., D001): ");

        // Get the doctor's availability slots
        List<AppointmentSlot> doctorAvailability = slotData.getAllSlots().stream()
                .filter(slot -> slot.getDoctorID().equalsIgnoreCase(doctorID))
                .collect(Collectors.toList());

        if (doctorAvailability.isEmpty()) {
            System.out.println("No availability found for the specified doctor.");
            return;
        }

        // Display doctor's working hours
        System.out.println("\nDoctor's Availability:");
        doctorAvailability.forEach(slot -> System.out.printf("Working Days: %s, Time: %s to %s%n",
                slot.getWorkingDays(), slot.getStartTime(), slot.getEndTime()));

        // Get a validated appointment date
        LocalDate appointmentDate = getValidatedAppointmentDate(scanner, doctorAvailability);

        // Get already booked times for the doctor on the selected date
        List<LocalTime> bookedTimes = appointmentCRUD.getAppointments(null, doctorID, null).stream()
                .filter(appointment -> appointment.getDate().equals(appointmentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .map(appointment -> LocalTime.parse(appointment.getTime(), DateTimeFormatter.ofPattern("HH:mm")))
                .collect(Collectors.toList());

        // Dynamically calculate available times
        List<LocalTime> availableTimes = calculateAvailableTimes(doctorAvailability, appointmentDate, bookedTimes);

        if (availableTimes.isEmpty()) {
            System.out.println("No available times on the selected date.");
            return;
        }

        // Display available times
        System.out.println("\nAvailable Times:");
        availableTimes.forEach(time -> System.out.println(time.format(DateTimeFormatter.ofPattern("HH:mm"))));

        // Get a validated appointment time
        LocalTime appointmentTime = getValidatedAppointmentTime(scanner, availableTimes);

        // Check if the selected date and time have already passed
        LocalDateTime selectedDateTime = LocalDateTime.of(appointmentDate, appointmentTime);
        if (selectedDateTime.isBefore(LocalDateTime.now())) {
            System.out.println("Error: The selected date and time have already passed. Please choose another slot.");
            return; // Exit the method to prevent scheduling
        }

        // Create the appointment
        appointmentCRUD.scheduleAppointment(patientID, doctorID,
                appointmentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                appointmentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                AppointmentStatus.PENDING);

        System.out.println("Appointment scheduled successfully.");
}

    

    /**
     * Calculates available times for appointments based on doctor's availability and booked times.
     *
     * @param availabilitySlots the doctor's availability slots
     * @param appointmentDate   the selected appointment date
     * @param bookedTimes       the times that are already booked
     * @return a list of available times
     */
    private List<LocalTime> calculateAvailableTimes(List<AppointmentSlot> availabilitySlots, LocalDate appointmentDate, List<LocalTime> bookedTimes) {
        List<LocalTime> availableTimes = new ArrayList<>();

        WorkingDay dayOfWeek = WorkingDay.valueOf(appointmentDate.getDayOfWeek().name());

        // Filter availability slots based on the selected day
        for (AppointmentSlot slot : availabilitySlots) {
            if (slot.getWorkingDays().contains(dayOfWeek)) {
                LocalTime currentTime = slot.getStartTime();
                while (!currentTime.isAfter(slot.getEndTime())) {
                    if (!bookedTimes.contains(currentTime)) {
                        availableTimes.add(currentTime);
                    }
                    currentTime = currentTime.plusMinutes(30); // Assuming 30-minute slots
                }
            }
        }
        return availableTimes;
    }

    /**
     * Gets a validated appointment date based on doctor's availability.
     *
     * @param scanner            the scanner for user input
     * @param doctorAvailability the doctor's availability slots
     * @return a validated appointment date
     */
    private LocalDate getValidatedAppointmentDate(Scanner scanner, List<AppointmentSlot> doctorAvailability) {
        while (true) {
            String dateInput = promptInput(scanner, "Enter Appointment Date (dd/MM/yyyy): ");
            try {
                LocalDate appointmentDate = LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                WorkingDay dayOfWeek = WorkingDay.valueOf(appointmentDate.getDayOfWeek().name());

                boolean isWorkingDay = doctorAvailability.stream()
                        .anyMatch(slot -> slot.getWorkingDays().contains(dayOfWeek));

                if (isWorkingDay) {
                    return appointmentDate;
                } else {
                    System.out.println("The selected date is not within the doctor's working days. Please choose another date.");
                }
            } catch (Exception e) {
                System.out.println("Invalid date format or selection. Please try again.");
            }
        }
    }

    /**
     * Gets a validated appointment time from the user.
     *
     * @param scanner        the scanner for user input
     * @param availableTimes the list of available times
     * @return a validated appointment time
     */
    private LocalTime getValidatedAppointmentTime(Scanner scanner, List<LocalTime> availableTimes) {
        while (true) {
            String timeInput = promptInput(scanner, "Enter Appointment Time (HH:mm): ");
            try {
                LocalTime appointmentTime = LocalTime.parse(timeInput, DateTimeFormatter.ofPattern("HH:mm"));
                if (availableTimes.contains(appointmentTime)) {
                    return appointmentTime;
                } else {
                    System.out.println("The selected time is not available. Please choose another time.");
                }
            } catch (Exception e) {
                System.out.println("Invalid time format or selection. Please try again.");
            }
        }
    }

    /**
     * Prompts the user for input with the given message.
     *
     * @param scanner the scanner for user input
     * @param message the message to display
     * @return the user's input
     */
    private String promptInput(Scanner scanner, String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
