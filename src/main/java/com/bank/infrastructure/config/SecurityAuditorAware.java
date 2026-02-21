package com.bank.infrastructure.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Clasă pentru a obține utilizatorul curent pentru auditing JPA
 * Folosit cu @CreatedBy și @LastModifiedBy
 */
@Component
public class SecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("SYSTEM");
        }

        String principal = authentication.getName();

        // Verifică dacă este utilizator anonim
        if ("anonymousUser".equals(principal)) {
            return Optional.of("SYSTEM");
        }

        return Optional.of(principal);
    }
}
