package pl.flopper.bankSystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a financial transaction associated with a bank account.
 * Stores details about the type, amount, timestamp, and description of the transaction.
 *
 * This class can be used to log deposits, withdrawals, or transfers while maintaining
 * additional metadata associated with each transaction.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    /**
     * Specifies the type of the financial transaction.
     *
     * This field is used to differentiate between various transaction types such as
     * deposits, withdrawals, and transfers.
     */
    private String type;
    /**
     * Represents the monetary value involved in a financial transaction.
     * This amount can either be positive (e.g., deposits) or negative (e.g., withdrawals).
     */
    private BigDecimal amount;
    /**
     * The timestamp indicating when the transaction occurred.
     * This value is typically assigned at the time the transaction is created.
     */
    private LocalDateTime timestamp;
    /**
     * A brief summary or remark describing the financial transaction.
     * This field can capture additional details about the purpose or context
     * of the transaction, such as "Online Purchase" or "Bill Payment".
     */
    private String description;

    /**
     * Constructs a new Transaction instance with the specified type, amount, and description.
     * The timestamp of the transaction is automatically set to the current date and time.
     *
     * @param type the type of the transaction, such as "deposit", "withdrawal", or "transfer"
     * @param amount the monetary amount involved in the transaction
     * @param description a brief description or note about the transaction
     */
    public Transaction(String type, BigDecimal amount, String description) {
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }
}