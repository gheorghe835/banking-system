package com.bank.domain.exception;

/**
 * Excepție aruncată când un client nu este găsit.
 */
public class CustomerNotFoundException extends BankingException {

    private final String customerId;
    private final String searchCriteria;

    public CustomerNotFoundException(String customerId) {
        super(BankingErrorCode.CUSTOMER_NOT_FOUND,
                "Clientul cu ID-ul '" + customerId + "' nu a fost găsit");
        this.customerId = customerId;
        this.searchCriteria = "id";
    }

    public CustomerNotFoundException(String criteria, String value) {
        super(BankingErrorCode.CUSTOMER_NOT_FOUND,
                "Clientul cu " + criteria + " '" + value + "' nu a fost găsit");
        this.customerId = null;
        this.searchCriteria = criteria;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getSearchCriteria() {
        return searchCriteria;
    }
}
