package datamgmt.retrievers;

import users.Users;
import users.staff.administrator.Administrator;
import users.staff.doctor.Doctor;
import users.staff.pharmacist.Pharmacist;
import utils.enums.Gender;
import utils.enums.Roles;
import utils.env;

import java.io.IOException;

/**
 * Manages the retrieval, parsing, and management of staff data
 * from the file specified in the {@link env}.
 */
public class StaffData extends BaseDataHandler<Users> {

    private final env environment;

    public StaffData() {
        this.environment = new env();
    }

    /**
     * Imports staff data from the file specified in the environment configuration.
     */
    public void importData() {
        try {
            loadData(environment.getStaffListPath());
        } catch (IOException e) {
            System.err.println("Error reading staff data: " + e.getMessage());
        }
    }

    /**
     * Reloads staff data by clearing existing in-memory data and re-importing it.
     */
    public void reloadData() {
        clearData();
        importData();
    }

    /**
     * Parses a line of staff data and creates the appropriate {@link Users} object.
     *
     * @param line The line of data to parse.
     * @return The corresponding {@link Users} object, or null if parsing fails.
     */
    @Override
    protected Users parseLine(String line) {
        try {
            String[] data = line.split(",");
            if (data.length != 6) {
                System.err.println("Invalid data format: " + line);
                return null; // Skip invalid line
            }

            String userID = data[0].trim();
            String name = data[1].trim();
            Roles role = Roles.valueOf(data[2].trim().toUpperCase());
            Gender gender = Gender.fromString(data[3].trim());
            int age = Integer.parseInt(data[4].trim());
            String password = data[5].trim();

            // Validate UserID prefix matches the expected role
            if (!userID.matches("^(U|D|A|PH)\\d+$")) {
                System.err.println("UserID must start with 'U', 'D', 'A', or 'PH'. Found: " + userID);
                return null; // Skip invalid line
            }

            // Create specific user types based on the role
            switch (role) {
                case DOCTOR:
                    if (!userID.startsWith("D")) {
                        System.err.println("Invalid UserID for DOCTOR role. Found: " + userID);
                        return null; // Skip invalid line
                    }
                    return new Doctor(userID, name, gender, age, password); // UI not instantiated
                case ADMINISTRATOR:
                    if (!userID.startsWith("A")) {
                        System.err.println("Invalid UserID for ADMINISTRATOR role. Found: " + userID);
                        return null; // Skip invalid line
                    }
                    return new Administrator(userID, name, gender, age, password); // UI not instantiated
                case PHARMACIST:
                    if (!userID.startsWith("PH")) {
                        System.err.println("Invalid UserID for PHARMACIST role. Found: " + userID);
                        return null; // Skip invalid line
                    }
                    return new Pharmacist(userID, name, gender, age, password); // UI not instantiated
                default:
                    System.err.println("Unknown role for UserID: " + userID);
                    return null; // Skip unknown role
            }
        } catch (Exception e) {
            System.err.println("Error parsing staff data: " + line + " - " + e.getMessage());
            return null; // Skip invalid line
        }
    }

    /**
     * Formats a {@link Users} object into a CSV-compatible string.
     *
     * @param staff The {@link Users} object to format.
     * @return A formatted string representing the staff member.
     */
    @Override
    protected String formatItem(Users staff) {
        return String.join(",",
                staff.getUserID(),
                staff.getName(),
                staff.getRole().toString(),
                staff.getGender().toString(),
                String.valueOf(staff.getAge()),
                staff.getPassword());
    }

    /**
     * Provides the header for the staff data file.
     *
     * @return The header string.
     */
    @Override
    protected String getHeader() {
        return "UserID,Name,Role,Gender,Age,Password";
    }

    // Utility methods for manipulating staff data

    public void updatePassword(Users user, String newPassword) throws IOException {
        if (user == null) {
            throw new IllegalArgumentException("Staff member not found.");
        }
        user.setPassword(newPassword);
        writeData(environment.getStaffListPath());
    }

   /**
     * Generates the next staff ID based on the role.
     *
     * @param role The role of the staff (e.g., DOCTOR, ADMINISTRATOR, PHARMACIST).
     * @return The next available staff ID for the given role, formatted with leading zeros.
     */
    public String generateNextStaffID(Roles role) {
        // Define the prefix based on the role
        String prefix;
        switch (role) {
            case DOCTOR:
                prefix = "D";
                break;
            case ADMINISTRATOR:
                prefix = "A";
                break;
            case PHARMACIST:
                prefix = "PH";
                break;
            default:
                throw new IllegalArgumentException("Unsupported role: " + role);
        }

        // Find the maximum existing ID for the given role
        int maxId = getAllData().stream()
                .filter(user -> user.getUserID().startsWith(prefix))
                .map(user -> Integer.parseInt(user.getUserID().substring(prefix.length()))) // Extract the numeric part
                .max(Integer::compareTo)
                .orElse(0); // Default to 0 if no IDs exist for the role

        // Increment the maximum ID and format it with leading zeros
        return String.format("%s%03d", prefix, maxId + 1);
    }

    public Users findUserById(String userID) {
        return getAllData().stream()
                .filter(user -> user.getUserID().equalsIgnoreCase(userID))
                .findFirst()
                .orElse(null);
    }

    public void updateStaffName(String userID, String newName) throws IOException {
        Users user = findUserById(userID);
        if (user == null) {
            throw new IllegalArgumentException("Staff member not found.");
        }
        user.setName(newName);
        writeData(environment.getStaffListPath());
    }

    public void updateStaffAge(String userID, int newAge) throws IOException {
        Users user = findUserById(userID);
        if (user == null) {
            throw new IllegalArgumentException("Staff member not found.");
        }
        user.setAge(newAge);
        writeData(environment.getStaffListPath());
    }

    /**
     * Removes a staff member by their User ID and persists changes to the file.
     *
     * @param userID the ID of the staff member to remove
     * @throws IOException if there is an issue writing to the file
     */
    public void removeStaff(String userID) throws IOException {
        // Find the staff member by their ID
        Users user = findUserById(userID);
        if (user == null) {
            throw new IllegalArgumentException("Staff member not found: " + userID);
        }

        // Remove the user from the in-memory list
        dataList.remove(user);

        // Persist the updated list to the file
        writeData(environment.getStaffListPath());
    }


    /**
     * Adds a new staff member to the in-memory list and writes to the CSV file.
     *
     * @param user the staff member to add
     * @throws IOException if writing to the file fails
     */
    public void addStaff(Users user) throws IOException {
        if (findUserById(user.getUserID()) != null) {
            throw new IllegalArgumentException("A staff member with the same ID already exists.");
        }
        // Add to the in-memory list
        dataList.add(user);

        // Write the updated list to the file
        writeData(environment.getStaffListPath());
    }

    public void updateStaffRole(String userID, Roles newRole) throws IOException {
        Users user = findUserById(userID);
        if (user == null) {
            throw new IllegalArgumentException("Staff member not found.");
        }
        user.setRole(newRole);
        writeData(environment.getStaffListPath());
    }

    /**
     * Updates the details of a staff member.
     *
     * @param updatedStaff the updated staff member object
     * @throws IOException if saving data fails
     */
    public void updateStaff(Users updatedStaff) throws IOException {
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getUserID().equals(updatedStaff.getUserID())) {
                dataList.set(i, updatedStaff);
                writeData(environment.getStaffListPath());
                return;
            }
        }
        throw new IllegalArgumentException("Staff member not found for update: " + updatedStaff.getUserID());
    }
}
