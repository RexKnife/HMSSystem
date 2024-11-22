package utils.medicinemanagements;

import datamgmt.retrievers.MedicineData;

import java.util.Optional;
import java.util.Scanner;

/**
 * Provides functionality for managing medicines, including refilling stock,
 * removing medicines, adding new medicines, displaying inventory, and updating medicine details.
 */
public class MedicineManager {
    private final MedicineData medicineData;

    /**
     * Constructs the MedicineManager with the given MedicineData handler.
     *
     * @param medicineData the MedicineData handler
     */
    public MedicineManager(MedicineData medicineData) {
        this.medicineData = medicineData;
        this.medicineData.reloadData();
    }

    /**
     * Displays the entire inventory of medicines.
     */
    public void displayInventory() {
        System.out.println("\n--- Medicine Inventory ---");
        if (medicineData.getAllData().isEmpty()) {
            System.out.println("No medicines found in the inventory.");
            return;
        }

        System.out.printf("%-20s %-15s %-15s%n", "Name", "Initial Stock", "Low Stock Alert");
        System.out.println("----------------------------------------------");
        for (Medicine medicine : medicineData.getAllData()) {
            System.out.printf("%-20s %-15d %-15d%n",
                    medicine.getName(),
                    medicine.getInitialStock(),
                    medicine.getLowStockLevelAlert());
        }
    }
    /**
     * Exposes the underlying MedicineData instance for external usage.
     *
     * @return the MedicineData instance
     */
    public MedicineData getMedicineData() {
        return medicineData;
    }
    /**
     * Searches for a medicine by its name and displays its details.
     *
     * @param scanner the Scanner for user input
     */
    public void searchMedicineByName(Scanner scanner) {
        System.out.print("Enter the name of the medicine to search: ");
        String name = scanner.nextLine().trim();

        Optional<Medicine> medicine = medicineData.findMedicineByName(name);
        if (medicine.isPresent()) {
            System.out.println("\n--- Medicine Details ---");
            System.out.printf("Name: %s%nInitial Stock: %d%nLow Stock Alert: %d%n",
                    medicine.get().getName(),
                    medicine.get().getInitialStock(),
                    medicine.get().getLowStockLevelAlert());
        } else {
            System.out.println("Medicine not found.");
        }
    }

    /**
     * Adds a new medicine to the inventory.
     *
     * @param scanner the Scanner for user input
     */
    public void addMedicine(Scanner scanner) {
        System.out.print("Enter the name of the new medicine: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter the initial stock quantity: ");
        int initialStock = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter the low stock level alert threshold: ");
        int lowStockLevel = Integer.parseInt(scanner.nextLine().trim());

        Medicine newMedicine = new Medicine(name, initialStock, lowStockLevel);

        try {
            medicineData.addMedicine(newMedicine);
            System.out.println("Medicine added successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error adding medicine: " + e.getMessage());
        }
    }

    /**
     * Refills the stock of an existing medicine.
     *
     * @param scanner the Scanner for user input
     */
    public void refillStock(Scanner scanner) {
        System.out.print("Enter the name of the medicine to refill: ");
        String name = scanner.nextLine().trim();

        Optional<Medicine> medicine = medicineData.findMedicineByName(name);
        if (medicine.isEmpty()) {
            System.out.println("Medicine not found.");
            return;
        }

        System.out.print("Enter the quantity to add to the stock: ");
        int quantityToAdd = Integer.parseInt(scanner.nextLine().trim());

        Medicine updatedMedicine = medicine.get();
        updatedMedicine.refillStock(quantityToAdd);

        try {
            medicineData.updateMedicine(updatedMedicine);
            System.out.println("Stock refilled successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error refilling stock: " + e.getMessage());
        }
    }

    /**
     * Removes a medicine from the inventory.
     *
     * @param scanner the Scanner for user input
     */
    public void removeMedicine(Scanner scanner) {
        System.out.print("Enter the name of the medicine to remove: ");
        String name = scanner.nextLine().trim();

        try {
            medicineData.removeMedicine(name);
            System.out.println("Medicine removed successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error removing medicine: " + e.getMessage());
        }
    }

    /**
     * Updates the details of an existing medicine.
     *
     * @param scanner the Scanner for user input
     */
    public void updateMedicineDetails(Scanner scanner) {
        System.out.print("Enter the name of the medicine to update: ");
        String name = scanner.nextLine().trim();

        Optional<Medicine> medicine = medicineData.findMedicineByName(name);
        if (medicine.isEmpty()) {
            System.out.println("Medicine not found.");
            return;
        }

        Medicine existingMedicine = medicine.get();

        System.out.println("\n--- Update Medicine Details ---");
        System.out.print("Enter new initial stock (leave blank to keep unchanged): ");
        String initialStockInput = scanner.nextLine().trim();
        if (!initialStockInput.isEmpty()) {
            existingMedicine.setInitialStock(Integer.parseInt(initialStockInput));
        }

        System.out.print("Enter new low stock alert threshold (leave blank to keep unchanged): ");
        String lowStockInput = scanner.nextLine().trim();
        if (!lowStockInput.isEmpty()) {
            existingMedicine.setLowStockLevelAlert(Integer.parseInt(lowStockInput));
        }

        try {
            medicineData.updateMedicine(existingMedicine);
            System.out.println("Medicine details updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating medicine: " + e.getMessage());
        }
    }
}
