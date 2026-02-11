package com.bank.domain.service;

import com.bank.domain.exception.ValidationException;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Serviciu pentru validarea datelor în sistemul bancar
 */
public class ValidationService {

    // Patterns for validation
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("\\d{16}");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{6,}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern IDENTITY_NUMBER_PATTERN = Pattern.compile("^[0-9]{13}$");

    // Validation constants
    public static final int MIN_ACCOUNT_NUMBER_LENGTH = 16;
    public static final int MAX_ACCOUNT_NUMBER_LENGTH = 16;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final BigDecimal MIN_DEPOSIT_AMOUNT = BigDecimal.valueOf(1.0);
    public static final BigDecimal MIN_WITHDRAWAL_AMOUNT = BigDecimal.valueOf(0.01);
    public static final BigDecimal MIN_BALANCE = BigDecimal.valueOf(10.0);
    public static final BigDecimal MIN_WITHDRAWAL_LIMIT = BigDecimal.valueOf(100.0);

    /**
     * Validează un număr de cont
     */
    public void validateAccountNumber(String accountNumber) throws ValidationException {
        ValidationException exception = new ValidationException("Validare număr cont");

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            exception.addError("accountNumber", "Numărul contului este obligatoriu", accountNumber);
        } else if (accountNumber.length() != MIN_ACCOUNT_NUMBER_LENGTH) {
            exception.addError("accountNumber",
                    String.format("Numărul contului trebuie să aibă %d cifre", MIN_ACCOUNT_NUMBER_LENGTH),
                    accountNumber);
        } else if (!ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches()) {
            exception.addError("accountNumber", "Numărul contului trebuie să conțină doar cifre", accountNumber);
        }

        if (exception.hasErrors()) {
            throw exception;
        }
    }

    /**
     * Validează o parolă
     */
    public void validatePassword(String password) throws ValidationException {
        ValidationException exception = new ValidationException("Validare parolă");

        if (password == null || password.trim().isEmpty()) {
            exception.addError("password", "Parola este obligatorie", password);
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            exception.addError("password",
                    String.format("Parola trebuie să aibă minim %d caractere", MIN_PASSWORD_LENGTH),
                    password);
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            exception.addError("password",
                    "Parola trebuie să conțină atât litere cât și cifre",
                    password);
        }

        if (exception.hasErrors()) {
            throw exception;
        }
    }

    /**
     * Validează un email
     */
    public void validateEmail(String email) throws ValidationException {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw ValidationException.withError("email", "Adresa de email este invalidă", email);
        }
    }

    /**
     * Validează un număr de telefon
     */
    public void validatePhoneNumber(String phoneNumber) throws ValidationException {
        if (phoneNumber == null || !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw ValidationException.withError("phoneNumber", "Număr de telefon invalid", phoneNumber);
        }
    }

    /**
     * Validează un număr de identitate (IDNP/CNP)
     */
    public void validateIdentityNumber(String identityNumber) throws ValidationException {
        if (identityNumber == null || !IDENTITY_NUMBER_PATTERN.matcher(identityNumber).matches()) {
            throw ValidationException.withError("identityNumber", "Număr de identitate invalid", identityNumber);
        }
    }

    /**
     * Validează data nașterii (trebuie să aibă minim 18 ani)
     */
    public void validateBirthDate(LocalDate birthDate) throws ValidationException {
        if (birthDate == null) {
            throw ValidationException.withError("birthDate", "Data nașterii este obligatorie", null);
        }

        LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
        if (birthDate.isAfter(eighteenYearsAgo)) {
            throw ValidationException.withError("birthDate",
                    "Clientul trebuie să aibă minim 18 ani",
                    birthDate);
        }
    }

    /**
     * Validează o sumă pentru depunere
     */
    public void validateDepositAmount(BigDecimal amount, Currency currency) throws ValidationException {
        if (amount == null || amount.compareTo(MIN_DEPOSIT_AMOUNT) < 0) {
            throw ValidationException.withError("amount",
                    String.format("Suma minimă pentru depunere este %s %s",
                            MIN_DEPOSIT_AMOUNT, currency),
                    amount);
        }
    }

    /**
     * Validează o sumă pentru retragere
     */
    public void validateWithdrawalAmount(BigDecimal amount, Currency currency) throws ValidationException {
        if (amount == null || amount.compareTo(MIN_WITHDRAWAL_AMOUNT) < 0) {
            throw ValidationException.withError("amount",
                    String.format("Suma minimă pentru retragere este %s %s",
                            MIN_WITHDRAWAL_AMOUNT, currency),
                    amount);
        }
    }

    /**
     * Validează un sold
     */
    public void validateBalance(BigDecimal balance) throws ValidationException {
        if (balance == null || balance.compareTo(MIN_BALANCE) < 0) {
            throw ValidationException.withError("balance",
                    String.format("Soldul minim este %s MDL", MIN_BALANCE),
                    balance);
        }
    }

    /**
     * Validează o limită de retragere zilnică
     */
    public void validateWithdrawalLimit(BigDecimal limit) throws ValidationException {
        if (limit == null || limit.compareTo(MIN_WITHDRAWAL_LIMIT) < 0) {
            throw ValidationException.withError("withdrawalLimit",
                    String.format("Limita zilnică trebuie să fie minim %s MDL", MIN_WITHDRAWAL_LIMIT),
                    limit);
        }
    }

    /**
     * Validează un client
     */
    public void validateCustomer(Customer customer) throws ValidationException {
        ValidationException exception = new ValidationException("Validare client");

        try {
            if (customer.getFirstName() == null || customer.getFirstName().trim().length() < 2) {
                exception.addError("firstName", "Prenumele trebuie să aibă minim 2 caractere", customer.getFirstName());
            }

            if (customer.getLastName() == null || customer.getLastName().trim().length() < 2) {
                exception.addError("lastName", "Numele trebuie să aibă minim 2 caractere", customer.getLastName());
            }

            validateEmail(customer.getEmail());
        } catch (ValidationException e) {
            exception.addErrors(e.getErrors());
        }

        if (exception.hasErrors()) {
            throw exception;
        }
    }

    /**
     * Validează o monedă
     */
    public void validateCurrency(String currencyCode) throws ValidationException {
        try {
            Currency currency = Currency.fromCode(currencyCode);
            // Dacă ajunge aici, moneda este validă
        } catch (IllegalArgumentException e) {
            throw ValidationException.withError("currency",
                    String.format("Monedă invalidă: %s. Monede valide: %s",
                            currencyCode, getValidCurrencies()),
                    currencyCode);
        }
    }

    /**
     * Returnează lista monedelor valide
     */
    private String getValidCurrencies() {
        StringBuilder sb = new StringBuilder();
        for (Currency currency : Currency.values()) {
            sb.append(currency.getCode()).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    /**
     * Validează un cont (verifică doar datele de bază)
     */
    public void validateAccount(Account account) throws ValidationException {
        ValidationException exception = new ValidationException("Validare cont");

        try {
            validateAccountNumber(account.getAccountNumber());
        } catch (ValidationException e) {
            exception.addErrors(e.getErrors());
        }

        if (account.getOwner() == null) {
            exception.addError("owner", "Proprietarul contului este obligatoriu", null);
        }

        if (exception.hasErrors()) {
            throw exception;
        }
    }
}
