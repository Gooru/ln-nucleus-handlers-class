package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

class UpdateRoute0ClassSettingHandler implements DBHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateRoute0ClassSettingHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private AJEntityClass entityClass;
    private boolean route0;

    UpdateRoute0ClassSettingHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        // There should be a class id present
        if (context.classId() == null || context.classId().isEmpty()) {
            LOGGER.warn("Missing class");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.class.id")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        // The user should not be anonymous
        if (context.userId() == null || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            LOGGER.warn("Anonymous user attempting to edit class");
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        // Route0 field should not be null or empty
        final Boolean route0 = context.request().getBoolean(AJEntityClass.ROUTE0);
        if (route0 == null) {
            LOGGER.warn("Missing class route0 setting value");
            return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
                RESOURCE_BUNDLE.getString("missing.class.route0.setting")), ExecutionResult.ExecutionStatus.FAILED);
        }
        this.route0 = route0;
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
        // Class should be of current version and Class should not be archived
        if (!this.entityClass.isCurrentVersion() || this.entityClass.isArchived()) {
            LOGGER.warn("Class '{}' is either archived or not of current version", context.classId());
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("class.archived.or.incorrect.version")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        final String settings = this.entityClass.getString(AJEntityClass.SETTING);
        final JsonObject classSettings = settings != null ? new JsonObject(settings) : null;
        // Class route0 setting won't allow to turn off once it's turn on.
        if (classSettings != null && classSettings.getBoolean(AJEntityClass.ROUTE0) && !route0) {
            LOGGER.warn("Route0 already turned on to this class {}, not allowed to turn off", context.classId());
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("class.route0.not.allowed.turn.off")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        return AuthorizerBuilder.buildUpdateClassAuthorizer(this.context).authorize(this.entityClass);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        this.entityClass.setModifierId(this.context.userId());
        final String settings = this.entityClass.getString(AJEntityClass.SETTING);
        final JsonObject classSettings = settings != null ? new JsonObject(settings) : new JsonObject();
        classSettings.put(AJEntityClass.ROUTE0, route0);
        this.entityClass.setClassSettings(classSettings);
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
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("updated"),
                EventBuilderFactory.getUpdateClassRoute0SettingEventBuilder(context.classId(), route0)),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);

    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

}
