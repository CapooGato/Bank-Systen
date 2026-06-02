package pl.mlodawski.bankSystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.mlodawski.bankSystem.model.User;
import pl.mlodawski.bankSystem.model.dto.AuthRequest;
import pl.mlodawski.bankSystem.model.dto.AuthResponse;
import pl.mlodawski.bankSystem.model.dto.RegisterRequest;
import pl.mlodawski.bankSystem.service.UserService;


@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserService userService;

    @Autowired
    public AuthRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody AuthRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        boolean result = userService.loginWith2FA(request, httpRequest, httpResponse);

        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Endpoint do rejestracji nowego użytkownika poprzez REST API.
     *
     * @param request Żądanie rejestracji zawierające dane użytkownika
     * @return ResponseEntity z informacją o statusie rejestracji
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User savedUser = userService.registerNewUser(request);
            return ResponseEntity.ok(new AuthResponse(true, "Registration completed successfully.", savedUser.getUsername()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Wylogowanie użytkownika
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Wyczyszczenie ciasteczka JSESSIONID
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(new AuthResponse(true, "You have logged out successfully.", null));
    }
}
