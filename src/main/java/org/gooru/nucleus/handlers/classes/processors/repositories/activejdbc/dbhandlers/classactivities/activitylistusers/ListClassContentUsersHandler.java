package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylistusers;

import java.util.List;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
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
      new RequestValidator(context).validate();
      initializeContentId();
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
    return AuthorizerBuilder.buildListClassContentUsersAuthorizer(context)
        .authorize(this.entityClass);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    List<String> users = entityClassContent.getUsers();
    return new ResponseBuilder(context.classId(), users).buildResponse();
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
    // Validator took care of type conversion
    classContentId = Long.parseLong(contentId);
  }

}
