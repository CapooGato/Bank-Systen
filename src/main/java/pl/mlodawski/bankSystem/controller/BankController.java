package pl.mlodawski.bankSystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.mlodawski.bankSystem.model.Transaction;
import pl.mlodawski.bankSystem.model.User;
import pl.mlodawski.bankSystem.service.BankAccountServiceImpl;
import pl.mlodawski.bankSystem.service.UserService;


import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
public class BankController {

    private final BankAccountServiceImpl bankAccountService;
    private final UserService userService;

    @Autowired
    public BankController(BankAccountServiceImpl bankAccountService, UserService userService) {
        this.bankAccountService = bankAccountService;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount, RedirectAttributes redirectAttributes, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName());

        try {
            bankAccountService.deposit(user.getId(), amount);
            redirectAttributes.addFlashAttribute("successMessage", "Deposit successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/management/userAccount/" + user.getUsername();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount, RedirectAttributes redirectAttributes, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName());

        try {
            bankAccountService.withdraw(user.getId(), amount);
            redirectAttributes.addFlashAttribute("successMessage", "Withdrawal successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/management/userAccount/" + user.getUsername();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/transfer")
    public String showTransferForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName());

        model.addAttribute("sourceAccount", bankAccountService.getAccount(user.getId()));
        model.addAttribute("user", user);
        return "transfer";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/transfer")
    public String transfer(@RequestParam String targetPesel,
                           @RequestParam BigDecimal amount,
                           RedirectAttributes redirectAttributes,
                           Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName());

        try {
            bankAccountService.transfer(user.getId(), targetPesel, amount);
            redirectAttributes.addFlashAttribute("successMessage", "Transfer successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/management/userAccount/" + user.getUsername();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/transactions")
    public String showTransactions(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName());

        List<Transaction> transactions = bankAccountService.getTransactionHistory(user.getId());
        model.addAttribute("transactions", transactions);
        model.addAttribute("user", user);
        return "transactions";
    }
}
