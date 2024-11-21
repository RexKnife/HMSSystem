package users.patient;

import datamgmt.retrievers.AppointmentData;
import datamgmt.retrievers.AppointmentSlotData;
import datamgmt.retrievers.MedicalRecordData;
import datamgmt.retrievers.StaffData;
import users.ui.BaseUI;
import utils.ValidationUtils;
import utils.appointments.*;
import utils.appointments.appointmentslots.AppointmentSlot;
import utils.enums.AppointmentStatus;
import utils.enums.WorkingDay;
import utils.medicalrecords.MedicalRecord;
import utils.medicalrecords.OutcomeRecord;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Provides a user-friendly menu for patients to manage their appointments, medical records, and other features.
 */
public class PatientUI extends BaseUI {
    private final Patient patient;
    private final AppointmentCRUD appointmentCRUD;
    private final MedicalRecordData medicalRecordData;
    private final StaffData staffData;
    /**
     * Constructs the PatientUI with necessary data handlers.
     *
     * @param patient the patient instance
     */
    public PatientUI(Patient patient) {
        this.patient = patient;
        AppointmentData appointmentData = new AppointmentData();
        appointmentData.importData();
        this.appointmentCRUD = new AppointmentCRUD(appointmentData);

        this.medicalRecordData = new MedicalRecordData();
        this.medicalRecordData.importData();

        this.staffData = new StaffData();
        this.staffData.importData();
    }

    @Override
    public void displayMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayMenuHeader("PATIENT MENU");
            displayMenuOption(1, "View Medical Record");
            displayMenuOption(2, "View Available Appointment Slots");
            displayMenuOption(3, "Schedule an Appointment");
            displayMenuOption(4, "Reschedule an Appointment");
            displayMenuOption(5, "Cancel an Appointment");
            displayMenuOption(6, "View Scheduled Appointments");
            displayMenuOption(7, "View Past Appointment Outcome Records");
            displayMenuOption(8, "Logout");

            int choice = getMenuChoice();

            if (!isValidChoice(choice, 1, 8)) {
                displayInvalidInputMessage();
                pauseForUser();
                continue;
            }

