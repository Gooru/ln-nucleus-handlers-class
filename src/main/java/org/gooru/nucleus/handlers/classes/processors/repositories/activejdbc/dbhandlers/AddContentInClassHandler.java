package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AddContentInClassHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(AddContentInClassHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private AJEntityClassContents classContents;
  private String contentType;
  private String contentId;
  private int forYear;
  private int forMonth;
  private String dcaDateString;

  AddContentInClassHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      validateUser();
      validateClassId();
      initializeFields();
      validateContextRequest();
      validateContextRequestFields();
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityClass> classes = AJEntityClass
        .where(AJEntityClass.FETCH_QUERY_FILTER, context.classId());
    if (classes.isEmpty()) {
      LOGGER.warn("Not able to find class '{}'", this.context.classId());
      return new ExecutionResult<>(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    AJEntityClass entityClass = classes.get(0);
    if (!entityClass.isCurrentVersion() || entityClass.isArchived()) {
      LOGGER.warn("Class '{}' is either archived or not of current version", context.classId());
      return new ExecutionResult<>(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("class.archived.or.incorrect.version")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

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
      LOGGER.debug("Pretending to add content, id: {}, type: {}", contentId, contentType);
      return pretendToAddContentToClass(content);
    } else {
      LOGGER.debug("Actually adding content, id: {}, type: {}", contentId, contentType);
      return reallyAddContentToClass();
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private ExecutionResult<MessageResponse> contentDrivenAuthorization() {
    LazyList<AJEntityContent> entityContents = AJEntityContent
        .findBySQL(AJEntityContent.SELECT_CONTENT_TO_AUTHORIZE, contentId);
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
        .findBySQL(AJEntityCollection.SELECT_COLLECTION_TO_AUTHORIZE, contentId);
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
              contentId, contentType,
              classContents.getDcaAddedDate());
    } else {
      return AJEntityClassContents
          .findFirst(AJEntityClassContents.SELECT_DUPLICATED_ADDED_CONTENT_FOR_MONTH,
              context.classId(), contentId, contentType, forMonth, forYear);
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
                    contentId, contentType)), ExecutionStatus.SUCCESSFUL);
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

  private void validateContextRequest() {
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("Empty payload supplied to create content map for class");
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("empty.payload")));
    }
  }

  private void validateClassId() {
    if (context.classId() == null || context.classId().isEmpty()) {
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.class.id")));
    }
  }

  private boolean isContentTypeEntityIsCollection() {
    return (AJEntityClassContents.ASSESSMENT_TYPES.matcher(contentType).matches() || 
        AJEntityClassContents.COLLECTION_TYPES.matcher(contentType).matches());
  }

  private boolean isContentTypeEntityIsContent() {
    return (contentType.equalsIgnoreCase(AJEntityClassContents.RESOURCE) || contentType
        .equalsIgnoreCase(AJEntityClassContents.QUESTION));
  }

  private void validateContextRequestFields() {
    JsonObject errors = new DefaultPayloadValidator()
        .validatePayload(context.request(), AJEntityClassContents.createFieldSelector(),
            AJEntityClassContents.getValidatorRegistry());
    if (errors != null && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createValidationErrorResponse(errors));
    }
    validateDependentFields();
  }

  private void validateDependentFields() {

    LocalDate firstOfThisMonth = LocalDate.now().withDayOfMonth(1);
    LocalDate firstOfSpecifiedMonthYear = LocalDate.of(forYear, forMonth, 1);

    // Do not allow past month-year
    if (firstOfThisMonth.isAfter(firstOfSpecifiedMonthYear)) {
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("dca.past.monthyear")));
    }

    if (dcaDateString != null) {
      // Parsing won't fail as validator registry has done the job
      LocalDate date = LocalDate.parse(dcaDateString, DateTimeFormatter.ISO_LOCAL_DATE);
      if (date.getMonthValue() != forMonth || date.getYear() != forYear) {
        throw new MessageResponseWrapperException(
            MessageResponseFactory
                .createInvalidRequestResponse(
                    RESOURCE_BUNDLE.getString("dca.monthyear.addeddate.mismatch")));
      }
    }
  }

  private void initializeFields() {
    dcaDateString = context.request().getString(AJEntityClassContents.DCA_ADDED_DATE);
    contentType = context.request().getString(AJEntityClassContents.CONTENT_TYPE);
    contentId = context.request().getString(AJEntityClassContents.CONTENT_ID);
    forMonth = context.request().getInteger(AJEntityClassContents.FOR_MONTH);
    forYear = context.request().getInteger(AJEntityClassContents.FOR_YEAR);
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.classContents.errors().entrySet()
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }

  private static class DefaultPayloadValidator implements PayloadValidator {

  }

  private static class DefaultAJEntityClassContentsBuilder implements
      EntityBuilder<AJEntityClassContents> {

  }

}
