
package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityTaxonomySubject;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On 17-Sep-2019
 */
public class FindSecondaryClassesForClassHandler implements DBHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FindSecondaryClassesForClassHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private static final String RESP_KEY_CLASSES = "classes";

  private final ProcessorContext context;
  private AJEntityClass entityClass;
  private String classSubject;

  public FindSecondaryClassesForClassHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be a class id present
    if (context.classId() == null || context.classId().isEmpty()) {
      LOGGER.warn("Missing class");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.class.id")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty()
        || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to edit class");
      return new ExecutionResult<>(
          MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityClass> classes =
        AJEntityClass.where(AJEntityClass.FETCH_QUERY_FILTER, context.classId());
    if (classes.isEmpty()) {
      LOGGER.warn("Not able to find class '{}'", this.context.classId());
      return new ExecutionResult<>(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    this.entityClass = classes.get(0);

    if (!this.entityClass.isCurrentVersion() || this.entityClass.isArchived()) {
      LOGGER.warn("Class with id '{}' is either archived or not of current version",
          this.context.classId());
      return new ExecutionResult<>(
          MessageResponseFactory.createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("class.archived.or.incorrect.version")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    // Verify that subject preference is exists for the class, if not return empty response
    ExecutionResult<MessageResponse> result = validateSubjectExistance();
    if (!result.continueProcessing()) {
      LOGGER.warn("class '{}' does not have subject preference set", this.context.classId());
      return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(new JsonObject()),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    // Verify that the user finding the secondary classes is owner or collaborator of the class
    return AuthorizerBuilder.buildFindSecondaryClassesAuthorizer(this.context)
        .authorize(this.entityClass);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    LazyList<AJEntityClass> secondaryClasses =
        AJEntityClass.findBySQL(AJEntityClass.FIND_SECONDARY_CLASSES, this.context.userId(),
            this.context.userId(), this.context.classId());

    // If there is no secondary class found, return empty response
    if (secondaryClasses.isEmpty()) {
      LOGGER.debug("there are no secondary classes present for class '{}' and user '{}'",
          this.context.classId(), this.context.userId());
      return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(new JsonObject()),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    JsonArray classesArray = new JsonArray();
    secondaryClasses.forEach(cls -> {
      JsonObject preference = new JsonObject(cls.getString(AJEntityClass.PREFERENCE));
      if (!preference.isEmpty()) {
        String subject = preference.getString(AJEntityTaxonomySubject.RESP_KEY_SUBJECT, null);
        if (subject != null && !subject.isEmpty() && subject.equalsIgnoreCase(this.classSubject)) {
          classesArray.add(new JsonObject(JsonFormatterBuilder
              .buildSimpleJsonFormatter(false, AJEntityClass.SECONDARY_CLASSES_FIELD_LIST)
              .toJson(cls)));
        }
      }
    });

    // After the subject filter if there are no classes to return, send empty response
    if (classesArray.isEmpty()) {
      LOGGER.debug("not classes filtered after subject check");
      return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(new JsonObject()),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    JsonObject response = new JsonObject();
    response.put(RESP_KEY_CLASSES, classesArray);
    return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(response),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

  private ExecutionResult<MessageResponse> validateSubjectExistance() {
    String preference = this.entityClass.getString(AJEntityClass.PREFERENCE);
    if (preference == null || preference.isEmpty()) {
      return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.FAILED);
    }

    JsonObject preferenceJson = new JsonObject(preference);
    if (preferenceJson.isEmpty()
        || !preferenceJson.containsKey(AJEntityTaxonomySubject.RESP_KEY_SUBJECT)) {
      return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.FAILED);
    }

    this.classSubject = preferenceJson.getString(AJEntityTaxonomySubject.RESP_KEY_SUBJECT);
    if (this.classSubject == null || this.classSubject.isEmpty()) {
      return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }
}
