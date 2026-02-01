package  com.bank.domain.model;

import java.util.Objects;

/**
 * clasa care reprezinta un manager bancar
 * are acces la toate functionalitatile administrative
 */

public class BankManager {
    private String employeeId;
    private String username;
    private String passwordHash; // parola hash
    private String firstName;
    private String lastName;
    private String email;
    private String departament;
    private boolean isActive;
    private AccessLevel accessLevel;

    //niveluri de acces
    public enum AccessLevel{
        JUNIOR, //acces limitat
        SENIOR, //acces complet pe departament
        ADMIN, //acces total sistem
        SUPER_ADMIN //acces complet + configurare
    }

    //constructori
    public BankManager(){
        this.isActive = true;
        this.accessLevel = AccessLevel.JUNIOR;
    }
    public BankManager(String username,String firstName,String lastName,String email,AccessLevel accessLevel){
        this();
        setUsername(username);
        this.firstName = Objects.requireNonNull(firstName,"Prenumele este obligatoriu.");
        this.lastName = Objects.requireNonNull(lastName,"Numele este obligatoriu.");
        setEmail(email);
        this.accessLevel = accessLevel != null ? accessLevel : AccessLevel.JUNIOR;
        generateEmployeeId();
    }

    //generare ID angajat
    private void generateEmployeeId(){
        this.employeeId = "MGR" + System.currentTimeMillis()%1000;
    }

    //getteri si setteri
    public String getEmployeeId(){return employeeId;}
    public String getUsername(){return username;}
    public void setUsername(String username){
        if (username == null || username.trim().length() < 4){
            throw new IllegalArgumentException("Username trebuie sa fie minim 4 caractere.");
        }
        this.username = username.trim().toLowerCase();
    }
    public String getPasswordHash(){return  passwordHash;}
    public void setPasswordHash(String passwordHash){this.passwordHash = passwordHash;}
    public String getFirstName(){return  firstName;}
    public void setFirstName(String firstName){this.firstName = firstName;}
    public String getLastName(){return lastName;}
    public void setLastName(String lastName){this.lastName = lastName;}
    public String getFullName(){return firstName + " " + lastName;}
    public String getEmail(){return email;}
    public void setEmail(String email){
        if (!isValidEmail(email)){
            throw new IllegalArgumentException("Email invalid.");
        }
        this.email = email;
    }
    public String getDepartament(){return departament;}
    public void setDepartament(String departament){this.departament = departament;}
    public boolean isActive(){return isActive;}
    public void isActive(boolean activ){isActive = activ;}
    public AccessLevel getAccessLevel(){return accessLevel;}
    public void setAccessLevel(AccessLevel accessLevel){this.accessLevel = accessLevel;}

    //metode de validare
    private boolean isValidEmail(String email){
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    //metode de buisiness logic
    public boolean hasAdminAccess(){
        return accessLevel == AccessLevel.ADMIN || accessLevel == AccessLevel.SUPER_ADMIN;
    }
    public boolean hasFullAccess(){
        return accessLevel == AccessLevel.SUPER_ADMIN;
    }
    public boolean canCreateAccounts(){
        return accessLevel == AccessLevel.SENIOR || hasAdminAccess();
    }
    public boolean canDeleteAccounts(){
        return hasAdminAccess();
    }
    public boolean canManageOtherManagers(){
        return accessLevel ==AccessLevel.SUPER_ADMIN;
    }

    // override metode
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

