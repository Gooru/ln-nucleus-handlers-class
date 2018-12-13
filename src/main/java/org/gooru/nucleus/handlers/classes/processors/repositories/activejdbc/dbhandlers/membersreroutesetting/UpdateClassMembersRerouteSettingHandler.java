package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting;

import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

public class UpdateClassMembersRerouteSettingHandler implements DBHandler {

  /*
  - Following settings need to be updated
    - grade high, grade low for specified class members
  - class is valid - not deleted, not archived
  - validations that class level low / high are set (whichever is being set for member) and that member level info is bounded by class level info
  - validations for grade low < grade high, grade id from grade master
  - PUT request with payload
  - all validations have to be successful for update, else everything fails
  - If validation succeeds, update the db
   */

  private final ProcessorContext context;
  private MembersRerouteSettingCommand command;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(UpdateClassMembersRerouteSettingHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private AJEntityClass entityClass;

  public UpdateClassMembersRerouteSettingHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      command = MembersRerouteSettingCommand.build(context);
    } catch (MessageResponseWrapperException mwe) {
      return new ExecutionResult<>(mwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionStatus.CONTINUE_PROCESSING);
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
            ExecutionStatus.FAILED);
      }
      this.entityClass = classes.get(0);

      new RequestDbValidator(context.classId(), entityClass, command).validate();

      return AuthorizerBuilder.buildUpdateMembersRerouteSettingAuthorizer(this.context)
          .authorize(this.entityClass);

    } catch (MessageResponseWrapperException mwe) {
      return new ExecutionResult<>(mwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      // NOTE: To avoid any race conditions first update the class, if needed
      if (command.isClassUpperBoundUpdateNeeded()) {
        entityClass.setGradeUpperBound(command.getGradeUpperBound());
        boolean result = entityClass.save();
        if (!result) {
          LOGGER.error("Class with id '{}' failed to save", context.classId());
          if (this.entityClass.hasErrors()) {
            Map<String, String> map = this.entityClass.errors();
            JsonObject errors = new JsonObject();
            map.forEach(errors::put);
            return new ExecutionResult<>(
                MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
          } else {
            return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(),
                ExecutionResult.ExecutionStatus.FAILED);
          }
        }
      }

      new ClassMemberUpdater(command).update();

      // NOTE: We are not generating any events here right now
      return new ExecutionResult<>(
          MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("updated")),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
    } catch (DBException dbe) {
      LOGGER.warn("Unable to update membership reroute settings for class '{}' ",
          context.classId(), dbe);
      return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(
          RESOURCE_BUNDLE.getString("internal.error")), ExecutionResult.ExecutionStatus.FAILED);
    }
  }


  @Override
  public boolean handlerReadOnly() {
    return false;
  }
}
