package utils.appointments;

import datamgmt.retrievers.AppointmentData;
import datamgmt.retrievers.MedicalRecordData;
import utils.enums.AppointmentStatus;
import utils.medicalrecords.MedicalRecord;
import utils.medicalrecords.OutcomeRecord;
import utils.medicalrecords.Prescription;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Provides functionality for doctors to manage their schedule,
 * including viewing schedules, handling appointment requests, and rescheduling appointments.
 */
public class DoctorScheduleManager {
    private final AppointmentCRUD appointmentCRUD;
    private final MedicalRecordData medicalRecordData;
    private final String doctorID;

    /**
     * Constructs a DoctorScheduleManager for a specific doctor.
     *
     * @param appointmentData   the data handler for appointments
     * @param medicalRecordData the data handler for medical records
     * @param doctorID          the doctor's ID
     */
    public DoctorScheduleManager(AppointmentData appointmentData, MedicalRecordData medicalRecordData, String doctorID) {
        this.appointmentCRUD = new AppointmentCRUD(appointmentData);
        this.medicalRecordData = medicalRecordData;
        this.doctorID = doctorID;
    }

    /**
     * Displays a menu for the doctor to manage their schedule.
     *
     * @param scanner the scanner for user input
     */
    public void manageSchedule(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Doctor Schedule Manager ---");
            System.out.println("1. View Upcoming Appointments");
            System.out.println("2. Reschedule an Appointment");
            System.out.println("3. Handle Appointment Requests");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    viewUpcomingAppointments(scanner);
                    break;
                case 2:
                    rescheduleAppointment(scanner);
                    break;
                case 3:
                    handleAppointmentRequests(scanner);
                    break;
                case 4:
                    System.out.println("Exiting schedule manager.");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Displays upcoming appointments and prompts for confirming past appointments.
     *
     * @param scanner the scanner for user input
     */
    private void viewUpcomingAppointments(Scanner scanner) {
        System.out.println("\n--- Upcoming Appointments ---");
        LocalDate today = LocalDate.now();

        List<Appointment> appointments = getSortedAppointments(today);

        if (appointments.isEmpty()) {
            System.out.println("No upcoming appointments.");
            return;
        }

        for (int i = 0; i < appointments.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, appointments.get(i));
        }

        for (Appointment appointment : appointments) {
            if (isPastAppointment(appointment)) {
                confirmPastAppointment(scanner, appointment);
            }
        }
    }

