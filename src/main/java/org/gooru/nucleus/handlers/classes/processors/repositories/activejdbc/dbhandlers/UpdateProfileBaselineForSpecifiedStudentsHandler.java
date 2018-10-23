package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;

/**
 * @author ashish.
 */

class UpdateProfileBaselineForSpecifiedStudentsHandler implements DBHandler {

  /*
  - users are provided as list
  - class is valid and not deleted
  - class should have course
  - course should have subject bucket
  - specified users are members of class
  - POST request
  - after validation, fire baseline trigger
  - all validations have to be successful for update, else everything fails
   */

  private final ProcessorContext context;

  UpdateProfileBaselineForSpecifiedStudentsHandler(ProcessorContext context) {
    this.context = context;
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
    // TODO: Implement this
    return true;
  }
}
