package com.bank.domain.exception;

/**
 * Excepție aruncată când se încearcă crearea unui client care există deja.
 */
public class DuplicateCustomerException extends BankingException {

    private final String field;
    private final String value;

    public DuplicateCustomerException(String message) {
        super(BankingErrorCode.DUPLICATE_CUSTOMER, message);
        this.field = null;
        this.value = null;
    }

    public DuplicateCustomerException(String field, String value) {
        super(BankingErrorCode.DUPLICATE_CUSTOMER,
                "Client duplicat: " + field + " = " + value);
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
