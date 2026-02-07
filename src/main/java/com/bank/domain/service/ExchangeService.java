package com.bank.domain.service;

import com.bank.domain.exception.*;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviciul pentru operatiuni de schimb valutar
 */

public class ExchangeService {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private Map<Currency, BigDecimal> exchangeRates = new HashMap<>();

    //comision pentru schimb valutar(0.5%)
    private static final BigDecimal EXCHANGE_COMMISSION = BigDecimal.valueOf(0.005);

    public ExchangeService(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.exchangeRates = new HashMap<>();
        initializeExchangeRates();
    }

    //initializeaza ratele de schimb
    private void initializeExchangeRates(){
        exchangeRates.put(Currency.MDL,BigDecimal.ONE);
        exchangeRates.put(Currency.EUR,BigDecimal.valueOf(19.45));
        exchangeRates.put(Currency.USD,BigDecimal.valueOf(17.55));
        exchangeRates.put(Currency.GBP,BigDecimal.valueOf(22.10));
        exchangeRates.put(Currency.RON,BigDecimal.valueOf(4.0));
    }

    //Operatiuni de schimb valutar
    /**
     * schimba banii dintr-o moneda in alta
     */
    public Account exchangeCurrency(String accountNumber,
                                    Currency fromCurrency,
                                    Currency toCurrency,
                                    BigDecimal amount){
        //validare input
        if (fromCurrency == toCurrency){
            throw new CurrencyExchangeException(fromCurrency,toCurrency,amount.doubleValue(),
                    "Nu puteti schimba aceeasi moneda");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new ValidationException("Suma invalida")
                    .addError("amount","Suma trebuie sa fie pozitiva",amount);
        }

        //gaseste contul
        Account account = accountService.findActiveAccount(accountNumber);

        //verifica daca are suficiente fonduri in moneda sursa
        if (!account.hasSufficientFounds(amount,fromCurrency)){
            BigDecimal available = account.getBalance(fromCurrency);
            throw new InsufficientFundsException(accountNumber,
                    amount.doubleValue(),available.doubleValue(),fromCurrency);
        }

        //calculeaza suma schimbata(cu comision)
        BigDecimal exchangeAmount = calculateExchange(amount,fromCurrency,toCurrency);

        //aplica comision
        BigDecimal commission = exchangeAmount.multiply(EXCHANGE_COMMISSION);
        exchangeAmount = exchangeAmount.subtract(commission);

        //efectuiaza schimbul
        //retrage din moneda sursa
        account.withdraw(amount,fromCurrency);

        //depune in moneda destinatie
        account.deposit(exchangeAmount,toCurrency);

        //salveaza contul
        //inregistreaza tranzactia
        String description = String.format("Schimb valutar %s -> %s(comision: %s%s",
                fromCurrency,toCurrency,commission.setScale(4, RoundingMode.HALF_UP),toCurrency);

        //este nevoie de a crea o tranzactie de tip Exchange

        return account;
    }

    /**
     * calculeaza suma schimbata intre doua monede
     */
    public BigDecimal calculateExchange(BigDecimal amount,Currency fromCurrency,Currency toCurrency){
        BigDecimal rateFrom = exchangeRates.get(fromCurrency);
        BigDecimal rateTo = exchangeRates.get(toCurrency);

        if (rateFrom == null || rateTo == null){
            throw new CurrencyExchangeException(fromCurrency,toCurrency,amount.doubleValue(),
                    "Rate de schimb indisponibile");
        }

        //conversie: amount * (rateFrom/rateTo)
        return amount.multiply(rateFrom).divide(rateTo,4,RoundingMode.HALF_UP);
    }

    /**
     * calculeaza suma schimbata intre doua monede(cu string)
     */
    public BigDecimal calculateExchange(BigDecimal amount,String fromCurrencyCode,String toCurrencyCode){
        Currency fromCurrency = Currency.fromCode(fromCurrencyCode);
        Currency toCurrency = Currency.fromCode(toCurrencyCode);

        return calculateExchange(amount,fromCurrency,toCurrency);
    }

