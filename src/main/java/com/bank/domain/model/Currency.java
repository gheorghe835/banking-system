package com.bank.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum pentru valutele suportate în sistemul bancar
 * Fiecare monedă are un cod ISO și o rată de schimb implicită față de MDL
 */
public enum Currency {

    MDL("Moldovan Leu", "MDL", 1.0),
    EUR("Euro", "EUR", 19.45),
    USD("US Dollar", "USD", 17.55),
    GBP("British Pound", "GBP", 22.10),
    RON("Romanian Leu", "RON", 4.0);

    private final String name;
    private final String code;
    private final double exchangeRateToMDL; // Rată implicită

    Currency(String name, String code, double exchangeRateToMDL) {
        this.name = name;
        this.code = code;
        this.exchangeRateToMDL = exchangeRateToMDL;
    }

    // Getteri
    public String getName() {
        return name;
    }

    @JsonValue
    public String getCode() {
        return this.name();
    }

    public double getExchangeRateToMDL() {
        return exchangeRateToMDL;
    }

    // Metode utilitare
    // Găsește Currency după cod
    @JsonCreator
    public static Currency fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Codul valutar nu poate fi null");
        }
        for (Currency currency : values()) {
            if (currency.getCode().equalsIgnoreCase(code.trim())) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Cod valutar necunoscut: " + code);
    }

    public static boolean isValidCurrency(String code) {
        for (Currency currency : values()) {
            if (currency.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    public double convertTo(Currency target, double amount) {
        if (this == target) return amount;
        // Convertim prin MDL ca monedă intermediară
        double amountInMDL = amount * this.exchangeRateToMDL;
        return amountInMDL / target.exchangeRateToMDL;
    }

    // Verifică dacă un cod de monedă este valid
    public static boolean isValid(String code) {
        if (code == null) return false;
        for (Currency currency : values()) {
            if (currency.getCode().equalsIgnoreCase(code.trim())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return code;
    }
}