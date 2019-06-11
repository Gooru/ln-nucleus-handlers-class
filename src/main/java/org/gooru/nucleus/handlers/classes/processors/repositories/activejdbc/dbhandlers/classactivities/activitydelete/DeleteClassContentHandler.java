package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitydelete;

import io.vertx.core.json.JsonObject;
import java.math.BigInteger;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilder;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteClassContentHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteClassContentHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private AJEntityClassContents classContents;
  private String classContentId;

  public DeleteClassContentHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      validateUser();
      validateClassId();
      validateAndInitializeContentId();
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityClassContents> classContents =
        AJEntityClassContents
            .where(AJEntityClassContents.FETCH_CLASS_CONTENT, classContentId, context.classId());
    if (classContents.isEmpty()) {
      LOGGER.warn("content {} not add to this class  {}", classContentId, context.classId());
      return new ExecutionResult<>(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    this.classContents = classContents.get(0);

    LazyList<AJEntityClass> classes = AJEntityClass
        .where(AJEntityClass.FETCH_QUERY_FILTER, context.classId());
    if (classes.isEmpty()) {
      LOGGER.warn("Not able to find class '{}'", this.context.classId());
      return new ExecutionResult<>(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    AJEntityClass entityClass = classes.get(0);
    // Class should be of current version and Class should not be archived
    if (!entityClass.isCurrentVersion() || entityClass.isArchived()) {
      LOGGER.warn("Class '{}' is either archived or not of current version", context.classId());
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(
                  RESOURCE_BUNDLE.getString("class.archived.or.incorrect.version")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return AuthorizerBuilder.buildClassContentAuthorizer(this.context).authorize(entityClass);

  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    EventBuilder deleteEventBuilder =
        EventBuilderFactory
            .getDeleteClassContentEventBuilder(classContents.getId(), this.context.classId(),
                classContents.getContentId(), classContents.getContentType());
    boolean result = this.classContents.delete();
    if (!result) {
      if (classContents.hasErrors()) {
        LOGGER.warn("error in deleting class content");
        return new ExecutionResult<>(
            MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
            ExecutionStatus.FAILED);
      }
    }
    return new ExecutionResult<>(
        MessageResponseFactory
            .createNoContentResponse(RESOURCE_BUNDLE.getString("deleted"), deleteEventBuilder),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private void validateUser() {
    if ((context.userId() == null) || context.userId().isEmpty()
        || MessageConstants.MSG_USER_ANONYMOUS.equalsIgnoreCase(context.userId())) {
      LOGGER.warn("Invalid user");
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")));
    }
  }

  private void validateClassId() {
    if (context.classId() == null || context.classId().isEmpty()) {
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.class.id")));
    }
  }

  private void validateAndInitializeContentId() {
    try {
      classContentId = context.requestHeaders().get(AJEntityClassContents.ID_CONTENT);
      new BigInteger(classContentId);
      if (classContentId == null) {
        throw new MessageResponseWrapperException(
            MessageResponseFactory
                .createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.content.id")));
      }
    } catch (Exception e) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class.content.id")));
    }
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.classContents.errors().entrySet()
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }

}
