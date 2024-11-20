package users.staff;

import datamgmt.retrievers.StaffData;
import users.Users;
import users.usermgmt.PasswordHasher;
import utils.enums.Gender;
import utils.enums.Roles;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides CRUD operations for managing staff data.
 */
public class StaffCRUD {

    private final StaffData data;

    /**
     * Constructs the StaffCRUD with the given StaffData handler.
     *
     * @param data the StaffData handler
     */
    public StaffCRUD(StaffData data) {
        this.data = data;
        data.reloadData(); // Load the latest staff data
    }

     /** Adds a new staff member with the provided details.
     *
     * @param name     the staff member's name
     * @param role     the staff member's role
     * @param gender   the staff member's gender
     * @param age      the staff member's age
     * @param password the staff member's plaintext password
     */
    public void addStaff(String name, Roles role, Gender gender, int age, String password) {
        // Generate a unique staff ID based on the role
        String staffID = data.generateNextStaffID(role);

        // Hash the password before storing it
        String hashedPassword = PasswordHasher.hashPassword(password);

        Users newStaff;
        switch (role) {
            case DOCTOR:
                newStaff = new users.staff.doctor.Doctor(staffID, name, gender, age, hashedPassword);
                break;
            case ADMINISTRATOR:
                newStaff = new users.staff.administrator.Administrator(staffID, name, gender, age, hashedPassword);
                break;
            case PHARMACIST:
                newStaff = new users.staff.pharmacist.Pharmacist(staffID, name, gender, age, hashedPassword);
                break;
            default:
                System.out.println("Invalid role provided.");
                return;
        }

        try {
            // Add the new staff to in-memory list and persist to the CSV file
            data.addStaff(newStaff);

            // Log success message
            System.out.println("Staff member added successfully: " + newStaff);
        } catch (IOException e) {
            // Handle any errors that occur during the addition process
            System.err.println("Error adding staff member: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Handle duplicate or invalid entries
            System.out.println("Failed to add staff: " + e.getMessage());
        }
    }


    /**
     * Searches and displays staff members filtered by criteria.
     *
     * @param userID the staff member's ID to filter by, or null to ignore
     * @param name   the staff member's name to filter by, or null to ignore
     * @param role   the staff member's role to filter by, or null to ignore
     */
    public void searchStaff(String userID, String name, Roles role) {
        List<Users> staff = data.getAllData();

        if (userID != null) {
            staff = staff.stream()
                    .filter(s -> s.getUserID().equalsIgnoreCase(userID))
                    .collect(Collectors.toList());
        }

        if (name != null) {
            staff = staff.stream()
                    .filter(s -> s.getName().equalsIgnoreCase(name))
                    .collect(Collectors.toList());
        }

        if (role != null) {
            staff = staff.stream()
                    .filter(s -> s.getRole() == role)
                    .collect(Collectors.toList());
        }

        if (staff.isEmpty()) {
            System.out.println("No staff members found matching the criteria.");
        } else {
            System.out.printf("%-10s %-20s %-15s %-10s %-5s%n", "UserID", "Name", "Role", "Gender", "Age");
            System.out.println("----------------------------------------------------------");
            for (Users user : staff) {
                System.out.printf("%-10s %-20s %-15s %-10s %-5d%n",
                        user.getUserID(), user.getName(), user.getRole(), user.getGender(), user.getAge());
            }
        }
    }

    /**
     * Updates the details of a staff member.
     *
     * @param userID   the ID of the staff member to update
     * @param newName  the new name (optional)
     * @param newAge   the new age (optional)
     * @param newRole  the new role (optional)
     * @param newGender the new gender (optional)
     * @param newPassword the new password (optional)
     */
    public void updateStaff(String userID, String newName, Integer newAge, Roles newRole, Gender newGender, String newPassword) {
        Users staff = data.findUserById(userID);

        if (staff == null) {
            System.out.println("Staff member not found with ID: " + userID);
            return;
        }

        try {
            if (newName != null) {
                staff.setName(newName);
            }
            if (newAge != null) {
                staff.setAge(newAge);
            }
            if (newRole != null) {
                staff.setRole(newRole);
            }
            if (newGender != null) {
                staff.setGender(newGender);
            }
            if (newPassword != null) {
                staff.setPassword(newPassword);
            }
            data.updateStaff(staff);
            System.out.println("Staff member updated successfully.");
        } catch (IOException e) {
            System.err.println("Error updating staff member: " + e.getMessage());
        }
    }

    /**
     * Deletes a staff member by their User ID.
     *
     * @param userID the ID of the staff member to delete
     */
    public void deleteStaff(String userID) {
        try {
            // Use the StaffData method to handle removal and persistence
            data.removeStaff(userID);

            // Log success message
            System.out.println("Staff member removed successfully.");
        } catch (IOException e) {
            // Log file-related errors
            System.err.println("Error removing staff member: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Log validation errors (e.g., staff member not found)
            System.out.println(e.getMessage());
        }
    }


    /**
     * Displays all staff members.
     */
    public void viewAllStaff() {
        List<Users> staffList = data.getAllData();

        if (staffList.isEmpty()) {
            System.out.println("No staff members found.");
        } else {
            System.out.printf("%-10s %-20s %-15s %-10s %-5s%n", "UserID", "Name", "Role", "Gender", "Age");
            System.out.println("----------------------------------------------------------");
            for (Users user : staffList) {
                System.out.printf("%-10s %-20s %-15s %-10s %-5d%n",
                        user.getUserID(), user.getName(), user.getRole(), user.getGender(), user.getAge());
            }
        }
    }
}
