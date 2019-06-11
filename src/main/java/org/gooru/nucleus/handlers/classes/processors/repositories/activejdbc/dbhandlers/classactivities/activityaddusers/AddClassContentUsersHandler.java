package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activityaddusers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.PayloadValidator;
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

public class AddClassContentUsersHandler implements DBHandler {

  private final ProcessorContext context;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private static final Logger LOGGER = LoggerFactory.getLogger(AddClassContentUsersHandler.class);
  private long classContentId;
  private AJEntityClass entityClass;
  private List<String> users;

  public AddClassContentUsersHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      JsonObject errors = new DefaultPayloadValidator()
          .validatePayload(context.request(), AJEntityClassContents.updateUsersFieldSelector(),
              AJEntityClassContents.getValidatorRegistry());
      if (errors != null && !errors.isEmpty()) {
        LOGGER.warn("Validation errors for request");
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
            ExecutionResult.ExecutionStatus.FAILED);
      }
      new RequestValidator(context).validate();

      initializeContentId();
      initializeUsers();
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
      new ClassMembersDBValidator(users, context.classId()).validate();
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
    return AuthorizerBuilder.buildAddClassContentUsersAuthorizer(context)
        .authorize(this.entityClass);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    int usersCount = users == null ? -1 : users.size();
    AJEntityClassContents.updateClassContentUsers(classContentId,
        users == null ? null : DbHelperUtil.toPostgresArrayString(users), usersCount);
    return new ExecutionResult<>(MessageResponseFactory
        .createNoContentResponse(RESOURCE_BUNDLE.getString("updated")),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
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
    // Validator has already validates this
    classContentId = Long.parseLong(contentId);
  }

  private void initializeUsers() {
    JsonArray usersArray = context.request().getJsonArray(AJEntityClassContents.USERS);
    if (usersArray == null) {
      users = null;
    } else {
      users = usersArray.getList();
    }
    if (users != null && !users.isEmpty()) {
      Set<String> uniqueUsers = new HashSet<>(users);
      users = new ArrayList<>(uniqueUsers);
    }
  }


  private static class DefaultPayloadValidator implements PayloadValidator {

  }

}
