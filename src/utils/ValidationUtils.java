package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import utils.appointments.appointmentslots.AppointmentSlot;
import utils.enums.WorkingDay;

/**
 * Utility class for validating strings, dates, times, and general objects.
 */
public class ValidationUtils {
    /**
     * Validates if the given date and time are within the working slots of a doctor.
     *
     * @param doctorSlots the list of {@link AppointmentSlot} for the doctor
     * @param date        the appointment date to validate
     * @param time        the appointment time to validate
     * @return {@code true} if the date and time are valid for the doctor's slots, otherwise {@code false}
     */
    public static boolean isValidAppointmentTime(List<AppointmentSlot> doctorSlots, LocalDate date, LocalTime time) {
        try {
            // Combine date and time into a LocalDateTime
            LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);
    
            // Check if the date and time are in the future
            if (!appointmentDateTime.isAfter(LocalDateTime.now())) {
                return false;
            }
    
            // Get the working day corresponding to the appointment date
            WorkingDay appointmentDay = WorkingDay.valueOf(date.getDayOfWeek().name());
    
            // Validate that the time falls within one of the doctor's slots for that working day
            return doctorSlots.stream().anyMatch(slot ->
                    slot.getWorkingDays().contains(appointmentDay) &&
                    !time.isBefore(slot.getStartTime()) &&
                    !time.isAfter(slot.getEndTime())
            );
        } catch (Exception e) {
            System.err.println("Error validating appointment time: " + e.getMessage());
            return false;
        }
    }
    
    
    
    /** 
     * Validates if a string is null or empty.
     *
     * @param value the string to validate
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Validates a non-null string and ensures it is not empty.
     *
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @return the trimmed string if valid
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static String validateString(String value, String fieldName) {
        if (isNullOrEmpty(value)) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty.");
        }
        return value.trim();
    }
    /**
     * Validates and parses a string into an enum value.
     *
     * @param <E>       The enum type.
     * @param value     The string value to validate.
     * @param enumClass The class of the enum.
     * @param fieldName The name of the field being validated (for error messages).
     * @return The corresponding enum value.
     * @throws IllegalArgumentException if the value is null, empty, or cannot be matched to an enum constant.
     */
    public static <E extends Enum<E>> E validateEnum(String value, Class<E> enumClass, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty.");
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid value for " + fieldName + ": '" + value + "'. Expected one of: " + getEnumValues(enumClass));
        }
    }

    /**
     * Returns a comma-separated string of all valid enum values.
     *
     * @param <E>       The enum type.
     * @param enumClass The class of the enum.
     * @return A string listing all enum values.
     */
    private static <E extends Enum<E>> String getEnumValues(Class<E> enumClass) {
        E[] constants = enumClass.getEnumConstants();
        List<String> names = new ArrayList<>();
        for (E constant : constants) {
            names.add(constant.name());
        }
        return String.join(", ", names);
    }

    /**
     * Validates a date string against the provided format.
     *
     * @param date the date string to validate
     * @param format the expected date format
     * @param fieldName the name of the field for error messages
     * @return the validated date string
     * @throws IllegalArgumentException if the date is invalid
     */
    public static String validateDate(String date, String format, String fieldName) {
        if (isNullOrEmpty(date)) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty.");
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDate.parse(date, formatter);
            return date;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ". Expected format: " + format);
        }
    }

    /**
     * Validates a time string against the provided format.
     *
     * @param time the time string to validate
     * @param format the expected time format
     * @param fieldName the name of the field for error messages
     * @return the validated time string
     * @throws IllegalArgumentException if the time is invalid
     */
    public static String validateTime(String time, String format, String fieldName) {
        if (isNullOrEmpty(time)) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty.");
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalTime.parse(time, formatter);
            return time;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ". Expected format: " + format);
        }
    }
    /**
     * Validates the age of a user.
     *
     * @param age the age to validate
     * @throws IllegalArgumentException if the age is not between 18 and 100
     */
    public static void validateAge(int age) {
        if (age <= 0 || age > 120) {
            throw new IllegalArgumentException("Age must be between 18 and 120.");
        }
    }
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * Validates if the given email address is in a valid format.
     *
     * <p>
     * An email is considered valid if it:
     * </p>
     * <ul>
     *   <li>Contains only allowed characters (letters, digits, '.', '_', '-', '+').</li>
     *   <li>Has a valid domain and top-level domain (TLD).</li>
     *   <li>Is in the general format "local-part@domain".</li>
     * </ul>
     *
     * @param email the email address to validate
     * @return {@code true} if the email is valid; {@code false} otherwise
     * @throws IllegalArgumentException if the email is null or empty
     */
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    /**
     * Validates and parses a date string into a LocalDate object.
     *
     * @param dateString the date string to validate
     * @param format     the expected date format
     * @param fieldName  the name of the field being validated
     * @return the parsed LocalDate object
     * @throws IllegalArgumentException if the date is invalid
     */
    public static LocalDate validateLocalDate(String dateString, String format, String fieldName) {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty.");
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + dateString + ". Expected format: " + format);
        }
    }

    /**
     * Validates and parses a time string into a LocalTime object.
     *
     * @param timeString the time string to validate
     * @param format     the expected time format
     * @param fieldName  the name of the field being validated
     * @return the parsed LocalTime object
     * @throws IllegalArgumentException if the time is invalid
     */
    public static LocalTime validateLocalTime(String timeString, String format, String fieldName) {
        if (timeString == null || timeString.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty.");
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return LocalTime.parse(timeString, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + timeString + ". Expected format: " + format);
        }
    }
    /**
     * Validates the strength and validity of a password.
     *
     * @param password the password to validate
     * @throws IllegalArgumentException if the password is null, too short, or lacks required criteria
     */
    public static void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUppercase || !hasLowercase || !hasDigit) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter, one lowercase letter, and one digit.");
        }
    }
    public static boolean isFutureDate(String date, String time, String dateFormat, String timeFormat) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timeFormat);

            LocalDate appointmentDate = LocalDate.parse(date, dateFormatter);
            LocalTime appointmentTime = LocalTime.parse(time, timeFormatter);

            LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, appointmentTime);
            return appointmentDateTime.isAfter(LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Error validating date and time: " + e.getMessage());
            return false;
        }
    }
    /**
     * Validates a date of birth to ensure it is a past date and not null.
     *
     * @param dateOfBirth the date of birth to validate
     * @param fieldName   the name of the field for error messages
     * @return the validated date of birth
     * @throws IllegalArgumentException if the date of birth is null or not a past date
     */
    public static LocalDate validateDateOfBirth(LocalDate dateOfBirth, String fieldName) {
        if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(fieldName + " must be a valid past date.");
        }
        return dateOfBirth;
    }

    /**
     * Validates contact information to ensure it is non-null, non-empty, and follows a valid format.
     *
     * @param contactInfo the contact information to validate
     * @return the validated contact information
     * @throws IllegalArgumentException if the contact information is null, empty, or invalid
     */
    public static String validateContactInfo(String contactInfo) {
        if (contactInfo == null || contactInfo.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact information cannot be null or empty.");
        }
    
        // Regex for basic phone number validation
        String phoneRegex = "^\\+?[0-9]{7,15}$";
        // Regex for basic email address validation
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
        if (contactInfo.matches(phoneRegex) || contactInfo.matches(emailRegex)) {
            return contactInfo; // Valid contact information
        } else {
            throw new IllegalArgumentException("Invalid contact information format. Must be a valid phone number or email.");
        }
    }
    

    /**
     * Validates a non-null and non-empty string.
     *
     * @param value     the string to validate
     * @param fieldName the name of the field for error messages
     * @return the validated string
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static String validateStringNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty.");
        }
        return value;
    }
    /**
     * Validates a general object to ensure it is not null.
     *
     * @param object the object to validate
     * @param fieldName the name of the field for error messages
     * @param <T> the type of the object
     * @return the validated object
     * @throws IllegalArgumentException if the object is null
     */
    public static <T> T validateObject(T object, String fieldName) {
        if (object == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null.");
        }
        return object;
    }
    /**
     * Validates that the given string starts with the specified prefix.
     * 
     * @param value  The string to validate.
     * @param prefix The required prefix.
     * @param fieldName The name of the field being validated, for error messages.
     * @return The original value if validation is successful.
     * @throws IllegalArgumentException if the string is null, empty, or does not start with the prefix.
     */
    public static String validateStringStartsWith(String value, String prefix, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty.");
        }
        if (!value.startsWith(prefix)) {
            throw new IllegalArgumentException(fieldName + " must start with \"" + prefix + "\". Found: " + value);
        }
        return value;
    }
}
