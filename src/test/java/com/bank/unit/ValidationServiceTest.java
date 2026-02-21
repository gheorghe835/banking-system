package com.bank.unit;

import com.bank.domain.exception.ValidationException;
import com.bank.domain.model.Currency;
import com.bank.domain.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    // ============ METODE SIMPLE - DOAR VERIFICĂ CĂ ARUNCĂ EXCEPȚIE ============

    @Test
    void validateAccountNumber_Valid16Digits_ShouldPass() {
        assertThatCode(() -> validationService.validateAccountNumber("1234567890123456"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAccountNumber_TooShort_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateAccountNumber("12345"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateAccountNumber_TooLong_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateAccountNumber("12345678901234567890"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateAccountNumber_ContainsLetters_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateAccountNumber("1234567890ABCDEF"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateAccountNumber_Null_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateAccountNumber(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateAccountNumber_Empty_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateAccountNumber(""))
                .isInstanceOf(ValidationException.class);
    }

    // ============ PASSWORD TESTS ============

    @Test
    void validatePassword_ValidPassword_ShouldPass() {
        assertThatCode(() -> validationService.validatePassword("Password123"))
                .doesNotThrowAnyException();
    }

    @Test
    void validatePassword_TooShort_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validatePassword("Pass1"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validatePassword_OnlyLetters_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validatePassword("Password"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validatePassword_OnlyDigits_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validatePassword("123456"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validatePassword_Null_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validatePassword(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validatePassword_Empty_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validatePassword(""))
                .isInstanceOf(ValidationException.class);
    }

    // ============ EMAIL TESTS ============

    @Test
    void validateEmail_ValidEmail_ShouldPass() {
        assertThatCode(() -> validationService.validateEmail("ion.popescu@example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateEmail_InvalidFormat_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateEmail("ion.popescu"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateEmail_Null_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateEmail(null))
                .isInstanceOf(ValidationException.class);
    }

    // ============ PHONE NUMBER TESTS ============

    @Test
    void validatePhoneNumber_ValidPhone_ShouldPass() {
        assertThatCode(() -> validationService.validatePhoneNumber("069123456"))
                .doesNotThrowAnyException();

        assertThatCode(() -> validationService.validatePhoneNumber("+37369123456"))
                .doesNotThrowAnyException();
    }

    @Test
    void validatePhoneNumber_InvalidFormat_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validatePhoneNumber("abc"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validatePhoneNumber_Null_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validatePhoneNumber(null))
                .isInstanceOf(ValidationException.class);
    }

    // ============ IDENTITY NUMBER TESTS ============

    @Test
    void validateIdentityNumber_Valid13Digits_ShouldPass() {
        assertThatCode(() -> validationService.validateIdentityNumber("1234567890123"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateIdentityNumber_InvalidLength_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateIdentityNumber("12345"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateIdentityNumber_ContainsLetters_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateIdentityNumber("1234567890ABC"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateIdentityNumber_Null_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateIdentityNumber(null))
                .isInstanceOf(ValidationException.class);
    }

    // ============ BIRTH DATE TESTS ============

    @Test
    void validateBirthDate_Adult_ShouldPass() {
        LocalDate adultDate = LocalDate.now().minusYears(20);
        assertThatCode(() -> validationService.validateBirthDate(adultDate))
                .doesNotThrowAnyException();
    }

    @Test
    void validateBirthDate_Underage_ShouldThrowException() {
        LocalDate childDate = LocalDate.now().minusYears(17);
        assertThatThrownBy(() -> validationService.validateBirthDate(childDate))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateBirthDate_Null_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateBirthDate(null))
                .isInstanceOf(ValidationException.class);
    }

    // ============ AMOUNT TESTS ============

    @Test
    void validateDepositAmount_PositiveAmount_ShouldPass() {
        assertThatCode(() -> validationService.validateDepositAmount(BigDecimal.valueOf(100), Currency.MDL))
                .doesNotThrowAnyException();
    }

    @Test
    void validateDepositAmount_NegativeAmount_ShouldThrowException() {
        assertThatThrownBy(() ->
                validationService.validateDepositAmount(BigDecimal.valueOf(-50), Currency.MDL))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateDepositAmount_ZeroAmount_ShouldThrowException() {
        assertThatThrownBy(() ->
                validationService.validateDepositAmount(BigDecimal.ZERO, Currency.MDL))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateDepositAmount_NullAmount_ShouldThrowException() {
        assertThatThrownBy(() ->
                validationService.validateDepositAmount(null, Currency.MDL))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateWithdrawalAmount_PositiveAmount_ShouldPass() {
        assertThatCode(() -> validationService.validateWithdrawalAmount(BigDecimal.valueOf(100), Currency.MDL))
                .doesNotThrowAnyException();
    }

    @Test
    void validateWithdrawalAmount_NegativeAmount_ShouldThrowException() {
        assertThatThrownBy(() ->
                validationService.validateWithdrawalAmount(BigDecimal.valueOf(-50), Currency.MDL))
                .isInstanceOf(ValidationException.class);
    }

    // ============ CURRENCY TESTS ============

    @Test
    void validateCurrency_ValidCode_ShouldPass() {
        assertThatCode(() -> validationService.validateCurrency("EUR"))
                .doesNotThrowAnyException();

        assertThatCode(() -> validationService.validateCurrency("usd")) // case-insensitive
                .doesNotThrowAnyException();
    }

    @Test
    void validateCurrency_InvalidCode_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateCurrency("XYZ"))
                .isInstanceOf(ValidationException.class);
    }

    // ============ CUSTOMER NAME TESTS ============

    @Test
    void validateCustomerName_ValidNames_ShouldPass() {
        assertThatCode(() -> validationService.validateCustomerName("Ion", "Popescu"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCustomerName_TooShortFirstName_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateCustomerName("I", "Popescu"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateCustomerName_TooShortLastName_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateCustomerName("Ion", "P"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateCustomerName_NullFirstName_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateCustomerName(null, "Popescu"))
                .isInstanceOf(ValidationException.class);
    }

    // ============ WITHDRAWAL LIMIT TESTS ============

    @Test
    void validateWithdrawalLimit_ValidLimit_ShouldPass() {
        assertThatCode(() -> validationService.validateWithdrawalLimit(BigDecimal.valueOf(1000)))
                .doesNotThrowAnyException();
    }

    @Test
    void validateWithdrawalLimit_BelowMinimum_ShouldThrowException() {
        assertThatThrownBy(() ->
                validationService.validateWithdrawalLimit(BigDecimal.valueOf(50)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateWithdrawalLimit_Null_ShouldThrowException() {
        assertThatThrownBy(() -> validationService.validateWithdrawalLimit(null))
                .isInstanceOf(ValidationException.class);
    }
}