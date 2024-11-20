package datamgmt.retrievers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for managing file-based data operations.
 * Handles common file operations like reading, writing, and appending.
 *
 * @param <T> the type of data handled by this class
 */
public abstract class BaseDataHandler<T> {
    protected final List<T> dataList = new ArrayList<>();

    /**
     * Loads data from the file into memory.
     *
     * @param filePath the file to read from
     * @throws IOException if an error occurs during file reading
     */
    public void loadData(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine(); // Skip header
            String line;
            while ((line = reader.readLine()) != null) {
                T item = parseLine(line);
                if (item != null) {
                    if (!dataList.contains(item)) {
                        dataList.add(item);
                    }
                }
            }
        }
    }

    /**
     * Writes all data in memory to the file, overwriting its contents.
     *
     * @param filePath the file to write to
     * @throws IOException if an error occurs during file writing
     */
    public void writeData(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(getHeader() + "\n");
            for (T item : dataList) {
                writer.write(formatItem(item) + "\n");
            }
        }
    }

    /**
     * Appends a single data item to the file.
     *
     * @param filePath the file to append to
     * @param item     the data item to append
     * @throws IOException if an error occurs during file writing
     */
    public void appendData(String filePath, T item) throws IOException {
        boolean fileExists = new File(filePath).exists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            if (!fileExists) {
                writer.write(getHeader() + "\n");
            }
            writer.write(formatItem(item) + "\n");
        }
    }

    /**
     * Clears all in-memory data.
     */
    public void clearData() {
        dataList.clear();
    }

    /**
     * Returns all in-memory data as a list.
     *
     * @return the list of data
     */
    public List<T> getAllData() {
        return new ArrayList<>(dataList);
    }

    /**
     * Abstract method to parse a line from the file into a data object.
     *
     * @param line the line of data to parse
     * @return the parsed data object, or null if parsing fails
     */
    protected abstract T parseLine(String line);

    /**
     * Abstract method to format a data object into a string for writing to a file.
     *
     * @param item the data object to format
     * @return the formatted string representation of the data
     */
    protected abstract String formatItem(T item);

    /**
     * Abstract method to provide the header for the data file.
     *
     * @return the header string
     */
    protected abstract String getHeader();
}
