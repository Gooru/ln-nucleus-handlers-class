package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.activitylistusers;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContextHelper;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

public class ListClassContentUsersHandler implements DBHandler {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(ListClassContentUsersHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private long classContentId;
  private AJEntityClass entityClass;
  private AJEntityClassContents entityClassContent;

  public ListClassContentUsersHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      if (!ProcessorContextHelper.validateId(context.classId())) {
        LOGGER.warn("Invalid format of class id: '{}'", context.classId());
        return new ExecutionResult<>(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class")),
            ExecutionStatus.FAILED);
      }
      initializeContentId();
      if (!ProcessorContextHelper.validateId(context.userId())) {
        LOGGER.warn("Invalid user");
        return new ExecutionResult<>(
            MessageResponseFactory
                .createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    try {
      initializeAndValidateClass();
      validateClassContentId();
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
    return AuthorizerBuilder.buildAddClassContentUsersAuthorizer(context)
        .authorize(this.entityClass);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // TODO: Implement this
    // Fetch the users for this activity
    // Enrich them with the status and filter out from class_members table
    // Enrich them further with data from users table
    // Create Json payload
    return null;
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

  private void validateClassContentId() {
    LazyList<AJEntityClassContents> classContents =
        AJEntityClassContents
            .where(AJEntityClassContents.FETCH_CLASS_CONTENT, classContentId, context.classId());
    if (classContents.isEmpty()) {
      LOGGER.warn("content {} not added to class  {}", classContentId, context.classId());
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")));
    }
    this.entityClassContent = classContents.get(0);

  }

  private void initializeAndValidateClass() {
    try {
      LazyList<AJEntityClass> classes = AJEntityClass
          .where(AJEntityClass.FETCH_QUERY_FILTER, context.classId());
      if (classes.isEmpty()) {
        LOGGER.warn("Not able to find class '{}'", this.context.classId());
        throw new MessageResponseWrapperException(
            MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")));
      }
      this.entityClass = classes.get(0);
    } catch (DBException e) {
      LOGGER.error("Not able to fetch class from DB", e);
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")));
    }
  }

  private void initializeContentId() {
    String contentId = context.requestHeaders().get(MessageConstants.CLASS_CONTENT_ID);
    try {
      classContentId = Long.parseLong(contentId);
    } catch (NumberFormatException nfe) {
      LOGGER.warn("Invalid class activity id: '{}'", contentId);
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.classcontentid")));
    }
  }
  
}
