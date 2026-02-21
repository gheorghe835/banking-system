package com.bank.domain.service;

import com.bank.domain.exception.*;
import com.bank.domain.model.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Serviciu pentru operațiuni de schimb valutar
 */
@Service
public class ExchangeService {


        private final AccountService accountService;
        private final TransactionService transactionService;
        private Map<Currency, BigDecimal> exchangeRates;

        // Comision pentru schimb valutar (0.5%)
        private static final BigDecimal EXCHANGE_COMMISSION = BigDecimal.valueOf(0.005);

        public ExchangeService(AccountService accountService, TransactionService transactionService) {
            this.accountService = accountService;
            this.transactionService = transactionService;
            initializeExchangeRates();
        }


        /**
     * Inițializează ratele de schimb
     */
    private void initializeExchangeRates() {
        exchangeRates = new HashMap<>();

        // Rate față de MDL
        exchangeRates.put(Currency.MDL, BigDecimal.ONE);
        exchangeRates.put(Currency.EUR, BigDecimal.valueOf(19.45));
        exchangeRates.put(Currency.USD, BigDecimal.valueOf(17.55));
        exchangeRates.put(Currency.GBP, BigDecimal.valueOf(22.10));
        exchangeRates.put(Currency.RON, BigDecimal.valueOf(4.0));
    }

    // ===== OPERAȚIUNI DE SCHIMB VALUTAR =====

    /**
     * Schimbă bani dintr-o monedă în alta
     */
    public Account exchangeCurrency(String accountNumber,
                                    Currency fromCurrency,
                                    Currency toCurrency,
                                    BigDecimal amount) {

        // Validare input
        if (fromCurrency == toCurrency) {
            throw new CurrencyExchangeException(fromCurrency, toCurrency, amount,
                    "Nu puteți schimba aceeași monedă");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Sumă invalidă")
                    .addError("amount", "Suma trebuie să fie pozitivă", amount);
        }

        // Găsește contul
        Account account = accountService.findActiveAccount(accountNumber);

        // Verifică dacă are suficiente fonduri în moneda sursă
        if (!account.hasSufficientFunds(amount, fromCurrency)) {
            BigDecimal available = account.getBalance(fromCurrency);
            throw new InsufficientFundsException(accountNumber,
                    amount, available, fromCurrency);
        }

        // Calculează suma schimbată (cu comision)
        BigDecimal exchangedAmount = calculateExchange(amount, fromCurrency, toCurrency);

        // Aplică comision
        BigDecimal commission = calculateCommission(amount, fromCurrency, toCurrency);
        exchangedAmount = exchangedAmount.subtract(commission);
        // Efectuează schimbul
        // Retrage din moneda sursă
        account.withdraw(amount, fromCurrency);

        // Depune în moneda destinație
        account.deposit(exchangedAmount, toCurrency);


        // Înregistrează tranzacția
        String description = String.format("Schimb valutar %s -> %s (comision: %s %s)",
                fromCurrency, toCurrency, commission.setScale(4, RoundingMode.HALF_UP), toCurrency);

        return account;
    }

    /**
     * Calculează suma schimbată între două monede
     */

    @Cacheable(value = "exchangeRates", key = "'calculate-' + #amount + '-' " +
            "+ #fromCurrency.code + '-' + #toCurrency.code", unless = "#result == null")
    public BigDecimal calculateExchange(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        BigDecimal rateFrom = exchangeRates.get(fromCurrency);
        BigDecimal rateTo = exchangeRates.get(toCurrency);
        BigDecimal result = amount.multiply(rateFrom).divide(rateTo, 4, RoundingMode.HALF_UP);
        return result.setScale(2, RoundingMode.HALF_UP);
    }
    /**
     * Calculează suma schimbată între două monede (cu string-uri)
     */
    public BigDecimal calculateExchange(BigDecimal amount, String fromCurrencyCode, String toCurrencyCode) {
        Currency fromCurrency = Currency.fromCode(fromCurrencyCode);
        Currency toCurrency = Currency.fromCode(toCurrencyCode);

        return calculateExchange(amount, fromCurrency, toCurrency);
    }

    // ===== GESTIUNE RATE DE SCHIMB =====

    /**
     * Actualizează rata de schimb pentru o monedă
     */
    @CacheEvict(value = "exchangeRates", allEntries = true)
    public void updateExchangeRate(Currency currency, BigDecimal newRate) {
        if (newRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Rată invalidă")
                    .addError("rate", "Rata trebuie să fie pozitivă", newRate);
        }

        exchangeRates.put(currency, newRate);
    }

    /**
     * Returnează rata de schimb pentru o monedă
     */
    @Cacheable(value = "exchangeRates", key = "#currency.code", unless = "#result == null")
    public BigDecimal getExchangeRate(Currency currency) {
        BigDecimal rate = exchangeRates.get(currency);

        if (rate == null) {
            throw new BankingException(BankingErrorCode.EXCHANGE_RATE_UNAVAILABLE,
                    "Rată de schimb indisponibilă pentru " + currency);
        }

        return rate;
    }

