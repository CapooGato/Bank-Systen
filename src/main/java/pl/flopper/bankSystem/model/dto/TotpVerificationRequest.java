package pl.flopper.bankSystem.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a request payload for verifying a TOTP code.
 */
@Getter
@NoArgsConstructor
public class TotpVerificationRequest {
    private String username;
    private String totpCode;
}
