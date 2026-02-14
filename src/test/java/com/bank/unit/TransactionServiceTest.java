package com.bank.unit;

import com.bank.domain.exception.BankingException;
import com.bank.domain.exception.ValidationException;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Transaction;
import com.bank.domain.repository.TransactionRepository;
import com.bank.domain.service.AccountService;
import com.bank.domain.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    private TransactionService transactionService;

    private Transaction testTransaction;
    private final String TRANSACTION_ID = "TRX12345";
    private final String ACCOUNT_NUMBER = "1234567890123456";

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, accountService);

        testTransaction = new Transaction(
                Transaction.TransactionType.DEPOSIT,
                BigDecimal.valueOf(500),
                Currency.MDL,
                "Test transaction"
        );
        testTransaction.setTransactionId(TRANSACTION_ID);
        testTransaction.setTargetAccountNumber(ACCOUNT_NUMBER);
        testTransaction.markAsCompleted();
    }

    @Test
    void findTransaction_ExistingId_ShouldReturnTransaction() {
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(testTransaction));

        Transaction found = transactionService.findTransaction(TRANSACTION_ID);

        assertThat(found).isNotNull();
        assertThat(found.getTransactionId()).isEqualTo(TRANSACTION_ID);
        verify(transactionRepository).findById(TRANSACTION_ID);
    }

    @Test
    void findTransaction_NonExistingId_ShouldThrowException() {
        when(transactionRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.findTransaction("INVALID"))
                .isInstanceOf(BankingException.class)
                .hasMessageContaining("Tranzacția cu ID-ul INVALID nu a fost găsită");
    }

    @Test
    void getAccountTransactions_ValidAccount_ShouldReturnList() {
        List<Transaction> transactions = List.of(testTransaction);
        when(transactionRepository.findByAccountNumber(ACCOUNT_NUMBER)).thenReturn(transactions);

        List<Transaction> result = transactionService.getAccountTransactions(ACCOUNT_NUMBER);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransactionId()).isEqualTo(TRANSACTION_ID);
        verify(transactionRepository).findByAccountNumber(ACCOUNT_NUMBER);
    }

    @Test
    void getLastTransactions_ValidLimit_ShouldReturnLimitedList() {
        List<Transaction> transactions = List.of(testTransaction);
        when(transactionRepository.findLastTransactionsByAccount(ACCOUNT_NUMBER, 5))
                .thenReturn(transactions);

        List<Transaction> result = transactionService.getLastTransactions(ACCOUNT_NUMBER, 5);

        assertThat(result).hasSize(1);
        verify(transactionRepository).findLastTransactionsByAccount(ACCOUNT_NUMBER, 5);
    }

    @Test
    void getLastTransactions_InvalidLimit_ShouldThrowException() {
        assertThatThrownBy(() ->
                transactionService.getLastTransactions(ACCOUNT_NUMBER, 0)
        ).isInstanceOf(ValidationException.class);

        assertThatThrownBy(() ->
                transactionService.getLastTransactions(ACCOUNT_NUMBER, 101)
        ).isInstanceOf(ValidationException.class);

        verify(transactionRepository, never()).findLastTransactionsByAccount(any(), anyInt());
    }

    @Test
    void markAsCompleted_PendingTransaction_ShouldComplete() {
        // Arrange
        Transaction pendingTransaction = new Transaction(
                Transaction.TransactionType.DEPOSIT,
                BigDecimal.valueOf(500),
                Currency.MDL,
                "Test transaction"
        );
        pendingTransaction.setTransactionId(TRANSACTION_ID);
        pendingTransaction.setStatus(Transaction.TransactionStatus.PENDING);

        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(pendingTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(pendingTransaction);

        // Act
        Transaction completed = transactionService.markAsCompleted(TRANSACTION_ID);

        // Assert
        assertThat(completed.getStatus()).isEqualTo(Transaction.TransactionStatus.COMPLETED);
        verify(transactionRepository).save(pendingTransaction);
    }

    @Test
    void markAsCompleted_NonPendingTransaction_ShouldThrowException() {
        // Arrange - transaction is already COMPLETED
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(testTransaction));

        // Act & Assert
        assertThatThrownBy(() -> transactionService.markAsCompleted(TRANSACTION_ID))
                .isInstanceOf(BankingException.class)
                .hasMessageContaining("nu poate fi marcată");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTotalTransactionAmount_ShouldReturnSum() {
        when(transactionRepository.getTotalTransactionAmount()).thenReturn(BigDecimal.valueOf(5000));

        BigDecimal total = transactionService.getTotalTransactionAmount();

        assertThat(total).isEqualTo(BigDecimal.valueOf(5000));
        verify(transactionRepository).getTotalTransactionAmount();
    }

    @Test
    void getFailedTransactions_ShouldReturnList() {
        List<Transaction> failedTransactions = List.of(testTransaction);
        when(transactionRepository.findFailedTransactions()).thenReturn(failedTransactions);

        List<Transaction> result = transactionService.getFailedTransactions();

        assertThat(result).hasSize(1);
        verify(transactionRepository).findFailedTransactions();
    }

    @Test
    void getPendingTransactions_ShouldReturnList() {
        List<Transaction> pendingTransactions = List.of(testTransaction);
        when(transactionRepository.findPendingTransactions()).thenReturn(pendingTransactions);

        List<Transaction> result = transactionService.getPendingTransactions();

        assertThat(result).hasSize(1);
        verify(transactionRepository).findPendingTransactions();
    }
}
