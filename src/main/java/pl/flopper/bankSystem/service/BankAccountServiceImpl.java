package pl.flopper.bankSystem.service;


import org.springframework.stereotype.Service;
import pl.flopper.bankSystem.exception.InsufficientFundsException;
import pl.flopper.bankSystem.exception.ResourceNotFoundException;
import pl.flopper.bankSystem.model.BankAccount;
import pl.flopper.bankSystem.model.Transaction;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    private final Map<Long, BankAccount> accountRepository = new HashMap<>();

    /**
     * Creates a new bank account by sending a POST request to the external API.
     *
     * @param account the BankAccount object to be created
     * @return the created BankAccount object as returned by the external API
     * @throws RuntimeException if the account creation fails or an error occurs
     */
    @Override
    public BankAccount createAccount(BankAccount account) {
        return accountRepository.put(account.getOwnerId(), account);
    }

    /**
     * Retrieves the bank account associated with the given PESEL.
     *

     * @return the {@code BankAccount} object if the account is found
     * @throws ResourceNotFoundException if no account is found for the specified PESEL
     * @throws RuntimeException if an unexpected error occurs during retrieval
     */
    @Override
    public BankAccount getAccount(Long idUser) {
        BankAccount account = accountRepository.get(idUser);
        if (account == null) {
            throw new ResourceNotFoundException("Konto nie istnieje dla użytkownika ID: " + idUser);
        }
        return account;
    }

    @Override
    public BankAccount findAccountByPesel(String pesel) {
        BankAccount account = accountRepository.values().stream()
                .filter(bankAccount -> bankAccount.getPesel().equals(pesel))
                .findFirst()
                .orElse(null);
        if (account == null) {
            throw new ResourceNotFoundException("Konto nie istnieje dla użytkownika o PESEL: " + pesel);
        }
        return account;
    }

    /**
     * Initiates a deposit transaction for the specified account by PESEL with the given amount.
     *
     * @param userId  the unique identifier of the account (PESEL) where the deposit should be made
     * @param amount the monetary amount to be deposited into the account
     * @throws RuntimeException if the deposit transaction fails or encounters an error
     */
    @Override
    public void deposit(Long userId, BigDecimal amount) {
        BankAccount account = getAccount(userId);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Kwota musi być większa niż 0");
        }

        account.setBalance(account.getBalance().add(amount));
        account.getTransactionHistory().add(new Transaction("DEPOSIT", amount, "Wpłata środków"));
    }

    /**
     * Withdraws a specified amount of money from the bank account associated with the given PESEL.
     * If the withdrawal request fails due to insufficient funds or invalid input, an exception will be thrown.
     *
     * @param userId the PESEL (unique identifier) of the account holder
     * @param amount the monetary amount to withdraw
     * @throws InsufficientFundsException if there are insufficient funds or the amount is invalid
     * @throws RuntimeException if the withdrawal request fails due to other reasons
     */
    @Override
    public void withdraw(Long userId, BigDecimal amount) {
        BankAccount account = getAccount(userId);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Kwota musi być większa niż 0");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Brak wystarczających środków");
        }

        account.setBalance(account.getBalance().subtract(amount));
        account.getTransactionHistory().add(new Transaction("WITHDRAW", amount, "Wypłata środków"));
    }

    /**
     * Transfers a specified amount from one bank account to another.
     *
     * @param fromUserId the PESEL number of the source account from which the amount is to be transferred
     * @param targetPesel the PESEL number of the target account to which the amount is to be transferred
     * @param amount the amount of money to be transferred
     * @throws InsufficientFundsException if the source account has insufficient funds or the transaction details are invalid
     * @throws RuntimeException if the transfer operation fails for any other reason
     */
    @Override
    public void transfer(Long fromUserId, String targetPesel, BigDecimal amount) {
        BankAccount bankAccount = accountRepository.get(fromUserId);
        BankAccount bankAccount1 = findAccountByPesel(targetPesel);
        if (bankAccount.getPesel().equals(targetPesel)) {
            throw new IllegalArgumentException("Nie można przelać środków na to samo konto");
        }

        withdraw(fromUserId, amount);
        deposit(bankAccount1.getOwnerId(), amount);

        BankAccount fromAccount = getAccount(fromUserId);
        BankAccount toAccount = getAccount(bankAccount1.getOwnerId());

        fromAccount.getTransactionHistory().add(new Transaction("TRANSFER_OUT", amount, "Przelew do: " + bankAccount1.getPesel()));
        toAccount.getTransactionHistory().add(new Transaction("TRANSFER_IN", amount, "Przelew od: " + fromUserId));
    }

    /**
     * Retrieves the transaction history for a specific account identified by the provided PESEL number.
     *
     * @param userId the PESEL number of the account whose transaction history is to be retrieved
     * @return a list of transactions associated with the specified account
     * @throws ResourceNotFoundException if the account associated with the given PESEL number is not found
     * @throws RuntimeException if there is a failure in retrieving the transaction history
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Transaction> getTransactionHistory(Long userId) {
        BankAccount account = getAccount(userId);
        return account.getTransactionHistory();
    }


}