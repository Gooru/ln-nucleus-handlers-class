package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ResourceBundle;
import java.util.UUID;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContextHelper;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

class RouteSettingCommandSanityValidator {

  private final ProcessorContext context;
  private static final Logger LOGGER =
      LoggerFactory.getLogger(RouteSettingCommandSanityValidator.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  RouteSettingCommandSanityValidator(ProcessorContext context) {
    this.context = context;
  }

  void validate() {
    if (context.classId() == null) {
      LOGGER.warn("Invalid or incorrect class id");
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class")));
    }

    try {
      UUID.fromString(context.classId());
    } catch (IllegalArgumentException iae) {
      LOGGER.warn("class id is not UUID");
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class")));
    }

    JsonArray users = context.request().getJsonArray(MembersRerouteSettingRequestAttributes.USERS);

    if (users == null || users.isEmpty()) {
      LOGGER.warn("No users specified in payload");
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.user")));
    }

    for (int i = 0; i < users.size(); i++) {
      JsonObject userJson = users.getJsonObject(i);
      String user = userJson.getString(MembersRerouteSettingRequestAttributes.USER_ID);
      if (!ProcessorContextHelper.validateUuid(user)) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.user")));
      }
    }
  }
}
