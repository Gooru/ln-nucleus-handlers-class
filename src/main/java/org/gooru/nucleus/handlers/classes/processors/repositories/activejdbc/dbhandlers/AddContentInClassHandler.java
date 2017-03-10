package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

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
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityLesson;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityUnit;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

class AddContentInClassHandler implements DBHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddContentInClassHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private AJEntityClassContents classContents;
    private String ctxCourseId;
    private String ctxUnitId;
    private String ctxLessonId;
    private String ctxCollectionId;
    private String contentType;
    private String contentId;

    AddContentInClassHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        try {
            validateUser();
            validateClassId();
            contentType = context.request().getString(AJEntityClassContents.CONTENT_TYPE);
            contentId = context.request().getString(AJEntityClassContents.CONTENT_ID);
            ctxCourseId = context.request().getString(AJEntityClassContents.CTX_COURSE_ID);
            ctxUnitId = context.request().getString(AJEntityClassContents.CTX_UNIT_ID);
            ctxLessonId = context.request().getString(AJEntityClassContents.CTX_LESSON_ID);
            ctxCollectionId = context.request().getString(AJEntityClassContents.CTX_COLLECTION_ID);
            validateContextRequest();
            validateContextRequestFields();
            validateCtxULCIdsIfCtxCourseIdExists();
        } catch (MessageResponseWrapperException mrwe) {
            return new ExecutionResult<>(mrwe.getMessageResponse(), ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LazyList<AJEntityClassContents> classContents = AJEntityClassContents
            .findBySQL(AJEntityClassContents.SELECT_CLASS_CONTENTS_TO_VALIDATE, context.classId(), contentId);
        if (!classContents.isEmpty()) {
            LOGGER.warn("class {} already added with this content {}", context.classId(), contentId);
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
                RESOURCE_BUNDLE.getString("class.add.with.content")), ExecutionResult.ExecutionStatus.FAILED);
        }

        LazyList<AJEntityClass> classes = AJEntityClass.where(AJEntityClass.FETCH_QUERY_FILTER, context.classId());
        if (classes.isEmpty()) {
            LOGGER.warn("Not able to find class '{}'", this.context.classId());
            return new ExecutionResult<>(
                MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        AJEntityClass entityClass = classes.get(0);
        // Class should be of current version and Class should not be archived
        if (!entityClass.isCurrentVersion() || entityClass.isArchived()) {
            LOGGER.warn("Class '{}' is either archived or not of current version", context.classId());
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("class.archived.or.incorrect.version")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        ExecutionResult<MessageResponse> classAuthorize =
            AuthorizerBuilder.buildClassContentAuthorizer(this.context).authorize(entityClass);
        if (classAuthorize.continueProcessing()) {
            if (ctxCourseId != null) {
                LazyList<AJEntityCourse> ajEntityCourse = AJEntityCourse.findBySQL(
                    AJEntityCourse.SELECT_COURSE_TO_AUTHORIZE, ctxCourseId, context.userId(), context.userId());
                if (ajEntityCourse.isEmpty()) {
                    LOGGER
                        .warn("user is not owner or collaborator of context course to create class contents. aborting");
                    return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(
                        RESOURCE_BUNDLE.getString("course.not.found.or.not.available")), ExecutionStatus.FAILED);
                }
                LazyList<AJEntityUnit> ajEntityUnit =
                    AJEntityUnit.findBySQL(AJEntityUnit.SELECT_UNIT_TO_VALIDATE, ctxUnitId, ctxCourseId);
                if (ajEntityUnit.isEmpty()) {
                    LOGGER.warn("Context unit {} not found, aborting", ctxUnitId);
                    return new ExecutionResult<>(
                        MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("ctxunit.not.found")),
                        ExecutionStatus.FAILED);
                }
                LazyList<AJEntityLesson> ajEntityLesson = AJEntityLesson
                    .findBySQL(AJEntityLesson.SELECT_LESSON_TO_VALIDATE, ctxLessonId, ctxUnitId, ctxCourseId);
                if (ajEntityLesson.isEmpty()) {
                    LOGGER.warn("Context lesson {} not found, aborting", ctxLessonId);
                    return new ExecutionResult<>(
                        MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("ctxlesson.not.found")),
                        ExecutionStatus.FAILED);
                }
                if (isContentTypeEntityIsCollection()) {
                    LazyList<AJEntityCollection> ajEntityCollection =
                        AJEntityCollection.findBySQL(AJEntityCollection.SELECT_CUL_COLLECTION_TO_VALIDATE, contentId,
                            ctxLessonId, ctxUnitId, ctxCourseId, contentType);
                    if (ajEntityCollection.isEmpty()) {
                        LOGGER.warn("Content type collection {} not found, aborting", contentId);
                        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(
                            RESOURCE_BUNDLE.getString("collection.not.found")), ExecutionStatus.FAILED);
                    }
                }
                if (isContentTypeEntityIsContent()) {
                    LazyList<AJEntityContent> ajEntityContent =
                        AJEntityContent.findBySQL(AJEntityContent.SELECT_CULC_CONTENT_TO_VALIDATE, contentId,
                            ctxCollectionId, ctxLessonId, ctxUnitId, ctxCourseId, contentType);
                    if (ajEntityContent.isEmpty()) {
                        LOGGER.warn("Context collection {} not found, aborting", ctxCollectionId);
                        return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(
                            RESOURCE_BUNDLE.getString("content.not.found")), ExecutionStatus.FAILED);
                    }
                }

            } else if (isContentTypeEntityIsCollection()) {
                LazyList<AJEntityCollection> ajEntityCollection = AJEntityCollection.findBySQL(
                    AJEntityCollection.SELECT_COLLECTION_TO_AUTHORIZE, contentId, context.userId(), context.userId());
                if (ajEntityCollection.isEmpty()) {
                    LOGGER.warn(
                        "user is not owner or collaborator of content type collection to create class contents. aborting",
                        contentId);
                    return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(
                        RESOURCE_BUNDLE.getString("collection.not.found")), ExecutionStatus.FAILED);
                }
            } else if (isContentTypeEntityIsContent() && ctxCollectionId != null) {
                LazyList<AJEntityCollection> ajEntityCollection =
                    AJEntityCollection.findBySQL(AJEntityCollection.SELECT_COLLECTION_TO_AUTHORIZE, ctxCollectionId,
                        context.userId(), context.userId());
                if (ajEntityCollection.isEmpty()) {
                    LOGGER.warn(
                        "user is not owner or collaborator of context collection to create class contents. aborting",
                        ctxCollectionId);
                    return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse(
                        RESOURCE_BUNDLE.getString("ctxcollection.not.found")), ExecutionStatus.FAILED);
                }
                LazyList<AJEntityContent> ajEntityContent = AJEntityContent.findBySQL(
                    AJEntityContent.SELECT_COLLECTION_CONTENT_TO_VALIDATE, contentId, ctxCollectionId, contentType);
                if (ajEntityContent.isEmpty()) {
                    LOGGER.warn("content {} not found, aborting", contentId);
                    return new ExecutionResult<>(
                        MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("content.not.found")),
                        ExecutionStatus.FAILED);
                }

            } else if (isContentTypeEntityIsContent() && ctxCollectionId == null) {
                LazyList<AJEntityContent> ajEntityContent =
                    AJEntityContent.findBySQL(AJEntityContent.SELECT_CONTENT_TO_VALIDATE, contentId, contentType);
                if (ajEntityContent.isEmpty()) {
                    LOGGER.warn("content {} not found, aborting", contentId);
                    return new ExecutionResult<>(
                        MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("content.not.found")),
                        ExecutionStatus.FAILED);
                }
            }
        } else {
            return classAuthorize;
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        classContents = new AJEntityClassContents();
        new DefaultAJEntityClassContentsBuilder().build(this.classContents, context.request(),
            AJEntityClassContents.getConverterRegistry());
        classContents.set(AJEntityClassContents.SEQUENCE, getSequenceId());
        classContents.setClassId(context.classId());
        boolean result = this.classContents.save();
        if (!result) {
            if (classContents.hasErrors()) {
                LOGGER.warn("error in creating content map for class");
                return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                    ExecutionStatus.FAILED);
            }
        }
        return new ExecutionResult<>(
            MessageResponseFactory.createCreatedResponse(context.classId(),
                EventBuilderFactory.getCreateClassContentEventBuilder(context.classId(), contentId, contentType)),
            ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private void validateUser() {
        if ((context.userId() == null) || context.userId().isEmpty()
            || MessageConstants.MSG_USER_ANONYMOUS.equalsIgnoreCase(context.userId())) {
            LOGGER.warn("Invalid user");
            throw new MessageResponseWrapperException(
                MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")));
        }
    }

    private void validateContextRequest() {
        if (context.request() == null || context.request().isEmpty()) {
            LOGGER.warn("Empty payload supplied to create content map for class");
            throw new MessageResponseWrapperException(
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("empty.payload")));
        }
    }

    private void validateClassId() {
        if (context.classId() == null || context.classId().isEmpty()) {
            throw new MessageResponseWrapperException(
                MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.class.id")));
        }
    }

    private void validateCtxULCIdsIfCtxCourseIdExists() {
        if (ctxCourseId != null) {
            if (ctxUnitId == null || ctxUnitId.isEmpty()) {
                throw new MessageResponseWrapperException(
                    MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.ctx.unit.id")));
            }
            if (ctxLessonId == null || ctxLessonId.isEmpty()) {
                throw new MessageResponseWrapperException(
                    MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.ctx.lesson.id")));
            }
            if (isContentTypeEntityIsCollection()) {
                if (ctxCollectionId != null) {
                    throw new MessageResponseWrapperException(MessageResponseFactory.createInvalidRequestResponse(
                        "Context collection id exists in request for content type resource/question"));
                }
            }
            if (isContentTypeEntityIsContent()) {
                if (ctxCollectionId == null || ctxCollectionId.isEmpty()) {
                    throw new MessageResponseWrapperException(MessageResponseFactory
                        .createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.ctx.collection.id")));
                }
            }
        }
    }

    private boolean isContentTypeEntityIsCollection() {
        return (contentType.equalsIgnoreCase(AJEntityClassContents.ASSESSMENT)
            || contentType.equalsIgnoreCase(AJEntityClassContents.COLLECTION));
    }

    private boolean isContentTypeEntityIsContent() {
        return (contentType.equalsIgnoreCase(AJEntityClassContents.RESOURCE)
            || contentType.equalsIgnoreCase(AJEntityClassContents.QUESTION));
    }

    private void validateContextRequestFields() {
        JsonObject errors = new DefaultPayloadValidator().validatePayload(context.request(),
            AJEntityClassContents.createFieldSelector(), AJEntityClassContents.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            throw new MessageResponseWrapperException(MessageResponseFactory.createValidationErrorResponse(errors));
        }
    }

    private int getSequenceId() {
        Object maxSequenceId =
            Base.firstCell(AJEntityClassContents.SELECT_CLASS_CONTENT_MAX_SEQUENCEID, context.classId());
        int sequenceId = 1;
        if (maxSequenceId != null) {
            sequenceId = Integer.valueOf(maxSequenceId.toString()) + 1;
        }
        return sequenceId;
    }

    private JsonObject getModelErrors() {
        JsonObject errors = new JsonObject();
        this.classContents.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
        return errors;
    }

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

    private static class DefaultAJEntityClassContentsBuilder implements EntityBuilder<AJEntityClassContents> {
    }

}
