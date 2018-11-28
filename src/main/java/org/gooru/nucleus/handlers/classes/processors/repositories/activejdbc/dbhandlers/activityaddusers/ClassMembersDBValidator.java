package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.activityaddusers;

import java.util.List;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author ashish.
 */

class ClassMembersDBValidator {

  private final List<String> users;
  private final String classId;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  ClassMembersDBValidator(List<String> users, String classId) {
    this.users = users;
    this.classId = classId;
  }

  public void validate() {
    if (users == null || users.isEmpty()) {
      return;
    }
    validateUsersWithClassMembers();
  }

  private void validateUsersWithClassMembers() {
    Long countOfStudents = AJClassMember
        .count(AJClassMember.STUDENT_COUNT_FROM_SET_FILTER, classId,
            Utils.convertListToPostgresArrayStringRepresentation(users));
    if (countOfStudents == null || countOfStudents != users.size()) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("users.not.members")));
    }
  }
}
