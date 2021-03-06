package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting;

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
  private final Boolean route0;
  private final Long gradeLowerBound;
  private final Long gradeCurrent;
  private final Boolean forceCalculateILP;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(RouteSettingCommandSanityValidator.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  RouteSettingCommandSanityValidator(UUID classId, Boolean route0, Long gradeLowerBound,
      Long gradeCurrent, Boolean forceCalculateILP) {
    this.classId = classId;
    this.route0 = route0;
    this.gradeLowerBound = gradeLowerBound;
    this.gradeCurrent = gradeCurrent;
    this.forceCalculateILP = forceCalculateILP;
  }

  void validate() {
    if (classId == null) {
      LOGGER.warn("Invalid or incorrect class id");
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class")));
    }
    if (route0 == null && gradeCurrent == null && gradeLowerBound == null
        && forceCalculateILP == null) {
      LOGGER.warn("All null values in payload");
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")));
    }
  }
}
