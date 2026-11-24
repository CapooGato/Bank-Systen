package pl.flopper.bankSystem.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAccount {

    /**
     * Represents the IBAN account number associated with a bank account.
     * This field is validated to ensure it is not blank and follows
     * the specific IBAN format for Polish accounts, starting with "PL"
     * followed by 26 numeric digits.
     *
     * Validation Constraints:
     * - Must not be blank. A blank account number will throw a validation error with the message
     *   "Account number cannot be blank".
     * - Must match the regex pattern "PL[0-9]{26}". If the format is invalid,
     *   a validation error will be triggered with the message "Account number must
     *   be in IBAN format".
     */
    @NotBlank(message = "Account number cannot be blank")
    @Pattern(regexp = "PL[0-9]{26}", message = "Account number must be in IBAN format")
    private String accountNumber;

    /**
     * Represents the current monetary amount available in the bank account.
     *
     * This field stores the account balance as a non-null {@code BigDecimal} value.
     * The initial balance is set to {@code BigDecimal.ZERO}, indicating no funds
     * in the account at the time of creation.
     *
     * Validation:
     * - Must not be null. If null, a validation error with the message "Balance cannot be null" will be triggered.
     */
    @NotNull(message = "Balance cannot be null")
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Maintains a list of financial transactions associated with the bank account.
     * Each transaction represents a record of deposits, withdrawals, or other account activities,
     * encapsulated as instances of the {@link Transaction} class.
     *
     * This list provides a historical log of all account-related operations, including
     * details about the transaction type, amount, timestamp, and description.
     */
    private List<Transaction> transactionHistory = new ArrayList<>();

    /**
     * Represents the PESEL (Polish National Identification Number) associated with a bank account.
     *
     * A PESEL number consists of exactly 11 digits and is used for uniquely identifying individuals in Poland.
     * This field must adhere to the following constraints:
     * - It cannot be blank.
     * - It must match the format of 11 numeric digits.
     *
     * Validation is enforced through annotations:
     * - {@code @NotBlank} ensures the field is not null or empty, and a descriptive error message is provided if the validation fails.
     * - {@code @Pattern} validates that the input strictly adheres to the 11-digit numeric format.
     */
    @NotBlank(message = "PESEL cannot be blank")
    @Pattern(regexp = "[0-9]{11}", message = "PESEL must be 11 digits")
    private String pesel;

    private Long ownerId;
}