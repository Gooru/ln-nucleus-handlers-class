
package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.classes.processors.repositories.generators.GeneratorBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On 16-Jul-2019
 */
public class AddStudentsToClassHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(AddStudentsToClassHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private AJEntityClass entityClass;
  private List<String> studentList;

  public AddStudentsToClassHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be a class id present
    if (context.classId() == null || context.classId().isEmpty()) {
      LOGGER.warn("Missing class");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.class.id")),
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

    // Payload should not be empty
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("Empty payload supplied to edit class");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
          RESOURCE_BUNDLE.getString("empty.payload")), ExecutionResult.ExecutionStatus.FAILED);
    }

    // Our validators should certify this
    JsonObject errors = new DefaultPayloadValidator().validatePayload(context.request(),
        AJEntityClass.addStudentsToClassFieldSelector(), AJEntityClass.getValidatorRegistry());
    if (errors != null && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityClass> classes =
        AJEntityClass.where(AJEntityClass.FETCH_QUERY_FILTER, context.classId());
    if (classes.isEmpty()) {
      LOGGER.warn("Not able to find class '{}'", this.context.classId());
      return new ExecutionResult<>(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    this.entityClass = classes.get(0);
    initializeStudentList();

    return AuthorizerBuilder.buildAddStudentsToClassAuthorizer(this.context)
        .authorize(this.entityClass);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    Map<String, AJClassMember> existingMembers = fetchExistingMembers();
    Map<String, String> userEmails = fetchUserEmails();

    PreparedStatement cmBatch = Base.startBatch(
        "INSERT INTO class_member(class_id, email, user_id, class_member_status, grade_upper_bound) VALUES (?::uuid, ?, ?::uuid, ?::class_member_status_type, ?)"
            + " ON CONFLICT (class_id, email) DO UPDATE SET class_member_status = 'joined'::class_member_status_type, grade_upper_bound = ?, updated_at = now()");
    try {

      this.studentList.forEach(id -> {
        if (existingMembers.containsKey(id)) {
          AJClassMember member = existingMembers.get(id);
          // If user is only invited, we need to update the status to be joined.
          if (AJClassMember.CLASS_MEMBER_STATUS_TYPE_INVITED
              .equalsIgnoreCase(member.getString(AJClassMember.CLASS_MEMBER_STATUS))) {
            Base.addBatch(cmBatch, this.context.classId(), member.getString(AJClassMember.EMAIL),
                this.context.userId(), AJClassMember.CLASS_MEMBER_STATUS_TYPE_JOINED,
                this.entityClass.getGradeCurrent(), this.entityClass.getGradeCurrent());
          } else {
            // No action as the users is already joined class
          }
        } else {
          String email = userEmails.get(id);
          Base.addBatch(cmBatch, this.context.classId(),
              (email != null ? email : getUserEmailAddress()), id,
              AJClassMember.CLASS_MEMBER_STATUS_TYPE_JOINED, this.entityClass.getGradeCurrent(),
              this.entityClass.getGradeCurrent());
        }
      });

      Base.executeBatch(cmBatch);
      LOGGER.debug("students added to the class successfully");
      return new ExecutionResult<>(
          MessageResponseFactory.createCreatedResponse(this.context.classId(),
              EventBuilderFactory.getAddStudentsToClassEventBuilder(this.context.classId(),
                  this.context.request().getJsonArray(AJEntityClass.STUDENTS))),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
    } catch (Exception e) {
      LOGGER.error("error while adding students to class '{}'", this.context.classId(), e);
      Throwable t = e.getCause();
      if (t != null) {
        if (t instanceof BatchUpdateException) {
          LOGGER.error("actual error", ((BatchUpdateException) t).getNextException());
        }
      }

      return new ExecutionResult<>(
          MessageResponseFactory.createValidationErrorResponse(new JsonObject()
              .put(MessageConstants.MSG_MESSAGE, "error while adding students to class")),
          ExecutionResult.ExecutionStatus.FAILED);
    } finally {
      try {
        cmBatch.close();
      } catch (SQLException e) {
        LOGGER.error("error while closing statement", e);
      }
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private static class DefaultPayloadValidator implements PayloadValidator {
  }

  private void initializeStudentList() {
    JsonArray input = this.context.request().getJsonArray(AJEntityClass.STUDENTS);
    this.studentList = new ArrayList<>(input.size());
    input.forEach(entry -> {
      this.studentList.add(entry.toString());
    });
  }

  private Map<String, AJClassMember> fetchExistingMembers() {
    LazyList<AJClassMember> members =
        AJClassMember.where(AJClassMember.FETCH_SPECIFIC_USERS_QUERY_FILTER, this.context.classId(),
            DbHelperUtil.toPostgresArrayString(this.studentList));

    if (members.isEmpty()) {
      return new HashMap();
    } else {
      Map<String, AJClassMember> membersMap = new HashMap<>();
      members.forEach(member -> {
        membersMap.put(member.getString(AJClassMember.USER_ID), member);
      });

      return membersMap;
    }
  }

  private Map<String, String> fetchUserEmails() {
    LazyList<AJEntityUser> demographics = AJEntityUser.findBySQL(AJEntityUser.GET_SUMMARY_QUERY,
        DbHelperUtil.toPostgresArrayString(this.studentList));
    Map<String, String> userEmails = new HashMap<>();
    if (!demographics.isEmpty()) {
      demographics.forEach(user -> {
        userEmails.put(user.getString(AJEntityUser.ID), user.getString(AJEntityUser.EMAIL));
      });
    }

    return userEmails;
  }

  private String getUserEmailAddress() {
    return GeneratorBuilder.buildDummyEmailGenerator(this.context.userId()).generate();
  }
}
