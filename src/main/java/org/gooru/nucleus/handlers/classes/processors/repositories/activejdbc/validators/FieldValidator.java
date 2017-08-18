package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 28/1/16.
 */
public interface FieldValidator {
    static boolean validateStringIfPresent(Object o, int len) {
        return o == null || o instanceof String && !((String) o).isEmpty() && ((String) o).length() < len;
    }

    static boolean validateString(Object o, int len) {
        return !(o == null || !(o instanceof String) || ((String) o).isEmpty() || (((String) o).length() > len));
    }

    static boolean validateInteger(Object o) {
        if (o == null) {
            return false;
        }
        try {
            Integer.valueOf(o.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    static boolean validateDateWithFormat(Object o, DateTimeFormatter formatter, boolean allowedInPast,
        boolean allowCurrentDate) {
        if (o == null) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(o.toString(), formatter);
            if (!allowedInPast) {
                boolean isValid = date.isAfter(LocalDate.now());
                if (!isValid && allowCurrentDate) {
                    isValid = date.isEqual(LocalDate.now());
                }
                return isValid;
            }
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    static boolean validateDateWithFormatWithInDaysBoundary(Object o, DateTimeFormatter formatter, long daysInPast,
        long daysInFuture) {
        if (o == null) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(o.toString(), formatter);
            LocalDate today = LocalDate.now();

            if (today.minusDays(daysInPast).isAfter(date) || today.plusDays(daysInFuture).isBefore(date)) {
                return false;
            }
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    static boolean validateDateWithFormatIfPresent(Object o, DateTimeFormatter formatter, boolean allowedInPast,
        boolean allowCurrentDate) {
        if (o != null) {
            return validateDateWithFormat(o, formatter, allowedInPast, allowCurrentDate);
        }
        return true;
    }

    static boolean validateJsonIfPresent(Object o) {
        return o == null || o instanceof JsonObject && !((JsonObject) o).isEmpty();
    }

    static boolean validateJson(Object o) {
        return !(o == null || !(o instanceof JsonObject) || ((JsonObject) o).isEmpty());
    }

    static boolean validateJsonArrayIfPresent(Object o) {
        return o == null || o instanceof JsonArray && !((JsonArray) o).isEmpty();
    }

    static boolean validateJsonArray(Object o) {
        return !(o == null || !(o instanceof JsonArray) || ((JsonArray) o).isEmpty());
    }

    static boolean validateDeepJsonArrayIfPresent(Object o, FieldValidator fv) {
        if (o == null) {
            return true;
        } else if (!(o instanceof JsonArray) || ((JsonArray) o).isEmpty()) {
            return false;
        } else {
            JsonArray array = (JsonArray) o;
            for (Object element : array) {
                if (!fv.validateField(element)) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean validateDeepJsonArray(Object o, FieldValidator fv) {
        if (o == null || !(o instanceof JsonArray) || ((JsonArray) o).isEmpty()) {
            return false;
        }
        JsonArray array = (JsonArray) o;
        for (Object element : array) {
            if (!fv.validateField(element)) {
                return false;
            }
        }
        return true;
    }

    static boolean validateBoolean(Object o) {
        return o != null && o instanceof Boolean;
    }

    static boolean validateBooleanIfPresent(Object o) {
        return o == null || o instanceof Boolean;
    }

    static boolean validateUuid(Object o) {
        try {
            UUID uuid = UUID.fromString((String) o);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static boolean validateUuidIfPresent(String o) {
        return o == null || validateUuid(o);
    }

    static boolean validateValueExists(String o, Set<String> acceptedFields) {
        return o != null && acceptedFields.contains(o);
    }

    boolean validateField(Object value);

    Pattern EMAIL_PATTERN =
        Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    static boolean validateEmail(Object o) {
        Matcher matcher = EMAIL_PATTERN.matcher((String) o);
        return matcher.matches();
    }
}
