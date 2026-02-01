package com.bank.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Clasa care reprezinta un client al bancii
 * Contine informatii personale si de contact
 */

public class Customer {
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate birthDate;
    private String address;
    private String identityNumber;
    private LocalDateTime registrationDate;
    private boolean isActive;

    //constructori
    public Customer() {
        this.registrationDate = LocalDateTime.now();
        this.isActive = true;
    }
    public Customer(String firstName,
                    String lastName,
                    String email,
                    String phoneNumber,
                    LocalDate birthDate,
                    String identityNumber){
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.identityNumber = identityNumber;
        generateCustomerId();
    }

    //metoda de generare Id client
    private void generateCustomerId(){
        this.customerId = "CUST" + System.currentTimeMillis()%1000;
    }

    //getteri si setteri
    public String getCustomerId(){return customerId;}
    public void setCustomerId(String customerId){this.customerId = customerId;}
    public String getFirstName(){return firstName;}
    public void setFirstName(String firstName){
        if (firstName == null || firstName.trim().isEmpty()){
            throw new IllegalArgumentException("Prenumele nu poate fi gol.");
        }
        this.firstName = firstName.trim();
    }
    public String getLastName(){return lastName;}
    public void setLastName(String lastName){
        if (lastName == null || lastName.trim().isEmpty()){
            throw new IllegalArgumentException("Numele nu poate fi gol.");
        }
        this.lastName = lastName.trim();
    }
    public String getFullName(){return firstName + " " + lastName;}
    public String getEmail(){return email;}
    public void setEmail(String email){
        if (!isValidEmail(email)){
            throw new IllegalArgumentException("Email invalid.");
        }
        this.email = email;
    }
    public String getPhoneNumber(){return phoneNumber;}
    public void setPhoneNumber(String phoneNumber){
        if (!isValidPhoneNumber(phoneNumber)){
            throw new IllegalArgumentException("Numar de telfon invalid.");
        }
        this.phoneNumber = phoneNumber;
    }
    public LocalDate getBirthDate(){return birthDate;}
    public void setBirthDate(LocalDate birthDate){
        if (birthDate == null || birthDate.isAfter(LocalDate.now().minusYears(18))){
            throw new IllegalArgumentException("Clientul trebuie sa aiba 18 ani.");
        }
        this.birthDate = birthDate;
    }
    public String getAddress(){return address;}
    public void setAddress(String address){this.address = address;}
    public String getIdentityNumber(){return identityNumber;}
    public void setIdentityNumber(){
        if (identityNumber == null || identityNumber.trim().length() < 13 || identityNumber.trim().length() > 13){
            throw new IllegalArgumentException("Numarul de identitate invalid.");
        }
        this.identityNumber = identityNumber.trim();
    }
    public LocalDateTime getRegistrationDate(){return registrationDate;}
    public boolean isActive(){return isActive;}
    public void setActive(boolean active){isActive = active;}
    public void deactivate(){this.isActive = false;}
    public void activate(){this.isActive = true;}

    //metode de validare
    private boolean isValidEmail(String email){
        return email != null && email.matches("^[A-Za-z0-9+_.]+@(.+)$");
    }
    private boolean isValidPhoneNumber(String phoneNumber){
        return phoneNumber != null && phoneNumber.matches("^[+]?[0-9]{10,15}$");
    }

    //metode de buisiness logic
    public int getAge(){
        if (birthDate == null)return 0;
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    //override metode
    @Override
    public boolean equals(Object o){
        if (this == o)return true;
        if (this == null || getClass() != o.getClass())return false;
        Customer customer =(Customer) o;
        return Objects.equals(customerId,customer.customerId)&&
                Objects.equals(identityNumber,customer.identityNumber);
    }

    @Override
    public int hashCode(){
        return Objects.hash(customerId,identityNumber);
    }

    @Override
    public String toString(){
        return String.format("Customer[ID=%s %s, Email=%s, Active=%s]",
                customerId,firstName,lastName,email,isActive);
    }
}

