package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.offlinecompleted;

import java.util.List;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.contentfetcher.ActivityFetcher;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.enricher.ContentEnricher;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.common.validators.SanityValidators;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassDao;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ClassContentListOfflineCompletedHandler implements DBHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ClassContentListOfflineCompletedHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private List<AJEntityClassContents> classContents;
  private ListActivityOfflineCompletedCommand command;
  private ContentEnricher contentEnricher;
  private ActivityFetcher activityFetcher;

  public ClassContentListOfflineCompletedHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      SanityValidators.validateUser(context);
      SanityValidators.validateClassId(context);
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    try {
      boolean studentAuthorization = false;
      AJEntityClass entityClass = EntityClassDao.fetchClassById(context.classId());
      ExecutionResult<MessageResponse> classAuthorization =
          AuthorizerBuilder.buildClassContentAuthorizer(this.context).authorize(entityClass);
      if (!classAuthorization.continueProcessing()) {
        classAuthorization =
            AuthorizerBuilder.buildClassStudentAuthorizer(context).authorize(entityClass);
        if (!classAuthorization.continueProcessing()) {
          return new ExecutionResult<>(
              MessageResponseFactory
                  .createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
              ExecutionResult.ExecutionStatus.FAILED);
        }
        studentAuthorization = true;
      }
      command = new ListActivityOfflineCompletedCommand(context, studentAuthorization);
      command.validate();

      return classAuthorization;
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {

    fetchClassContents();
    contentEnricher = ContentEnricher
        .buildContentEnricherForOfflineCompletedActivities(classContents, command.isStudent());

    return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(createResponse()),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private JsonObject createResponse() {
    JsonArray renderedContent = contentEnricher.enrichContent();

    return new JsonObject().put(MessageConstants.CLASS_CONTENTS, renderedContent)
        .put(MessageConstants.COUNT, activityFetcher.fetchTotalContentCount());
  }

  private void fetchClassContents() {
    activityFetcher = ActivityFetcher.buildContentFetcherForOfflineCompletedActivities(command);
    classContents = activityFetcher.fetchContents();
  }


  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
