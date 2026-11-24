package pl.flopper.bankSystem.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Unique identifier of the user.
     */
    private Long id;

    /**
     * Represents the first name of the account holder.
     * This field must not be blank and is validated to ensure it contains a meaningful value.
     */
    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    /**
     * Represents the last name of the account holder.
     * This field must not be blank and is used to store the surname of the user
     * associated with the bank account.
     */
    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    /**
     * Represents the email address associated with a bank account.
     * The email must not be blank and is validated to ensure it follows
     * a valid email format.
     */
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Represents the user's password.
     * This field must not be blank.
     */
    @NotBlank(message = "Password cannot be blank")
    private String password;

    /**
     * Represents the unique username for login purposes.
     * This field must not be blank.
     */
    @NotBlank(message = "Username cannot be blank")
    private String username;

    /**
     * Represents the roles assigned to the user.
     * Roles determine access levels and permissions within the application.
     */
    private String[] roles;

    /**
     * Indicates whether multi-factor authentication (2FA) is enabled for the user.
     * If true, the system will require a valid TOTP code during login.
     */
    private boolean mfaEnabled = false;

    /**
     * The Base32-encoded secret key used to generate TOTP codes for this user.
     * Stored securely and used for TOTP verification.
     */
    private String mfaSecret;

    /**
     * Indicates whether the TOTP secret has been successfully verified by the user.
     */
    private boolean mfaVerified = false;
}
