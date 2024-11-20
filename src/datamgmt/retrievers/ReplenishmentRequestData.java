package datamgmt.retrievers;

import utils.ValidationUtils;
import utils.enums.RequestStatus;
import utils.env;
import utils.medicinemanagements.ReplenishmentRequest;

import java.io.IOException;
import java.util.List;
/**
 * Manages the retrieval, parsing, and management of replenishment request data
 * from the file specified in the {@link env}.
 */
public class ReplenishmentRequestData extends BaseDataHandler<ReplenishmentRequest> {
    private final env environment;

    /**
     * Initializes the replenishment request data handler with an environment configuration.
     */
    public ReplenishmentRequestData() {
        this.environment = new env();
    }

    /**
     * Imports replenishment request data from the file path specified in the {@link env}.
     */
    public void importData() {
        try {
            loadData(environment.getReplenishmentRequestDataPath());
        } catch (IOException e) {
            System.err.println("Error reading replenishment request data: " + e.getMessage());
        }
    }

    /**
     * Reloads all replenishment request data by clearing existing data and re-importing it.
     */
    public void reloadData() {
        clearData();
        importData();
    }

    @Override
    protected ReplenishmentRequest parseLine(String line) {
        String[] data = line.split(",", -1); // Handle empty fields as well
        if (data.length != 5) {
            System.err.println("Invalid data format: " + line);
            return null;
        }

        try {
            // Validate and parse each field
            String medicineName = ValidationUtils.validateStringNotEmpty(data[0].trim(), "Medicine Name");
            int requestedQuantity = Integer.parseInt(data[1].trim());
            RequestStatus status = RequestStatus.valueOf(data[2].trim().toUpperCase());
            String requestBy = ValidationUtils.validateStringNotEmpty(data[3].trim(), "Request By");
            boolean isNewMedicine = Boolean.parseBoolean(data[4].trim());

            return new ReplenishmentRequest(medicineName, requestedQuantity, requestBy, isNewMedicine, status);
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing replenishment request data: " + line + " - " + e.getMessage());
            return null;
        }
    }

    @Override
    protected String formatItem(ReplenishmentRequest request) {
        return String.join(",",
                request.getMedicineName(),
                String.valueOf(request.getRequestedStock()),
                request.getStatus().name(),
                request.getRequestBy(),
                String.valueOf(request.getIsNewMedicine())
        );
    }

    @Override
    protected String getHeader() {
        return "MedicineName,RequestedQuantity,Status,RequestBy,IsNewMedicine";
    }

    /**
     * Retrieves the list of all replenishment requests.
     *
     * @return a list of {@link ReplenishmentRequest} objects
     */
    public List<ReplenishmentRequest> getRequests() {
        return getAllData();
    }

    /**
     * Adds a new replenishment request.
     *
     * @param request the {@link ReplenishmentRequest} object to add
     */
    public void addRequest(ReplenishmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Replenishment request cannot be null.");
        }

        if (!dataList.contains(request)) {
            dataList.add(request);
            try {
                appendData(environment.getReplenishmentRequestDataPath(), request);
            } catch (IOException e) {
                System.err.println("Error appending replenishment request: " + e.getMessage());
            }
        } else {
            System.err.println("Duplicate replenishment request detected: " + request.getMedicineName());
        }
    }

    /**
     * Updates an existing replenishment request in the file and memory.
     *
     * @param updatedRequest the updated {@link ReplenishmentRequest} object
     */
    public void updateRequest(ReplenishmentRequest updatedRequest) {
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getMedicineName().equals(updatedRequest.getMedicineName()) &&
                dataList.get(i).getRequestBy().equals(updatedRequest.getRequestBy())) {
                dataList.set(i, updatedRequest);
                try {
                    writeData(environment.getReplenishmentRequestDataPath());
                } catch (IOException e) {
                    System.err.println("Error updating replenishment request: " + e.getMessage());
                }
                return;
            }
        }
        System.err.println("Replenishment request not found for update: " + updatedRequest.getMedicineName());
    }
}
