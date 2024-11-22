package users.staff.administrator;

import datamgmt.retrievers.MedicineData;
import datamgmt.retrievers.ReplenishmentRequestData;
import utils.medicinemanagements.MedicineManager;
import utils.medicinemanagements.ReplenishmentRequestManager;
import users.ui.BaseUI;

import java.util.Scanner;

/**
 * UI for administrators to manage inventory, including medicine management
 * and replenishment requests.
 */
public class AdministratorInventoryManagementUI extends BaseUI {

    private final MedicineManager medicineManager;
    private final ReplenishmentRequestManager replenishmentRequestManager;
    private Scanner scanner;
    /**
     * Constructs the AdministratorInventoryManagementUI with necessary managers.
     *
     * @param medicineData            the medicine data handler
     * @param replenishmentRequestData the replenishment request data handler
     */
    public AdministratorInventoryManagementUI(MedicineData medicineData, ReplenishmentRequestData replenishmentRequestData, Scanner scanner) {
        this.medicineManager = new MedicineManager(medicineData);
        this.replenishmentRequestManager = new ReplenishmentRequestManager(replenishmentRequestData, medicineManager);
        this.scanner = scanner;
    }

    @Override
    public void displayMenu() {
        while (true) {
            displayMenuHeader("INVENTORY MANAGEMENT MENU");
            displayMenuOption(1, "View Medicine Inventory");
            displayMenuOption(2, "Add New Medicine");
            displayMenuOption(3, "Remove Medicine");
            displayMenuOption(4, "Update Medicine Details");
            displayMenuOption(5, "Search Medicine by Name");
            displayMenuOption(6, "Manage Replenishment Requests");
            displayMenuOption(7, "Go Back to Main Menu");

            int choice = getMenuChoice();

            if (choice == 1) {
                medicineManager.displayInventory();
            } else if (choice == 2) {
                medicineManager.addMedicine(this.scanner);
            } else if (choice == 3) {
                medicineManager.removeMedicine(this.scanner);
            } else if (choice == 4) {
                medicineManager.updateMedicineDetails(this.scanner);
            } else if (choice == 5) {
                medicineManager.searchMedicineByName(this.scanner);
            } else if (choice == 6) {
                manageReplenishmentRequests(this.scanner);
            } else if (choice == 7) {
                System.out.println("Returning to the main menu...");
                return;
            } else {
                displayInvalidInputMessage();
            }
            pauseForUser();
        }
    }

    /**
     * Manages replenishment requests by allowing approval, denial, and viewing requests.
     *
     * @param scanner the Scanner for user input
     */
    private void manageReplenishmentRequests(Scanner scanner) {
        while (true) {
            displayMenuHeader("REPLENISHMENT REQUESTS MANAGEMENT");
            displayMenuOption(1, "View All Replenishment Requests");
            displayMenuOption(2, "Approve or Deny Requests");
            displayMenuOption(3, "Search Requests by Medicine Name");
            displayMenuOption(4, "Return to Inventory Management");

            int choice = getMenuChoice();

            if (choice == 1) {
                replenishmentRequestManager.displayPendingRequests();
            } else if (choice == 2) {
                replenishmentRequestManager.managePendingRequests(scanner);
            } else if (choice == 3) {
                replenishmentRequestManager.searchRequestByMedicineName(scanner);
            } else if (choice == 4) {
                System.out.println("Returning to Inventory Management...");
                return;
            } else {
                displayInvalidInputMessage();
            }
            pauseForUser();
        }
    }
}