    /**
     * Returnează toate ratele de schimb
     */
    @Cacheable(value = "exchangeRates", key = "'all'", unless = "#result == null")
    public Map<Currency, BigDecimal> getAllExchangeRates() {
        System.out.println("📦 Încărcăm cursurile DIN BAZA DE DATE...");
        return new HashMap<>(exchangeRates);
    }
    public Map<String, BigDecimal> getAllExchangeRatesAsString() {
        System.out.println("📦 Încărcăm cursurile DIN BAZA DE DATE...");
        Map<String, BigDecimal> result = new HashMap<>();
        for (Map.Entry<Currency, BigDecimal> entry : exchangeRates.entrySet()) {
            result.put(entry.getKey().getCode(), entry.getValue());
        }
        return result;
    }

    /**
     * Returnează rata de schimb între două monede
     */
    @Cacheable(value = "exchangeRates", key = "#fromCurrency.code + '-' + #toCurrency.code", unless = "#result == null")
    public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {

        System.out.println("📦 Încărcăm cursul de schimb " +
                fromCurrency + " -> " + toCurrency +
                " DIN BAZA DE DATE...");

        if (fromCurrency == toCurrency) {
            return BigDecimal.ONE;
        }

        BigDecimal rateFrom = getExchangeRate(fromCurrency);
        BigDecimal rateTo = getExchangeRate(toCurrency);

        return rateFrom.divide(rateTo, 4, RoundingMode.HALF_UP);
    }

    // ===== CONVERSII =====

    /**
     * Converstește o sumă în MDL
     */
    @Cacheable(value = "exchangeRates", key = "'convert-' + " +
            " #amount + '-' + #fromCurrency.code + '-MDL'", unless = "#result == null")
    public BigDecimal convertToMDL(BigDecimal amount, Currency currency) {
        return amount.multiply(getExchangeRate(currency));
    }

    /**
     * Converstește o sumă din MDL într-o altă monedă
     */
    public BigDecimal convertFromMDL(BigDecimal amountInMDL, Currency toCurrency) {
        return amountInMDL.divide(getExchangeRate(toCurrency), 4, RoundingMode.HALF_UP);
    }

    /**
     * Converstește o sumă între oricare două monede
     */
    public BigDecimal convert(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency == toCurrency) {
            return amount;
        }

        // Convert to MDL first
        BigDecimal amountInMDL = convertToMDL(amount, fromCurrency);

        // Convert from MDL to target currency
        return convertFromMDL(amountInMDL, toCurrency);
    }

    // ===== RAPOARTE =====

    /**
     * Calculează valoarea totală a contului în MDL
     */
    public BigDecimal calculateTotalValueInMDL(Account account) {
        BigDecimal total = BigDecimal.ZERO;

        for (Currency currency : Currency.values()) {
            BigDecimal balance = account.getBalance(currency);
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal valueInMDL = convertToMDL(balance, currency);
                total = total.add(valueInMDL);
            }
        }

        return total;
    }

    /**
     * Calculează comisionul pentru un schimb valutar
     */
     public BigDecimal calculateCommission(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        // 1. Calculează suma schimbată (deja rotunjită)
        BigDecimal exchangedAmount = calculateExchange(amount, fromCurrency, toCurrency);

        // 2. Calculează comisionul (0.5%)
        BigDecimal commission = exchangedAmount.multiply(EXCHANGE_COMMISSION);

        // 3. Rotunjește comisionul la 2 zecimale
        return commission.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Returnează suma primită după comision
     */
    public BigDecimal getAmountAfterCommission(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        BigDecimal exchangedAmount = calculateExchange(amount, fromCurrency, toCurrency);
        BigDecimal commission = exchangedAmount.multiply(EXCHANGE_COMMISSION);
        return exchangedAmount.subtract(commission);
    }

    /**
     * Actualizează multiple rate deodată
     */
    @CacheEvict(value = "exchangeRates", allEntries = true)
    public void updateMultipleRates(Map<Currency, BigDecimal> newRates) {
        for (Map.Entry<Currency, BigDecimal> entry : newRates.entrySet()) {
            if (entry.getValue() != null && entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                exchangeRates.put(entry.getKey(), entry.getValue());
            }
        }
        System.out.println("✅ Ratele de schimb au fost actualizate");
    }

    /**
     * Resetează ratele la valorile implicite
     */
    @CacheEvict(value = "exchangeRates", allEntries = true)
    public void resetToDefaultRates() {
        initializeExchangeRates();
        System.out.println("✅ Ratele de schimb au fost resetate la valorile implicite");
    }
}
