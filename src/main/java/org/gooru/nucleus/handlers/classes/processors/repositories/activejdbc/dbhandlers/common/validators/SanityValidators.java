package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.common.validators;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inline validation w/o having interaction with DB
 *
 * @author ashish.
 */

public final class SanityValidators {

  private static final Logger LOGGER = LoggerFactory.getLogger(SanityValidators.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  private SanityValidators() {
    throw new AssertionError();
  }

  public static void validateUser(ProcessorContext context) {
    if ((context.userId() == null) || context.userId().isEmpty()
        || MessageConstants.MSG_USER_ANONYMOUS.equalsIgnoreCase(context.userId())) {
      LOGGER.warn("Invalid user");
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")));
    }
  }

  public static void validateClassId(ProcessorContext context) {
    if (context.classId() == null || context.classId().isEmpty()) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.class.id")));
    }
  }

  public static String validateAndFetchContentId(ProcessorContext context) {
    try {
      String contentId = context.requestHeaders().get(AJEntityClassContents.ID_CONTENT);
      new BigInteger(contentId);
      if (contentId == null) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.content.id")));
      }
      return contentId;
    } catch (Exception e) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class.content.id")));
    }
  }

  public static boolean validateStartAndEndDateTimeWithInBoundarys(String startDateTime,
      String endDateTime, DateTimeFormatter formatter) {
    try {
      LocalDateTime startTime = LocalDateTime.parse(startDateTime, formatter);
      LocalDateTime endTime = LocalDateTime.parse(endDateTime, formatter);
      if (startTime.isBefore(endTime)) {
        return false;
      }
    } catch (DateTimeParseException e) {
      return false;
    }
    return true;
  }

}
