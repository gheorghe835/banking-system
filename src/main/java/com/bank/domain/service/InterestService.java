package com.bank.domain.service;

import com.bank.domain.exception.BankingErrorCode;
import com.bank.domain.exception.BankingException;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Transaction;
import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Serviciu pentru calculul și aplicarea dobânzilor bancare
 *
 * <p>Responsabilități principale:</p>
 * <ul>
 *   <li>Calculul dobânzilor pentru diferite tipuri de conturi</li>
 *   <li>Aplicarea periodică a dobânzilor</li>
 *   <li>Înregistrarea tranzacțiilor de dobândă</li>
 *   <li>Generarea de rapoarte și statistici</li>
 *   <li>Gestiunea ratelor dobânzilor</li>
 * </ul>
 */
public class InterestService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    // Rate de dobândă standard (în procente pe an)
    private static final double SAVINGS_ACCOUNT_RATE = 3.5;   // 3.5% pentru conturi de economii
    private static final double CURRENT_ACCOUNT_RATE = 0.5;   // 0.5% pentru conturi curente
    private static final double BUSINESS_ACCOUNT_RATE = 1.5;  // 1.5% pentru conturi business

    // Sold minim pentru a primi dobândă
    private static final BigDecimal MIN_BALANCE_FOR_INTEREST = BigDecimal.valueOf(100.0);

    // Dobândă minimă pentru a fi aplicată (pentru a evita tranzacții prea mici)
    private static final BigDecimal MIN_INTEREST_TO_APPLY = BigDecimal.valueOf(0.01);

    /**
     * Constructor pentru InterestService
     *
     * @param accountRepository Repository pentru conturi
     * @param transactionRepository Repository pentru tranzacții
     * @param accountService Serviciul pentru conturi
     */
    public InterestService(AccountRepository accountRepository,
                           TransactionRepository transactionRepository,
                           AccountService accountService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    // ===============================
    // METODE DE CALCUL DOBÂNZI
    // ===============================

    /**
     * Calculează dobânda pentru un cont pentru o perioadă specificată
     *
     * @param account Contul pentru care se calculează dobânda
     * @param days Numărul de zile pentru care se calculează dobânda
     * @param annualRate Rata anuală a dobânzii (în procente)
     * @return Suma dobânzii calculate
     */
    public BigDecimal calculateInterest(Account account, int days, double annualRate) {
        // Validare input
        if (account == null) {
            throw new IllegalArgumentException("Contul nu poate fi null");
        }

        if (days <= 0) {
            throw new IllegalArgumentException("Numărul de zile trebuie să fie pozitiv");
        }

        if (annualRate < 0) {
            throw new IllegalArgumentException("Rata dobânzii nu poate fi negativă");
        }

        // Contul inactiv nu primește dobândă
        if (!account.isActive()) {
            return BigDecimal.ZERO;
        }

        // Calculează soldul total în MDL
        BigDecimal totalBalanceInMDL = account.getTotalBalanceInMDL();

        // Verifică dacă soldul este suficient pentru dobândă
        if (totalBalanceInMDL.compareTo(MIN_BALANCE_FOR_INTEREST) < 0) {
            return BigDecimal.ZERO;
        }

        // Formula dobânzii simple: dobânda = principal * rata * timp
        // Rata zilnică = annualRate / 365 / 100
        BigDecimal dailyRate = BigDecimal.valueOf(annualRate)
                .divide(BigDecimal.valueOf(36500), 10, RoundingMode.HALF_UP); // /365 /100

        BigDecimal interest = totalBalanceInMDL
                .multiply(dailyRate)
                .multiply(BigDecimal.valueOf(days));

        // Rotunjește la două zecimale
        return interest.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculează dobânda pentru un cont folosind rata potrivită tipului de cont
     *
     * @param account Contul pentru care se calculează dobânda
     * @param days Numărul de zile pentru care se calculează dobânda
     * @return Suma dobânzii calculate
     */
    public BigDecimal calculateInterestForAccount(Account account, int days) {
        double annualRate = getInterestRateForAccountType(account.getAccountType());
        return calculateInterest(account, days, annualRate);
    }

    /**
     * Returnează rata de dobândă în funcție de tipul contului
     *
     * @param accountType Tipul contului
     * @return Rata anuală a dobânzii (în procente)
     */
    private double getInterestRateForAccountType(String accountType) {
        if (accountType == null) {
            return CURRENT_ACCOUNT_RATE; // Rata implicită
        }

        return switch (accountType.toUpperCase()) {
            case Account.ACCOUNT_TYPE_SAVINGS -> SAVINGS_ACCOUNT_RATE;
            case Account.ACCOUNT_TYPE_BUSINESS -> BUSINESS_ACCOUNT_RATE;
            case Account.ACCOUNT_TYPE_CURRENT -> CURRENT_ACCOUNT_RATE;
            default -> CURRENT_ACCOUNT_RATE; // Rata implicită pentru tipuri necunoscute
        };
    }

    /**
     * Calculează dobânda pentru o perioadă specificată
     *
     * @param account Contul pentru care se calculează dobânda
     * @param startDate Data de început
     * @param endDate Data de sfârșit
     * @return Suma dobânzii calculate
     */
    public BigDecimal calculateInterestForPeriod(Account account, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Datele nu pot fi null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data de început nu poate fi după data de sfârșit");
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return calculateInterestForAccount(account, (int) days);
    }

    // ===============================
    // METODE DE APLICARE DOBÂNZI
    // ===============================

    /**
     * Aplică dobânda zilnică pentru un cont
     *
     * @param account Contul pentru care se aplică dobânda
     * @param date Data pentru care se aplică dobânda
     * @return Suma dobânzii aplicate
     */
    private BigDecimal applyDailyInterest(Account account, LocalDate date) {
        // Calculează dobânda pentru o zi
        BigDecimal dailyInterest = calculateInterestForAccount(account, 1);

        // Aplică dobânda doar dacă e mai mare de MIN_INTEREST_TO_APPLY
        if (dailyInterest.compareTo(MIN_INTEREST_TO_APPLY) > 0) {
            // Adaugă dobânda în cont (în MDL)
            account.deposit(dailyInterest, Currency.MDL);

            // Salvează contul actualizat
            accountRepository.save(account);

            return dailyInterest;
        }

        return BigDecimal.ZERO;
    }

    /**
     * Aplică dobânda la toate conturile active (pentru ziua curentă)
     */
    public void applyInterestToAllAccounts() {
        List<Account> activeAccounts = accountRepository.findActiveAccounts();
        LocalDate today = LocalDate.now();

        System.out.println("\n🏦 APLICARE DOBÂNZI ZILNICE");
        System.out.println("══════════════════════════════════════════");
        System.out.println("Data: " + today);
        System.out.println("Conturi active: " + activeAccounts.size());
        System.out.println("══════════════════════════════════════════");

        int processedCount = 0;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (Account account : activeAccounts) {
            try {
                BigDecimal interest = applyDailyInterest(account, today);

                if (interest.compareTo(BigDecimal.ZERO) > 0) {
                    processedCount++;
                    totalInterest = totalInterest.add(interest);

                    // Înregistrează tranzacția de dobândă
                    recordInterestTransaction(account, interest);
                }
            } catch (Exception e) {
                System.err.println("❌ Eroare la aplicarea dobânzii pentru contul " +
                        account.getAccountNumber() + ": " + e.getMessage());
            }
        }

        System.out.println("══════════════════════════════════════════");
        System.out.println("📊 REZUMAT:");
        System.out.println("   Conturi procesate: " + processedCount + " / " + activeAccounts.size());
        System.out.println("   Dobânzi distribuite: " + totalInterest.setScale(2, RoundingMode.HALF_UP) + " MDL");
        System.out.println("══════════════════════════════════════════");
    }

    /**
     * Aplică dobânda pentru o perioadă specificată
     *
     * @param startDate Data de început
     * @param endDate Data de sfârșit
     */
    public void applyInterestForPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Datele nu pot fi null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data de început nu poate fi după data de sfârșit");
        }

        int days = (int) ChronoUnit.DAYS.between(startDate, endDate);

        if (days <= 0) {
            throw new IllegalArgumentException("Perioada trebuie să fie pozitivă");
        }

        List<Account> activeAccounts = accountRepository.findActiveAccounts();

        System.out.println("\n🏦 APLICARE DOBÂNZI PENTRU PERIOADĂ");
        System.out.println("══════════════════════════════════════════");
        System.out.println("Perioada: " + startDate + " - " + endDate + " (" + days + " zile)");
        System.out.println("══════════════════════════════════════════");

        int processedCount = 0;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (Account account : activeAccounts) {
            try {
                BigDecimal interest = calculateInterestForAccount(account, days);

                if (interest.compareTo(MIN_INTEREST_TO_APPLY) > 0) {
                    // Aplică dobânda
                    account.deposit(interest, Currency.MDL);
                    accountRepository.save(account);

                    // Înregistrează tranzacția
                    recordInterestTransaction(account, interest);

                    processedCount++;
                    totalInterest = totalInterest.add(interest);

                    System.out.printf("   ✅ Cont %s: %s MDL%n",
                            account.getAccountNumber(),
                            interest.setScale(2, RoundingMode.HALF_UP));
                }
            } catch (Exception e) {
                System.err.println("❌ Eroare la contul " + account.getAccountNumber() + ": " + e.getMessage());
            }
        }

        System.out.println("══════════════════════════════════════════");
        System.out.printf("📊 Rezumat: %d conturi procesate, %s MDL total%n",
                processedCount, totalInterest.setScale(2, RoundingMode.HALF_UP));
        System.out.println("══════════════════════════════════════════");
    }

    // ===============================
    // METODE DE ÎNREGISTRARE TRANZACȚII
    // ===============================

    /**
     * Înregistrează o tranzacție de dobândă - IMPLEMENTARE COMPLETĂ
     *
     * @param account Contul care primește dobânda
     * @param interest Suma dobânzii
     */
    private void recordInterestTransaction(Account account, BigDecimal interest) {
        try {
            // Validare input
            if (account == null) {
                throw new IllegalArgumentException("Contul nu poate fi null");
            }

            if (interest == null || interest.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Dobânda trebuie să fie pozitivă");
            }

            // Creează descrierea detaliată
            String description = String.format(
                    "Dobândă aplicată pe contul %s (%s). Tip cont: %s. Rata anuală: %.1f%%",
                    account.getAccountNumber(),
                    account.getOwner().getFullName(),
                    account.getAccountType(),
                    getInterestRateForAccountType(account.getAccountType())
            );

            // Creează obiectul Transaction complet
            Transaction interestTransaction = new Transaction(
                    Transaction.TransactionType.INTEREST,
                    interest,
                    Currency.MDL, // Dobânda se aplică în MDL
                    description
            );

            // Setează informațiile suplimentare
            interestTransaction.setSourceAccountNumber(null); // Banca este sursa
            interestTransaction.setTargetAccountNumber(account.getAccountNumber());
            interestTransaction.markAsCompleted();

            // Salvează tranzacția în repository
            Transaction savedTransaction = transactionRepository.save(interestTransaction);

            // Afișează confirmarea
            printFormattedTransactionDetails(account, interest, savedTransaction);

        } catch (Exception e) {
            // Logare eroare
            System.err.printf("❌ Eroare la înregistrarea dobânzii pentru contul %s: %s%n",
                    account.getAccountNumber(),
                    e.getMessage());

            // Aruncă excepția mai departe
            throw new BankingException(
                    BankingErrorCode.TRANSACTION_FAILED,
                    "Eroare la înregistrarea tranzacției de dobândă: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Afișează detalii formateate ale tranzacției de dobândă
     */
    private void printFormattedTransactionDetails(Account account,
                                                  BigDecimal interest,
                                                  Transaction transaction) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));

        System.out.println("   ╔══════════════════════════════════════════╗");
        System.out.printf("    ║  📈 TRANZACȚIE DOBÂNDĂ APLICATĂ          ║%n");
        System.out.println("   ╠══════════════════════════════════════════╣");
        System.out.printf("    ║  Cont: %-32s ║%n", account.getAccountNumber());
        System.out.printf("    ║  Proprietar: %-27s ║%n", account.getOwner().getFullName());
        System.out.printf("    ║  Dobândă: %8.2f %-22s ║%n",
                interest.doubleValue(), "MDL");
        System.out.printf("    ║  Tip cont: %-30s ║%n", account.getAccountType());
        System.out.printf("    ║  ID Tranzacție: %-25s ║%n", transaction.getTransactionId());
        System.out.printf("    ║  Data: %-32s ║%n", timestamp);
        System.out.println("   ╚══════════════════════════════════════════╝");
    }

    /**
     * Variantă simplificată pentru debugging/testing
     */
    private void recordInterestTransactionSimple(Account account, BigDecimal interest) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        System.out.printf("   📈 %s | Cont: %s | Dobândă: %6.2f MDL | Proprietar: %s%n",
                timestamp,
                account.getAccountNumber(),
                interest.doubleValue(),
                account.getOwner().getFullName());
    }

    // ===============================
    // RAPOARTE ȘI STATISTICI
    // ===============================

    /**
     * Calculează dobânda totală care va fi plătită pentru toate conturile
     *
     * @param days Numărul de zile pentru proiecție
     * @return Dobânda totală proiectată
     */
    public BigDecimal calculateTotalInterestProjection(int days) {
        List<Account> activeAccounts = accountRepository.findActiveAccounts();
        BigDecimal total = BigDecimal.ZERO;

        for (Account account : activeAccounts) {
            BigDecimal interest = calculateInterestForAccount(account, days);
            total = total.add(interest);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Generează raport de dobânzi pentru toate conturile
     */
    public void generateInterestReport() {
        List<Account> accounts = accountRepository.findAll();

        System.out.println("\n📊 RAPORT DOBÂNZI - TOATE CONTURILE");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.printf("%-20s %-25s %-12s %-15s %-15s%n",
                "Număr Cont", "Proprietar", "Tip", "Sold MDL", "Dobândă/Lună");
        System.out.println("═══════════════════════════════════════════════════════════════");

        BigDecimal monthlyTotal = BigDecimal.ZERO;
        BigDecimal yearlyTotal = BigDecimal.ZERO;
        int earningInterestCount = 0;

        for (Account account : accounts) {
            BigDecimal balanceMDL = account.getBalance(Currency.MDL);
            BigDecimal monthlyInterest = calculateInterestForAccount(account, 30);
            BigDecimal yearlyInterest = calculateInterestForAccount(account, 365);

            if (monthlyInterest.compareTo(BigDecimal.ZERO) > 0) {
                earningInterestCount++;
                System.out.printf("%-20s %-25s %-12s %-15.2f %-15.2f%n",
                        account.getAccountNumber(),
                        account.getOwner().getFullName(),
                        account.getAccountType(),
                        balanceMDL.doubleValue(),
                        monthlyInterest.doubleValue());

                monthlyTotal = monthlyTotal.add(monthlyInterest);
                yearlyTotal = yearlyTotal.add(yearlyInterest);
            }
        }

        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("📈 STATISTICI:");
        System.out.println("   Conturi care primesc dobândă: " + earningInterestCount + " / " + accounts.size());
        System.out.println("   Dobândă lunară totală: " + monthlyTotal.setScale(2, RoundingMode.HALF_UP) + " MDL");
        System.out.println("   Dobândă anuală totală: " + yearlyTotal.setScale(2, RoundingMode.HALF_UP) + " MDL");
        System.out.println("═══════════════════════════════════════════════════════════════");
    }

    /**
     * Găsește conturile care primesc cea mai mare dobândă
     *
     * @param limit Numărul maxim de conturi de returnat
     * @return Lista conturilor sortate după dobânda primită
     */
    public List<Account> getTopInterestEarners(int limit) {
        List<Account> activeAccounts = accountRepository.findActiveAccounts();

        // Sortează după sold (dobânda este proporțională cu soldul)
        activeAccounts.sort((a1, a2) ->
                a2.getTotalBalanceInMDL().compareTo(a1.getTotalBalanceInMDL()));

        return activeAccounts.stream()
                .limit(Math.min(limit, activeAccounts.size()))
                .toList();
    }

    /**
     * Afișează topul investitorilor (cei care primesc cea mai mare dobândă)
     *
     * @param topN Numărul de conturi de afișat
     */
    public void displayTopInterestEarners(int topN) {
        List<Account> topEarners = getTopInterestEarners(topN);

        System.out.println("\n🏆 TOP " + topN + " INVESTITORI (DUPĂ DOBÂNDĂ)");
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.printf("%-3s %-20s %-25s %-15s %-15s%n",
                "#", "Cont", "Proprietar", "Sold MDL", "Dobândă/Lună");
        System.out.println("══════════════════════════════════════════════════════════");

        int rank = 1;
        for (Account account : topEarners) {
            BigDecimal balanceMDL = account.getTotalBalanceInMDL();
            BigDecimal monthlyInterest = calculateInterestForAccount(account, 30);

            System.out.printf("%-3d %-20s %-25s %-15.2f %-15.2f%n",
                    rank++,
                    account.getAccountNumber(),
                    account.getOwner().getFullName(),
                    balanceMDL.doubleValue(),
                    monthlyInterest.doubleValue());
        }
        System.out.println("══════════════════════════════════════════════════════════");
    }

    /**
     * Afișează ratele curente ale dobânzilor
     */
    public void displayInterestRates() {
        System.out.println("\n📈 RATELE DOBÂNZILOR ACTUALE");
        System.out.println("══════════════════════════════════════════");
        System.out.printf("%-15s %-20s %-20s%n", "Tip Cont", "Dobândă Anuală", "Dobândă Lunară*");
        System.out.println("══════════════════════════════════════════");
        System.out.printf("%-15s %-20.2f%% %-20.2f%%%n",
                "CURRENT", CURRENT_ACCOUNT_RATE, CURRENT_ACCOUNT_RATE / 12);
        System.out.printf("%-15s %-20.2f%% %-20.2f%%%n",
                "SAVINGS", SAVINGS_ACCOUNT_RATE, SAVINGS_ACCOUNT_RATE / 12);
        System.out.printf("%-15s %-20.2f%% %-20.2f%%%n",
                "BUSINESS", BUSINESS_ACCOUNT_RATE, BUSINESS_ACCOUNT_RATE / 12);
        System.out.println("══════════════════════════════════════════");
        System.out.println("💡 *Dobânda lunară calculată pentru solduri ≥ 100 MDL");
        System.out.println("⚠️  Dobânzile se aplică doar pe conturile active");
        System.out.println("══════════════════════════════════════════");
    }

    // ===============================
    // UTILITARE
    // ===============================

    /**
     * Verifică dacă un cont primește dobândă
     *
     * @param account Contul de verificat
     * @return true dacă contul primește dobândă, false altfel
     */
    public boolean isEarningInterest(Account account) {
        if (account == null || !account.isActive()) {
            return false;
        }

        BigDecimal balance = account.getTotalBalanceInMDL();
        return balance.compareTo(MIN_BALANCE_FOR_INTEREST) >= 0;
    }

    /**
     * Calculează cât timp e nevoie pentru a ajunge la o anumită sumă prin dobândă
     *
     * @param account Contul pentru calcul
     * @param targetAmount Suma țintă
     * @param annualRate Rata anuală a dobânzii (opțional, dacă null se folosește rata contului)
     * @return Numărul de zile necesare, sau -1 dacă nu se poate calcula
     */
    public int calculateTimeToTarget(Account account, BigDecimal targetAmount, Double annualRate) {
        if (account == null || targetAmount == null) {
            return -1;
        }

        BigDecimal currentBalance = account.getTotalBalanceInMDL();

        if (currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return -1; // Nu se poate calcula
        }

        BigDecimal neededInterest = targetAmount.subtract(currentBalance);

        if (neededInterest.compareTo(BigDecimal.ZERO) <= 0) {
            return 0; // Deja la țintă
        }

        // Folosește rata specificată sau rata contului
        double rateToUse = (annualRate != null) ? annualRate :
                getInterestRateForAccountType(account.getAccountType());

        BigDecimal dailyInterest = calculateInterest(account, 1, rateToUse);

        if (dailyInterest.compareTo(BigDecimal.ZERO) <= 0) {
            return -1; // Nu primește dobândă
        }

        // Zile necesare = dobânda necesară / dobânda zilnică
        BigDecimal daysNeeded = neededInterest.divide(dailyInterest, 0, RoundingMode.UP);

        return daysNeeded.intValue();
    }

    /**
     * Aplică dobânda pentru un cont specific (pentru test/manual)
     *
     * @param accountNumber Numărul contului
     */
    public void applyInterestToSingleAccount(String accountNumber) {
        try {
            Account account = accountService.findActiveAccount(accountNumber);
            BigDecimal interest = calculateInterestForAccount(account, 30); // Pentru o lună

            if (interest.compareTo(MIN_INTEREST_TO_APPLY) > 0) {
                // Aplică dobânda
                account.deposit(interest, Currency.MDL);
                accountRepository.save(account);

                // Înregistrează tranzacția
                recordInterestTransaction(account, interest);

                System.out.printf("✅ Dobândă aplicată cu succes pentru contul %s%n", accountNumber);
                System.out.printf("   Suma dobânzii: %.2f MDL%n", interest.doubleValue());
            } else {
                System.out.printf("⚠️  Dobânda este prea mică pentru aplicare (< %.2f MDL)%n",
                        MIN_INTEREST_TO_APPLY.doubleValue());
            }

        } catch (Exception e) {
            System.err.printf("❌ Eroare la aplicarea dobânzii: %s%n", e.getMessage());
        }
    }

    /**
     * Calculează dobânda pentru o perioadă specifică (în zile)
     *
     * @param account Contul pentru calcul
     * @param days Numărul de zile
     * @return Suma dobânzii
     */
    public BigDecimal calculateInterestForPeriod(Account account, int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Numărul de zile trebuie să fie pozitiv");
        }

        return calculateInterestForAccount(account, days);
    }

    /**
     * Obține rata de dobândă pentru un tip de cont
     *
     * @param accountType Tipul contului
     * @return Rata anuală a dobânzii
     */
    public double getInterestRate(String accountType) {
        return getInterestRateForAccountType(accountType);
    }
}