package users.patient;

import datamgmt.retrievers.AppointmentData;
import datamgmt.retrievers.AppointmentSlotData;
import datamgmt.retrievers.MedicalRecordData;
import datamgmt.retrievers.StaffData;
import users.ui.BaseUI;
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
        System.out.println("\n--- Schedule an Appointment ---");

        AppointmentScheduler scheduler = new AppointmentScheduler(new AppointmentData(), new AppointmentSlotData());

        try {
            scheduler.scheduleAppointment(scanner, this.patient.getUserID()); // Delegates the scheduling to the AppointmentScheduler
        } catch (Exception e) {
            System.err.println("Error scheduling appointment: " + e.getMessage());
        }
    }


    private void rescheduleAppointment(Scanner scanner) {
        displayMenuHeader("RESCHEDULE AN APPOINTMENT");
    
        // Retrieve all appointments for rescheduling
        List<Appointment> appointments = appointmentCRUD.getAppointments(patient.getUserID(), null, null)
                .stream()
                .filter(appt -> appt.getStatus() == AppointmentStatus.PENDING || appt.getStatus() == AppointmentStatus.ACCEPTED)
                .collect(Collectors.toList());
    
        if (appointments.isEmpty()) {
            System.out.println("No appointments to reschedule.");
            return;
        }
    
        // Use the new method to display appointments
        displayAppointments(appointments);
    
        // Prompt the user to select an appointment
        System.out.print("\nSelect the appointment to reschedule: ");
        int selection = scanner.nextInt() - 1;
        scanner.nextLine(); // Clear buffer
    
        if (selection < 0 || selection >= appointments.size()) {
            System.out.println("Invalid selection.");
            return;
        }
    
        Appointment selectedAppointment = appointments.get(selection);
    
        // Get new date and time
        while (true) {
            try {
                System.out.print("Enter new date (dd/MM/yyyy): ");
                String newDate = scanner.nextLine().trim();
                System.out.print("Enter new time (HH:mm): ");
                String newTime = scanner.nextLine().trim();
    
                // Validate the new date and time
                LocalDate rescheduleDate = LocalDate.parse(newDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                LocalTime rescheduleTime = LocalTime.parse(newTime, DateTimeFormatter.ofPattern("HH:mm"));
    
                if (rescheduleDate.isBefore(LocalDate.now()) ||
                        (rescheduleDate.isEqual(LocalDate.now()) && rescheduleTime.isBefore(LocalTime.now()))) {
                    System.out.println("Error: The selected date and time have already passed. Please choose another slot.");
                    continue;
                }
    
                // Reschedule the appointment
                selectedAppointment.setDate(newDate);
                selectedAppointment.setTime(newTime);
                selectedAppointment.updateStatus(AppointmentStatus.PENDING); // Revert status to pending
    
                appointmentCRUD.rescheduleAppointment(selectedAppointment.getAppointmentID(), newDate, newTime);
                System.out.println("Appointment rescheduled successfully. Its status has been set back to PENDING.");
                break;
    
            } catch (Exception e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }
    


    private void cancelAppointment(Scanner scanner) {
        displayMenuHeader("CANCEL AN APPOINTMENT");
    
        // Retrieve appointments with PENDING or CONFIRMED status
        List<Appointment> appointments = appointmentCRUD.getAppointments(patient.getUserID(), null, null)
                .stream()
                .filter(appt -> appt.getStatus() == AppointmentStatus.PENDING || appt.getStatus() == AppointmentStatus.ACCEPTED)
                .collect(Collectors.toList());
    
        if (appointments.isEmpty()) {
            System.out.println("No pending or confirmed appointments to cancel.");
            return;
        }
    
        // Use the new method to display appointments
        displayAppointments(appointments);
    
        // Prompt the user to select an appointment
        System.out.print("\nSelect the appointment to cancel: ");
        int selection = scanner.nextInt() - 1;
        scanner.nextLine(); // Clear buffer
    
        if (selection < 0 || selection >= appointments.size()) {
            System.out.println("Invalid selection.");
            return;
        }
    
        Appointment selectedAppointment = appointments.get(selection);
    
        // Cancel the selected appointment
        appointmentCRUD.cancelAppointment(selectedAppointment.getAppointmentID());
        System.out.println("Appointment cancelled successfully.");
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
                "%-15s %-25s %-12s %-8s %-10s %-30s %-30s%n",
                "Patient ID", "Doctor Name", "Date", "Time", "Status", "Consultation Notes", "Prescriptions"
        );
        System.out.println("=".repeat(130));
    
        for (Appointment appointment : appointments) {
            // Retrieve doctor's name
            String doctorName = staffData.findUserById(appointment.getDoctorID()).getName();
    
            // Extract OutcomeRecord details
            String consultationNotes = "N/A";
            String prescriptions = "N/A";
    
            if (appointment.getOutcomeRecord() != null) {
                OutcomeRecord outcome = appointment.getOutcomeRecord();
    
                // Display consultation notes if available
                consultationNotes = outcome.getConsultationNotes().isEmpty() ? "N/A" : outcome.getConsultationNotes();
    
                // Format prescriptions if available
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
                    "%-15s %-25s %-12s %-8s %-10s %-30s %-30s%n",
                    appointment.getPatientID(),
                    doctorName,
                    appointment.getDate(),
                    appointment.getTime(),
                    appointment.getStatus(),
                    consultationNotes,
                    prescriptions
            );
        }
    
        System.out.println("=".repeat(130));
    }
    
    
    
    
    
}
