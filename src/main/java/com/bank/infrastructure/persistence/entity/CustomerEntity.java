package com.bank.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entitate JPA pentru clienti bancari
 * Mapeaza clasa Customer din domain layer
 */

@Entity
@Table(name = "customers")
public class CustomerEntity {
    @Id
    @Column(name = "customer_id",length = 20,nullable = false,unique = true)
    private String customerId;

    @Column(name = "first_name",length = 50,nullable = false)
    private String firstName;
    @Column(name = "last_name",length = 50,nullable = false)
    private String lastName;
    @Column(name = "email",length = 100,nullable = false,unique = true)
    private String email;
    @Column(name = "phone_number",length = 20)
    private String phoneNumber;
    @Column(name = "birt_date")
    private LocalDate birthDate;
    @Column(name = "address",length = 200)
    private String address;
    @Column(name = "identity_number",length = 20,unique = true)
    private String identityNumber;
    @Column(name = "registration_date",nullable = false)
    private LocalDateTime registrationDate;
    @Column(name = "is_active",nullable = false)
    private boolean active = true;

    //relatie OneToMany cu conturile clientului
    @OneToMany(mappedBy = "owner",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<AccountEntity> accounts = new ArrayList<>();

    //constructor implicit(JPA)
    public CustomerEntity(){
        this.registrationDate = LocalDateTime.now();
    }

    //constructori cu parametri
    public CustomerEntity(String customerId,String firstName,String lastName,
                          String email,String phoneNumber,LocalDate birthDate,
                          String identityNumber){
        this();
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.identityNumber = identityNumber;
    }

    //getteri si setteri

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountEntity> acconts) {
        this.accounts = acconts;
    }

    //metode utilitare
    public String getFullName(){
        return firstName + " " + lastName;
    }
    public int getAge(){
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    @Override
    public String toString(){
        return String.format("CustomerEntity[id=%s, name=%s%s, email=%s, active=%s]",
                customerId,firstName,lastName,email,active);
    }
}

