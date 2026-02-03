package com.bank.domain.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exceptie aruncata pentru erori de validare a datelor
 */

public class ValidationException extends BankingException{
    private final List<ValidationError> errors;

    //clasa interna pentru erori de validare individuale
    public static class ValidationError{
        private final String field;
        private final String message;
        private final Object invalidValue;

        public ValidationError(String field,String message,Object invalidValue){
            this.field = field;
            this.message =message;
            this.invalidValue = invalidValue;
        }

        //getteri
        public String getField(){return field;}
        public String getMessage(){return message;}
        public Object getInvalidValue(){return invalidValue;}

        @Override
        public String toString(){
            return String.format("ValidationError[field=%s, message=%s,value=%s]",
                    field,message,invalidValue);
        }
    }

    public ValidationException(String message){
        super(BankingErrorCode.VALIDATION_ERROR,message);
        this.errors = new ArrayList<>();
    }
    public ValidationException(List<ValidationError> errors){
        super(BankingErrorCode.VALIDATION_ERROR,
                "Validare esuata cu " + errors.size() + " erori.");
        this.errors = new ArrayList<>();
    }

    //metode pentru adaugare erori

    public ValidationException addError(String field,String message,Object invalidValue){
        this.errors.add(new ValidationError(field,message,invalidValue));
        return this;
    }
    public ValidationException addError(ValidationError error){
        this.errors.add(error);
        return this;
    }
    public boolean hasErrors(){return !errors.isEmpty();}

    //getteri
    public List<ValidationError> getErrors(){return new ArrayList<>();}
    public boolean hasError(){return !errors.isEmpty();}
    public int getErrorCount(){return errors.size();}

    /**
     * returneaza mesajul complet cu toate erorile
     */
    public String getDetailedMessage(){
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage()).append(":\n");
        for (int i = 0; i < errors.size(); i++){
            ValidationError error = errors.get(i);
            sb.append(String.format(" %d. %s: %s (valoare: %s)\n",
                    i + 1,error.getField(),error.getMessage(),error.getInvalidValue()));
        }
        return sb.toString();
    }
    public static ValidationException withError(String field, String message, Object invalidValue) {
        ValidationException ex = new ValidationException("Validare esuata");
        ex.addError(field, message, invalidValue);
        return ex;
    }

    @Override
    public String toString(){
        return String.format("ValidationException[%d errors, message=%s]",
                errors.size(),getMessage());
    }
}

