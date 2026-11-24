package pl.flopper.bankSystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.flopper.bankSystem.model.BankAccount;
import pl.flopper.bankSystem.model.User;
import pl.flopper.bankSystem.service.BankAccountService;
import pl.flopper.bankSystem.service.UserService;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/management")
public class UserController {

    private final UserService userService;
    private final BankAccountService bankAccountService;

    @Autowired
    public UserController(UserService userService, BankAccountService bankAccountService) {
        this.userService = userService;
        this.bankAccountService = bankAccountService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @GetMapping("/getAll")
    public String getAllUsers(Model model, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());

        List<User> users = userService.findAllUsers().stream()
                .filter(user -> !Arrays.asList(user.getRoles()).contains("ROLE_ADMIN"))
                .filter(user -> !user.getUsername().equals(principal.getName()))
                .toList();
        model.addAttribute("users", users);
        model.addAttribute("user", currentUser);
        model.addAttribute("availableRoles", List.of("ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_USER"));
        return "userManagement";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/management/getAll";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/toggleRole")
    public String toggleUserRole(@RequestParam Long userId, @RequestParam String role, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleRole(userId, role);
            redirectAttributes.addFlashAttribute("successMessage", "Zmieniono role użytkownika.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Błąd: " + e.getMessage());
        }

        return "redirect:/management/getAll";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public String accountInfo(Model model, Principal principal) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
        }
        return "accountInfo";
    }

    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    @GetMapping("/userAccount/{username}")
    public String userAccount(@PathVariable String username, Model model) {
        User user = userService.findByUsername(username);
        BankAccount account = bankAccountService.getAccount(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        return "account";
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @GetMapping("/userManagement")
    public String moderatorPanel(Model model, Principal principal) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
        }
        return "userManagement";
    }

}
