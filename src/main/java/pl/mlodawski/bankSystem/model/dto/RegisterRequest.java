package pl.mlodawski.bankSystem.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the data required to register a new user.
 * Used when submitting registration forms.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String pesel;
    private String password;
    private String confirmPassword;
}
