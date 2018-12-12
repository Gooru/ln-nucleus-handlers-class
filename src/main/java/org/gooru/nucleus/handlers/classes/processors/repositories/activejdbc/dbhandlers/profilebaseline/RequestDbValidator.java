package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.profilebaseline;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;

/**
 * @author ashish.
 */

class RequestDbValidator {
  /*
  - Class being valid is already validated before coming here. Following validations will happen here:
    - class should have course
    - course should have subject bucket
    - specified users are members of class
   */

  private final String classId;
  private final AJEntityClass entityClass;
  private final ProfileBaselineCommand command;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private String courseId;

  RequestDbValidator(String classId, AJEntityClass entityClass,
      ProfileBaselineCommand command) {
    this.classId = classId;
    this.entityClass = entityClass;
    this.command = command;
  }

  void validate() {
    courseId = entityClass.getCourseId();
    validateCourseInClass();
    validateSubjectBucketForCourse();
    validateMembershipForSpecifiedUsers();
  }

  private void validateMembershipForSpecifiedUsers() {
    if (!command.getUsersList().isEmpty()) {
      Set<String> uniqueUsers = new HashSet<>(command.getUsersList());

      Long countOfStudents = AJClassMember
          .count(AJClassMember.STUDENT_COUNT_FROM_SET_FILTER, classId,
              Utils.convertListToPostgresArrayStringRepresentation(command.getUsersList()));
      if (countOfStudents == null || countOfStudents != uniqueUsers.size()) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(
                RESOURCE_BUNDLE.getString("users.not.members")));
      }
    }
  }

  private void validateSubjectBucketForCourse() {
    final Object subjectBucket = Base
        .firstCell(AJEntityCourse.COURSE_SUBJECT_BUCKET_FETCH_QUERY, courseId);
    if (subjectBucket == null || String.valueOf(subjectBucket).trim().isEmpty()) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("profilebaseline.course.subject.needed")));
    }
  }

  private void validateCourseInClass() {
    if (courseId == null) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("profilebaseline.course.needed")));
    }
  }
}
