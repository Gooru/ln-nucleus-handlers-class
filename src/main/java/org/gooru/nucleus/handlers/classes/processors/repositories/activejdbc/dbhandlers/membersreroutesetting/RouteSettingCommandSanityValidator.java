package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting;

import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

class RouteSettingCommandSanityValidator {

  private final UUID classId;
  private final Long gradeLowerBound;
  private final Long gradeUpperBound;
  private final List<String> users;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(
          RouteSettingCommandSanityValidator.class);
  protected static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  RouteSettingCommandSanityValidator(UUID classId, Long gradeLowerBound,
      Long gradeUpperBound, List<String> users) {
    this.classId = classId;
    this.gradeLowerBound = gradeLowerBound;
    this.gradeUpperBound = gradeUpperBound;
    this.users = users;
  }

  void validate() {
    if (classId == null) {
      LOGGER.warn("Invalid or incorrect class id");
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class")));
    }
    if (gradeLowerBound == null && gradeUpperBound == null) {
      LOGGER.warn("All null values in payload");
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")));
    }
    if (users == null || users.isEmpty()) {
      LOGGER.warn("No users specified in payload");
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.user")));
    }
  }
}
