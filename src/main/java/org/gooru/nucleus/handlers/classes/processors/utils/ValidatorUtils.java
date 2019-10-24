package org.gooru.nucleus.handlers.classes.processors.utils;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author renuka
 */
public final class ValidatorUtils {

  private final static Logger LOGGER = LoggerFactory.getLogger(ValidatorUtils.class);

  private ValidatorUtils() {
    throw new AssertionError();
  }

  public static boolean isNullOrEmpty(String value) {
    return (value == null || value.isEmpty() || value.trim().isEmpty());
  }

  public static boolean isValidUUID(String id) {
    try {
      if (!isNullOrEmpty(id) && id.length() == 36) {
        UUID.fromString(id);
        return true;
      }

      return false;
    } catch (IllegalArgumentException iae) {
      LOGGER.warn("invalid UUID string '{}'", id);
    }

    return false;
  }
}
