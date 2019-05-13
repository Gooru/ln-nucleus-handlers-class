package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activityschedule;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;

/**
 * @author ashish.
 */

public class ScheduleContentInClassHandler implements DBHandler {

  public ScheduleContentInClassHandler(ProcessorContext context) {

  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // TODO: Implement this
    return null;
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // TODO: Implement this
    return null;
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // TODO: Implement this
    return null;
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }
}
