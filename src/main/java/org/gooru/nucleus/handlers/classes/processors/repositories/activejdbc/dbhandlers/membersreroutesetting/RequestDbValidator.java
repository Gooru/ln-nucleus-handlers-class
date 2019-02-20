package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityGradeMaster;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

class RequestDbValidator {
  /*
   * - class is valid - not deleted, not archived - validations for grade low < grade high, grade id
   * from grade master, route0 only if course is assigned and is premium
   */


  private final String classId;
  private final AJEntityClass entityClass;
  private final MembersRerouteSettingCommand command;
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestDbValidator.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  RequestDbValidator(String classId, AJEntityClass entityClass,
      MembersRerouteSettingCommand command) {
    this.classId = classId;
    this.entityClass = entityClass;
    this.command = command;
  }

  void validate() {
    validateClassNotArchivedAndCorrectVersion();
    validateCourse();
    validateMembershipForSpecifiedUsers();
    validateGradesSequence();
  }

  private void validateGradesSequence() {
    // Used to compute the highest grade across all members of the clas
    Set<Long> studentHighGrades = new HashSet<>();

    // Used to hold all grade ids
    Set<Long> idsSet = new HashSet<>();

    Long classLower = entityClass.getGradeLowerBound();
    Long classHigher = entityClass.getGradeUpperBound();
    Long classCurrent = entityClass.getGradeCurrent();

    if (classLower != null) {
      idsSet.add(classLower);
    }
    if (classHigher != null) {
      idsSet.add(classHigher);
    }
    if (classCurrent != null) {
      idsSet.add(classCurrent);
    }

    // Iterate through all members and populate the grade ids to fetch from DB
    command.getUsers().forEach(user -> {
      Long memberLower = user.getGradeLowerBound();
      Long memberHigher = user.getGradeUpperBound();

      if ((classLower == null && memberLower != null)
          || (classCurrent == null && memberHigher != null)) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("class.bounds.not.set")));
      }

      studentHighGrades.add(memberHigher);

      if (memberLower != null) {
        idsSet.add(memberLower);
      }
      if (memberHigher != null) {
        idsSet.add(memberHigher);
      }
    });

    Map<Long, Integer> gradeSeqMap = fetchGrades(idsSet);

    command.getUsers().forEach(user -> {
      Long memberLower = user.getGradeLowerBound();
      Long memberHigher = user.getGradeUpperBound();
      if (memberLower != null) {
        if (gradeSeqMap.get(memberLower) < gradeSeqMap.get(classLower)) {
          throw new MessageResponseWrapperException(
              MessageResponseFactory.createInvalidRequestResponse(
                  RESOURCE_BUNDLE.getString("grades.incorrect.sequence")));
        }
      }

      if (memberHigher != null) {
        if (gradeSeqMap.get(memberHigher) < gradeSeqMap.get(classCurrent)) {
          throw new MessageResponseWrapperException(
              MessageResponseFactory.createInvalidRequestResponse(
                  RESOURCE_BUNDLE.getString("grades.incorrect.sequence")));
        }
      }

      if (memberHigher != null && memberLower != null) {
        if (gradeSeqMap.get(memberHigher) < gradeSeqMap.get(memberLower)) {
          throw new MessageResponseWrapperException(
              MessageResponseFactory.createInvalidRequestResponse(
                  RESOURCE_BUNDLE.getString("grades.incorrect.sequence")));
        }
      }
    });

    // Find maximum grade from the all members and if it is greater than class high grade, then
    // update the class upper bound
    Long maxGrade = Collections.max(studentHighGrades);
    if (gradeSeqMap.get(maxGrade) > gradeSeqMap.get(classHigher)) {
      // save state to update class high bound
      command.setClassUpperBoundUpdateNeeded(true);
      command.setClassUpperBound(maxGrade);
    }

  }

  /*
   * Fetch all grade sequence from the database and convert into map
   */
  private Map<Long, Integer> fetchGrades(Set<Long> idsSet) {
    List<Long> idsList = new ArrayList<>(idsSet);
    List<AJEntityGradeMaster> gradeEffectiveList = AJEntityGradeMaster.getAllByIds(idsList);

    if (idsList.size() != gradeEffectiveList.size()) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("grades.incorrect")));
    }

    Map<Long, Integer> gradeSeqMap = new HashMap<>();
    for (AJEntityGradeMaster ajEntityGradeMaster : gradeEffectiveList) {
      gradeSeqMap.put(ajEntityGradeMaster.getId(), ajEntityGradeMaster.getGradeSeq());
    }

    return gradeSeqMap;
  }

  private void validateCourse() {
    String courseId = entityClass.getCourseId();
    if (courseId == null) {
      throw new MessageResponseWrapperException(MessageResponseFactory.createInvalidRequestResponse(
          RESOURCE_BUNDLE.getString("reroute.settings.course.needed")));
    }
    final Object subjectBucket =
        Base.firstCell(AJEntityCourse.COURSE_SUBJECT_BUCKET_FETCH_QUERY, courseId);
    if (subjectBucket == null || String.valueOf(subjectBucket).trim().isEmpty()) {
      throw new MessageResponseWrapperException(MessageResponseFactory.createInvalidRequestResponse(
          RESOURCE_BUNDLE.getString("reroute.settings.course.subject.needed")));
    }
  }

  private void validateMembershipForSpecifiedUsers() {
    List<String> userIds = new ArrayList<>();

    command.getUsers().forEach(user -> {
      userIds.add(user.getUserId().toString());
    });

    Long countOfStudents = AJClassMember.count(AJClassMember.STUDENT_COUNT_FROM_SET_FILTER, classId,
        Utils.convertListToPostgresArrayStringRepresentation(userIds));
    if (countOfStudents == null || countOfStudents != command.getUsers().size()) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("users.not.members")));
    }
  }

  private void validateClassNotArchivedAndCorrectVersion() {
    if (!this.entityClass.isCurrentVersion() || this.entityClass.isArchived()) {
      LOGGER.warn("Class '{}' is either archived or not of current version", classId);
      throw new MessageResponseWrapperException(MessageResponseFactory.createInvalidRequestResponse(
          RESOURCE_BUNDLE.getString("class.archived.or.incorrect.version")));
    }
  }
}
