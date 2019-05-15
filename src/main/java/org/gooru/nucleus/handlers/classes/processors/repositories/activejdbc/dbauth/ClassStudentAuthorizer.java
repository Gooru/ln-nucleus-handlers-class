package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On 04-Feb-2019
 */
public class ClassStudentAuthorizer implements Authorizer<AJEntityClass> {

  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(ClassMemberAuthorizer.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  public ClassStudentAuthorizer(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> authorize(AJEntityClass model) {
    if (checkStudent(model)) {
      return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }
    return new ExecutionResult<>(
        MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
        ExecutionResult.ExecutionStatus.FAILED);
  }

  private boolean checkStudent(AJEntityClass model) {
    String classId =
        this.context.classId() != null ? this.context.classId() : model.getString(AJEntityClass.ID);
    if (classId == null) {
      LOGGER.warn("no class id present to check student enrollment");
      return false;
    }
    LazyList<AJClassMember> members = AJClassMember
        .where(AJClassMember.FETCH_FOR_ACTIVE_USER_QUERY_FILTER,
            classId, this.context.userId());
    return !members.isEmpty();
  }
}
