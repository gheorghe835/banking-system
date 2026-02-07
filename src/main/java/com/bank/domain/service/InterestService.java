package com.bank.domain.service;

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
 * Serviciu pentru calculul si aplicarea dobinzilor bancare
 */

public class InterestService {
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    //Rate de dobinda standard(in procente pe an)
    private static final double SAVINGS_ACCOUNT_RATE = 3.5; //3.5% pentru conturi de economii
    private static final double CURRENT_ACCOUNT_RATE = 0.5; //0.5% pentru conturi curente
    private static final double BUSINESS_ACCOUNT_RATE = 1.5; //1.5% pentru conturi buisiness

    //minimum balance pentru a primi dobinda
    private static final BigDecimal MIN_BALANCE_FOR_INTEREST = BigDecimal.valueOf(100.0);

    //dobinda minima pentru a fi aplicata(pentru a evita tranzactii prea mici)
    private static final BigDecimal MIN_INTEREST_TO_APPLY = BigDecimal.valueOf(0.01);

    //constructori
    public InterestService(AccountRepository accountRepository,
                           TransactionRepository transactionRepository,
                           AccountService accountService){
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }


    //Calcul dobinzi
    /**
     * calculeaza dobinda pentru un cont pentru o perioada specificata
     */
    public BigDecimal calculateInterest(Account account,int days,double annualRate){
        //validare input
        if (account == null){
            throw new IllegalArgumentException("Contul nu poate fi null");
        }

        if (days <= 0){
            throw new IllegalArgumentException("Numarul de zile trebiue sa fie pozitiv");
        }

        if (annualRate < 0){
            throw new IllegalArgumentException("Rata dobinzii nu poate fi negativa");
        }

        //contul inactiv nu primeste dobinda
        if (!account.isActive()){
            return BigDecimal.ZERO;
        }

        //calculeaza soldul total in MDL
        BigDecimal totalBalanceInMDL = account.getTotalBalancesInMDL();

        //verifica daca soldul este suficient pentru dobinda
        if (totalBalanceInMDL.compareTo(MIN_BALANCE_FOR_INTEREST) < 0){
            return BigDecimal.ZERO;
        }

        //formula dabinzii simple: dobinda = principal * rata * timp
        //rata zilnica = annualRate / 365/ 100
        BigDecimal dailyRate = BigDecimal.valueOf(annualRate)
                .divide(BigDecimal.valueOf(36500),10,RoundingMode.HALF_UP);

        BigDecimal interest = totalBalanceInMDL
                .multiply(dailyRate)
                .multiply(BigDecimal.valueOf(days));

        //rotungeste la doua zecimale
        return interest.setScale(2,RoundingMode.HALF_UP);
    }

    /**
     * calculeaza dobinda pentru un cont folosind rata potrivita tipului de cont
     */
    public BigDecimal calculateInterestForAccount(Account account,int days){
        double annualRate = getInterestRateForAccountType(account.getAccountType());
        return calculateInterest(account,days,annualRate);
    }

    /**
     * returneaza rata de dobinda in functie de tipul contului
     */
    private double getInterestRateForAccountType(String accountType){
        if (accountType == null){
            return CURRENT_ACCOUNT_RATE;
        }

        return switch (accountType.toUpperCase()){
            case Account.ACCOUNT_TYPE_SAVINGS -> SAVINGS_ACCOUNT_RATE;
            case Account.ACCOUNT_TYPE_BUISINESS -> BUSINESS_ACCOUNT_RATE;
            case Account.ACCOUNT_TYPE_CURRENT -> CURRENT_ACCOUNT_RATE;
            default -> CURRENT_ACCOUNT_RATE;  //default
        };
    }

    //calculeaza dobinda pentru o perioada specificata
    private BigDecimal calculateInterestForPeriod(Account account,LocalDate startDate,LocalDate endDate){
        if (startDate == null || endDate == null){
            throw new IllegalArgumentException("Datele nu pot fi nulle");
        }

        if (startDate.isAfter(endDate)){
            throw new IllegalArgumentException("Data de inceput nu poate fi dupa data de sfirsit");
        }

        long days = ChronoUnit.DAYS.between(startDate,endDate);
        return calculateInterestForAccount(account,(int) days);
    }

