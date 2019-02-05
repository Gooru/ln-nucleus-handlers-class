package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.profilebaseline;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

public class UpdateProfileBaselineForSpecifiedStudentsHandler implements DBHandler {

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
  private ProfileBaselineCommand command;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(UpdateProfileBaselineForSpecifiedStudentsHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private AJEntityClass entityClass;

  public UpdateProfileBaselineForSpecifiedStudentsHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      command = ProfileBaselineCommand.build(context);
    } catch (MessageResponseWrapperException mwe) {
      return new ExecutionResult<>(mwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    try {
      LazyList<AJEntityClass> classes = AJEntityClass
          .where(AJEntityClass.FETCH_QUERY_FILTER, context.classId());
      if (classes.isEmpty()) {
        LOGGER.warn("Not able to find class '{}'", this.context.classId());
        return new ExecutionResult<>(
            MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
            ExecutionResult.ExecutionStatus.FAILED);
      }
      this.entityClass = classes.get(0);

      new RequestDbValidator(context.classId(), entityClass, command).validate();

      return AuthorizerBuilder.buildUpdateProfileBaselineAuthorizer(this.context)
          .authorize(this.entityClass);

    } catch (MessageResponseWrapperException mwe) {
      return new ExecutionResult<>(mwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    return new ProfileBaselineRequestQueueService(command, this.context.classId(),
        entityClass.getCourseId()).enqueue();
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }
}