    //Gestiune rate de schimb
    /**
     * actualizeaza rata de schimb pentru o moneda
     */
    public void updateExchangeRate(Currency currency,BigDecimal newRate){
        if (newRate.compareTo(BigDecimal.ZERO) <= 0){
            throw new ValidationException("Rata invalida")
                    .addError("rate","Rata trebuie sa fie pozitiva",newRate);
        }
        exchangeRates.put(currency,newRate);
    }

    /**
     * returneaza rata de schimb pentru o moneda
     */
    public BigDecimal getExchangeRate(Currency currency){
        BigDecimal rate = exchangeRates.get(currency);

        if (rate == null){
            throw new BankingException(BankingErrorCode.EXCHANGE_RATE_UNAVAILABLE,
                    "Rata de schimb indisponibila pentru " + currency);
        }
        return rate;
    }

    /**
     * returneaza toate ratele de schimb
     */
    public Map<Currency,BigDecimal> getAllExchangeRates(){
        return new HashMap<>(exchangeRates);
    }

    /**
     * returneaza rata de schimb intre doua monede
     */
    public BigDecimal getExchangeRate(Currency fromCurency,Currency toCurrency){
        if (fromCurency == toCurrency){
            return BigDecimal.ONE;
        }

        BigDecimal rateFrom = getExchangeRate(fromCurency);
        BigDecimal rateTo = getExchangeRate(toCurrency);

        return rateFrom.divide(rateTo,4,RoundingMode.HALF_UP);
    }

    //Conversii
    /**
     * converteste o suma in MDL
     */
    public BigDecimal convertToMDL(BigDecimal amount,Currency currency){
        return amount.multiply(getExchangeRate(currency));
    }

    /**
     * converteste o suma din MDl intr-o alta moneda
     */
    public BigDecimal convertFromMDL(BigDecimal amountInMDL,Currency currency){
        return amountInMDL.divide(getExchangeRate(currency),4,RoundingMode.HALF_UP);
    }

    /**
     * converteste o suma intre oricare doua monede
     */
    public BigDecimal convert(BigDecimal amount,Currency fromCurrency,Currency toCurrency){
        if (fromCurrency == toCurrency){
            return amount;
        }

        //converteste MDL in prim rind
        BigDecimal amoountInMDL = convertToMDL(amount,fromCurrency);

        //converteste din MDL in moneda tinta
        return convertFromMDL(amoountInMDL,toCurrency);
    }

    //Rapoarte
    /**
     * calculeaza valoarea totala a contului in MDL
     */
    public BigDecimal calculateTotalValueInMDL(Account account) {
        BigDecimal total = BigDecimal.ZERO;

        for (Currency currency : Currency.values()) {
            BigDecimal balance = account.getBalance(currency);
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal valueInMDL = convertToMDL(balance, currency);
            }
        }
        return total;
    }

    /**
     * calculeaza comisionul pentru un schimb valutar
     */
    public BigDecimal calculateCommission(BigDecimal amount,Currency fromCurrency,Currency toCurrency){
        BigDecimal exchangedAmount = calculateExchange(amount,fromCurrency,toCurrency);
        return exchangedAmount.multiply(EXCHANGE_COMMISSION);
    }

    /**
     * returneaza suma primita dupa comision
     */
    public BigDecimal getAmountAfterCommission(BigDecimal amount,Currency fromCurrency,Currency toCurrency){
        BigDecimal exchangedAmount = calculateExchange(amount,fromCurrency,toCurrency);
        BigDecimal commission = exchangedAmount.multiply(EXCHANGE_COMMISSION);
        return exchangedAmount.subtract(commission);
    }

    // setarea ratelor (pentru testare):
    public void setExchangeRates(Map<Currency, BigDecimal> rates) {
        this.exchangeRates = new HashMap<>(rates);
    }
}























