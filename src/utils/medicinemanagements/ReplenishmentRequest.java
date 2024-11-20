package utils.medicinemanagements;

import utils.enums.RequestStatus;

/**
 * Represents a replenishment request for medication inventory.
 */
public class ReplenishmentRequest {
    private String medicineName;
    private int requestedStock;
    private RequestStatus status;
    private String requestBy;
    private Boolean isNewMedicine;

    /**
     * Constructs a new replenishment request with the specified status.
     *
     * @param medicineName   the name of the medicine
     * @param requestedStock the quantity requested
     * @param requestBy      the requester (e.g., pharmacist ID)
     * @param isNewMedicine  whether the medicine is a new addition to the inventory
     * @param status         the status of the replenishment request
     */
    public ReplenishmentRequest(String medicineName, int requestedStock, String requestBy, boolean isNewMedicine, RequestStatus status) {
        if (medicineName == null || medicineName.trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name cannot be null or empty.");
        }
        if (requestedStock <= 0) {
            throw new IllegalArgumentException("Requested stock must be greater than zero.");
        }
        if (requestBy == null || requestBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Requester ID cannot be null or empty.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Request status cannot be null.");
        }

        this.medicineName = medicineName;
        this.requestedStock = requestedStock;
        this.requestBy = requestBy;
        this.isNewMedicine = isNewMedicine;
        this.status = status;
    }

    /**
     * Constructs a new replenishment request with default status as PENDING.
     *
     * @param medicineName   the name of the medicine
     * @param requestedStock the quantity requested
     * @param requestBy      the requester (e.g., pharmacist ID)
     * @param isNewMedicine  whether the medicine is a new addition to the inventory
     */
    public ReplenishmentRequest(String medicineName, int requestedStock, String requestBy, boolean isNewMedicine) {
        this(medicineName, requestedStock, requestBy, isNewMedicine, RequestStatus.PENDING);
    }

    // Getters
    public String getMedicineName() {
        return medicineName;
    }

    public int getRequestedStock() {
        return requestedStock;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getRequestBy() {
        return requestBy;
    }

    public Boolean getIsNewMedicine() {
        return isNewMedicine;
    }

    // Setters
    public void setMedicineName(String medicineName) {
        if (medicineName == null || medicineName.trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name cannot be null or empty.");
        }
        this.medicineName = medicineName;
    }

    public void setRequestedStock(int requestedStock) {
        if (requestedStock <= 0) {
            throw new IllegalArgumentException("Requested stock must be greater than zero.");
        }
        this.requestedStock = requestedStock;
    }

    public void setStatus(RequestStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Request status cannot be null.");
        }
        this.status = status;
    }

    public void setRequestBy(String requestBy) {
        if (requestBy == null || requestBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Requester ID cannot be null or empty.");
        }
        this.requestBy = requestBy;
    }

    public void setIsNewMedicine(Boolean isNewMedicine) {
        this.isNewMedicine = isNewMedicine != null ? isNewMedicine : false;
    }

    @Override
    public String toString() {
        return String.format(
                "Medicine Name: %s, Requested Stock: %d, Status: %s, Requested By: %s, Is New Medicine: %s",
                medicineName, requestedStock, status, requestBy, isNewMedicine ? "Yes" : "No"
        );
    }

    /**
     * Updates the status of the replenishment request.
     *
     * @param newStatus the new status of the request
     */
    public void updateStatus(RequestStatus newStatus) {
        setStatus(newStatus);
    }

    /**
     * Validates if the replenishment request is for a new medicine.
     *
     * @return true if it is a new medicine request, false otherwise
     */
    public boolean isNewMedicineRequest() {
        return Boolean.TRUE.equals(isNewMedicine);
    }
}
