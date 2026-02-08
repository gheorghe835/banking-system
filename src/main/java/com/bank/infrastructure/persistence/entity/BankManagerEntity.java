package com.bank.infrastructure.persistence.entity;

/**
 * Entitate JPA pentru manageri bancari
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bank_managers")
public class BankManagerEntity {
    @Id
    @Column(name = "employee_id",length = 20,nullable = false,unique = true)
    private String employeeId;
    @Column(name = "username",length = 50,nullable = false,unique = true)
    private String username;
    @Column(name = "password_hash",length = 100,nullable = false)
    private String passwordHash;
    @Column(name = "first_name",length = 50,nullable = false)
    private String firstName;
    @Column(name = "last_name",length = 50,nullable = false)
    private String lastName;
    @Column(name = "email",length = 100,nullable = false,unique = true)
    private String email;
    @Column(name = "department",length = 50)
    private String department;
    @Column(name = "is_active",nullable = false)
    private boolean active = true;
    @Column(name = "access_level",length = 20,nullable = false)
    private String accessLevel;// "JUNIOR", "SENIOR", "ADMIN", "SUPER_ADMIN"

    //constructor implicit(JPA)
    public BankManagerEntity(){}

    //constructori cu parametri
    public BankManagerEntity(String employeeId,String username,String passwordHash,
                             String firstName,String lastName,String email,
                             String accessLevel){
        this.employeeId = employeeId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.accessLevel = accessLevel;
    }

    //getteri si setteri

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    //metode utilitare
    public String getFullName(){
        return firstName + " " + lastName;
    }

    public boolean hasAdminAccess(){
        return "ADMIN".equals(accessLevel) || "SUPER_ADMIN".equals(accessLevel);
    }

    public boolean hasFullAccess(){
        return "SUPER_ADMIN".equals(accessLevel);
    }

    @Override
    public String toString(){
        return String.format("BankManagerEntity[id=%s, name=%s %s, username=%s, level=%s, active=%s]",
                employeeId,firstName,lastName,username,accessLevel,active);
    }
}


