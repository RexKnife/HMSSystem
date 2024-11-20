package utils.medicinemanagements;

import datamgmt.retrievers.ReplenishmentRequestData;
import utils.enums.RequestStatus;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Manages replenishment requests, allowing for creation, editing, and management
 * of requests for medication stock.
 */
public class ReplenishmentRequestManager {
    private final ReplenishmentRequestData requestData;

    /**
     * Constructs the ReplenishmentRequestManager with the provided data handler.
     *
     * @param requestData the handler for replenishment request data
     */
    public ReplenishmentRequestManager(ReplenishmentRequestData requestData) {
        this.requestData = requestData;
        this.requestData.reloadData();
    }

    /**
     * Displays all replenishment requests in the system.
     */
    public void displayAllRequests() {
        System.out.println("\n--- Replenishment Requests ---");
        List<ReplenishmentRequest> requests = requestData.getRequests();

        if (requests.isEmpty()) {
            System.out.println("No requests found.");
            return;
        }

        System.out.printf("%-20s %-15s %-15s %-15s %-10s%n", "Medicine Name", "Requested Qty", "Status", "Requested By", "New Medicine");
        System.out.println("-----------------------------------------------------------------------------------");
        for (ReplenishmentRequest request : requests) {
            System.out.printf("%-20s %-15d %-15s %-15s %-10s%n",
                    request.getMedicineName(),
                    request.getRequestedStock(),
                    request.getStatus(),
                    request.getRequestBy(),
                    request.getIsNewMedicine() ? "Yes" : "No");
        }
    }

    /**
     * Allows a pharmacist to create a new replenishment request.
     *
     * @param scanner the Scanner for user input
     * @param pharmacistID the ID of the pharmacist making the request
     */
    public void createRequest(Scanner scanner, String pharmacistID) {
        System.out.println("\n--- Create Replenishment Request ---");

        System.out.print("Enter the medicine name: ");
        String medicineName = scanner.nextLine().trim();

        System.out.print("Enter the quantity requested: ");
        int quantity = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Is this a request for a new medicine? (yes/no): ");
        boolean isNewMedicine = scanner.nextLine().trim().equalsIgnoreCase("yes");

        ReplenishmentRequest newRequest = new ReplenishmentRequest(medicineName, quantity, pharmacistID, isNewMedicine, RequestStatus.PENDING);

        try {
            requestData.addRequest(newRequest);
            System.out.println("Replenishment request created successfully.");
        } catch (Exception e) {
            System.err.println("Error creating request: " + e.getMessage());
        }
    }

    /**
     * Allows administrators to approve or deny pending replenishment requests.
     *
     * @param scanner the Scanner for user input
     */
    public void managePendingRequests(Scanner scanner) {
        System.out.println("\n--- Manage Pending Replenishment Requests ---");

        List<ReplenishmentRequest> pendingRequests = requestData.getRequests().stream()
                .filter(request -> request.getStatus() == RequestStatus.PENDING)
                .collect(Collectors.toList());

        if (pendingRequests.isEmpty()) {
            System.out.println("No pending requests to manage.");
            return;
        }

        int index = 1;
        for (ReplenishmentRequest request : pendingRequests) {
            System.out.printf("%d. Medicine: %s, Quantity: %d, Requested By: %s, New Medicine: %s%n",
                    index++, request.getMedicineName(), request.getRequestedStock(), request.getRequestBy(),
                    request.getIsNewMedicine() ? "Yes" : "No");
        }

        System.out.print("Enter the number of the request to manage (or 0 to exit): ");
        int choice = Integer.parseInt(scanner.nextLine().trim());

        if (choice == 0) {
            System.out.println("Exiting request management.");
            return;
        }

        if (choice < 1 || choice > pendingRequests.size()) {
            System.out.println("Invalid choice. Please try again.");
            return;
        }

        ReplenishmentRequest selectedRequest = pendingRequests.get(choice - 1);

        System.out.print("Approve or Deny this request? (approve/deny): ");
        String action = scanner.nextLine().trim().toLowerCase();

        if (action.equals("approve")) {
            selectedRequest.setStatus(RequestStatus.FULFILLED);
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
        System.out.print("Enter the medicine name to search for: ");
        String medicineName = scanner.nextLine().trim();

        List<ReplenishmentRequest> results = requestData.getRequests().stream()
                .filter(request -> request.getMedicineName().equalsIgnoreCase(medicineName))
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            System.out.println("No requests found for the specified medicine.");
        } else {
            System.out.printf("%-20s %-15s %-15s %-15s %-10s%n", "Medicine Name", "Requested Qty", "Status", "Requested By", "New Medicine");
            System.out.println("-----------------------------------------------------------------------------------");
            for (ReplenishmentRequest request : results) {
                System.out.printf("%-20s %-15d %-15s %-15s %-10s%n",
                        request.getMedicineName(),
                        request.getRequestedStock(),
                        request.getStatus(),
                        request.getRequestBy(),
                        request.getIsNewMedicine() ? "Yes" : "No");
            }
        }
    }
}
