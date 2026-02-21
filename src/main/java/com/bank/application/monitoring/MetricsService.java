package com.bank.application.monitoring;

import com.bank.domain.repository.AccountRepository;
import com.bank.domain.repository.TransactionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // Contoare pentru diverse metrici
    private final Counter transactionCounter;
    private final Counter loginCounter;
    private final Counter accountCreationCounter;
    private final AtomicLong totalBalanceGauge;

    public MetricsService(MeterRegistry meterRegistry,
                          AccountRepository accountRepository,
                          TransactionRepository transactionRepository) {
        this.meterRegistry = meterRegistry;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;

        // Inițializare contoare
        this.transactionCounter = Counter.builder("bank.transactions.total")
                .description("Numărul total de tranzacții")
                .register(meterRegistry);

        this.loginCounter = Counter.builder("bank.logins.total")
                .description("Numărul total de autentificări")
                .register(meterRegistry);

        this.accountCreationCounter = Counter.builder("bank.accounts.created")
                .description("Numărul de conturi create")
                .register(meterRegistry);

        // Gauge pentru soldul total (se actualizează la fiecare citire)
        this.totalBalanceGauge = meterRegistry.gauge("bank.balance.total",
                new AtomicLong(0), AtomicLong::get);

        // Inițializare gauge cu valoarea din DB
        updateTotalBalanceGauge();
    }

    // Metode pentru incrementarea contoarelor
    public void incrementTransactionCount() {
        transactionCounter.increment();
    }

    public void incrementLoginCount() {
        loginCounter.increment();
    }

    public void incrementAccountCreationCount() {
        accountCreationCounter.increment();
    }

    // Actualizare gauge pentru sold total
    public void updateTotalBalanceGauge() {
        BigDecimal total = accountRepository.getTotalBalanceInMDL();
        totalBalanceGauge.set(total.longValue());
    }

    // Timer pentru măsurarea duratei operațiilor
    public void recordTransactionTime(long duration, TimeUnit unit) {
        Timer.builder("bank.transaction.duration")
                .description("Durata procesării tranzacțiilor")
                .register(meterRegistry)
                .record(duration, unit);
    }

    // Metrici pentru tranzacții pe zi
    public void recordDailyTransactionCount() {
        long todayCount = transactionRepository.countByDate(LocalDate.now());
        meterRegistry.gauge("bank.transactions.daily", todayCount);
    }
}
