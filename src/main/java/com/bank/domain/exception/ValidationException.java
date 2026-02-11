package com.bank.domain.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Excepție aruncată pentru erori de validare a datelor
 */
public class ValidationException extends BankingException {

    private final List<ValidationError> errors;

    /**
     * Clasă internă pentru erori de validare individuale
     */
    public static class ValidationError {
        private final String field;
        private final String message;
        private final Object invalidValue;

        public ValidationError(String field, String message, Object invalidValue) {
            this.field = field;
            this.message = message;
            this.invalidValue = invalidValue;
        }

        // Getteri
        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        public Object getInvalidValue() {
            return invalidValue;
        }

        @Override
        public String toString() {
            return String.format("ValidationError[field=%s, message=%s, value=%s]",
                    field, message, invalidValue);
        }
    }

    public ValidationException(String message) {
        super(BankingErrorCode.VALIDATION_ERROR, message);
        this.errors = new ArrayList<>();
    }

    public ValidationException(List<ValidationError> errors) {
        super(BankingErrorCode.VALIDATION_ERROR,
                "Validare esuata cu " + errors.size() + " erori");
        this.errors = new ArrayList<>(errors);
    }

    // Metode pentru adăugare erori
    public ValidationException addError(String field, String message, Object invalidValue) {
        errors.add(new ValidationError(field, message, invalidValue));
        return this;
    }

    public void addError(ValidationError error) {
        errors.add(error);
    }

    // Getteri
    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public int getErrorCount() {
        return errors.size();
    }

    /**
     * Returnează mesajul complet cu toate erorile
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage()).append(":\n");
        for (int i = 0; i < errors.size(); i++) {
            ValidationError error = errors.get(i);
            sb.append(String.format("  %d. %s: %s (valoare: %s)\n",
                    i + 1, error.getField(), error.getMessage(), error.getInvalidValue()));
        }
        return sb.toString();
    }
    public static ValidationException withError(String field, String msg, Object value) {
        ValidationException ex = new ValidationException("Validare eșuată");
        ex.addError(field, msg, value);
        return ex;
    }

    public void addErrors(List<ValidationError> errors) {
        this.errors.addAll(errors);
    }

    @Override
    public String toString() {
        return String.format("ValidationException[%d errors, message=%s]",
                errors.size(), getMessage());
    }
}