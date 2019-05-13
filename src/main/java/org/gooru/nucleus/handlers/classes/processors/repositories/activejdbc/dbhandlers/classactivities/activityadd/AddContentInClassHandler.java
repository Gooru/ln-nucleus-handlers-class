package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activityadd;

import io.vertx.core.json.JsonObject;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.common.validators.SanityValidators;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassDao;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddContentInClassHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(AddContentInClassHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private AJEntityClassContents classContents;
  private AJEntityClass entityClass;
  private AddActivityCommand command;

  public AddContentInClassHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      SanityValidators.validateUser(context);
      SanityValidators.validateClassId(context);
      validateContextRequest();
      command = new AddActivityCommand(context.request());
      command.validate();
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
      ExecutionResult<MessageResponse> classAuthorizationResult =
          AuthorizerBuilder.buildClassContentAuthorizer(this.context).authorize(entityClass);
      if (classAuthorizationResult.continueProcessing()) {
        if (isContentTypeEntityIsCollection()) {
          return collectionDrivenAuthorization();
        } else if (isContentTypeEntityIsContent()) {
          return contentDrivenAuthorization();
        }
      } else {
        return classAuthorizationResult;
      }
      return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    classContents = new AJEntityClassContents();
    new DefaultAJEntityClassContentsBuilder()
        .build(this.classContents, context.request(), AJEntityClassContents.getConverterRegistry());
    classContents.setClassId(context.classId());
    classContents.setInitialUsersCount();

    AJEntityClassContents content = findAlreadyAddedContent();
    if (content != null) {
      LOGGER.debug("Pretending to add content, id: {}, type: {}", command.getContentId(),
          command.getContentType());
      return pretendToAddContentToClass(content);
    } else {
      LOGGER.debug("Actually adding content, id: {}, type: {}", command.getContentId(),
          command.getContentType());
      return reallyAddContentToClass();
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private ExecutionResult<MessageResponse> contentDrivenAuthorization() {
    LazyList<AJEntityContent> entityContents = AJEntityContent
        .findBySQL(AJEntityContent.SELECT_CONTENT_TO_AUTHORIZE, command.getContentId());
    AJEntityContent entityContent;
    if (entityContents.isEmpty()) {
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(
          RESOURCE_BUNDLE.getString("content.not.found")), ExecutionStatus.FAILED);
    }
    entityContent = entityContents.get(0);
    if (entityContent.getCourseId() != null) {
      LazyList<AJEntityCourse> entityCourses = AJEntityCourse
          .findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE, entityContent.getCourseId());
      return AuthorizerBuilder.buildTenantReadCourseAuthorizer(context)
          .authorize(entityCourses.get(0));
    } else if (entityContent.getCollectionId() != null) {
      LazyList<AJEntityCollection> ajEntityCollections = AJEntityCollection
          .findBySQL(AJEntityCollection.SELECT_COLLECTION_TO_AUTHORIZE,
              entityContent.getCollectionId());
      return AuthorizerBuilder.buildTenantReadCollectionAuthorizer(context)
          .authorize(ajEntityCollections.get(0));
    } else {
      return AuthorizerBuilder.buildTenantReadContentAuthorizer(context).authorize(entityContent);
    }
  }

  private ExecutionResult<MessageResponse> collectionDrivenAuthorization() {
    LazyList<AJEntityCollection> ajEntityCollection = AJEntityCollection
        .findBySQL(AJEntityCollection.SELECT_COLLECTION_TO_AUTHORIZE, command.getContentId());
    AJEntityCollection entityCollection;
    if (ajEntityCollection.isEmpty()) {
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(
          RESOURCE_BUNDLE.getString("collection.not.found")), ExecutionStatus.FAILED);
    }
    entityCollection = ajEntityCollection.get(0);
    String courseId = entityCollection.getCourseId();
    if (courseId == null) {
      return AuthorizerBuilder.buildTenantReadCollectionAuthorizer(context)
          .authorize(entityCollection);
    } else {
      LazyList<AJEntityCourse> entityCourses = AJEntityCourse
          .findBySQL(AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE, courseId);
      return AuthorizerBuilder.buildTenantReadCourseAuthorizer(context)
          .authorize(entityCourses.get(0));
    }
  }

  private ExecutionResult<MessageResponse> pretendToAddContentToClass(
      AJEntityClassContents content) {
    return new ExecutionResult<>(
        MessageResponseFactory.createCreatedResponse(content.getString(AJEntityClassContents.ID)),
        ExecutionStatus.SUCCESSFUL);
  }

  private AJEntityClassContents findAlreadyAddedContent() {
    if (classContents.getDcaAddedDate() != null) {
      return AJEntityClassContents
          .findFirst(AJEntityClassContents.SELECT_DUPLICATED_ADDED_CONTENT, context.classId(),
              command.getContentId(), command.getContentType(), classContents.getDcaAddedDate());
    } else {
      return AJEntityClassContents
          .findFirst(AJEntityClassContents.SELECT_DUPLICATED_ADDED_CONTENT_FOR_MONTH,
              context.classId(), command.getContentId(), command.getContentType(),
              command.getForMonth(),
              command.getForYear());
    }
  }

  private ExecutionResult<MessageResponse> reallyAddContentToClass() {
    boolean result = this.classContents.save();
    if (!result) {
      if (classContents.hasErrors()) {
        LOGGER.warn("error in creating content map for class");
        return new ExecutionResult<>(
            MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
            ExecutionStatus.FAILED);
      }
    }
    return new ExecutionResult<>(MessageResponseFactory
        .createCreatedResponse(classContents.getString(AJEntityClassContents.ID),
            EventBuilderFactory
                .getCreateClassContentEventBuilder(
                    classContents.getString(AJEntityClassContents.ID), context.classId(),
                    command.getContentId(), command.getContentType())), ExecutionStatus.SUCCESSFUL);
  }

  private void validateContextRequest() {
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("Empty payload supplied to create content map for class");
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("empty.payload")));
    }
  }

  private boolean isContentTypeEntityIsCollection() {
    return (AJEntityClassContents.ASSESSMENT_TYPES.matcher(command.getContentType()).matches() ||
        AJEntityClassContents.COLLECTION_TYPES.matcher(command.getContentType()).matches());
  }

  private boolean isContentTypeEntityIsContent() {
    return (command.getContentType().equalsIgnoreCase(AJEntityClassContents.RESOURCE) || command
        .getContentType().equalsIgnoreCase(AJEntityClassContents.QUESTION));
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.classContents.errors().entrySet()
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }

  private static class DefaultAJEntityClassContentsBuilder implements
      EntityBuilder<AJEntityClassContents> {

  }
}