    //Aplicare dobinzi

    /**
     * aplica dobinda la toate conturile active
     */
    public void applyInterestToAllAccounts(){
        List<Account> activeAccounts = accountRepository.findActiveAccounts();
        LocalDate today = LocalDate.now();

        System.out.println("\n🏦 APLICARE DOBINZI ZILNICE");
        System.out.println("══════════════════════════════════════════");
        System.out.println("Data: " + today);
        System.out.println("Conturi active: " + activeAccounts.size());
        System.out.println("══════════════════════════════════════════");

        int processedCount = 0;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (Account account : activeAccounts){
            try {
                BigDecimal interest = applyDailyInterest(account,today);

                if (interest.compareTo(BigDecimal.ZERO) > 0){
                    processedCount ++;
                    totalInterest = totalInterest.add(interest);

                    //inregistreaza tranzactio de dobinda
                    recordInterestTransaction(account,interest);
                }
            }
            catch (Exception e){
                System.err.println("❌ Eroare la aplicarea dobânzii pentru contul " +
                        account.getAccountNumber() + ": " + e.getMessage());
            }
        }
        System.out.println("══════════════════════════════════════════");
        System.out.println("📊 REZUMAT:");
        System.out.println("   Conturi procesate: " + processedCount + " / " + activeAccounts.size());
        System.out.println("   Dobinzi distribuite: " + totalInterest.setScale(2, RoundingMode.HALF_UP) + " MDL");
        System.out.println("══════════════════════════════════════════");

    }

    /**
     * aplica dobinda zilnica pentru un cont
     */
    private BigDecimal applyDailyInterest(Account account,LocalDate date){
        //calculeaza dobinda pentru o zi
        BigDecimal dailyInterest = calculateInterestForAccount(account,1);

        //aplica dobinda doar daca e mai mare de 0.01
        if(dailyInterest.compareTo(MIN_INTEREST_TO_APPLY) > 0){
            //adauga dobinda in cont(in MDL)
            account.deposit(dailyInterest,Currency.MDL);

            //salveaza contul actualizat
            accountRepository.save(account);

            return dailyInterest;
        }
        return BigDecimal.ZERO;
    }

    /**
     * aplica dobinda pentru o perioada specificata
     */
    public void applyInterestForPeriod(LocalDate startDate,LocalDate endDate){
        if (startDate == null || endDate == null){
            throw new IllegalArgumentException("Datele nu pot fi nulle");
        }
        if (startDate.isAfter(endDate)){
            throw new IllegalArgumentException("Data de inceput nu poate fi dupa data de sfirsit");
        }

        int days = (int) ChronoUnit.DAYS.between(startDate,endDate);

        if (days <= 0){
            throw new IllegalArgumentException("Perioada trebuie sa fie pozitiva");
        }
        List<Account> activeAccounts = accountRepository.findActiveAccounts();

        System.out.println("\n🏦 APLICARE DOBINZI PENTRU PERIOADA");
        System.out.println("══════════════════════════════════════════");
        System.out.println("Perioada: " + startDate + " - " + endDate + " (" + days + " zile)");
        System.out.println("══════════════════════════════════════════");

        int processedCount = 0;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (Account account : activeAccounts){
            try {
                BigDecimal interest = calculateInterestForAccount(account,days);

                if (interest.compareTo(MIN_INTEREST_TO_APPLY) > 0){
                    //aplica dobinda
                    account.deposit(interest,Currency.MDL);
                    accountRepository.save(account);

                    //inregistreaza tranzactia
                    recordInterestTransaction(account,interest);

                    processedCount++;
                    totalInterest = totalInterest.add(interest);

                    System.out.printf("   ✅ Cont %s: %s MDL%n",
                            account.getAccountNumber(),
                            interest.setScale(2, RoundingMode.HALF_UP));
                }
            }
            catch (Exception e){
                System.err.println("❌ Eroare la contul " + account.getAccountNumber() + ": " + e.getMessage());
            }
        }
        System.out.println("══════════════════════════════════════════");
        System.out.printf("📊 Rezumat: %d conturi procesate, %s MDL total%n",
                processedCount, totalInterest.setScale(2, RoundingMode.HALF_UP));
        System.out.println("══════════════════════════════════════════");

    }

