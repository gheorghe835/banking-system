package com.bank.application.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public CustomHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            // Verifică conexiunea la baza de date
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            // Verifică dacă există tranzacții recente
            Integer recentTransactions = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM transactions WHERE timestamp > NOW() - INTERVAL 1 HOUR",
                    Integer.class
            );

            return Health.up()
                    .withDetail("database", "Conexiune OK")
                    .withDetail("recent_transactions", recentTransactions)
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "Conexiune eșuată")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
