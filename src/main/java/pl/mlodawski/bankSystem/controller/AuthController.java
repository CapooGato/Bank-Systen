package pl.mlodawski.bankSystem.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.mlodawski.bankSystem.model.dto.RegisterRequest;
import pl.mlodawski.bankSystem.service.UserService;


@Controller
@AllArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "totpError", required = false) String totpError,
                        @RequestParam(value = "username", required = false) String username,
                        Model model) {

        if (error != null) {
            model.addAttribute("errorMsg", "Incorrect username or password.");
        }

        if (logout != null) {
            model.addAttribute("logoutMsg", "You have been successfully logged out.");
            return "index";
        }

        if (totpError != null) {
            model.addAttribute("errorMsg", "Invalid verification code.");
        }

        if (username != null) {
            model.addAttribute("username", username);
        }

        return "login";  // nazwa szablonu Thymeleaf lub JSP
    }

    /**
     * Wyświetla formularz rejestracji.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegisterRequest());
        return "register";  // nazwa szablonu Thymeleaf
    }

    /**
     * Obsługuje żądanie rejestracji nowego użytkownika.
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") RegisterRequest registerRequest,
                               BindingResult result) {
        try {
            userService.registerNewUser(registerRequest);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "register";
        }
    }

}
