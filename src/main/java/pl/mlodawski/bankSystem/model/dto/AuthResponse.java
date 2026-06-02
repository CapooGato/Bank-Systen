package pl.mlodawski.bankSystem.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response returned after an authentication attempt.
 * It contains the result of the login process and related metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String username;
}
