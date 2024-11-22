package datamgmt.retrievers;

import utils.ValidationUtils;
import utils.appointments.appointmentslots.AppointmentSlot;
import utils.enums.WorkingDay;
import utils.env;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the retrieval and management of appointment slot data from the file specified in {@link env}.
 */
public class AppointmentSlotData extends BaseDataHandler<AppointmentSlot> {
    private final env environment;

    /**
     * Initializes the appointment slot data handler.
     */
    public AppointmentSlotData() {
        this.environment = new env();
    }

    /**
     * Imports appointment slot data from the file path specified in the {@link env}.
     */
    public void importData() {
        try {
            loadData(environment.getAppointmentSlotDataPath());
        } catch (IOException e) {
            System.err.println("Error reading appointment slot data: " + e.getMessage());
        }
    }

    /**
     * Reloads all appointment slot data by clearing existing data and re-importing it.
     */
    public void reloadData() {
        clearData();
        importData();
    }

    @Override
    protected AppointmentSlot parseLine(String line) {
        String[] data = line.split(",");
        if (data.length != 4) {
            System.err.println("Invalid data format: " + line);
            return null;
        }

        try {
            // Validate and parse DoctorID
            String doctorID = ValidationUtils.validateStringStartsWith(data[0], "D", "Doctor ID");

            // Validate and parse StartTime and EndTime
            String startTimeStr = ValidationUtils.validateTime(data[1], "HH:mm", "Start Time");
            String endTimeStr = ValidationUtils.validateTime(data[2], "HH:mm", "End Time");
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);

            // Parse and validate WorkingDays
            List<WorkingDay> workingDays = parseWorkingDays(data[3].trim());

            return new AppointmentSlot(doctorID, startTime, endTime, workingDays);
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing appointment slot data: " + line + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses a string of working days into a list of {@link WorkingDay} enums.
     *
     * @param workingDaysString the string representation of working days, separated by semicolons
     * @return a list of {@link WorkingDay} enums
     */
    private List<WorkingDay> parseWorkingDays(String workingDaysString) {
        List<WorkingDay> workingDays = new ArrayList<>();
        for (String day : workingDaysString.split(";")) {
            try {
                workingDays.add(WorkingDay.valueOf(day.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid working day: " + day);
            }
        }
        if (workingDays.isEmpty()) {
            throw new IllegalArgumentException("Working days cannot be empty or invalid.");
        }
        return workingDays;
    }

    @Override
    protected String formatItem(AppointmentSlot slot) {
        String workingDaysString = String.join(";", slot.getWorkingDays().stream()
                .map(WorkingDay::name)
                .toArray(String[]::new));
        return slot.getDoctorID() + "," + slot.getStartTime() + "," + slot.getEndTime() + "," + workingDaysString;
    }

    @Override
    protected String getHeader() {
        return "DoctorID,StartTime,EndTime,WorkingDays";
    }

    /**
     * Saves all appointment slots to the file.
     */
    public void saveAppointmentSlots() {
        try {
            writeData(environment.getAppointmentSlotDataPath());
            System.out.println("Appointment slots saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving appointment slots: " + e.getMessage());
        }
    }
   /**
     * Adds a new appointment slot and persists it to the CSV file.
     *
     * @param slot the {@link AppointmentSlot} object to add
     */
    public void addSlot(AppointmentSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("Appointment slot cannot be null.");
        }

        try {
            // Add slot to the in-memory data list managed by BaseDataHandler
            dataList.add(slot); // This method ensures the slot is added to the internal list
            saveAppointmentSlots(); // Save the updated list to the CSV file
            System.out.println("Appointment slot added successfully.");
        } catch (Exception e) {
            System.err.println("Error adding appointment slot: " + e.getMessage());
        }
    }

    /**
     * Returns the list of all appointment slots.
     *
     * @return a list of all {@link AppointmentSlot} objects
     */
    public List<AppointmentSlot> getAllSlots() {
        return getAllData(); // Use the inherited getAllData method from BaseDataHandler
    }
    
}
