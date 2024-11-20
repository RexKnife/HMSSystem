package utils.medicinemanagements;

/**
 * Represents a medicine with a name, initial stock, and low stock alert threshold.
 */
public class Medicine {
    private String name;
    private int initialStock;
    private int lowStockLevelAlert;

    /**
     * Constructs a Medicine object with the given details.
     *
     * @param name              the name of the medicine
     * @param initialStock      the initial stock quantity
     * @param lowStockLevelAlert the low stock level alert threshold
     */
    public Medicine(String name, int initialStock, int lowStockLevelAlert) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Medicine name cannot be null or empty.");
        }
        if (initialStock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative.");
        }
        if (lowStockLevelAlert < 0) {
            throw new IllegalArgumentException("Low stock level alert cannot be negative.");
        }

        this.name = name;
        this.initialStock = initialStock;
        this.lowStockLevelAlert = lowStockLevelAlert;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public int getInitialStock() {
        return initialStock;
    }

    public int getLowStockLevelAlert() {
        return lowStockLevelAlert;
    }

    public void setInitialStock(int initialStock) {
        if (initialStock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative.");
        }
        this.initialStock = initialStock;
    }

    public void setLowStockLevelAlert(int lowStockLevelAlert) {
        if (lowStockLevelAlert < 0) {
            throw new IllegalArgumentException("Low stock level alert cannot be negative.");
        }
        this.lowStockLevelAlert = lowStockLevelAlert;
    }

    /**
     * Refills the stock of the medicine by the specified quantity.
     *
     * @param quantity the quantity to add to the stock
     */
    public void refillStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Refill quantity must be positive.");
        }
        this.initialStock += quantity;
    }

    @Override
    public String toString() {
        return String.format("Name: %s, Initial Stock: %d, Low Stock Alert: %d", name, initialStock, lowStockLevelAlert);
    }
}
