package com.bank.unit;

import com.bank.domain.exception.CurrencyExchangeException;
import com.bank.domain.exception.InsufficientFundsException;
import com.bank.domain.exception.ValidationException;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Customer;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.ExchangeService;
import com.bank.domain.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    private ExchangeService exchangeService;

    private Account testAccount;
    private final String ACCOUNT_NUMBER = "1234567890123456";

    @BeforeEach
    void setUp() {
        exchangeService = new ExchangeService(accountService, transactionService);

        Customer customer = new Customer();
        customer.setFirstName("Ion");
        customer.setLastName("Popescu");

        testAccount = new Account(ACCOUNT_NUMBER, customer, Account.ACCOUNT_TYPE_CURRENT);
    }

    @Test
    void calculateExchange_EURtoUSD_ShouldReturnCorrectAmount() {
        BigDecimal amount = BigDecimal.valueOf(100);
        BigDecimal result = exchangeService.calculateExchange(amount, Currency.EUR, Currency.USD);

        // 100 EUR = 100 * (19.45 / 17.55) = 110.83 USD
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(110.83).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void calculateExchange_USDtoMDL_ShouldReturnCorrectAmount() {
        BigDecimal amount = BigDecimal.valueOf(100);
        BigDecimal result = exchangeService.calculateExchange(amount, Currency.USD, Currency.MDL);

        // 100 USD = 100 * 17.55 = 1755 MDL
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1755.00));
    }

    @Test
    void getExchangeRate_ValidCurrency_ShouldReturnRate() {
        BigDecimal rate = exchangeService.getExchangeRate(Currency.EUR);
        assertThat(rate).isEqualByComparingTo(BigDecimal.valueOf(19.45));
    }

    @Test
    void getExchangeRate_InvalidCurrency_ShouldThrowException() {
        assertThatThrownBy(() ->
                exchangeService.getExchangeRate(Currency.fromCode("XYZ"))
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getAllExchangeRates_ShouldReturnAllRates() {
        Map<Currency, BigDecimal> rates = exchangeService.getAllExchangeRates();

        assertThat(rates).hasSize(5);
        assertThat(rates).containsKeys(Currency.MDL, Currency.EUR, Currency.USD, Currency.GBP, Currency.RON);
        assertThat(rates.get(Currency.MDL)).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void convertToMDL_ValidAmount_ShouldReturnConvertedValue() {
        BigDecimal amount = BigDecimal.valueOf(100);
        BigDecimal result = exchangeService.convertToMDL(amount, Currency.EUR);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1945.00));
    }

    @Test
    void exchangeCurrency_ValidData_ShouldExchangeMoney() {
        testAccount.deposit(BigDecimal.valueOf(1000), Currency.EUR);
        when(accountService.findActiveAccount(ACCOUNT_NUMBER)).thenReturn(testAccount);

        BigDecimal amount = BigDecimal.valueOf(100);

        Account updated = exchangeService.exchangeCurrency(ACCOUNT_NUMBER, Currency.EUR, Currency.USD, amount);

        // Sold EUR ar trebui să scadă cu 100
        assertThat(updated.getBalance(Currency.EUR)).isEqualByComparingTo(BigDecimal.valueOf(900));

        // Sold USD ar trebui să crească cu ~110.83 - comision 0.5% = ~110.28
        BigDecimal expectedUSD = BigDecimal.valueOf(110.28).setScale(2, RoundingMode.HALF_UP);
        assertThat(updated.getBalance(Currency.USD)).isEqualByComparingTo(expectedUSD);
    }

    @Test
    void exchangeCurrency_InsufficientFunds_ShouldThrowException() {
        testAccount.deposit(BigDecimal.valueOf(50), Currency.EUR);
        when(accountService.findActiveAccount(ACCOUNT_NUMBER)).thenReturn(testAccount);

        assertThatThrownBy(() ->
                exchangeService.exchangeCurrency(ACCOUNT_NUMBER, Currency.EUR, Currency.USD, BigDecimal.valueOf(100))
        ).isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void calculateCommission_ShouldReturnCorrectAmount() {
        BigDecimal amount = BigDecimal.valueOf(100);
        BigDecimal commission = exchangeService.calculateCommission(amount, Currency.EUR, Currency.USD);

        // Comision 0.5% din 110.83 = 0.55
        assertThat(commission).isEqualByComparingTo(BigDecimal.valueOf(0.55).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void updateExchangeRate_ValidRate_ShouldUpdate() {
        BigDecimal newRate = BigDecimal.valueOf(20.00);
        exchangeService.updateExchangeRate(Currency.EUR, newRate);

        BigDecimal rate = exchangeService.getExchangeRate(Currency.EUR);
        assertThat(rate).isEqualByComparingTo(newRate);
    }

    @Test
    void updateExchangeRate_NegativeRate_ShouldThrowException() {
        assertThatThrownBy(() ->
                exchangeService.updateExchangeRate(Currency.EUR, BigDecimal.valueOf(-10))
        ).isInstanceOf(ValidationException.class);
    }
}
