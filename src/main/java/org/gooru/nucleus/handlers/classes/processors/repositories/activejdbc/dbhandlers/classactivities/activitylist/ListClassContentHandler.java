package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.common.validators.SanityValidators;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassDao;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListClassContentHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListClassContentHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private LazyList<AJEntityClassContents> classContents;
  private ListActivityCommand command;

  public ListClassContentHandler(ProcessorContext context) {
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
        classAuthorization = AuthorizerBuilder.buildClassStudentAuthorizer(context)
            .authorize(entityClass);
        if (!classAuthorization.continueProcessing()) {
          return new ExecutionResult<>(
              MessageResponseFactory
                  .createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
              ExecutionResult.ExecutionStatus.FAILED);
        }
        studentAuthorization = true;
      }
      command = new ListActivityCommand(context, studentAuthorization);
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
    JsonArray renderedContent = new ContentRenderer(classContents, command).renderContent();

    return new ExecutionResult<>(
        MessageResponseFactory
            .createOkayResponse(
                new JsonObject().put(MessageConstants.CLASS_CONTENTS, renderedContent)),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private void fetchClassContents() {
    if (command.fetchingAllContentTypesButOffline()) {
      classContents = ActivityFetcher.buildAllNonOfflineActivitiesFetcher(command).fetchContents();
    } else if (command.fetchingSpecificContentType()) {
      classContents = ActivityFetcher.buildSpecificContentTypeActivitiesFetcher(command)
          .fetchContents();
    } else if (command.fetchingOfflineContentType()) {
      classContents = ActivityFetcher.buildOfflineActivitiesFetcher(command).fetchContents();
    }
  }


  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
