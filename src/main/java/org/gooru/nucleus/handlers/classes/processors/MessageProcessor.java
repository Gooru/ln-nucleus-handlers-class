package org.gooru.nucleus.handlers.classes.processors;

import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.commands.CommandProcessorBuilder;
import org.gooru.nucleus.handlers.classes.processors.exceptions.VersionDeprecatedException;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

class MessageProcessor implements Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final Message<Object> message;
    private String userId;
    private JsonObject session;
    private JsonObject request;
    private String accessToken;

    public MessageProcessor(Message<Object> message) {
        this.message = message;
    }

    @Override
    public MessageResponse process() {

        try {
            ExecutionResult<MessageResponse> validateResult = validateAndInitialize();
            if (validateResult.isCompleted()) {
                return validateResult.result();
            }
            final String msgOp = message.headers().get(MessageConstants.MSG_HEADER_OP);
            return CommandProcessorBuilder.lookupBuilder(msgOp).build(createContext()).process();
        } catch (VersionDeprecatedException e) {
            LOGGER.error("Version is deprecated");
            return MessageResponseFactory.createVersionDeprecatedResponse();
        } catch (Throwable e) {
            LOGGER.error("Unhandled exception in processing", e);
            return MessageResponseFactory.createInternalErrorResponse(RESOURCE_BUNDLE.getString("unexpected.error"));
        }
    }

    private ProcessorContext createContext() {
        MultiMap headers = message.headers();
        String classId = headers.get(MessageConstants.CLASS_ID);
        String classCode = headers.get(MessageConstants.CLASS_CODE);
        String courseId = headers.get(MessageConstants.COURSE_ID);
        String studentId = message.headers().get(MessageConstants.USER_ID);
        String studentEmail = message.headers().get(MessageConstants.EMAIL);
        return new ProcessorContext.ProcessorContextBuilder(userId, session, request, classId, classCode, headers)
            .setCourseId(courseId).setStudentId(studentId).setStudentEmail(studentEmail).setAccessToken(accessToken).build();
    }

    private ExecutionResult<MessageResponse> validateAndInitialize() {
        if (message == null || !(message.body() instanceof JsonObject)) {
            LOGGER.error("Invalid message received, either null or body of message is not JsonObject ");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        userId = ((JsonObject) message.body()).getString(MessageConstants.MSG_USER_ID);
        if (!ProcessorContextHelper.validateUser(userId)) {
            LOGGER.error("Invalid user id passed. Not authorized.");
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("invalid.user")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        session = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_KEY_SESSION);
        request = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_HTTP_BODY);
        accessToken = ((JsonObject) message.body()).getString(MessageConstants.MSG_HEADER_TOKEN);

        if (session == null || session.isEmpty()) {
            LOGGER.error("Invalid session obtained, probably not authorized properly");
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("invalid.session")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        if (request == null) {
            LOGGER.error("Invalid JSON payload on Message Bus");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        // All is well, continue processing
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

}
