package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.activitylistusers;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContextHelper;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

class RequestValidator {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestValidator.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  public RequestValidator(ProcessorContext context) {
    this.context = context;
  }

  void validate() {
    if (!ProcessorContextHelper.validateId(context.classId())) {
      LOGGER.warn("Invalid format of class id: '{}'", context.classId());
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class")));
    }
    if (!ProcessorContextHelper.validateId(context.userId())) {
      LOGGER.warn("Invalid user");
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")));
    }

  }
}
