package pl.flopper.bankSystem.service;

import pl.flopper.bankSystem.model.BankAccount;
import pl.flopper.bankSystem.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface BankAccountService {

    BankAccount createAccount(BankAccount account);
    BankAccount getAccount(Long idUser);
    BankAccount findAccountByPesel(String pesel);
    void deposit(Long userId, BigDecimal amount);
    void withdraw(Long userId, BigDecimal amount);
    void transfer(Long fromUserId, String targetPesel, BigDecimal amount);
    List<Transaction> getTransactionHistory(Long userId);
}