    /**
     * Gets and sorts the appointments.
     *
     * @param today the current date
     * @return sorted list of appointments
     */
    private List<Appointment> getSortedAppointments(LocalDate today) {
        List<Appointment> appointments = new ArrayList<>();
        for (Appointment appointment : appointmentCRUD.getAppointments(null, doctorID, null)) {
            LocalDate appointmentDate = LocalDate.parse(
                    appointment.getDate(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            if (!appointmentDate.isBefore(today.minusDays(1))) {
                appointments.add(appointment);
            }
        }

        appointments.sort((a1, a2) -> {
            LocalDateTime dateTime1 = LocalDateTime.parse(
                    a1.getDate() + " " + a1.getTime(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            LocalDateTime dateTime2 = LocalDateTime.parse(
                    a2.getDate() + " " + a2.getTime(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            return dateTime1.compareTo(dateTime2);
        });

        return appointments;
    }

    /**
     * Checks if the appointment time has passed.
     *
     * @param appointment the appointment to check
     * @return true if the appointment is in the past, false otherwise
     */
    private boolean isPastAppointment(Appointment appointment) {
        LocalDateTime appointmentDateTime = LocalDateTime.parse(
                appointment.getDate() + " " + appointment.getTime(),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        return appointmentDateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Prompts the doctor to confirm a past appointment and update its records.
     *
     * @param scanner     the scanner for user input
     * @param appointment the appointment to confirm
     */
    private void confirmPastAppointment(Scanner scanner, Appointment appointment) {
        System.out.printf("Appointment with Patient ID %s on %s at %s has passed.%n",
                appointment.getPatientID(), appointment.getDate(), appointment.getTime());
        System.out.print("Do you want to confirm this appointment? (yes/no): ");
        String choice = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(choice)) {
            appointment.updateStatus(AppointmentStatus.COMPLETED);

            // Update outcome record
            System.out.println("Updating Outcome Record...");
            OutcomeRecord outcomeRecord = createOutcomeRecord(scanner);
            appointmentCRUD.addOutcomeRecord(appointment.getAppointmentID(), outcomeRecord);

            // Update medical record
            System.out.println("Updating Medical Record...");
            updateMedicalRecord(scanner, appointment.getPatientID(), outcomeRecord);

            System.out.println("Appointment confirmed and records updated.");
        } else {
            System.out.println("Skipping confirmation for this appointment.");
        }
    }

    /**
     * Updates a patient's medical record based on the outcome.
     *
     * @param scanner       the scanner for user input
     * @param patientID     the patient's ID
     * @param outcomeRecord the outcome record to add to the medical record
     */
    private void updateMedicalRecord(Scanner scanner, String patientID, OutcomeRecord outcomeRecord) {
        MedicalRecord medicalRecord = medicalRecordData.getMedicalRecordByPatientID(patientID);
        if (medicalRecord == null) {
            System.out.println("No existing medical record found. Creating a new one...");
            medicalRecord = new MedicalRecord(patientID);
        }

        medicalRecord.getDiagnoses().add(outcomeRecord.getServiceType());
        for (Prescription prescription : outcomeRecord.getPrescriptions()) {
            medicalRecord.addTreatment(prescription.getMedicationName());
        }

        medicalRecordData.updateMedicalRecord(medicalRecord);
        System.out.println("Medical record updated successfully.");
    }

    /**
     * Allows the doctor to reschedule an appointment.
     *
     * @param scanner the scanner for user input
     */
    private void rescheduleAppointment(Scanner scanner) {
        System.out.println("\n--- Reschedule Appointment ---");
        List<Appointment> appointments = new ArrayList<>(appointmentCRUD.getAppointments(null, doctorID, AppointmentStatus.ACCEPTED));

        if (appointments.isEmpty()) {
            System.out.println("No accepted appointments to reschedule.");
            return;
        }

        for (int i = 0; i < appointments.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, appointments.get(i));
        }

        System.out.print("Select an appointment to reschedule (number): ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice < 1 || choice > appointments.size()) {
            System.out.println("Invalid choice.");
            return;
        }

        Appointment selectedAppointment = appointments.get(choice - 1);

        System.out.print("Enter new date (dd/MM/yyyy): ");
        String newDate = scanner.nextLine().trim();
        System.out.print("Enter new time (HH:mm): ");
        String newTime = scanner.nextLine().trim();

        appointmentCRUD.rescheduleAppointment(selectedAppointment.getAppointmentID(), newDate, newTime);
    }

    /**
     * Handles appointment requests (confirming or denying).
     *
     * @param scanner the scanner for user input
     */
    private void handleAppointmentRequests(Scanner scanner) {
        System.out.println("\n--- Appointment Requests ---");
        List<Appointment> pendingAppointments = new ArrayList<>(appointmentCRUD.getAppointments(null, doctorID, AppointmentStatus.PENDING));

        if (pendingAppointments.isEmpty()) {
            System.out.println("No pending appointment requests.");
            return;
        }

        for (int i = 0; i < pendingAppointments.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, pendingAppointments.get(i));
        }

        System.out.print("Select an appointment to handle (number): ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice < 1 || choice > pendingAppointments.size()) {
            System.out.println("Invalid choice.");
            return;
        }

        Appointment selectedAppointment = pendingAppointments.get(choice - 1);

        System.out.print("Confirm this appointment? (yes/no): ");
        String decision = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(decision)) {
            appointmentCRUD.confirmAppointment(selectedAppointment.getAppointmentID());
            System.out.println("Appointment confirmed.");
        } else {
            appointmentCRUD.cancelAppointment(selectedAppointment.getAppointmentID());
            System.out.println("Appointment denied and cancelled.");
        }
    }

    /**
     * Creates an outcome record based on user input.
     *
     * @param scanner the scanner for user input
     * @return the created outcome record
     */
    private OutcomeRecord createOutcomeRecord(Scanner scanner) {
        System.out.print("Enter date of appointment outcome (dd/MM/yyyy): ");
        String date = scanner.nextLine().trim();
        System.out.print("Enter service type: ");
        String serviceType = scanner.nextLine().trim();
        System.out.print("Enter consultation notes: ");
        String consultationNotes = scanner.nextLine().trim();

        OutcomeRecord outcomeRecord = new OutcomeRecord(date, serviceType, consultationNotes);

        System.out.print("Enter number of prescriptions: ");
        int prescriptionCount = Integer.parseInt(scanner.nextLine().trim());

        for (int i = 0; i < prescriptionCount; i++) {
            System.out.printf("Prescription %d - Enter medication name: ", i + 1);
            String medicationName = scanner.nextLine().trim();
            System.out.print("Enter quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());
            outcomeRecord.addPrescription(new Prescription(medicationName, quantity));
        }

        return outcomeRecord;
    }
}
