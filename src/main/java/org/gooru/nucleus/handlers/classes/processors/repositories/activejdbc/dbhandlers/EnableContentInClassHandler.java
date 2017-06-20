package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

class EnableContentInClassHandler implements DBHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnableContentInClassHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private AJEntityClassContents classContents;
    private String contentId;
    private String activationDate;

    EnableContentInClassHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        try {
            validateUser();
            validateClassId();
            validateAndInitializeContentId();
            validateContextRequestFields();
            activationDate = context.request().getString(AJEntityClassContents.ACTIVATION_DATE);
        } catch (MessageResponseWrapperException mrwe) {
            return new ExecutionResult<>(mrwe.getMessageResponse(), ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LazyList<AJEntityClassContents> classContents =
            AJEntityClassContents.where(AJEntityClassContents.FETCH_CLASS_CONTENT, contentId, context.classId());
        if (classContents.isEmpty()) {
            LOGGER.warn("content {} not add to this class  {}", contentId, context.classId());
            return new ExecutionResult<>(
                MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        this.classContents = classContents.get(0);
        if (this.classContents.getActivationDate() != null) {
            LOGGER.warn("content {} already activated to this class {}", contentId, context.classId());
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("already.class.content.activated")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        try {
            validateActivationDate();
        } catch (MessageResponseWrapperException mrwe) {
            return new ExecutionResult<>(mrwe.getMessageResponse(), ExecutionResult.ExecutionStatus.FAILED);
        }
        LazyList<AJEntityClassContents> ajClassContents =
            AJEntityClassContents.findBySQL(AJEntityClassContents.SELECT_CLASS_CONTENTS_TO_VALIDATE, context.classId(),
                this.classContents.getContentId(), activationDate);
        if (!ajClassContents.isEmpty()) {
            LOGGER.warn("For this calss {} same content {} already activated for this date {}", context.classId(),
                this.classContents.getContentId(), activationDate);
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("same.content.already.activated")),
                ExecutionResult.ExecutionStatus.FAILED);
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
        return AuthorizerBuilder.buildClassContentAuthorizer(this.context).authorize(entityClass);

    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        new DefaultAJEntityClassContentsBuilder().build(this.classContents, context.request(),
            AJEntityClassContents.getConverterRegistry());
        boolean result = this.classContents.save();
        if (!result) {
            if (classContents.hasErrors()) {
                LOGGER.warn("error in creating content map for class");
                return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                    ExecutionStatus.FAILED);
            }
        }
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("updated"),
                EventBuilderFactory.getClassContentEnableEventBuilder(classContents.getId(), this.context.classId())),
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

    private void validateAndInitializeContentId() {
        try {
            contentId = context.requestHeaders().get(AJEntityClassContents.ID_CONTENT);
            new BigInteger(contentId);
            if (contentId == null) {
                throw new MessageResponseWrapperException(
                    MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.content.id")));
            }
        } catch (Exception e) {
            throw new MessageResponseWrapperException(MessageResponseFactory
                .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class.content.id")));
        }
    }

    private void validateContextRequestFields() {
        JsonObject errors = new DefaultPayloadValidator().validatePayload(context.request(),
            AJEntityClassContents.updateFieldSelector(), AJEntityClassContents.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            throw new MessageResponseWrapperException(MessageResponseFactory.createValidationErrorResponse(errors));
        }
    }

    private void validateActivationDate() {
        try {
            if (activationDate != null) {
                activationDate = LocalDate.parse(this.activationDate).toString();
            } else {
                // setting default value of today's date, if activation date is
                // not set.
                activationDate = LocalDate.now().toString();
            }
        } catch (DateTimeParseException e) {
            LOGGER.warn("Invalid activation date format {}", this.activationDate);
            throw new MessageResponseWrapperException(MessageResponseFactory
                .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.activation.date.format")));
        }
        // toString() method will extract the date only (yyyy-mm-dd)
        final String createdDate = this.classContents.getCreatedDate().toString();
        if (!activationDate.equals(createdDate)) {
            LOGGER.warn("Activation date {} should be same as class" + " content creation date {}", activationDate,
                createdDate);
            throw new MessageResponseWrapperException(MessageResponseFactory
                .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("activation.date.not.same.as.creation.date")));

        }
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
