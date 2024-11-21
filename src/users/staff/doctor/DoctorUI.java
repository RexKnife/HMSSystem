package users.staff.doctor;

import datamgmt.retrievers.AppointmentData;
import datamgmt.retrievers.MedicalRecordData;
import users.ui.BaseUI;
import utils.appointments.Appointment;
import utils.appointments.AppointmentCRUD;
import utils.enums.AppointmentStatus;
import utils.medicalrecords.MedicalRecord;
import utils.medicalrecords.OutcomeRecord;
import utils.medicalrecords.Prescription;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class DoctorUI extends BaseUI {
    private final AppointmentCRUD appointmentCRUD;
    private final MedicalRecordData medicalRecordData;
    private final String doctorID;

    public DoctorUI(String doctorID) {
        AppointmentData appointmentData = new AppointmentData();
        appointmentData.importData();
        this.appointmentCRUD = new AppointmentCRUD(appointmentData);

        this.medicalRecordData = new MedicalRecordData();
        this.medicalRecordData.importData();

        this.doctorID = doctorID;
    }

    public void displayMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayMenuHeader("DOCTOR MAIN MENU");
            displayMenuOption(1, "View Medical Records");
            displayMenuOption(2, "Update Medical Records");
            displayMenuOption(3, "View Personal Schedule");
            displayMenuOption(4, "Accept/Decline Appointment Requests");
            displayMenuOption(5, "View Upcoming Appointments");
            displayMenuOption(6, "Cancel an Appointment");
            displayMenuOption(7, "Record Appointment Outcome");
            displayMenuOption(8, "Logout");

            int choice = getMenuChoice();
            switch (choice) {
                case 1:
                    viewMedicalRecords(scanner);
                    break;
                case 2:
                    updateMedicalRecords(scanner);
                    break;
                case 3:
                    viewPersonalSchedule();
                    break;
                case 4:
                    manageAppointmentRequests(scanner);
                    break;
                case 5:
                    viewUpcomingAppointments();
                    break;
                case 6:
                    cancelAppointment(scanner);
                    break;
                case 7:
                    recordAppointmentOutcome(scanner);
                    break;
                case 8:
                    System.out.println("Logging out...");
                    return;
                default:
                    displayInvalidInputMessage();
            }
            pauseForUser();
        }
    }

    private void viewMedicalRecords(Scanner scanner) {
        // Prompt the doctor for the patient ID
        System.out.print("Enter Patient ID to view medical record (or press Enter to return to the main menu): ");
        String patientID = scanner.nextLine().trim();
    
        // Handle the case where no input is provided
        if (patientID.isEmpty()) {
            System.out.println("Returning to the main menu.");
            return;
        }
    
        // Retrieve the medical record for the given patient ID
        MedicalRecord record = medicalRecordData.getMedicalRecordByPatientID(patientID);
    
        // If no record is found, notify the user
        if (record == null) {
            System.out.println("No medical record found for Patient ID: " + patientID);
            return;
        }
    
        // Display the retrieved medical record
        System.out.println("\n=====================================");
        System.out.println("          MEDICAL RECORD");
        System.out.println("=====================================");
        System.out.printf("Patient ID: %s%n", record.getPatientID());
    
        // Display diagnoses
        System.out.println("\nDiagnoses:");
        if (record.getDiagnoses().isEmpty()) {
            System.out.println(" - No diagnoses recorded.");
        } else {
            for (int i = 0; i < record.getDiagnoses().size(); i++) {
                System.out.printf(" %d. %s%n", i + 1, record.getDiagnoses().get(i));
            }
        }
    
        // Display treatments
        System.out.println("\nTreatments:");
        if (record.getTreatments().isEmpty()) {
            System.out.println(" - No treatments recorded.");
        } else {
            for (int i = 0; i < record.getTreatments().size(); i++) {
                System.out.printf(" %d. %s%n", i + 1, record.getTreatments().get(i));
            }
        }
    
        System.out.println("=====================================");
    }
    

    private void updateMedicalRecords(Scanner scanner) {
        displayMenuHeader("UPDATE MEDICAL RECORDS");
        System.out.print("Enter Patient ID to update medical record: ");
        String patientID = scanner.nextLine().trim();
    
        // Fetch the medical record
        MedicalRecord medicalRecord = this.medicalRecordData.getMedicalRecordByPatientID(patientID);
        if (medicalRecord == null) {
            System.out.println("No medical record found for Patient ID: " + patientID);
            return;
        }
    
        // Display the existing medical record
        System.out.println("Existing Medical Record:");
        System.out.println(medicalRecord);
    
        // Add a new diagnosis
        System.out.print("\nEnter a new diagnosis to add (or press Enter to skip): ");
        String newDiagnosis = scanner.nextLine().trim();
        if (!newDiagnosis.isEmpty()) {
            medicalRecord.addDiagnosis(newDiagnosis);
            System.out.println("Diagnosis added.");
        } else {
            System.out.println("No new diagnosis added.");
        }
    
        // Add a new treatment
        System.out.print("Enter a new treatment to add (or press Enter to skip): ");
        String newTreatment = scanner.nextLine().trim();
        if (!newTreatment.isEmpty()) {
            medicalRecord.addTreatment(newTreatment);
            System.out.println("Treatment added.");
        } else {
            System.out.println("No new treatment added.");
        }
    
        // Save the updated medical record
        try {
            this.medicalRecordData.updateMedicalRecord(medicalRecord);
            System.out.println("Medical record updated successfully.");
        } catch (Exception e) {
            System.err.println("Error updating medical record: " + e.getMessage());
        }
    }
    

    private void viewPersonalSchedule() {
        List<Appointment> appointments = appointmentCRUD.getAppointments(null, doctorID, null);

        if (appointments.isEmpty()) {
            System.out.println("No scheduled appointments found.");
        } else {
            System.out.printf("%-15s %-15s %-15s %-15s%n", "Patient ID", "Date", "Time", "Status");
            appointments.forEach(appointment -> 
                System.out.printf("%-15s %-15s %-15s %-15s%n",
                    appointment.getPatientID(),
                    appointment.getDate(),
                    appointment.getTime(),
                    appointment.getStatus())
            );
        }
    }

    private void manageAppointmentRequests(Scanner scanner) {
        List<Appointment> pendingAppointments = appointmentCRUD.getAppointments(null, doctorID, AppointmentStatus.PENDING);

        if (pendingAppointments.isEmpty()) {
            System.out.println("No pending appointment requests.");
        } else {
            for (int i = 0; i < pendingAppointments.size(); i++) {
                Appointment appointment = pendingAppointments.get(i);
                System.out.printf("%d. Patient: %s, Date: %s, Time: %s%n", 
                    i + 1, 
                    appointment.getPatientID(), 
                    appointment.getDate(), 
                    appointment.getTime());
            }

            System.out.print("Enter the number of the appointment to manage (or 0 to exit): ");
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice > 0 && choice <= pendingAppointments.size()) {
                Appointment selectedAppointment = pendingAppointments.get(choice - 1);

                System.out.print("Accept or Decline this appointment? (accept/decline): ");
                String action = scanner.nextLine().trim().toLowerCase();

                if (action.equals("accept")) {
                    selectedAppointment.updateStatus(AppointmentStatus.ACCEPTED);
                    appointmentCRUD.updateAppointment(selectedAppointment);
                    System.out.println("Appointment accepted.");
                } else if (action.equals("decline")) {
                    selectedAppointment.updateStatus(AppointmentStatus.CANCELLED);
                    appointmentCRUD.updateAppointment(selectedAppointment);
                    System.out.println("Appointment declined.");
                } else {
                    System.out.println("Invalid action.");
                }
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void viewUpcomingAppointments() {
        List<Appointment> upcomingAppointments = appointmentCRUD.getAppointments(null, doctorID, AppointmentStatus.ACCEPTED);

        if (upcomingAppointments.isEmpty()) {
            System.out.println("No upcoming appointments.");
        } else {
            System.out.printf("%-15s %-15s %-15s %-15s%n", "Patient ID", "Date", "Time", "Status");
            upcomingAppointments.forEach(appointment -> 
                System.out.printf("%-15s %-15s %-15s %-15s%n",
                    appointment.getPatientID(),
                    appointment.getDate(),
                    appointment.getTime(),
                    appointment.getStatus())
            );
        }
    }

    private void cancelAppointment(Scanner scanner) {
        List<Appointment> appointments = appointmentCRUD.getAppointments(null, doctorID, AppointmentStatus.ACCEPTED);

        if (appointments.isEmpty()) {
            System.out.println("No appointments to cancel.");
        } else {
            for (int i = 0; i < appointments.size(); i++) {
                Appointment appointment = appointments.get(i);
                System.out.printf("%d. Patient: %s, Date: %s, Time: %s%n", 
                    i + 1, 
                    appointment.getPatientID(), 
                    appointment.getDate(), 
                    appointment.getTime());
            }

            System.out.print("Enter the number of the appointment to cancel (or 0 to exit): ");
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice > 0 && choice <= appointments.size()) {
                Appointment selectedAppointment = appointments.get(choice - 1);
                selectedAppointment.updateStatus(AppointmentStatus.CANCELLED);
                appointmentCRUD.updateAppointment(selectedAppointment);
                System.out.println("Appointment cancelled.");
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void recordAppointmentOutcome(Scanner scanner) {
        List<Appointment> appointments = appointmentCRUD.getAppointments(null, doctorID, AppointmentStatus.ACCEPTED);

        if (appointments.isEmpty()) {
            System.out.println("No eligible appointments for recording outcomes.");
        } else {
            for (int i = 0; i < appointments.size(); i++) {
                Appointment appointment = appointments.get(i);
                System.out.printf("%d. Patient: %s, Date: %s, Time: %s%n", 
                    i + 1, 
                    appointment.getPatientID(), 
                    appointment.getDate(), 
                    appointment.getTime());
            }

            System.out.print("Enter the number of the appointment to record an outcome for (or 0 to exit): ");
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice > 0 && choice <= appointments.size()) {
                Appointment selectedAppointment = appointments.get(choice - 1);

                // Validate that the appointment date/time has passed
                if (!hasAppointmentDatePassed(selectedAppointment)) {
                    System.out.println("Cannot record outcome for a future appointment.");
                    return;
                }

                System.out.print("Enter type of service provided: ");
                String serviceType = scanner.nextLine().trim();

                System.out.print("Enter consultation notes: ");
                String consultationNotes = scanner.nextLine().trim();

                OutcomeRecord outcomeRecord = new OutcomeRecord(selectedAppointment.getDate(), serviceType, consultationNotes);

                // Add prescriptions if needed
                while (true) {
                    System.out.print("Enter prescribed medication name (or press Enter to skip): ");
                    String medicationName = scanner.nextLine().trim();
                    if (medicationName.isEmpty()) break;

                    System.out.print("Enter the quantity of the medication: ");
                    int quantity = Integer.parseInt(scanner.nextLine().trim());
                    outcomeRecord.addPrescription(new Prescription(medicationName, quantity));
                }

                selectedAppointment.setOutcomeRecord(outcomeRecord);
                selectedAppointment.updateStatus(AppointmentStatus.COMPLETED);

                try {
                    appointmentCRUD.updateAppointment(selectedAppointment);
                    System.out.println("Appointment outcome recorded successfully.");
                } catch (Exception e) {
                    System.err.println("Error recording appointment outcome: " + e.getMessage());
                }
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private boolean hasAppointmentDatePassed(Appointment appointment) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            LocalDate appointmentDate = LocalDate.parse(appointment.getDate(), dateFormatter);
            LocalTime appointmentTime = LocalTime.parse(appointment.getTime(), timeFormatter);

            return (appointmentDate.isBefore(LocalDate.now())) && (appointmentTime.isBefore(LocalTime.now()));
        } catch (Exception e) {
            System.err.println("Error validating appointment date and time: " + e.getMessage());
            return false;
        }
    }
}
