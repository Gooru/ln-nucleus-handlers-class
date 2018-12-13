package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.classes.processors.repositories.generators.GeneratorBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 8/2/16.
 */
class JoinClassByStudentHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(JoinClassByStudentHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private AJEntityClass entityClass;
  private String classId;
  private String courseId;
  private AJClassMember membership;
  private String email;
  private static final String JOIN_CLASS = "join.class";

  JoinClassByStudentHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be a class code present
    if (context.classCode() == null || context.classCode().isEmpty()) {
      LOGGER.warn("Missing class code");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.class.code")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty()
        || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous user attempting to join class");
      return new ExecutionResult<>(
          MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    this.email = context.session().getString(MessageConstants.EMAIL);
    // Payload should not be null
    if (context.request() == null) {
      LOGGER.warn("Payload is null");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("empty.payload")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Our validators should certify this
    JsonObject errors = new DefaultPayloadValidator().validatePayload(context.request(),
        AJEntityClass.joinClassFieldSelector(), AJEntityClass.getValidatorRegistry());
    if (errors != null && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    String classCode = context.classCode().toUpperCase();
    LazyList<AJEntityClass> classes = AJEntityClass
        .where(AJEntityClass.FETCH_VIA_CODE_FILTER, classCode);
    if (classes.isEmpty()) {
      LOGGER.warn("Not able to find class with code '{}'", classCode);
      return new ExecutionResult<>(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    this.entityClass = classes.get(0);
    this.classId = this.entityClass.getId().toString();
    this.courseId = this.entityClass.getString(AJEntityClass.COURSE_ID);
    // Class should be of current version and Class should not be archived
    if (!this.entityClass.isCurrentVersion() || this.entityClass.isArchived()) {
      LOGGER.warn("Class with code '{}' is either archived or not of current version", classCode);
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(
                  RESOURCE_BUNDLE.getString("class.archived.or.incorrect.version")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // First level of authorization, user should not be teacher, co teacher
    // or student already
    if (isUserClassMember()) {
      // User is already joined, so nothing to do; just return
      // successfully
      return new ExecutionResult<>(
          MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("joined")),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }
    // Now get the membership record for that user
    if (this.email != null && !this.email.isEmpty()) {
      LazyList<AJClassMember> members =
          AJClassMember.where(AJClassMember.FETCH_FOR_EMAIL_QUERY_FILTER, this.classId, this.email);
      if (!members.isEmpty()) {
        this.membership = members.get(0);
      } else {
        this.membership = null;
      }
    } else {
      this.membership = null;
    }
    // Delegate it to authorizer to confirm if this is either an open class
    // in which case allow user to join or it is restricted class but the
    // user is invited which is also fine. Else no authorization
    return AuthorizerBuilder.buildJoinClassByStudentAuthorizer(this.context, this.membership)
        .authorize(this.entityClass);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    if (this.membership == null) {
      // Make an entry into the members tables in case of open class
      this.membership = new AJClassMember();
      this.membership.setClassId(this.classId);
      this.membership.setString(AJClassMember.EMAIL, getUserEmailAddress());
      this.membership.setUserId(this.context.userId());
      this.membership.setRosterId(this.context.request().getString(AJClassMember.ROSTER_ID));
      this.membership
          .setCreatorSystem(this.context.request().getString(AJClassMember.CREATOR_SYSTEM));
      this.membership.setStatusJoined();
      this.membership.setGradeLowerBound(entityClass.getGradeLowerBound());
      this.membership.setGradeUpperBound(entityClass.getGradeUpperBound());
      if (this.membership.hasErrors()) {
        return membershipErrors();
      }
    } else {
      // In case user is already invited we need to update the status
      if (AJClassMember.CLASS_MEMBER_STATUS_TYPE_INVITED
          .equalsIgnoreCase(this.membership.getString(AJClassMember.CLASS_MEMBER_STATUS))) {
        this.membership.setUserId(this.context.userId());
        this.membership.setStatusJoined();
        this.membership.setGradeLowerBound(entityClass.getGradeLowerBound());
        this.membership.setGradeUpperBound(entityClass.getGradeUpperBound());
      } else {
        // User is already joined, so nothing to do; just return successfully
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("joined")),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
      }
    }
    try {
      // Now we need to save
      boolean result = this.membership.save();
      if (!result) {
        LOGGER.error("Class membership with id '{}' and user '{}' failed to save", this.classId,
            context.userId());
        if (this.membership.hasErrors()) {
          return membershipErrors();
        }
      }
    } catch (Exception e) {
      if (e instanceof DBException && e.getCause() instanceof PSQLException && ((PSQLException) e
          .getCause())
          .getSQLState().startsWith("23")) {
        // User is already joined, during race condition, so nothing to do; just return successfully
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("joined")),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
      }
      throw e;
    }

    return new ExecutionResult<>(
        MessageResponseFactory.createCreatedResponse(this.classId,
            EventBuilderFactory.getStudentJoinedEventBuilder(this.classId, this.context.userId())),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private static class DefaultPayloadValidator implements PayloadValidator {

  }

  private ExecutionResult<MessageResponse> membershipErrors() {
    Map<String, String> map = this.membership.errors();
    JsonObject errors = new JsonObject();
    map.forEach(errors::put);
    return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
        ExecutionResult.ExecutionStatus.FAILED);
  }

  private boolean isUserClassMember() {
    ExecutionResult<MessageResponse> result =
        AuthorizerBuilder.buildClassMembersAuthorizer(context).authorize(this.entityClass);
    return result.continueProcessing();
  }

  private String getUserEmailAddress() {
    String userEmail = (this.email != null && !this.email.isEmpty()) ? this.email : null;
    if (userEmail == null) {
      userEmail = GeneratorBuilder.buildDummyEmailGenerator(this.context.userId()).generate();
    }
    return userEmail;
  }

}
