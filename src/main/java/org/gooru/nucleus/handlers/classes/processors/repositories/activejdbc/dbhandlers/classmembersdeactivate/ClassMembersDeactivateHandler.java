package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classmembersdeactivate;

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

public class ClassMembersDeactivateHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClassMembersDeactivateHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private ClassMembersDeactivateCommand command;
  private AJEntityClass entityClass;

  public ClassMembersDeactivateHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      command = ClassMembersDeactivateCommand.build(context);
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

      return AuthorizerBuilder.buildClassMemberDeactivateAuthorizer(this.context)
          .authorize(this.entityClass);

    } catch (MessageResponseWrapperException mwe) {
      return new ExecutionResult<>(mwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {

      AJClassMember.markMembersAsInactive(context.classId(), command.getUsersList());
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
