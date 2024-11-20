package utils.enums;

/**
 * Represents the gender of a user in the system.
 */
public enum Gender {
    MALE,
    FEMALE,
    NON_BINARY,
    OTHER;

    /**
     * Parses a string to its corresponding Gender enum value.
     *
     * @param genderStr the string representation of the gender
     * @return the corresponding Gender enum value
     * @throws IllegalArgumentException if the input does not match any enum value
     */
    public static Gender fromString(String genderStr) {
        if (genderStr == null || genderStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be null or empty.");
        }

        switch (genderStr.trim().toUpperCase()) {
            case "MALE":
                return MALE;
            case "FEMALE":
                return FEMALE;
            case "NON_BINARY":
                return NON_BINARY;
            case "OTHER":
                return OTHER;
            default:
                throw new IllegalArgumentException("Invalid gender value: " + genderStr);
        }
    }
}
