package com.bank.domain.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clasa care reprezintă un manager bancar
 * Are acces la toate funcționalitățile administrative
 */
public class BankManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private String employeeId;
    private String username;
    private String passwordHash; // Parola hash-uită
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private boolean isActive;
    private AccessLevel accessLevel;

    // Niveluri de acces
    public enum AccessLevel {
        JUNIOR,     // Acces limitat
        SENIOR,     // Acces complet pe departament
        ADMIN,      // Acces total sistem
        SUPER_ADMIN // Acces complet + configurare
    }

    // Constructori
    public BankManager() {
        this.isActive = true;
        this.accessLevel = AccessLevel.JUNIOR;
    }

    public BankManager(String username, String firstName, String lastName,
                       String email, AccessLevel accessLevel) {
        this();
        setUsername(username);
        this.firstName = Objects.requireNonNull(firstName, "Prenumele este obligatoriu");
        this.lastName = Objects.requireNonNull(lastName, "Numele este obligatoriu");
        setEmail(email);
        this.accessLevel = accessLevel != null ? accessLevel : AccessLevel.JUNIOR;
        generateEmployeeId();
    }

    // Generare ID angajat
    private void generateEmployeeId() {
        this.employeeId = "MGR" + System.currentTimeMillis() % 10000;
    }

    // Getteri și Setteri
    public String getEmployeeId() {
        return employeeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().length() < 4) {
            throw new IllegalArgumentException("Username-ul trebuie să aibă minim 4 caractere");
        }
        this.username = username.trim().toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email invalid");
        }
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    // Metode de validare
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // Metode de business logic
    public boolean hasAdminAccess() {
        return accessLevel == AccessLevel.ADMIN || accessLevel == AccessLevel.SUPER_ADMIN;
    }

    public boolean hasFullAccess() {
        return accessLevel == AccessLevel.SUPER_ADMIN;
    }

    public boolean canCreateAccounts() {
        return accessLevel == AccessLevel.SENIOR || hasAdminAccess();
    }

    public boolean canDeleteAccounts() {
        return hasAdminAccess();
    }

    public boolean canManageOtherManagers() {
        return accessLevel == AccessLevel.SUPER_ADMIN;
    }

    // Override metode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankManager that = (BankManager) o;
        return Objects.equals(employeeId, that.employeeId) &&
                Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, username);
    }

    @Override
    public String toString() {
        return String.format("BankManager[ID=%s, Name=%s %s, Username=%s, Level=%s, Active=%s]",
                employeeId, firstName, lastName, username, accessLevel, isActive);
    }
}