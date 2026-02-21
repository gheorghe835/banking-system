package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.bank.infrastructure.persistence.repository")  // ← FORȚEAZĂ!
public class BankingApplication {
    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(BankingApplication.class);
        app.setAdditionalProfiles("console");  // ← FORȚEAZĂ PROFILUL!
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
