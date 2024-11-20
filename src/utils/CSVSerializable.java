package utils;

public interface CSVSerializable {
    /**
     * Converts the object to a CSV-compatible string.
     * 
     * @return the CSV string representation of the object
     */
    String toCSV();

    /**
     * Parses a CSV string to create an object instance.
     * 
     * @param csvLine the CSV string
     * @return the parsed object instance
     * @throws IllegalArgumentException if the CSV string is invalid
     */
    static <T> T fromCSV(String csvLine) {
        throw new UnsupportedOperationException("fromCSV must be implemented in the implementing class.");
    }
}
