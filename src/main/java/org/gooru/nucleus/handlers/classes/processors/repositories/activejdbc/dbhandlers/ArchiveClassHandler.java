package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On: 17-May-2017
 */
public class ArchiveClassHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveClassHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private AJEntityClass entityClass;

  public ArchiveClassHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if (context.classId() == null || context.classId().isEmpty()) {
      LOGGER.warn("Missing class id");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.class.id")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty()
        || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to archive class");
      return new ExecutionResult<>(
          MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityClass> entityClasses = AJEntityClass
        .findBySQL(AJEntityClass.ARCHIVE_QUERY, context.classId());
    if (entityClasses.isEmpty()) {
      LOGGER.warn("Class id '{}' not present in DB", context.classId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(
          RESOURCE_BUNDLE.getString("class.id") + context.classId()),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    this.entityClass = entityClasses.get(0);

    return AuthorizerBuilder.buildArchiveAuthorizer(context).authorize(this.entityClass);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // If class is already archived, silently ignore and send success
    // response. Do not send event in this case.
    if (this.entityClass.isArchived()) {
      LOGGER
          .warn("Class '{}' is already archive, sending success without event", context.classId());
      return new ExecutionResult<>(
          MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("archived")),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    // Class should be of current version
    if (!this.entityClass.isCurrentVersion()) {
      LOGGER.warn("Class '{}' is not of current version, can not archive", context.classId());
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
          RESOURCE_BUNDLE.getString("class.incorrect.version")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    this.entityClass.setIsArchived(true);
    String endDate = sdf.format(new Date());
    this.entityClass.setEndDate(endDate);

    boolean result = this.entityClass.save();
    if (!result) {
      LOGGER.error("Class with id '{}' failed to archive", context.classId());
      if (this.entityClass.hasErrors()) {
        Map<String, String> map = this.entityClass.errors();
        JsonObject errors = new JsonObject();
        map.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    }

    return new ExecutionResult<>(
        MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("archived"),
            EventBuilderFactory.getArchiveClassEventBuilder(context.classId())),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
