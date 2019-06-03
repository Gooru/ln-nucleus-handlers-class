package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitycontentcompletion;

import io.vertx.core.json.JsonObject;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.postprocessors.OACompletionPostProcessorPayload;
import org.gooru.nucleus.handlers.classes.processors.postprocessors.PostProcessorEventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.common.validators.SanityValidators;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassContentsDao;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassDao;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

public class ClassContentCompletionMarkerHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ClassContentCompletionMarkerHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private AJEntityClassContents classContents;
  private String contentId;
  private AJEntityClass entityClass;

  public ClassContentCompletionMarkerHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      SanityValidators.validateUser(context);
      SanityValidators.validateClassId(context);
      contentId = SanityValidators.validateAndFetchContentId(context);
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    try {
      entityClass = EntityClassDao.fetchClassById(context.classId());
      classContents = EntityClassContentsDao
          .fetchActivityByIdAndClass(contentId, context.classId());
      validateCompletionCriteria();
      return AuthorizerBuilder.buildClassContentCompletionAuthorizer(context)
          .authorize(this.entityClass);
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    classContents.setCompleted();
    boolean result = classContents.save();
    if (!result) {
      if (classContents.hasErrors()) {
        LOGGER.warn("Error in completing activity for class");
        return new ExecutionResult<>(
            MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
            ExecutionStatus.FAILED);
      }
    }

    return returnResponseWithPostProcessorEvent();
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private void validateCompletionCriteria() {
    if (!classContents.isActivityOffline()) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("not.offline.activity")));
    }
    if (!classContents.isOfflineActivityActive()) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("offline.activity.not.active")));
    }
  }

  private ExecutionResult<MessageResponse> returnResponseWithPostProcessorEvent() {
    OACompletionPostProcessorPayload postProcessorPayload = new OACompletionPostProcessorPayload()
        .setClassId(context.classId()).setOAId(contentId)
        .setOADcaId(classContents.getDcaId());

    return new ExecutionResult<>(MessageResponseFactory
        .createNoContentResponse(RESOURCE_BUNDLE.getString("updated"), EventBuilderFactory
                .getClassContentCompletionEventBuilder(classContents.getId(), this.context.classId()),
            PostProcessorEventBuilderFactory.buildOACompletionEvent(postProcessorPayload)),
        ExecutionStatus.SUCCESSFUL);
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.classContents.errors().entrySet()
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }


}