    //Inregistreaza tranzactii
    /**
     * inregistreaza o tranzactie de dobinda
     */
    private void recordInterestTransaction(Account account,BigDecimal interest){
       try {
           //validare input
           if (account == null){
               throw new IllegalArgumentException("Contul nu poate fi null");
           }
           if (interest == null || interest.compareTo(BigDecimal.ZERO) <= 0){
               throw new IllegalArgumentException("Dobinda trebuie sa fie pozitiva");
           }

           //creaza descriera detaliata
           String description = String.format(
                   "Dobinda aplicata pe contul %s(%s). Tip cont: %s. Rata anuala: %.1f%%",
                   account.getAccountNumber(),
                   account.getOwner().getFullName(),
                   account.getAccountType(),
                   getInterestRateForAccountType(account.getAccountType())
           );

           //creaza obiectul Transaction complet
           Transaction interestTransaction = new Transaction(
                   Transaction.TransactionType.INTEREST,
                   interest,Currency.MDL,
                   description
           );

           //seteaza informatiile suplimentare
           interestTransaction.setSourceAccountNumber(null);//banca este sursa
           interestTransaction.setTargetAccountNumber(account.getAccountNumber());
           interestTransaction.markASCompleted();

           //salveaza tranzactia in repository
           Transaction savedTransaction = null;
           if (transactionRepository != null){
               savedTransaction = transactionRepository.save(interestTransaction);
           }

           //afiseaza confirmarea
           printFormattedTransactionDetails(account,interest,savedTransaction);
       }
       catch (Exception e){
           //logare eroare
           System.err.printf("❌ Eroare la înregistrarea dobinzii pentru contul %s: %s%n",
                   account.getAccountNumber(),
                   e.getMessage());
       }
    }

