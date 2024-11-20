package datamgmt.retrievers;

import utils.appointments.Appointment;
import utils.enums.AppointmentStatus;
import utils.env;
import utils.medicalrecords.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data handler for managing appointment records.
 * Provides file-based operations for Appointment objects.
 */
public class AppointmentData extends BaseDataHandler<Appointment> {

    private final env environment;
    private final String filePath;

    /**
     * Initializes the AppointmentData handler with the file path from the environment.
     */
    public AppointmentData() {
        this.environment = new env();
        this.filePath = environment.getAppointmentDataPath();
    }

    /**
     * Imports appointment data from the file path specified in the environment.
     * Skips the header line.
     */
    public void importData() {
        List<Appointment> appointments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true; // Flag to skip the header
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // Skip the first line (header)
                    continue;
                }
                try {
                    Appointment appointment = parseLine(line);
                    if (appointment != null) {
                        appointments.add(appointment);
                    }
                } catch (Exception e) {
                    System.err.println("Invalid appointment record: " + line + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading appointment data: " + e.getMessage());
        }

        // Replace in-memory data only after successful parsing
        dataList.clear();
        dataList.addAll(appointments);
        processOutdatedAppointments(); // Process outdated appointments after loading
    }

    /**
     * Processes outdated appointments:
     * - Deletes pending appointments if their date has passed.
     * - Cancels confirmed appointments if their date has passed.
     */
    private void processOutdatedAppointments() {
        LocalDate currentDate = LocalDate.now();
        List<Appointment> updatedAppointments = new ArrayList<>();

        for (Appointment appointment : dataList) {
            LocalDate appointmentDate = LocalDate.parse(appointment.getDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            if (appointmentDate.isBefore(currentDate)) {
                if (appointment.getStatus() == AppointmentStatus.PENDING) {
                    System.out.println("Deleting outdated pending appointment: " + appointment.getAppointmentID());
                    continue;
                } else if (appointment.getStatus() == AppointmentStatus.ACCEPTED) {
                    System.out.println("Cancelling outdated confirmed appointment: " + appointment.getAppointmentID());
                    appointment.updateStatus(AppointmentStatus.CANCELLED);
                }
            }
            updatedAppointments.add(appointment);
        }

        dataList.clear();
        dataList.addAll(updatedAppointments);
    }

    /**
     * Saves appointment data to the file in an atomic manner.
     * Uses a temporary file to ensure data integrity.
     */
    public void writeData() throws IOException {
        String outputPath = this.filePath; // Use the file path from the environment
        File tempFile = new File(outputPath + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            // Write the header
            writer.write(getHeader());
            writer.newLine();

            // Write each appointment as a CSV row
            for (Appointment appointment : dataList) {
                writer.write(formatItem(appointment));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing appointment data: " + e.getMessage());
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw e;
        }

        // Replace original file only if write is successful
        File originalFile = new File(outputPath);
        if (!tempFile.renameTo(originalFile)) {
            throw new IOException("Failed to replace the original appointment file.");
        }
    }

    /**
     * Reloads the appointment data by clearing the in-memory list and re-importing it.
     */
    public void reloadData() {
        clearData();
        importData();
    }

    /**
     * Retrieves all appointments.
     *
     * @return a list of all Appointment objects
     */
    public List<Appointment> getAppointments() {
        return new ArrayList<>(dataList);
    }

    @Override
    protected Appointment parseLine(String line) {
        try {
            String[] parts = line.split(",", 7); // Expect 7 fields max
            if (parts.length < 6) {
                throw new IllegalArgumentException("Invalid format: Insufficient data fields.");
            }

            String appointmentID = parts[0].trim();
            String patientID = parts[1].trim();
            String doctorID = parts[2].trim();
            String date = parts[3].trim();
            String time = parts[4].trim();
            AppointmentStatus status = AppointmentStatus.valueOf(parts[5].trim());

            OutcomeRecord outcomeRecord = null;
            if (parts.length > 6 && !parts[6].equals("-")) {
                outcomeRecord = parseOutcomeRecord(parts[6].trim());
            }

            return new Appointment(appointmentID, patientID, doctorID, date, time, status, outcomeRecord);

        } catch (Exception e) {
            System.err.println("Error parsing appointment data: " + line + " - " + e.getMessage());
            return null;
        }
    }

    private OutcomeRecord parseOutcomeRecord(String outcomeData) {
        try {
            String[] parts = outcomeData.split(",", 3);
            String dateOfAppointment = parts[0].trim();
            String serviceType = parts[1].trim();
            String consultationNotes = parts[2].trim();

            OutcomeRecord outcomeRecord = new OutcomeRecord(dateOfAppointment, serviceType, consultationNotes);

            if (outcomeData.contains("[") && outcomeData.contains("]")) {
                String prescriptionsData = outcomeData.substring(outcomeData.indexOf("[") + 1, outcomeData.indexOf("]")).trim();
                if (!prescriptionsData.isEmpty()) {
                    String[] prescriptionsArray = prescriptionsData.split(";");
                    for (String prescriptionCSV : prescriptionsArray) {
                        outcomeRecord.addPrescription(Prescription.fromCSV(prescriptionCSV.trim()));
                    }
                }
            }
            return outcomeRecord;

        } catch (Exception e) {
            System.err.println("Error parsing outcome record: " + outcomeData + " - " + e.getMessage());
            return null;
        }
    }

    @Override
    protected String formatItem(Appointment appointment) {
        String outcomeRecord = appointment.getOutcomeRecord() != null
                ? appointment.getOutcomeRecord().toCSV()
                : "-";

        return String.join(",",
                appointment.getAppointmentID(),
                appointment.getPatientID(),
                appointment.getDoctorID(),
                appointment.getDate(),
                appointment.getTime(),
                appointment.getStatus().name(),
                outcomeRecord);
    }

    @Override
    protected String getHeader() {
        return "AppointmentID,PatientID,DoctorID,Date,Time,Status,Outcome Record";
    }
}
