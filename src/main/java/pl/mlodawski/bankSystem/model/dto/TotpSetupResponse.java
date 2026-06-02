package pl.mlodawski.bankSystem.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents the response returned by the TOTP setup endpoint.
 * Contains information about the success of the operation, a user-facing message,
 * a QR code image (Base64-encoded), and the MFA secret (for manual entry).
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TotpSetupResponse {
    private boolean success;
    private String message;
    private String qrCodeImage;
    private String mfaSecret;
}
