package com.bank.domain.service;

import com.bank.domain.exception.BankingException;
import com.bank.domain.exception.ValidationException;
import com.bank.domain.model.Account;
import com.bank.domain.model.Customer;
import com.bank.domain.model.Currency;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Serviciu pentru validarea datelor in sistemul bancar
 */

public class ValidationService {
    //modele pentru validare
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("\\d{16}");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{6,}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern IDENTITY_NUMBER_PATTERN = Pattern.compile("^[0-9]{13}$");

    //constante pentru validare
    public static final int MIN_ACCOUNT_NUMBER_LENGTH = 16;
    public static final int MAX_ACCOUNT_NUMBER_LENGTH = 16;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final double MIN_DEPOSIT_AMOUNT = 1.0;
    public static final double MIN_WITHDRAWAL_AMOUNT = 0.01;
    public static final double MIN_BALANCE = 10.0;
    public static final double MIN_WITHDRAWAL_LIMIT = 100.0;

    //valideaza un numar de cont
    public void validateAccountNumber(String accountNumber)throws ValidationException{
        ValidationException exception = new ValidationException("Validare numar cont");

        if (accountNumber == null || accountNumber.trim().isEmpty()){
            exception.addError("accountNumber","Numarul contului este obligatoriu",accountNumber);
        }
        else if (accountNumber.length() != MIN_ACCOUNT_NUMBER_LENGTH){
            exception.addError("accountNumber",
                    String.format("Numarul contului trebuie sa aiba %d cifre",
                                   MIN_ACCOUNT_NUMBER_LENGTH),
                                   accountNumber);
        }
        else if (!ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches()){
            exception.addError("accountNumber","Numarul contului trbuie sa contina doar cifre",accountNumber);
        }

        if (exception.hasErrors()){
            throw exception;
        }
    }

    //valideaza o parola
    public void validatePassword(String password)throws ValidationException{
        ValidationException exception = new ValidationException("Validare parola");

        if (password == null || password.trim().isEmpty()){
            exception.addError("password","Parola este obligatorie",password);
        }
        else if (password.length() < MIN_PASSWORD_LENGTH){
            exception.addError("password",
                    String.format("Parola trebuie sa contina minim %d caractere",
                            MIN_PASSWORD_LENGTH),password);
        }
        else if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            exception.addError("password",
                    "Parola trebuie să conțină atât litere cât și cifre",
                    password);
        }
        if (exception.hasErrors()){
            throw exception;
        }
    }

    //valideaza un email
    public void validateEmail(String email)throws ValidationException{
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()){
            throw ValidationException.withError("email","Adresa de email este invalida",email);
        }
    }

    //valideaza un numar de telefon
    public void validatePhoneNumber(String phoneNumber)throws ValidationException{
        if (phoneNumber == null || !PHONE_PATTERN.matcher(phoneNumber).matches()){
            throw ValidationException.withError("phoneNumber","Numar de telefon invalid",phoneNumber);
        }
    }

    //valideaza un numar de identitate(IDNP/CNP)
    public void validateIdentityNumber(String identityNumber)throws ValidationException{
        if (identityNumber == null || !IDENTITY_NUMBER_PATTERN.matcher(identityNumber).matches()){
            throw ValidationException.withError("identityNumber","Numar de identitate invalid",identityNumber);
        }
    }

    //valideaza data nasterii(trebuie sa aiba minim 18 ani)
    public void validateBirthDate(LocalDate birthDate)throws ValidationException{
        if (birthDate == null){
            throw ValidationException.withError("birthDate","Data nasterii este obligatorie",null);
        }
        LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
        if (birthDate.isAfter(eighteenYearsAgo)){
            throw ValidationException.withError("birthDate","Clientul trebuie sa aiba minim 18 ani",birthDate);
        }
    }

    //valideaza o suma pentru depunere
    public void validateDepositAmount(double amount, Currency currency)throws ValidationException{
        if (amount < MIN_DEPOSIT_AMOUNT){
            throw ValidationException.withError("amount",
                    String.format("Suma minima pentru depunere este %.2f%s",
                            MIN_DEPOSIT_AMOUNT,currency),amount);
        }
    }

    //valideaza o suma pentru retragere
    public void validateWithdrawalAmount(double amount,Currency currency)throws ValidationException{
        if (amount < MIN_WITHDRAWAL_AMOUNT){
            throw ValidationException.withError("amount",
                    String.format("Suma minima pentru retragere este %.2f%s",
                            MIN_WITHDRAWAL_AMOUNT,currency),amount);
        }
    }

    //valideaza un sold
    public void validateBalance(double balance)throws ValidationException{
        if (balance < MIN_BALANCE){
            throw ValidationException.withError("balance",
                    String.format("Soldul minim este %.3f%s MDL",MIN_BALANCE),balance);
        }
    }

    //valideaza o limita de retragere zilnica
    public void validateWithdrawalLimit(double limit)throws ValidationException{
        if (limit < MIN_WITHDRAWAL_LIMIT){
            throw ValidationException.withError("withdrawalLimit",
                    String.format("Limita zilnica de retragere trebuie sa die minim %.2f MDL",
                            MIN_WITHDRAWAL_LIMIT),limit);
        }
    }

    //valideaza un client
    public void validateCustomer(Customer customer) throws ValidationException {  // ✅ CORRECT TYPE
        ValidationException exception = new ValidationException("Validare client");

        // Validare simplă și directă  ✅
        if (customer.getFirstName() == null || customer.getFirstName().trim().length() < 2) {
            exception.addError("firstName", "Prenume invalid", customer.getFirstName());
        }

        if (customer.getLastName() == null || customer.getLastName().trim().length() < 2) {
            exception.addError("lastName", "Nume invalid", customer.getLastName());
        }

        if (customer.getEmail() == null || !customer.getEmail().contains("@")) {
            exception.addError("email", "Email invalid", customer.getEmail());
        }

        if (exception.hasErrors()) {
            throw exception;
        }
    }

    //valideaza o moneda
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

    //returneaza lista monedelor valide
    private String getValidCurrencies() {
        StringBuilder sb = new StringBuilder();

        // Folosește Currency.values() care există implicit la toate enum-urile
        for (Currency currency : Currency.values()) {
            // Folosește name() care există implicit
            sb.append(currency.name()).append(", ");
        }

        // Elimină ultima virgulă și spațiu
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
    }

    //valideaza un cont(verifica doar datele de baza)
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

