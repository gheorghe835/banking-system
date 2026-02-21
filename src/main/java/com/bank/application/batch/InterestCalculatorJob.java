package com.bank.application.batch;

import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Transaction;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.TransactionRepository;
import com.bank.domain.service.InterestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class InterestCalculatorJob {

    private static final Logger logger = LoggerFactory.getLogger(InterestCalculatorJob.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private InterestService interestService;

    // Rulează în fiecare zi la 00:00 (miezul nopții)
    @Scheduled(cron = "0 0 0 * * ?")
    public void calculateDailyInterest() {
        logger.info("🚀 START JOB: Calcul dobândă zilnică - {}", LocalDateTime.now());

        try {
            List<Account> activeAccounts = accountRepository.findActiveAccounts();
            logger.info("Număr conturi active: {}", activeAccounts.size());

            int processedCount = 0;
            BigDecimal totalInterest = BigDecimal.ZERO;

            for (Account account : activeAccounts) {
                // Aplică dobândă doar pentru conturile de economii (SAVINGS)
                if ("SAVINGS".equals(account.getAccountType())) {
                    BigDecimal interest = interestService.calculateInterestForAccount(account, 1);

                    if (interest.compareTo(BigDecimal.ZERO) > 0) {
                        // Aplică dobânda
                        account.deposit(interest, Currency.MDL);
                        accountRepository.save(account);

                        // Înregistrează tranzacția
                        Transaction transaction = new Transaction(
                                Transaction.TransactionType.INTEREST,
                                interest,
                                Currency.MDL,
                                "Dobândă zilnică automată"
                        );
                        transaction.setTargetAccountNumber(account.getAccountNumber());
                        transaction.markAsCompleted();
                        transactionRepository.save(transaction);

                        processedCount++;
                        totalInterest = totalInterest.add(interest);

                        logger.debug("Dobândă aplicată contului {}: {} MDL",
                                account.getAccountNumber(), interest);
                    }
                }
            }

            logger.info("✅ JOB FINALIZAT: {} conturi procesate, dobândă totală: {} MDL",
                    processedCount, totalInterest);

        } catch (Exception e) {
            logger.error("❌ EROARE la calculul dobânzii: {}", e.getMessage(), e);
        }
    }

    // Rulează în fiecare lună pe data de 1 la 01:00
    @Scheduled(cron = "0 0 1 1 * ?")
    public void generateMonthlyInterestReport() {
        logger.info("📊 Generare raport lunar dobânzi - {}", LocalDateTime.now());
        // Aici poți genera un raport PDF/Excel și salva sau trimite prin email
    }
}
