package datamgmt.retrievers;

import utils.medicinemanagements.Medicine;
import utils.env;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicineData {
    private final List<Medicine> medicines;
    private final String filePath;

    public MedicineData() {
        medicines = new ArrayList<>();
        filePath = new env().getMedicinePath(); // Get the file path from the env class
    }

    /**
     * Imports medicine data from the file specified in the env configuration.
     */
    public void importData() {
        medicines.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) {
                    String name = data[0].trim();
                    int initialStock = Integer.parseInt(data[1].trim());
                    int lowStockLevel = Integer.parseInt(data[2].trim());
                    medicines.add(new Medicine(name, initialStock, lowStockLevel));
                }
            }
        } catch (IOException e) {
            System.err.println("Error importing medicine data: " + e.getMessage());
        }
    }

    /**
     * Reloads all data by clearing existing records and re-importing from the file.
     */
    public void reloadData() {
        importData();
    }

    /**
     * Retrieves all medicines as a list.
     *
     * @return the list of medicines
     */
    public List<Medicine> getAllData() {
        return new ArrayList<>(medicines); // Return a copy to prevent external modifications
    }

    /**
     * Finds a medicine by its name.
     *
     * @param name the name of the medicine
     * @return an optional containing the medicine if found, or empty otherwise
     */
    public Optional<Medicine> findMedicineByName(String name) {
        return medicines.stream()
                .filter(medicine -> medicine.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Adds a new medicine to the list and saves it to the file.
     *
     * @param medicine the medicine to add
     */
    public void addMedicine(Medicine medicine) {
        if (findMedicineByName(medicine.getName()).isPresent()) {
            throw new IllegalArgumentException("Medicine with name '" + medicine.getName() + "' already exists.");
        }
        medicines.add(medicine);
        saveData();
    }

    /**
     * Removes a medicine from the list and updates the file.
     *
     * @param name the name of the medicine to remove
     */
    public void removeMedicine(String name) {
        Medicine medicine = findMedicineByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Medicine with name '" + name + "' not found."));
        medicines.remove(medicine);
        saveData();
    }

    /**
     * Updates an existing medicine in the list and saves it to the file.
     *
     * @param medicine the updated medicine object
     */
    public void updateMedicine(Medicine medicine) {
        Medicine existingMedicine = findMedicineByName(medicine.getName())
                .orElseThrow(() -> new IllegalArgumentException("Medicine with name '" + medicine.getName() + "' not found."));
        existingMedicine.setInitialStock(medicine.getInitialStock());
        existingMedicine.setLowStockLevelAlert(medicine.getLowStockLevelAlert());
        saveData();
    }

    /**
     * Saves all medicines to the file.
     */
    private void saveData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            // Write the header
            bw.write("Name,InitialStock,LowStockLevelAlert\n");
            // Write each medicine's details
            for (Medicine medicine : medicines) {
                bw.write(medicine.getName() + "," + medicine.getInitialStock() + "," + medicine.getLowStockLevelAlert() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving medicine data: " + e.getMessage());
        }
    }
}
