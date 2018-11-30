package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classmembersdeactivate;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author ashish.
 */

class RequestDbValidator {

  private final String classId;
  private final AJEntityClass entityClass;
  private final ClassMembersDeactivateCommand command;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  RequestDbValidator(String classId, AJEntityClass entityClass,
      ClassMembersDeactivateCommand command) {
    this.classId = classId;
    this.entityClass = entityClass;
    this.command = command;
  }

  void validate() {
    validateMembershipForSpecifiedUsers();
  }

  private void validateMembershipForSpecifiedUsers() {
    Set<String> uniqueUsers = new HashSet<>(command.getUsersList());

    Long countOfStudents = AJClassMember
        .count(AJClassMember.STUDENT_COUNT_FROM_SET_IGNORE_STATUS_FILTER, classId,
            Utils.convertListToPostgresArrayStringRepresentation(command.getUsersList()));
    if (countOfStudents == null || countOfStudents != uniqueUsers.size()) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("users.not.members")));
    }
  }
}
