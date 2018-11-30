package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.app.components.AppConfiguration;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
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
  - class is valid - not deleted, not archived
  - validations for grade low < grade high, grade id from grade master, route0 only if course is assigned and is premium
   */


  private final String classId;
  private final AJEntityClass entityClass;
  private final RerouteSettingCommand command;
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestDbValidator.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private int validGradesCount = 0;

  RequestDbValidator(String classId, AJEntityClass entityClass, RerouteSettingCommand command) {
    this.classId = classId;
    this.entityClass = entityClass;
    this.command = command;
  }

  void validate() {
    validateClassNotArchivedAndCorrectVersion();
    validateCourse();
    validateGradeValues();
    if (validGradesCount > 0) {
      validateGradesSequence();
    }
    validateRoute0();
  }

  private void validateCourse() {
    String courseId = entityClass.getCourseId();
    if (courseId == null) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("reroute.settings.course.needed")));
    }
    final Object subjectBucket = Base
        .firstCell(AJEntityCourse.COURSE_SUBJECT_BUCKET_FETCH_QUERY, courseId);
    if (subjectBucket == null || String.valueOf(subjectBucket).trim().isEmpty()) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("reroute.settings.course.subject.needed")));
    }
  }

  private void validateRoute0() {
    if (command.getRoute0() != null) {
      if (command.getRoute0()) {
        String courseId = entityClass.getCourseId();
        final Object versionObject = Base
            .firstCell(AJEntityCourse.COURSE_VERSION_FETCH_QUERY, courseId);
        if (versionObject == null || !Objects
            .equals(AppConfiguration.getInstance().getCourseVersionForPremiumContent(),
                String.valueOf(versionObject))) {
          throw new MessageResponseWrapperException(MessageResponseFactory
              .createInvalidRequestResponse(
                  RESOURCE_BUNDLE.getString("route0.course.not.premium")));
        }
      }
    }
  }

  private void validateGradesSequence() {
    Long effectiveLowerGrade = command.getGradeLowerBound() != null ? command.getGradeLowerBound()
        : entityClass.getGradeLowerBound();
    Long effectiveUpperGrade = command.getGradeUpperBound() != null ? command.getGradeUpperBound()
        : entityClass.getGradeUpperBound();
    Long effectiveCurrentGrade = command.getGradeCurrent() != null ? command.getGradeCurrent()
        : entityClass.getGradeCurrent();
    Set<Long> idsSet = new HashSet<>();
    if (effectiveLowerGrade != null) {
      idsSet.add(effectiveLowerGrade);
    }
    if (effectiveCurrentGrade != null) {
      idsSet.add(effectiveCurrentGrade);
    }
    if (effectiveUpperGrade != null) {
      idsSet.add(effectiveUpperGrade);
    }
    List<Long> idsList = new ArrayList<>(idsSet);

    List<AJEntityGradeMaster> gradeEffectiveList = AJEntityGradeMaster.getAllByIds(idsList);

    Map<Long, Integer> gradeSeqMap = new HashMap<>();
    for (AJEntityGradeMaster ajEntityGradeMaster : gradeEffectiveList) {
      gradeSeqMap.put(ajEntityGradeMaster.getId(), ajEntityGradeMaster.getGradeSeq());
    }

    if (effectiveLowerGrade != null && effectiveCurrentGrade != null) {
      if (gradeSeqMap.get(effectiveLowerGrade) > gradeSeqMap.get(effectiveCurrentGrade)) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(
                RESOURCE_BUNDLE.getString("grades.incorrect.sequence")));
      }
    }

    if (effectiveCurrentGrade != null && effectiveUpperGrade != null) {
      if (gradeSeqMap.get(effectiveCurrentGrade) > gradeSeqMap.get(effectiveUpperGrade)) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(
                RESOURCE_BUNDLE.getString("grades.incorrect.sequence")));
      }
    }

    if (effectiveUpperGrade != null && effectiveLowerGrade != null) {
      if (gradeSeqMap.get(effectiveLowerGrade) > gradeSeqMap.get(effectiveUpperGrade)) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(
                RESOURCE_BUNDLE.getString("grades.incorrect.sequence")));
      }
    }

  }

  private void validateGradeValues() {
    Set<Long> idsSet = new HashSet<>();
    if (command.getGradeCurrent() != null) {
      idsSet.add(command.getGradeCurrent());
    }
    if (command.getGradeLowerBound() != null) {
      idsSet.add(command.getGradeLowerBound());
    }
    if (command.getGradeUpperBound() != null) {
      idsSet.add(command.getGradeUpperBound());
    }
    validGradesCount = idsSet.size();
    List<Long> idsList = new ArrayList<>(idsSet);

    List<AJEntityGradeMaster> gradeMasterList = AJEntityGradeMaster.getAllByIds(idsList);
    if ((gradeMasterList == null || gradeMasterList.isEmpty()) && validGradesCount != 0) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("grades.incorrect")));
    }
  }

  private void validateClassNotArchivedAndCorrectVersion() {
    if (!this.entityClass.isCurrentVersion() || this.entityClass.isArchived()) {
      LOGGER.warn("Class '{}' is either archived or not of current version", classId);
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("class.archived.or.incorrect.version")));
    }
  }
}