            switch (choice) {
                case 1:
                    viewMedicalRecord();
                    break;
                case 2:
                    viewAvailableAppointmentSlots(this.staffData);
                    break;
                case 3:
                    scheduleAppointment(scanner);
                    break;
                case 4:
                    rescheduleAppointment(scanner);
                    break;
                case 5:
                    cancelAppointment(scanner);
                    break;
                case 6:
                    viewScheduledAppointments();
                    break;
                case 7:
                    viewPastAppointments();
                    break;
                case 8:
                    System.out.println("Logging out...");
                    pauseForUser();
                    return;
                default:
                    displayInvalidInputMessage();
            }
            pauseForUser();
        }
    }

    private void viewMedicalRecord() {
        displayMenuHeader("VIEW MEDICAL RECORD");
    
        // Retrieve the medical record for the patient
        MedicalRecord record = medicalRecordData.getMedicalRecordByPatientID(patient.getUserID());
    
        // Check if a record exists
        if (record == null) {
            System.out.println("No medical record found for Patient ID: " + patient.getUserID());
            return;
        }
    
        // Display the medical record in a formatted way
        System.out.println("Patient Medical Record");
        System.out.println("=======================");
        System.out.println("Patient ID: " + record.getPatientID());
    
        System.out.println("\nDiagnoses:");
        if (record.getDiagnoses().isEmpty()) {
            System.out.println(" - No diagnoses found.");
        } else {
            for (int i = 0; i < record.getDiagnoses().size(); i++) {
                System.out.printf(" %d. %s%n", i + 1, record.getDiagnoses().get(i));
            }
        }
    
        System.out.println("\nTreatments:");
        if (record.getTreatments().isEmpty()) {
            System.out.println(" - No treatments found.");
        } else {
            for (int i = 0; i < record.getTreatments().size(); i++) {
                System.out.printf(" %d. %s%n", i + 1, record.getTreatments().get(i));
            }
        }
    }
    
    
    /**
     * Displays available appointment slots, including the doctor's name.
     */
    public void viewAvailableAppointmentSlots(StaffData staffData) {
        System.out.println("\n--- Available Appointment Slots ---");
    
        AppointmentSlotData slotData = new AppointmentSlotData();
        slotData.reloadData(); // Ensure the latest slot data is loaded
    
        List<AppointmentSlot> slots = slotData.getAllSlots();
    
        if (slots.isEmpty()) {
            System.out.println("No appointment slots available at the moment.");
            return;
        }
    
        System.out.printf("%-15s %-30s %-15s %-15s %-30s%n", "Doctor ID", "Doctor Name", "Start Time", "End Time", "Working Days");
        System.out.println("-----------------------------------------------------------------------------------------");
    
        for (AppointmentSlot slot : slots) {
            String doctorName = staffData.findUserById(slot.getDoctorID()) != null
                    ? staffData.findUserById(slot.getDoctorID()).getName()
                    : "Unknown";
    
            System.out.printf("%-15s %-30s %-15s %-15s %-30s%n",
                    slot.getDoctorID(),
                    doctorName,
                    slot.getStartTime(),
                    slot.getEndTime(),
                    slot.getWorkingDays().stream()
                            .map(WorkingDay::name)
                            .collect(Collectors.joining(", ")));
        }
    }
    


    /**
     * Allows the patient to schedule an appointment by selecting a doctor,
     * a date, and an available time slot.
     */
    public void scheduleAppointment(Scanner scanner) {
        System.out.println("\n--- Schedule a New Appointment ---");
    
        // Prompt the user for the doctor ID
        System.out.print("Enter Doctor ID (e.g., D001) or leave blank to cancel: ");
        String doctorID = scanner.nextLine().trim();
        if (doctorID.isEmpty()) {
            System.out.println("Operation cancelled.");
            return;
        }
    
        // Load the doctor's slots
        AppointmentSlotData slotData = new AppointmentSlotData();
        slotData.reloadData();
    
        List<AppointmentSlot> doctorSlots = slotData.getAllSlots()
                .stream()
                .filter(slot -> slot.getDoctorID().equalsIgnoreCase(doctorID))
                .collect(Collectors.toList());
    
        if (doctorSlots.isEmpty()) {
            System.out.println("No availability found for the specified doctor.");
            return;
        }
    
        System.out.println("\nDoctor's Availability:");
        for (AppointmentSlot slot : doctorSlots) {
            System.out.printf("Working Days: %s, Time: %s to %s%n",
                    slot.getWorkingDays(),
                    slot.getStartTime(),
                    slot.getEndTime());
        }
    
        // Get validated date and time
        while (true) {
            try {
                System.out.print("Enter Appointment Date (dd/MM/yyyy) or leave blank to cancel: ");
                String dateInput = scanner.nextLine().trim();
                if (dateInput.isEmpty()) {
                    System.out.println("Operation cancelled.");
                    return;
                }
    
                System.out.print("Enter Appointment Time (HH:mm) or leave blank to cancel: ");
                String timeInput = scanner.nextLine().trim();
                if (timeInput.isEmpty()) {
                    System.out.println("Operation cancelled.");
                    return;
                }
    
                LocalDate date = LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                LocalTime time = LocalTime.parse(timeInput, DateTimeFormatter.ofPattern("HH:mm"));
    
                if (!ValidationUtils.isValidAppointmentTime(doctorSlots, date, time) && !ValidationUtils.isFutureDate(dateInput, timeInput, "dd/MM/yyyy", "HH:mm")) {
                    System.out.println("The selected time is not valid. Please choose another time.");
                    continue;
                }
    
                // Schedule the appointment
                appointmentCRUD.scheduleAppointment(patient.getUserID(), doctorID, dateInput, timeInput, AppointmentStatus.PENDING);
                System.out.println("Appointment scheduled successfully.");
                break;
            } catch (Exception e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }
    

    private void rescheduleAppointment(Scanner scanner) {
        displayMenuHeader("RESCHEDULE AN APPOINTMENT");
    
        // Retrieve appointments eligible for rescheduling
        List<Appointment> appointments = appointmentCRUD.getAppointments(patient.getUserID(), null, null)
                .stream()
                .filter(appt -> appt.getStatus() == AppointmentStatus.PENDING || appt.getStatus() == AppointmentStatus.ACCEPTED)
                .collect(Collectors.toList());
    
        if (appointments.isEmpty()) {
            System.out.println("No appointments to reschedule.");
            return;
        }
    
        // Display appointments with numbering
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appointment = appointments.get(i);
            String doctorName = staffData.findUserById(appointment.getDoctorID()).getName();
            System.out.printf("%d. %-15s %-30s %-12s %-8s %-10s%n",
                    i + 1,
                    appointment.getPatientID(),
                    doctorName,
                    appointment.getDate(),
                    appointment.getTime(),
                    appointment.getStatus());
        }
    
        // Prompt the user to select an appointment
        System.out.print("\nSelect the appointment to reschedule (Enter number or leave blank to cancel): ");
        String selection = scanner.nextLine().trim();
        if (selection.isEmpty()) {
            System.out.println("Operation cancelled.");
            return;
        }
    
        int selectedIndex;
        try {
            selectedIndex = Integer.parseInt(selection) - 1;
            if (selectedIndex < 0 || selectedIndex >= appointments.size()) {
                throw new NumberFormatException("Invalid index");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection. Operation cancelled.");
            return;
        }
    
        Appointment selectedAppointment = appointments.get(selectedIndex);
    
        // Load the doctor's slots
        AppointmentSlotData slotData = new AppointmentSlotData();
        slotData.reloadData();
    
        List<AppointmentSlot> doctorSlots = slotData.getAllSlots()
                .stream()
                .filter(slot -> slot.getDoctorID().equals(selectedAppointment.getDoctorID()))
                .collect(Collectors.toList());
    
        if (doctorSlots.isEmpty()) {
            System.out.println("No available slots for the selected doctor.");
            return;
        }
    
        // Get new date and time with validation
        while (true) {
            try {
                System.out.print("Enter new date (dd/MM/yyyy) or leave blank to cancel: ");
                String newDateInput = scanner.nextLine().trim();
                if (newDateInput.isEmpty()) {
                    System.out.println("Operation cancelled.");
                    return;
                }
    
                System.out.print("Enter new time (HH:mm) or leave blank to cancel: ");
                String newTimeInput = scanner.nextLine().trim();
                if (newTimeInput.isEmpty()) {
                    System.out.println("Operation cancelled.");
                    return;
                }
    
                LocalDate newDate = LocalDate.parse(newDateInput, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                LocalTime newTime = LocalTime.parse(newTimeInput, DateTimeFormatter.ofPattern("HH:mm"));
    
                if (!ValidationUtils.isValidAppointmentTime(doctorSlots, newDate, newTime)) {
                    System.out.println("The selected time is not valid. Please choose another time.");
                    continue;
                }
    
                // Update the appointment and persist changes
                selectedAppointment.setDate(newDateInput);
                selectedAppointment.setTime(newTimeInput);
                selectedAppointment.updateStatus(AppointmentStatus.PENDING);
    
                appointmentCRUD.updateAppointment(selectedAppointment);
                System.out.println("Appointment rescheduled successfully.");
                break;
            } catch (Exception e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private void cancelAppointment(Scanner scanner) {
        displayMenuHeader("CANCEL AN APPOINTMENT");
    
        // Retrieve appointments with PENDING or ACCEPTED status
        List<Appointment> appointments = appointmentCRUD.getAppointments(patient.getUserID(), null, null)
                .stream()
                .filter(appt -> appt.getStatus() == AppointmentStatus.PENDING || appt.getStatus() == AppointmentStatus.ACCEPTED)
                .collect(Collectors.toList());
    
        if (appointments.isEmpty()) {
            System.out.println("No pending or confirmed appointments to cancel.");
            return;
        }
    
        // Display appointments with numbers
        System.out.println("Available Appointments to Cancel:");
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appointment = appointments.get(i);
            String doctorName = staffData.findUserById(appointment.getDoctorID()).getName();
            System.out.printf("%d. %s (Doctor: %s, Date: %s, Time: %s, Status: %s)%n",
                    i + 1, appointment.getAppointmentID(), doctorName, appointment.getDate(),
                    appointment.getTime(), appointment.getStatus());
        }
    
        // Prompt the user to select an appointment
        System.out.print("\nEnter the number of the appointment to cancel (or press Enter to exit): ");
        String input = scanner.nextLine().trim();
    
        // Allow user to exit
        if (input.isEmpty()) {
            System.out.println("Operation cancelled.");
            return;
        }
    
        try {
            int selection = Integer.parseInt(input) - 1;
    
            if (selection < 0 || selection >= appointments.size()) {
                System.out.println("Invalid selection. Please try again.");
                return;
            }
    
            Appointment selectedAppointment = appointments.get(selection);
    
            // Cancel the selected appointment
            appointmentCRUD.cancelAppointment(selectedAppointment.getAppointmentID());
            System.out.println("Appointment cancelled successfully.");
    
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        }
    }
        
    
    private void displayAppointments(List<Appointment> appointments) {
        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
            return;
        }
    
        // Display header for the table
        System.out.printf("%-15s %-30s %-12s %-8s %-10s %-15s%n", "Patient ID", "Doctor Name", "Date", "Time", "Status", "Additional Notes");
        System.out.println("------------------------------------------------------------------------------------------------------");
    
        // Iterate through appointments and display their details
        for (Appointment appointment : appointments) {
            String doctorName = staffData.findUserById(appointment.getDoctorID()).getName();
            String additionalNotes = "";
    
            // Add additional notes only if the appointment is COMPLETED
            if (appointment.getStatus() == AppointmentStatus.COMPLETED && appointment.getOutcomeRecord() != null) {
                OutcomeRecord outcome = appointment.getOutcomeRecord();
                additionalNotes = outcome.getConsultationNotes();
            }
    
            System.out.printf("%-15s %-30s %-12s %-8s %-10s %-15s%n",
                    appointment.getPatientID(),
                    doctorName,
                    appointment.getDate(),
                    appointment.getTime(),
                    appointment.getStatus(),
                    additionalNotes.isEmpty() ? "-" : additionalNotes);
        }
    }
    
    private void viewScheduledAppointments() {
        displayMenuHeader("VIEW SCHEDULED APPOINTMENTS");
    
        // Retrieve appointments with status PENDING or CONFIRMED
        List<Appointment> appointments = appointmentCRUD.getAppointments(patient.getUserID(), null, null)
                .stream()
                .filter(appt -> appt.getStatus() == AppointmentStatus.PENDING || appt.getStatus() == AppointmentStatus.ACCEPTED)
                .collect(Collectors.toList());
    
        // Use the new method to display appointments
        displayAppointments(appointments);
    }
    
    
    

    private void viewPastAppointments() {
        displayMenuHeader("VIEW PAST APPOINTMENT OUTCOME RECORDS");
    
        // Retrieve only completed appointments
        List<Appointment> appointments = appointmentCRUD.getAppointments(patient.getUserID(), null, AppointmentStatus.COMPLETED);
    
        if (appointments.isEmpty()) {
            System.out.println("No past appointments found.");
            return;
        }
    
        // Display header
        System.out.printf(
                "%-15s %-25s %-12s %-8s %-10s %-50s %-50s%n",
                "Patient ID", "Doctor Name", "Date", "Time", "Status", "Consultation Notes", "Prescriptions"
        );
        System.out.println("=".repeat(170));
    
        for (Appointment appointment : appointments) {
            // Retrieve doctor's name or provide a fallback
            String doctorName = "Unknown";
            if (staffData.findUserById(appointment.getDoctorID()) != null) {
                doctorName = staffData.findUserById(appointment.getDoctorID()).getName();
            }
    
            // Extract OutcomeRecord details
            String consultationNotes = "N/A";
            String prescriptions = "N/A";
    
            if (appointment.getOutcomeRecord() != null) {
                OutcomeRecord outcome = appointment.getOutcomeRecord();
    
                // Retrieve consultation notes, if available
                consultationNotes = outcome.getConsultationNotes().isEmpty() ? "N/A" : outcome.getConsultationNotes();
    
                // Format prescriptions, if available
                if (!outcome.getPrescriptions().isEmpty()) {
                    prescriptions = outcome.getPrescriptions().stream()
                            .map(p -> String.format("%s (%d, %s)",
                                    p.getMedicationName(),
                                    p.getQuantity(),
                                    p.getStatus().name()))
                            .collect(Collectors.joining("; "));
                }
            }
    
            // Print appointment details
            System.out.printf(
                    "%-15s %-25s %-12s %-8s %-10s %-50s %-50s%n",
                    appointment.getPatientID(),
                    doctorName,
                    appointment.getDate(),
                    appointment.getTime(),
                    appointment.getStatus(),
                    consultationNotes,
                    prescriptions
            );
        }
    
        System.out.println("=".repeat(170));
    }
    
    
    
    
    
    
}
