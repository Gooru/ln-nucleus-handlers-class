package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

class FetchClassContentHandler implements DBHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchClassContentHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private JsonArray contentTypes;
    private boolean isStudent;

    FetchClassContentHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        try {
            validateUser();
            contentTypes = context.request().getJsonArray(AJEntityClassContents.CONTENT_TYPE);
            validateClassId();
        } catch (MessageResponseWrapperException mrwe) {
            return new ExecutionResult<>(mrwe.getMessageResponse(), ExecutionResult.ExecutionStatus.FAILED);
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
            AuthorizerBuilder.buildContentMapClassAuthorizer(this.context).authorize(entityClass);
        if (classAuthorize.continueProcessing()) {
            if (contentTypes == null || contentTypes.getString(0) == null || contentTypes.getString(0).isEmpty()) {
                LOGGER.warn("Content Type should be pass for grouping the class contents by type", context.classId());
                return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
                    RESOURCE_BUNDLE.getString("missing.content.type")), ExecutionResult.ExecutionStatus.FAILED);
            }
        } else {
            isStudent = checkStudent(entityClass);
            if (!isStudent) {
                return new ExecutionResult<>(
                    MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        LazyList<AJEntityClassContents> classContents = null;
        if (isStudent) {
            classContents =
                AJEntityClassContents.findBySQL(AJEntityClassContents.SELECT_CLASS_CONTENTS, context.classId());
        } else {
            classContents = AJEntityClassContents.findBySQL(AJEntityClassContents.SELECT_CLASS_CONTENTS_GRP_BY_TYPE,
                context.classId(), contentTypes.getString(0));

        }
        JsonArray results = new JsonArray(JsonFormatterBuilder
            .buildSimpleJsonFormatter(false, AJEntityClassContents.RESPONSE_FIELDS).toJson(classContents));

        return new ExecutionResult<>(
            MessageResponseFactory.createOkayResponse(new JsonObject().put(MessageConstants.CLASS_CONTENTS, results)),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
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

    private void validateClassId() {
        if (context.classId() == null || context.classId().isEmpty()) {
            throw new MessageResponseWrapperException(
                MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.class.id")));
        }
    }

    private boolean checkStudent(AJEntityClass model) {
        LazyList<AJClassMember> members = AJClassMember.where(AJClassMember.FETCH_FOR_USER_QUERY_FILTER,
            this.context.classId(), this.context.userId());
        return !members.isEmpty();
    }
}
