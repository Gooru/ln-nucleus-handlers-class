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
  - validations for grade low <= grade current, grade id from grade master, route0 only if course is assigned and is premium
   */


  private final String classId;
  private final AJEntityClass entityClass;
  private final RerouteSettingCommand command;
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestDbValidator.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private int validGradesCount = 0;
  private Map<Long, Integer> gradeSeqMap;

  RequestDbValidator(String classId, AJEntityClass entityClass, RerouteSettingCommand command) {
    this.classId = classId;
    this.entityClass = entityClass;
    this.command = command;
  }

  void validate() {
    validateClassNotArchivedAndCorrectVersion();
    validateCourse();
    validateGradeCurrentNotGettingOverwritten();
    populateGradeSequenceMap();
    validateGradeValuesPresentInCommand();
    if (validGradesCount > 0) {
      validateGradesSequence();
    }
    validateRoute0();
  }

  private void validateGradeCurrentNotGettingOverwritten() {
    if (!entityClass.isClassSetupComplete()) {
      LOGGER.debug(
          "It seems the class setup is not yet complete, hence we are allowing to set class grade and uppper bound");
      return;
    } else {
      if (entityClass.getGradeCurrent() != null && command.getGradeCurrent() != null
          && entityClass.getGradeCurrent().longValue() != command.getGradeCurrent().longValue()) {
        throw new MessageResponseWrapperException(
            MessageResponseFactory.createInvalidRequestResponse(
                RESOURCE_BUNDLE.getString("reroute.settings.grade.set.not.allowed")));
      }
    }
  }

  private void populateGradeSequenceMap() {
    Set<Long> gradeIds = new HashSet<>();
    if (command.getGradeLowerBound() != null) {
      gradeIds.add(command.getGradeLowerBound());
    }
    if (command.getGradeCurrent() != null) {
      gradeIds.add(command.getGradeCurrent());
    }
    if (entityClass.getGradeLowerBound() != null) {
      gradeIds.add(entityClass.getGradeLowerBound());
    }
    if (entityClass.getGradeCurrent() != null) {
      gradeIds.add(entityClass.getGradeCurrent());
    }
    List<AJEntityGradeMaster> gradeEffectiveList = AJEntityGradeMaster
        .getAllByIds(new ArrayList<>(gradeIds));

    gradeSeqMap = new HashMap<>();
    for (AJEntityGradeMaster ajEntityGradeMaster : gradeEffectiveList) {
      gradeSeqMap.put(ajEntityGradeMaster.getId(), ajEntityGradeMaster.getGradeSeq());
    }
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
    Long effectiveCurrentGrade = command.getGradeCurrent() != null ? command.getGradeCurrent()
        : entityClass.getGradeCurrent();

    validateRangeIsNotShrinking();

    if (effectiveLowerGrade != null && effectiveCurrentGrade != null) {
      if (gradeSeqMap.get(effectiveLowerGrade) > gradeSeqMap.get(effectiveCurrentGrade)) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(
                RESOURCE_BUNDLE.getString("grades.incorrect.sequence")));
      }
    }
  }

  private void validateRangeIsNotShrinking() {
    if (command.getGradeLowerBound() != null && entityClass.getGradeLowerBound() != null) {
      if (gradeSeqMap.get(command.getGradeLowerBound()) > gradeSeqMap
          .get(entityClass.getGradeLowerBound())) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(
                RESOURCE_BUNDLE.getString("grades.range.shrink.not.allowed")));
      }
    }
  }

  private void validateGradeValuesPresentInCommand() {
    validGradesCount = 0;
    if (command.getGradeCurrent() != null) {
      if (gradeSeqMap.containsKey(command.getGradeCurrent())) {
        validGradesCount++;
      }
    }
    if (command.getGradeLowerBound() != null) {
      if (gradeSeqMap.containsKey(command.getGradeLowerBound())) {
        validGradesCount++;
      }
    }
    if (validGradesCount != command.validGradesCount()) {
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
