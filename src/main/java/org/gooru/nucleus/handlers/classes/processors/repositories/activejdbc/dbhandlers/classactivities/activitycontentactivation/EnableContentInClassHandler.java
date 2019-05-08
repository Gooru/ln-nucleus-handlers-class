package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitycontentactivation;

import io.vertx.core.json.JsonObject;
import java.math.BigInteger;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnableContentInClassHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnableContentInClassHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private AJEntityClassContents classContents;
  private String contentId;
  private FlowDeterminer flowDeterminer;
  private ContentActivator contentActivator;
  private AJEntityClass entityClass;

  public EnableContentInClassHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      validateUser();
      validateClassId();
      validateAndInitializeContentId();
      validateContextRequestFields();
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    try {
      validateAndInitializeClass();
      validateAndInitializeClassContents();
      initializationBasedOnFlowType();
      contentActivator.validate();
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    return AuthorizerBuilder.buildClassContentAuthorizer(this.context).authorize(entityClass);

  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      contentActivator.activateContent();
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(MessageResponseFactory
        .createNoContentResponse(RESOURCE_BUNDLE.getString("updated"),
            EventBuilderFactory
                .getClassContentEnableEventBuilder(classContents.getId(), this.context.classId())),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private void validateUser() {
    if ((context.userId() == null) || context.userId().isEmpty()
        || MessageConstants.MSG_USER_ANONYMOUS
        .equalsIgnoreCase(context.userId())) {
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
      contentId = context.requestHeaders().get(AJEntityClassContents.ID_CONTENT);
      new BigInteger(contentId);
      if (contentId == null) {
        throw new MessageResponseWrapperException(
            MessageResponseFactory
                .createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.content.id")));
      }
    } catch (Exception e) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class.content.id")));
    }
  }

  private void validateContextRequestFields() {
    JsonObject errors = new DefaultPayloadValidator()
        .validatePayload(context.request(), AJEntityClassContents.updateFieldSelector(),
            AJEntityClassContents.getValidatorRegistry());
    if (errors != null && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createValidationErrorResponse(errors));
    }
  }

  private void validateAndInitializeClass() {
    LazyList<AJEntityClass> classes = AJEntityClass
        .where(AJEntityClass.FETCH_QUERY_FILTER, context.classId());
    if (classes.isEmpty()) {
      LOGGER.warn("Not able to find class '{}'", this.context.classId());
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")));
    }
    entityClass = classes.get(0);
    if (!entityClass.isCurrentVersion() || entityClass.isArchived()) {
      LOGGER.warn("Class '{}' is either archived or not of current version", context.classId());
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("class.archived.or.incorrect.version")));
    }
  }

  private void initializationBasedOnFlowType() {
    flowDeterminer = new FlowDeterminer(context.request());
    if (flowDeterminer.isActivationFlow()) {
      contentActivator = ContentActivator
          .buildForActivationFlow(entityClass, classContents, flowDeterminer, context);
    } else {
      contentActivator = ContentActivator
          .buildForScheduleOnDayFlow(entityClass, classContents, flowDeterminer, context);
    }
  }

  private void validateAndInitializeClassContents() {
    LazyList<AJEntityClassContents> classContents =
        AJEntityClassContents
            .where(AJEntityClassContents.FETCH_CLASS_CONTENT, contentId, context.classId());
    if (classContents.isEmpty()) {
      LOGGER.warn("content {} not add to this class  {}", contentId, context.classId());
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")));
    }
    this.classContents = classContents.get(0);
  }

  private static class DefaultPayloadValidator implements PayloadValidator {

  }

  private static class DefaultAJEntityClassContentsBuilder implements
      EntityBuilder<AJEntityClassContents> {

  }

}
