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
import org.gooru.nucleus.handlers.classes.processors.utils.AppHelper;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

class UpdateRescopeClassSettingHandler implements DBHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateRescopeClassSettingHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private AJEntityClass entityClass;
    private boolean rescope;

    UpdateRescopeClassSettingHandler(ProcessorContext context) {
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

        // Rescope field should not be null or empty
        final Boolean rescope = context.request().getBoolean(AJEntityClass.RESCOPE);
        if (rescope == null) {
            LOGGER.warn("Missing class rescope setting value");
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.class.rescope.setting")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        this.rescope = rescope;
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
        // Class rescope setting won't allow to turn off once it's turn on.
        if (classSettings != null && classSettings.getBoolean(AJEntityClass.RESCOPE) && !rescope) {
            LOGGER.warn("Rescope already turned on to this class {}, not allowed to turn off", context.classId());
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("class.rescope.not.allowed.turn.off")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        return AuthorizerBuilder.buildUpdateClassAuthorizer(this.context).authorize(this.entityClass);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        this.entityClass.setModifierId(this.context.userId());
        final String settings = this.entityClass.getString(AJEntityClass.SETTING);
        final JsonObject classSettings = settings != null ? new JsonObject(settings) : new JsonObject();
        classSettings.put(AJEntityClass.RESCOPE, rescope);
        this.entityClass.setClassSettings(classSettings);
        if (rescope) {
            this.entityClass.setContentVisibility(AJEntityClass.CONTENT_VISIBILITY_TYPE_VISIBLE_ALL);
        }
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
        AppHelper.publishEventForRescope(context.accessToken(), context.classId(), AJEntityClass.RESCOPE, null);
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("updated"),
                EventBuilderFactory.getUpdateClassRescopeSettingEventBuilder(context.classId(), rescope)),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);

    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }
    
    
}
