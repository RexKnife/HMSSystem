package utils.medicinemanagements;

import datamgmt.retrievers.ReplenishmentRequestData;
import utils.enums.RequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Manages replenishment requests, allowing for creation, editing, and management
 * of requests for medication stock.
 */
public class ReplenishmentRequestManager {
    private final ReplenishmentRequestData requestData;
    private final MedicineManager medicineManager;

    /**
     * Constructs the ReplenishmentRequestManager with the provided data handler and MedicineManager.
     *
     * @param requestData     the handler for replenishment request data
     * @param medicineManager the manager for medicine inventory
     */
    public ReplenishmentRequestManager(ReplenishmentRequestData requestData, MedicineManager medicineManager) {
        this.requestData = requestData;
        this.medicineManager = medicineManager;
        this.requestData.reloadData();
    }
    /**
     * Allows a pharmacist to create a new replenishment request.
     *
     * @param scanner       the Scanner for user input
     * @param pharmacistID  the ID of the pharmacist making the request
     */
    public void createRequest(Scanner scanner, String pharmacistID) {
        System.out.println("\n======== Create Replenishment Request ========");

        // Prompt for medicine name
        System.out.print("Enter the medicine name: ");
        String medicineName = scanner.nextLine().trim();

        // Check if the medicine exists in the inventory
        Optional<Medicine> medicine = medicineManager.getMedicineData().findMedicineByName(medicineName);

        boolean isNewMedicine = false;
        if (medicine.isPresent()) {
            System.out.println("Medicine found in inventory.");
        } else {
            // Prompt for new medicine confirmation
            System.out.print("This medicine is not in the inventory. Is it a new medicine? (yes/no): ");
            String newMedicineResponse = scanner.nextLine().trim().toLowerCase();
            isNewMedicine = newMedicineResponse.equals("yes");

            if (!isNewMedicine) {
                System.out.println("Cannot proceed with the request for an unknown medicine.");
                return;
            }
        }

        // Prompt for requested quantity
        System.out.print("Enter the quantity requested: ");
        int requestedQuantity;
        try {
            requestedQuantity = Integer.parseInt(scanner.nextLine().trim());
            if (requestedQuantity <= 0) {
                System.out.println("Quantity must be greater than zero. Request not created.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Quantity must be a number. Request not created.");
            return;
        }

        // Create and add the replenishment request
        ReplenishmentRequest newRequest = new ReplenishmentRequest(
                medicineName,
                requestedQuantity,
                pharmacistID,
                isNewMedicine,
                RequestStatus.PENDING
        );

        try {
            requestData.addRequest(newRequest);
            System.out.println("Replenishment request created successfully.");
        } catch (Exception e) {
            System.err.println("Error creating request: " + e.getMessage());
        }

        // Automatically update stock for existing medicines
        if (medicine.isPresent()) {
            Medicine existingMedicine = medicine.get();
            existingMedicine.refillStock(requestedQuantity);

            try {
                medicineManager.getMedicineData().updateMedicine(existingMedicine);
                System.out.println("Stock updated successfully for existing medicine.");
            } catch (Exception e) {
                System.err.println("Error updating stock: " + e.getMessage());
            }
        }
    }

    /**
     * Displays all pending replenishment requests in the system.
     */
    public void displayPendingRequests() {
        System.out.println("\n======== Pending Replenishment Requests ========");
        List<ReplenishmentRequest> pendingRequests = requestData.getRequests().stream()
                .filter(request -> request.getStatus() == RequestStatus.PENDING)
                .collect(Collectors.toList());

        if (pendingRequests.isEmpty()) {
            System.out.println("No pending replenishment requests found.");
            return;
        }

        System.out.printf("%-5s %-20s %-15s %-20s %-10s%n", "No.", "Medicine Name", "Requested Qty", "Requested By", "New Medicine");
        System.out.println("-------------------------------------------------------------------------------");
        int index = 1;
        for (ReplenishmentRequest request : pendingRequests) {
            System.out.printf("%-5d %-20s %-15d %-20s %-10s%n",
                    index++,
                    request.getMedicineName(),
                    request.getRequestedStock(),
                    request.getRequestBy(),
                    request.getIsNewMedicine() ? "Yes" : "No");
        }
        System.out.println("===============================================================================");
    }

    /**
     * Allows administrators to approve or deny pending replenishment requests.
     *
     * @param scanner the Scanner for user input
     */
    public void managePendingRequests(Scanner scanner) {
        System.out.println("\n======== Manage Pending Replenishment Requests ========");
        List<ReplenishmentRequest> pendingRequests = requestData.getRequests().stream()
                .filter(request -> request.getStatus() == RequestStatus.PENDING)
                .collect(Collectors.toList());

        if (pendingRequests.isEmpty()) {
            System.out.println("No pending requests to manage.");
            return;
        }

        System.out.printf("%-5s %-20s %-15s %-20s %-10s%n", "No.", "Medicine Name", "Requested Qty", "Requested By", "New Medicine");
        System.out.println("-------------------------------------------------------------------------------");
        int index = 1;
        for (ReplenishmentRequest request : pendingRequests) {
            System.out.printf("%-5d %-20s %-15d %-20s %-10s%n",
                    index++,
                    request.getMedicineName(),
                    request.getRequestedStock(),
                    request.getRequestBy(),
                    request.getIsNewMedicine() ? "Yes" : "No");
        }
        System.out.println("===============================================================================");

        System.out.print("Enter the number of the request to manage (or 0 to exit): ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            return;
        }

        if (choice == 0) {
            System.out.println("Exiting request management.");
            return;
        }

        if (choice < 1 || choice > pendingRequests.size()) {
            System.out.println("Invalid choice. Please try again.");
            return;
        }

        ReplenishmentRequest selectedRequest = pendingRequests.get(choice - 1);

        System.out.println("\nSelected Request:");
        System.out.printf("Medicine: %s%nQuantity: %d%nRequested By: %s%nNew Medicine: %s%n",
                selectedRequest.getMedicineName(),
                selectedRequest.getRequestedStock(),
                selectedRequest.getRequestBy(),
                selectedRequest.getIsNewMedicine() ? "Yes" : "No");

        System.out.print("Approve or Deny this request? (approve/deny): ");
        String action = scanner.nextLine().trim().toLowerCase();

        if (action.equals("approve")) {
            selectedRequest.setStatus(RequestStatus.FULFILLED);

            // If the medicine exists, update its stock
            Optional<Medicine> medicine = medicineManager.getMedicineData().findMedicineByName(selectedRequest.getMedicineName());
            if (medicine.isPresent()) {
                Medicine updatedMedicine = medicine.get();
                updatedMedicine.refillStock(selectedRequest.getRequestedStock());
                try {
                    medicineManager.getMedicineData().updateMedicine(updatedMedicine);
                    System.out.println("Stock updated successfully.");
                } catch (Exception e) {
                    System.err.println("Error updating stock: " + e.getMessage());
                }
            }

            requestData.updateRequest(selectedRequest);
            System.out.println("Request approved.");
        } else if (action.equals("deny")) {
            selectedRequest.setStatus(RequestStatus.CANCELLED);
            requestData.updateRequest(selectedRequest);
            System.out.println("Request denied.");
        } else {
            System.out.println("Invalid action. Request remains unchanged.");
        }
    }
    /**
     * Searches for replenishment requests by medicine name.
     *
     * @param scanner the Scanner for user input
     */
    public void searchRequestByMedicineName(Scanner scanner) {
        System.out.println("\n======== Search Replenishment Requests ========");
        System.out.print("Enter the medicine name to search for: ");
        String medicineName = scanner.nextLine().trim();

        // Filter requests that match the entered medicine name
        List<ReplenishmentRequest> matchingRequests = requestData.getRequests().stream()
                .filter(request -> request.getMedicineName().equalsIgnoreCase(medicineName))
                .collect(Collectors.toList());

        if (matchingRequests.isEmpty()) {
            System.out.println("No replenishment requests found for the specified medicine.");
            return;
        }

        // Display matching requests
        System.out.printf("%-5s %-20s %-15s %-15s %-15s %-10s%n", "No.", "Medicine Name", "Requested Qty", "Status", "Requested By", "New Medicine");
        System.out.println("-----------------------------------------------------------------------------------------");
        int index = 1;
        for (ReplenishmentRequest request : matchingRequests) {
            System.out.printf("%-5d %-20s %-15d %-15s %-15s %-10s%n",
                    index++,
                    request.getMedicineName(),
                    request.getRequestedStock(),
                    request.getStatus(),
                    request.getRequestBy(),
                    request.getIsNewMedicine() ? "Yes" : "No");
        }
        System.out.println("=========================================================================================");
    }

}
