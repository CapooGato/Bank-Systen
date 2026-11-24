package pl.flopper.bankSystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.flopper.bankSystem.model.User;
import pl.flopper.bankSystem.model.dto.AuthResponse;
import pl.flopper.bankSystem.model.dto.TotpSetupResponse;
import pl.flopper.bankSystem.model.dto.TotpVerificationRequest;
import pl.flopper.bankSystem.service.UserService;
import pl.flopper.bankSystem.util.TotpUtil;

@RestController
@RequestMapping("/api/totp")
public class TotpController {

    private static final String ISSUER = "Banking System with TOTP";
    private final UserService userService;

    @Autowired
    public TotpController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint do rozpoczęcia konfiguracji TOTP.
     *
     * @param authentication Obiekt uwierzytelnienia z kontekstu bezpieczeństwa
     * @return Odpowiedź zawierająca kod QR i sekret
     */
    @PostMapping("/setup")
    public ResponseEntity<?> setupTotp(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new TotpSetupResponse(false, "User not found.", null, null));
        }

        if (user.isMfaEnabled()) {
            return ResponseEntity.badRequest()
                    .body(new TotpSetupResponse(false, "Two-factor authentication is now active.", null, null));
        }

        // Generuj sekret dla użytkownika
        String secret = userService.generateMfaSecret(user.getId());

        // Generuj URI TOTP
        String uri = TotpUtil.generateTotpUri(ISSUER, username, secret);

        // Generuj kod QR (teraz zwraca obrazek PNG w formacie Base64)
        String qrCode = TotpUtil.generateQrCode(uri);

        return ResponseEntity.ok(new TotpSetupResponse(
                true,
                "Scan the QR code in your authenticator app.",
                qrCode,
                secret
        ));
    }

    /**
     * Endpoint do weryfikacji i aktywacji TOTP.
     *
     * @param request Żądanie weryfikacji zawierające kod TOTP
     * @param authentication Obiekt uwierzytelnienia z kontekstu bezpieczeństwa
     * @return Odpowiedź z informacją o wyniku weryfikacji
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyTotp(
            @RequestBody TotpVerificationRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "User not found.", null));
        }

        boolean verified = userService.verifyAndEnableMfa(user.getId(), request.getTotpCode());

        if (verified) {
            return ResponseEntity.ok(new AuthResponse(
                    true,
                    "2FA verification completed successfully. Two-factor authentication has been activated.",
                    username
            ));
        } else {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(
                            false,
                            "Invalid verification code.",
                            username
                    ));
        }
    }

    /**
     * Endpoint do sprawdzenia stanu TOTP dla użytkownika.
     *
     * @param authentication Obiekt uwierzytelnienia z kontekstu bezpieczeństwa
     * @return Odpowiedź z informacją o stanie TOTP
     */
    @GetMapping("/status")
    public ResponseEntity<?> getTotpStatus(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "User not found.", null));
        }

        return ResponseEntity.ok(new AuthResponse(
                true,
                user.isMfaEnabled() ? "Two-factor authentication is active" : "Two-factor authentication is not active",
                username
        ));
    }

    /**
     * Endpoint do dezaktywacji TOTP.
     *
     * @param authentication Obiekt uwierzytelnienia z kontekstu bezpieczeństwa
     * @return Odpowiedź z informacją o wyniku dezaktywacji
     */
    @PostMapping("/disable")
    public ResponseEntity<?> disableTotp(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "User not found.", null));
        }

        if (!user.isMfaEnabled()) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(false, "Two-factor authentication is not active.", username));
        }

        boolean disabled = userService.disableMfa(user.getId());

        if (disabled) {
            return ResponseEntity.ok(new AuthResponse(
                    true,
                    "Two-factor authentication has been deactivated.",
                    username
            ));
        } else {
            return ResponseEntity.internalServerError()
                    .body(new AuthResponse(
                            false,
                            "Error deactivating two-factor authentication.",
                            username
                    ));
        }
    }
}