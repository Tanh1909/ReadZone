package com.example.app.data.genetor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionIdGenerator {
    private static final String DELIMITER = "_";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * Generates a unique transaction ID based on order ID
     * Format: orderId_timestamp_randomDigits
     *
     * @param orderId Order ID
     * @return Unique transaction ID string
     */
    public static String generateTransactionId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }

        // Generate current timestamp
        String timestamp = DATE_FORMAT.format(new Date());

        // Generate a random number (5 digits)
        Random random = new Random();
        int randomNum = random.nextInt(90000) + 10000; // 5-digit number between 10000 and 99999

        // Construct transaction ID
        return orderId + DELIMITER + timestamp + DELIMITER + randomNum;
    }

    /**
     * Alternative method using UUID for more complex transaction IDs
     * Format: orderId_timestamp_UUID
     *
     * @param orderId Order ID
     * @return Unique transaction ID string
     */
    public static String generateTransactionIdWithUUID(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }

        // Generate current timestamp
        String timestamp = DATE_FORMAT.format(new Date());

        // Generate a UUID and use first 8 characters
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        // Construct transaction ID
        return orderId + DELIMITER + timestamp + DELIMITER + uuid;
    }

    /**
     * Extracts information from a transaction ID
     *
     * @param transactionId The transaction ID to extract information from
     * @return Map containing extracted information (orderId, timestamp, randomPart)
     * @throws IllegalArgumentException if the transaction ID format is invalid
     */
    public static Map<String, String> extractTransactionInfo(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or empty");
        }

        String[] parts = transactionId.split(DELIMITER);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid transaction ID format. Expected format: orderId_timestamp_randomPart");
        }

        Map<String, String> result = new HashMap<>();
        result.put("orderId", parts[0]);
        result.put("timestamp", parts[1]);
        result.put("randomPart", parts[2]);

        // Convert timestamp to readable date if possible
        try {
            Date transactionDate = DATE_FORMAT.parse(parts[1]);
            SimpleDateFormat readableDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            result.put("formattedDate", readableDateFormat.format(transactionDate));
        } catch (ParseException e) {
            result.put("formattedDate", "Invalid date format");
        }

        return result;
    }

    /**
     * Extracts just the order ID from a transaction ID
     *
     * @param transactionId The transaction ID
     * @return The extracted order ID
     * @throws IllegalArgumentException if the transaction ID format is invalid
     */
    public static String extractOrderId(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or empty");
        }

        String[] parts = transactionId.split(DELIMITER);
        if (parts.length < 1) {
            throw new IllegalArgumentException("Invalid transaction ID format");
        }

        return parts[0];
    }

    /**
     * Checks if a transaction ID was generated on the same day as the current date
     *
     * @param transactionId The transaction ID to check
     * @return true if the transaction was created today, false otherwise
     */
    public static boolean isTransactionFromToday(String transactionId) {
        try {
            Map<String, String> info = extractTransactionInfo(transactionId);
            Date transactionDate = DATE_FORMAT.parse(info.get("timestamp"));

            // Get today's date without time
            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
            String today = dayFormat.format(new Date());
            String transactionDay = dayFormat.format(transactionDate);

            return today.equals(transactionDay);
        } catch (Exception e) {
            return false;
        }
    }

}
