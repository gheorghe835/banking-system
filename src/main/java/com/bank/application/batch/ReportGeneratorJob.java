package com.bank.application.batch;

import com.bank.domain.model.Account;
import com.bank.domain.model.Transaction;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReportGeneratorJob {

    private static final Logger logger = LoggerFactory.getLogger(ReportGeneratorJob.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // Rulează în fiecare zi la 23:55
    @Scheduled(cron = "0 55 23 * * ?")
    public void generateDailySummary() {
        logger.info("📝 Generare raport zilnic - {}", LocalDateTime.now());

        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            List<Transaction> todayTransactions = transactionRepository
                    .findByTimestampBetween(startOfDay, endOfDay);

            // Creează fișierul raport
            String fileName = "reports/daily-summary-" + today + ".csv";

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                writer.println("Data,Ora,Tip,Cont Sursă,Cont Destinație,Sumă,Monedă,Status");

                for (Transaction t : todayTransactions) {
                    writer.printf("%s,%s,%s,%s,%s,%.2f,%s,%s%n",
                            t.getTimestamp().toLocalDate(),
                            t.getTimestamp().toLocalTime(),
                            t.getType(),
                            t.getSourceAccountNumber() != null ? t.getSourceAccountNumber() : "",
                            t.getTargetAccountNumber() != null ? t.getTargetAccountNumber() : "",
                            t.getAmount(),
                            t.getCurrency(),
                            t.getStatus()
                    );
                }
            }

            logger.info("✅ Raport zilnic generat: {}", fileName);

        } catch (Exception e) {
            logger.error("❌ Eroare la generarea raportului: {}", e.getMessage(), e);
        }
    }

    // Rulează în fiecare duminică la 23:59
    @Scheduled(cron = "0 59 23 * * SUN")
    public void generateWeeklyReport() {
        logger.info("📊 Generare raport săptămânal - {}", LocalDateTime.now());

        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(7);

            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();

            List<Transaction> weeklyTransactions = transactionRepository
                    .findByTimestampBetween(start, end);

            List<Account> allAccounts = accountRepository.findAll();

            // Calculează statistici
            long totalAccounts = allAccounts.size();
            long activeAccounts = allAccounts.stream().filter(Account::isActive).count();

            double totalDeposits = weeklyTransactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.DEPOSIT)
                    .mapToDouble(t -> t.getAmount().doubleValue())
                    .sum();

            double totalWithdrawals = weeklyTransactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.WITHDRAWAL)
                    .mapToDouble(t -> t.getAmount().doubleValue())
                    .sum();

            // Generează raportul
            String fileName = "reports/weekly-summary-" + startDate + "-to-" + endDate + ".txt";

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                writer.println("=".repeat(60));
                writer.println("📊 RAPORT SĂPTĂMÂNAL");
                writer.println("=".repeat(60));
                writer.printf("Perioada: %s - %s%n", startDate, endDate);
                writer.printf("Data generării: %s%n", LocalDateTime.now());
                writer.println("=".repeat(60));
                writer.println("\n📈 STATISTICI GENERALE:");
                writer.printf("Total conturi: %d%n", totalAccounts);
                writer.printf("Conturi active: %d%n", activeAccounts);
                writer.printf("Conturi inactive: %d%n", totalAccounts - activeAccounts);
                writer.println("\n💰 TRANZACȚII:");
                writer.printf("Total tranzacții: %d%n", weeklyTransactions.size());
                writer.printf("Suma depuneri: %.2f MDL%n", totalDeposits);
                writer.printf("Suma retrageri: %.2f MDL%n", totalWithdrawals);
                writer.printf("Flux net: %.2f MDL%n", totalDeposits - totalWithdrawals);
                writer.println("=".repeat(60));
            }

            logger.info("✅ Raport săptămânal generat: {}", fileName);

        } catch (Exception e) {
            logger.error("❌ Eroare la generarea raportului săptămânal: {}", e.getMessage(), e);
        }
    }
}
