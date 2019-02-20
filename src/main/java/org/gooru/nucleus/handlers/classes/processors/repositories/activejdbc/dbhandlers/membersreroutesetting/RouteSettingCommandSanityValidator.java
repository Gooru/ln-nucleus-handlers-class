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
  private final List<UserSettingCommand> users;
  private static final Logger LOGGER =
      LoggerFactory.getLogger(RouteSettingCommandSanityValidator.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  RouteSettingCommandSanityValidator(UUID classId, List<UserSettingCommand> users) {
    this.classId = classId;
    this.users = users;
  }

  void validate() {
    if (classId == null) {
      LOGGER.warn("Invalid or incorrect class id");
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class")));
    }

    if (users == null || users.isEmpty()) {
      LOGGER.warn("No users specified in payload");
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.user")));
    }

    users.forEach(user -> {
      if (user.getGradeLowerBound() == null && user.getGradeUpperBound() == null) {
        LOGGER.warn("grade bound values are null in payload for user:'{}'",
            user.getUserId().toString());
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")));
      }
    });
  }
}
