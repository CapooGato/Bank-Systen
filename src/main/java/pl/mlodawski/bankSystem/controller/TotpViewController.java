package pl.mlodawski.bankSystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.mlodawski.bankSystem.model.User;
import pl.mlodawski.bankSystem.service.UserService;
import pl.mlodawski.bankSystem.util.TotpUtil;

@Controller
public class TotpViewController {

    private static final String ISSUER = "Banking System with TOTP";

    private final UserService userService;

    @Autowired
    public TotpViewController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Wyświetla stronę konfiguracji TOTP.
     */
    @GetMapping("/totp-setup")
    public String showTotpSetupPage(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());

        if (user.isMfaEnabled()) {
            return "redirect:/management/info?error=mfa-already-enabled";
        }

        // Generuj sekret dla użytkownika
        String secret = userService.generateMfaSecret(user.getId());

        // Generuj URI TOTP
        String uri = TotpUtil.generateTotpUri(ISSUER, user.getUsername(), secret);

        // Generuj kod QR (teraz zwraca obrazek PNG w formacie Base64)
        String qrCode = TotpUtil.generateQrCode(uri);

        model.addAttribute("username", user.getUsername());
        model.addAttribute("qrCodeImage", qrCode);
        model.addAttribute("mfaSecret", secret);

        return "totp-setup";
    }

    /**
     * Obsługuje weryfikację kodu TOTP i aktywację 2FA.
     */
    @PostMapping("/totp-setup")
    public String processTotpSetup(
            Authentication authentication,
            @RequestParam("totpCode") String totpCode) {

        User user = userService.findByUsername(authentication.getName());

        if (userService.verifyAndEnableMfa(user.getId(), totpCode)) {
            return "redirect:/management/info?success=mfa-enabled";
        } else {
            return "redirect:/totp-setup?error=invalid-code";
        }
    }

    /**
     * Obsługuje dezaktywację 2FA.
     */
    @PostMapping("/totp-disable")
    public String disableTotp(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());

        if (userService.disableMfa(user.getId())) {
            return "redirect:/management/info?success=mfa-disabled";
        } else {
            return "redirect:/management/info?error=mfa-disable-failed";
        }
    }
}