    /**
     * afiseaza detalii formatate ale tranzactiei de dobinda
     */
    private void printFormattedTransactionDetails(Account account,
                                                  BigDecimal interest,
                                                  Transaction transaction) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));

        System.out.println("   ╔══════════════════════════════════════════╗");
        System.out.printf ("   ║  📈 TRANZACȚIE DOBINDA APLICATA          ║%n");
        System.out.println("   ╠══════════════════════════════════════════╣");
        System.out.printf ("   ║  Cont: %-32s ║%n", account.getAccountNumber());
        System.out.printf ("   ║  Proprietar: %-27s ║%n", account.getOwner().getFullName());
        System.out.printf ("   ║  Dobinda: %8.2f %-22s ║%n",
                interest.doubleValue(), "MDL");
        System.out.printf ("   ║  Tip cont: %-30s ║%n", account.getAccountType());
        System.out.printf ("   ║  ID Tranzactie: %-25s ║%n", transaction.getTransactionId());
        System.out.printf ("   ║  Data: %-32s ║%n", timestamp);
        System.out.println("   ╚══════════════════════════════════════════╝");
    }


    // ===== RAPOARTE ȘI STATISTICI =====

    /**
     * Calculeaza dobinda totala care va fi platita
     */
    public BigDecimal calculateTotalInterestProjection(int days) {
        List<Account> activeAccounts = accountRepository.findActiveAccounts();
        BigDecimal total = BigDecimal.ZERO;

        for (Account account : activeAccounts) {
            BigDecimal interest = calculateInterestForAccount(account, days);
            total = total.add(interest);
        }

        return total.setScale(2,RoundingMode.HALF_UP);
    }

    /**
     * Genereaza raport de dobinzi
     */
    public void generateInterestReport() {
        List<Account> accounts = accountRepository.findAll();

        System.out.println("\n📊 RAPORT DOBINZI - TOATE CONTURILE");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.printf("%-20s %-25s %-12s %-15s %-15s%n",
                "Numar Cont", "Proprietar", "Tip", "Sold MDL", "Dobinda/Luna");
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
        System.out.println("   Conturi care primesc dobinda: " + earningInterestCount + " / " + accounts.size());
        System.out.println("   Dobândă lunara totala: " + monthlyTotal.setScale(2, RoundingMode.HALF_UP) + " MDL");
        System.out.println("   Dobândă anuala totala: " + yearlyTotal.setScale(2, RoundingMode.HALF_UP) + " MDL");
        System.out.println("═══════════════════════════════════════════════════════════════");

    }

    /**
     * gasește conturile care primesc cea mai mare dobinda
     */
    public List<Account> getTopInterestEarners(int limit) {
        List<Account> activeAccounts = accountRepository.findActiveAccounts();

        //sorteaza dupa sold (pentru ca dobinda e proporționala cu soldul)
        activeAccounts.sort((a1, a2) ->
                a2.getTotalBalancesInMDL().compareTo(a1.getTotalBalancesInMDL()));

        return activeAccounts.stream()
                .limit(limit)
                .toList();
    }

    //afiseaza topul investitiilor(cei care primesc cea mai mare dobinda)
    public void displayTopInterestEarners(int topN) {
        List<Account> topEarners = getTopInterestEarners(topN);

        System.out.println("\n🏆 TOP " + topN + " INVESTITORI (DUPA DOBINDA)");
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.printf("%-3s %-20s %-25s %-15s %-15s%n",
                "#", "Cont", "Proprietar", "Sold MDL", "Dobinda/Luna");
        System.out.println("══════════════════════════════════════════════════════════");

        int rank = 1;
        for (Account account : topEarners) {
            BigDecimal balanceMDL = account.getTotalBalancesInMDL();
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
    //afiseaza ratele curente ale dobinzilor
    public void displayInterestRates() {
        System.out.println("\n📈 RATELE DOBINZILOR ACTUALE");
        System.out.println("══════════════════════════════════════════");
        System.out.printf("%-15s %-20s %-20s%n", "Tip Cont", "Dobinda Anuala", "Dobinda Lunara*");
        System.out.println("══════════════════════════════════════════");
        System.out.printf("%-15s %-20.2f%% %-20.2f%%%n",
                "CURRENT", CURRENT_ACCOUNT_RATE, CURRENT_ACCOUNT_RATE / 12);
        System.out.printf("%-15s %-20.2f%% %-20.2f%%%n",
                "SAVINGS", SAVINGS_ACCOUNT_RATE, SAVINGS_ACCOUNT_RATE / 12);
        System.out.printf("%-15s %-20.2f%% %-20.2f%%%n",
                "BUSINESS", BUSINESS_ACCOUNT_RATE, BUSINESS_ACCOUNT_RATE / 12);
        System.out.println("══════════════════════════════════════════");
        System.out.println("💡 *Dobinda lunara calculata pentru solduri ≥ 100 MDL");
        System.out.println("⚠️  Dobânzile se aplică doar pe conturile active");
        System.out.println("══════════════════════════════════════════");
    }


    // ===== UTILITARE =====

    /**
     *verifica daca un cont primeste dobinda
     */
    public boolean isEarningInterest(Account account) {
        if (!account.isActive()) return false;

        BigDecimal balance = account.getTotalBalancesInMDL();
        return balance.compareTo(MIN_BALANCE_FOR_INTEREST) >= 0;
    }

    /**
     *calculeaza cit timp e nevoie pentru a ajunge la o anumita suma prin dobinda compusa
     */
    public int calculateTimeToTarget(Account account, BigDecimal targetAmount, double annualRate) {
        BigDecimal currentBalance = account.getTotalBalancesInMDL();

        if (currentBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return -1; // Nu se poate calcula
        }

        // Formula dobinzii compuse: A = P(1 + r/n)^(nt)
        // Pentru simplitate, folosim dobinda simpla
        BigDecimal neededInterest = targetAmount.subtract(currentBalance);

        if (neededInterest.compareTo(BigDecimal.ZERO) <= 0) {
            return 0; // Deja la ținta
        }

        BigDecimal dailyInterest = calculateInterest(account, 1, annualRate);

        if (dailyInterest.compareTo(BigDecimal.ZERO) <= 0) {
            return -1; // Nu primește dobinda
        }

        // Zile necesare = dobinda necesara / dobanda zilnica
        BigDecimal daysNeeded = neededInterest.divide(dailyInterest, 0, RoundingMode.UP);

        return daysNeeded.intValue();
    }
}
