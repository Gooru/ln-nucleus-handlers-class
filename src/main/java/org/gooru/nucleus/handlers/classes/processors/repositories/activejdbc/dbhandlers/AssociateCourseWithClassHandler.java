package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;
import org.gooru.nucleus.handlers.classes.app.components.AppConfiguration;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.common.milestone.MilestoneQueuer;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityTaxonomySubject;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 8/2/16.
 */
class AssociateCourseWithClassHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(AssociateCourseWithClassHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private AJEntityClass entityClass;
  private String courseVersion;
  private AJEntityCourse entityCourse;
  private String frameworkForCourse;

  AssociateCourseWithClassHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be a class id present
    if (context.classId() == null || context.classId().isEmpty() || context.courseId() == null
        || context.courseId()
        .isEmpty()) {
      LOGGER.warn("Missing class/course id");
      return new ExecutionResult<>(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class.or.course")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty() || context.userId()
        .equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to edit class");
      return new ExecutionResult<>(
          MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Payload should not be null but it should be empty
    if (context.request() == null || !context.request().isEmpty()) {
      LOGGER.warn("Null or non empty payload supplied to edit class");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")),
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
    this.entityClass = classes.get(0);
    String courseId = this.entityClass.getString(AJEntityClass.COURSE_ID);
    if (courseId != null) {
      LOGGER.warn(
          "Class '{}' is already associated with course '{}' so can't associate with course '{}'",
          this.context.classId(), courseId, this.context.courseId());
      return new ExecutionResult<>(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("class.associated.with.course")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Class should be of current version and Class should not be archived
    if (!this.entityClass.isCurrentVersion() || this.entityClass.isArchived()) {
      LOGGER.warn("Class '{}' is either archived or not of current version", context.classId());
      return new ExecutionResult<>(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("class.archived.or.incorrect.version")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Downstream authorizer to handle the course validation
    this.entityCourse =
        AJEntityCourse.findFirst(AJEntityCourse.COURSE_ASSOCIATION_FILTER, context.courseId());

    return AuthorizerBuilder.buildAssociateCourseWithClassAuthorizer(this.context, entityCourse)
        .authorize(this.entityClass);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // Set the modifier id and course id
    this.entityClass.setModifierId(this.context.userId());
    this.entityClass.setCourseId(this.context.courseId());
    this.courseVersion = getCourseVersion();
    setContentVisibilityBasedOnCourse();
    setClassAttributesBasedOnCourse();

    boolean result = this.entityClass.save();
    if (!result) {
      LOGGER.error("Class with id '{}' failed to save", context.classId());
      if (this.entityClass.hasErrors()) {
        Map<String, String> map = this.entityClass.errors();
        JsonObject errors = new JsonObject();
        map.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    }
    triggerPostProcess();
    return new ExecutionResult<>(MessageResponseFactory
        .createNoContentResponse(RESOURCE_BUNDLE.getString("updated"),
            EventBuilderFactory
                .getCourseAssignedEventBuilder(this.context.classId(), this.context.courseId())),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private void triggerPostProcess() {
    // If course is premium and class destination is set, trigger the milestone creation
    if (isCoursePremium() && this.entityClass.getGradeCurrent() != null) {
      LOGGER.info("Will trigger milestone creation");
      MilestoneQueuer.build().enqueue(UUID.fromString(this.context.courseId()), frameworkForCourse);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private void setContentVisibilityBasedOnCourse() {
    String alternateCourseVersion = AppConfiguration.getInstance()
        .getCourseVersionForAlternateVisibility();
    String premiumCourseVersion = AppConfiguration.getInstance()
        .getCourseVersionForPremiumContent();
    if (alternateCourseVersion == null) {
      LOGGER.error("Not able to obtain alternateCourseVersion from application configuration");
    }
    if (Objects.equals(alternateCourseVersion, this.courseVersion) || Objects
        .equals(premiumCourseVersion, this.courseVersion)) {
      this.entityClass
          .setContentVisibility(this.entityClass.getDefaultAlternateContentVisibility());
    } else {
      this.entityClass.setContentVisibility(this.entityClass.getDefaultContentVisibility());
    }
  }

  // Set if premium course else reset class settings when user deletes premium course and assigns a non-premium course to class.
  private void setClassAttributesBasedOnCourse() {
    setClassSettingsBasedOnCourseVersion();
    setClassMilestoneViewApplicabilityBasedOnCourseVersion();
    setClassPrefsBasedOnCourse();
    setClassPrimaryLangFromCourse();
  }

  private void setClassMilestoneViewApplicabilityBasedOnCourseVersion() {
    this.entityClass.setMilestoneViewApplicable(isCoursePremium());
  }

  private void setClassSettingsBasedOnCourseVersion() {
    final String settings = this.entityClass.getString(AJEntityClass.SETTING);
    JsonObject classSettings = settings != null ? new JsonObject(settings) : new JsonObject();
    if (isCoursePremium()) {
      classSettings.put(AJEntityClass.COURSE_PREMIUM, true);
    } else if (!classSettings.isEmpty() && classSettings
        .containsKey(AJEntityClass.COURSE_PREMIUM)) {
      classSettings.remove(AJEntityClass.COURSE_PREMIUM);
    }
    this.entityClass.setClassSettings(!classSettings.isEmpty() ? classSettings : null);
  }

  private boolean isCoursePremium() {
    return Objects.equals(AppConfiguration.getInstance().getCourseVersionForPremiumContent(),
        this.courseVersion);
  }

  private void setClassPrimaryLangFromCourse() {
    Integer primaryLanguage = this.entityCourse.getPrimaryLanguage();
    this.entityClass.setInteger(AJEntityClass.PRIMARY_LANGUAGE, primaryLanguage);
  }

  private void setClassPrefsBasedOnCourse() {
    String subjectBucket = this.entityCourse.getSubjectBucket();
    if (subjectBucket != null && !subjectBucket.trim().isEmpty()) {
      Long count = Base.count(AJEntityTaxonomySubject.TABLE,
          AJEntityTaxonomySubject.FETCH_SUBJECT_BY_ID, subjectBucket);
      if (count == 1) {
        long countDots = subjectBucket.chars().filter(ch -> ch == '.').count();
        JsonObject preference = new JsonObject();
        if (countDots > 1) {
          frameworkForCourse = subjectBucket.substring(0, subjectBucket.indexOf('.'));
          preference.put(AJEntityTaxonomySubject.RESP_KEY_FRAMEWORK, frameworkForCourse);
          preference.put(AJEntityTaxonomySubject.RESP_KEY_SUBJECT,
              subjectBucket.substring(subjectBucket.indexOf('.') + 1));
        } else {
          preference.putNull(AJEntityTaxonomySubject.RESP_KEY_FRAMEWORK);
          preference.put(AJEntityTaxonomySubject.RESP_KEY_SUBJECT, subjectBucket);
        }
        this.entityClass.setClassPreference(preference);
      } else {
        LOGGER.warn(
            "subject associated with course does not exists in database, skipping preference setting of course");
      }
    } else {
      LOGGER.debug("no subject associated with the course, skipping preference setting of course");
    }
  }

  private String getCourseVersion() {
    return this.entityCourse.getVersion();
  }
}
