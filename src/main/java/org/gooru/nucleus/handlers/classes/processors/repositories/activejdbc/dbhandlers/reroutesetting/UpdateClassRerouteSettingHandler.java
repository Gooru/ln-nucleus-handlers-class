package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting;

import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.common.milestone.MilestoneQueuer;
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

public class UpdateClassRerouteSettingHandler implements DBHandler {

  /*
  - Following settings need to be updated
    - route0, grade high, grade low, grade current
  - class is valid - not deleted, not archived
  - validations for grade low < grade high, grade id from grade master, route0 only if course is assigned and is premium
  - PUT request with payload
  - all validations have to be successful for update, else everything fails
  - If validation succeeds, update the db
  - If the value is null for any datum, then do not update it (no reset to null)
   */

  private final ProcessorContext context;
  private RerouteSettingCommand command;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(UpdateClassRerouteSettingHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private AJEntityClass entityClass;
  private boolean gradeWasSet = false;

  public UpdateClassRerouteSettingHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      command = RerouteSettingCommand.build(context);
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

      return AuthorizerBuilder.buildUpdateRerouteSettingAuthorizer(this.context)
          .authorize(this.entityClass);

    } catch (MessageResponseWrapperException mwe) {
      return new ExecutionResult<>(mwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    updateSettings();
    boolean result = this.entityClass.save();
    if (!result) {
      LOGGER.error("Class with id '{}' failed to save", context.classId());
      if (this.entityClass.hasErrors()) {
        Map<String, String> map = this.entityClass.errors();
        JsonObject errors = new JsonObject();
        map.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
            ExecutionResult.ExecutionStatus.FAILED);
      } else {
        return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    }

    return handlePostProcessing();
  }

  private ExecutionResult<MessageResponse> handlePostProcessing() {
    new ClassMemberUpdater(command, gradeWasSet).update();
    if (gradeWasSet && this.entityClass.getCourseId() != null) {
      MilestoneQueuer.build()
          .enqueue(UUID.fromString(this.entityClass.getCourseId()), command.getGradeCurrent());
    }

    return new ExecutionResult<>(
        MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("updated"),
            EventBuilderFactory.getUpdateClassEventBuilder(context.classId())),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private void updateSettings() {
    if (command.getGradeLowerBound() != null) {
      entityClass.setGradeLowerBound(command.getGradeLowerBound());
    }
    if (command.getGradeCurrent() != null
        && (entityClass.getGradeCurrent() == null || !entityClass.isClassSetupComplete())) {
      entityClass.setGradeCurrent(command.getGradeCurrent());
      entityClass.setGradeUpperBound(command.getGradeCurrent());
      gradeWasSet = true;
    }
    if (command.getRoute0() != null) {
      entityClass.setRoute0(command.getRoute0());
    }
    if (command.getForceCalculateILP() != null) {
      entityClass.setForceCalculateIlp(command.getForceCalculateILP());
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }
}
