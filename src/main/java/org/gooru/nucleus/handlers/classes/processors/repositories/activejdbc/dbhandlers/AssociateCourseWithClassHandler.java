package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.classes.app.components.AppConfiguration;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.classes.processors.utils.AppHelper;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 8/2/16.
 */
class AssociateCourseWithClassHandler implements DBHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssociateCourseWithClassHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private AJEntityClass entityClass;
    private String courseVersion;
    private static final String  ASSIGN_COURSE_TO_CLASS = "assign.course.to.class";
    AssociateCourseWithClassHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        // There should be a class id present
        if (context.classId() == null || context.classId().isEmpty() || context.courseId() == null || context.courseId()
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
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LazyList<AJEntityClass> classes = AJEntityClass.where(AJEntityClass.FETCH_QUERY_FILTER, context.classId());
        if (classes.isEmpty()) {
            LOGGER.warn("Not able to find class '{}'", this.context.classId());
            return new ExecutionResult<>(
                MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        this.entityClass = classes.get(0);
        String courseId = this.entityClass.getString(AJEntityClass.COURSE_ID);
        if (courseId != null) {
            LOGGER.warn("Class '{}' is already associated with course '{}' so can't associate with course '{}'",
                this.context.classId(), courseId, this.context.courseId());
            return new ExecutionResult<>(MessageResponseFactory
                .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("class.associated.with.course")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        // Class should be of current version and Class should not be archived
        if (!this.entityClass.isCurrentVersion() || this.entityClass.isArchived()) {
            LOGGER.warn("Class '{}' is either archived or not of current version", context.classId());
            return new ExecutionResult<>(MessageResponseFactory
                .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("class.archived.or.incorrect.version")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return AuthorizerBuilder.buildAssociateCourseWithClassAuthorizer(this.context).authorize(this.entityClass);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        // Set the modifier id and course id
        this.entityClass.setModifierId(this.context.userId());
        this.entityClass.setCourseId(this.context.courseId());
        this.courseVersion = getCourseVersion();
        setContentVisibilityBasedOnCourse();
        setClassSettingsBasedOnCourse();

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

        AppHelper.publishEventForRescopeAndRoute0(this.entityClass, context.accessToken(), this.context.classId(), ASSIGN_COURSE_TO_CLASS, null);
        // Fetch all the class members
        List<String> members = fetchAllMembersOfClass();
        if (!members.isEmpty()) AppHelper.doLpBaselineSave(this.entityClass, this.context.accessToken(), this.context.classId(),
                this.context.courseId(), members);
        
        return new ExecutionResult<>(MessageResponseFactory
            .createNoContentResponse(RESOURCE_BUNDLE.getString("updated"),
                EventBuilderFactory.getCourseAssignedEventBuilder(this.context.classId(), this.context.courseId())),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private void setContentVisibilityBasedOnCourse() {
        String alternateCourseVersion = AppConfiguration.getInstance().getCourseVersionForAlternateVisibility();
        String premiumCourseVersion = AppConfiguration.getInstance().getCourseVersionForPremiumContent();
        if (alternateCourseVersion == null) {
            LOGGER.error("Not able to obtain alternateCourseVersion from application configuration");
        }
        if (Objects.equals(alternateCourseVersion, this.courseVersion) || Objects.equals(premiumCourseVersion, this.courseVersion)) {
            this.entityClass.setContentVisibility(this.entityClass.getDefaultAlternateContentVisibility());
        } else {
            this.entityClass.setContentVisibility(this.entityClass.getDefaultContentVisibility());
        }
    }

    // Set if premium course else reset class settings when user deletes premium course and assigns a non-premium course to class.
    private void setClassSettingsBasedOnCourse() {
        final String settings = this.entityClass.getString(AJEntityClass.SETTING);
        JsonObject classSettings = settings != null ? new JsonObject(settings) : null;
        if (Objects.equals(AppConfiguration.getInstance().getCourseVersionForPremiumContent(), this.courseVersion)) {
            if (classSettings == null) classSettings = new JsonObject();
            classSettings.put(AJEntityClass.COURSE_PREMIUM, true);
        } else if (classSettings != null && classSettings.containsKey(AJEntityClass.COURSE_PREMIUM)) {
            classSettings.remove(AJEntityClass.COURSE_PREMIUM);
            if (classSettings.isEmpty()) classSettings = null;
        }
        this.entityClass.setClassSettings(classSettings);
    }
    
    private String getCourseVersion() {
        final Object versionObject = Base.firstCell(AJEntityCourse.COURSE_VERSION_FETCH_QUERY, this.context.courseId());
        return versionObject == null ? null : String.valueOf(versionObject);
    }

    private List<String> fetchAllMembersOfClass() {
        LazyList<AJClassMember> members =
            AJClassMember.where(AJClassMember.FETCH_ALL_QUERY_FILTER, this.context.classId());
        List<String> memberIdList = new ArrayList<>();
        if (!members.isEmpty()) {
            members.forEach(ajClassMember -> {
                final String ajClassMemberIdString = ajClassMember.getString(AJClassMember.USER_ID);
                if (ajClassMemberIdString != null && !ajClassMemberIdString.isEmpty()) {
                    memberIdList.add(ajClassMemberIdString);
                }
            });
        }
        return memberIdList;
    }

}
