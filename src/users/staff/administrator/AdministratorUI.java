package users.staff.administrator;

import datamgmt.retrievers.*;
import users.ui.BaseUI;

import java.util.Scanner;

/**
 * Main menu for administrators to manage the hospital's core functionalities.
 * Links to submenus for staff management, appointment management, inventory management,
 * patient management, and logout.
 */
public class AdministratorUI extends BaseUI {

    private final StaffData staffData;
    private final AppointmentData appointmentData;
    private final MedicineData medicineData;
    private final ReplenishmentRequestData replenishmentRequestData;
    private final PatientData patientData;

    /**
     * Constructs the AdministratorMainMenu and initializes required data handlers.
     */
    public AdministratorUI(Administrator user) {
        this.staffData = new StaffData();
        this.appointmentData = new AppointmentData();
        this.medicineData = new MedicineData();
        this.replenishmentRequestData = new ReplenishmentRequestData();
        this.patientData = new PatientData();

        // Import data to ensure fresh start
        this.staffData.importData();
        this.appointmentData.importData();
        this.medicineData.importData();
        this.replenishmentRequestData.importData();
        this.patientData.importData();
    }

    @Override
    public void displayMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayMenuHeader("ADMINISTRATOR MAIN MENU");
            displayMenuOption(1, "Staff Management");
            displayMenuOption(2, "Appointment Management");
            displayMenuOption(3, "Inventory Management");
            displayMenuOption(4, "Patient Management");
            displayMenuOption(5, "Logout");

            int choice = getMenuChoice();

            if (choice == 1) {
                openStaffManagementMenu(scanner);
            } else if (choice == 2) {
                openAppointmentManagementMenu(scanner);
            } else if (choice == 3) {
                openInventoryManagementMenu(scanner);
            } else if (choice == 4) {
                openPatientManagementMenu(scanner);
            } else if (choice == 5) {
                System.out.println("Logging out...");
                return;
            } else {
                displayInvalidInputMessage();
            }
            pauseForUser();
        }
    }

    /**
     * Opens the staff management menu.
     */
    private void openStaffManagementMenu(Scanner scanner) {
        AdministratorStaffManagementUI staffMenu = new AdministratorStaffManagementUI(scanner);
        staffMenu.displayMenu();
    }

    /**
     * Opens the appointment management menu.
     */
    private void openAppointmentManagementMenu(Scanner scanner) {
        AdministratorAppointmentManagementUI appointmentMenu = new AdministratorAppointmentManagementUI(scanner);
        appointmentMenu.displayMenu();
    }

    /**
     * Opens the inventory management menu.
     */
    private void openInventoryManagementMenu(Scanner scanner) {
        AdministratorInventoryManagementUI inventoryMenu = new AdministratorInventoryManagementUI(medicineData, replenishmentRequestData, scanner);
        inventoryMenu.displayMenu();
    }

    /**
     * Opens the patient management menu.
     */
    private void openPatientManagementMenu(Scanner scanner) {
        AdministratorPatientManagementUI patientMenu = new AdministratorPatientManagementUI(patientData, scanner);
        patientMenu.displayMenu();
    }
}
