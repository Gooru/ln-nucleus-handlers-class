package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.contentfetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.scheduled.ListScheduledActivityCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassContentsDao;

/**
 * @author renuka
 */

class ContentFetcherForScheduledActivities implements ActivityFetcher {
  private final ListScheduledActivityCommand command;
  private List<AJEntityClassContents> contents;
  private boolean contentFetchDone = false;
  private String OFFLINE_ACTIVITY = "offline-activity";
  private String ASSESSMENT = "assessment";
  private String COLLECTION = "collection";

  ContentFetcherForScheduledActivities(ListScheduledActivityCommand command) {
    this.command = command;
  }

  @Override
  public List<AJEntityClassContents> fetchContents() {
    if (!contentFetchDone) {
      contents = new ArrayList<AJEntityClassContents>();

      if (command.isStudent()) {
        populateOfflineScheduledActivitiesForStudent();
        populateOnlineActivitiesForStudent();
      } else {
        // Get secondary classes from command and add primary class id to filter the data for all
        // classes including primary class. No need to check null on the set object as we are always
        // returning non null.
        Set<String> classes = command.getSecondaryClasses();
        classes.add(command.getClassId());
        populateOfflineScheduledActivitiesForTeacher(classes);
        populateOnlineScheduledActivitiesForTeacher(classes);
      }
      contentFetchDone = true;
    }
    return contents;
  }

  private void populateOfflineScheduledActivitiesForStudent() {
    if (hasNoFilterOrHasValidContentTypeFilterForOffline()) {
      contents.addAll(EntityClassContentsDao.fetchOfflineScheduledActivitiesForStudent(
          command.getClassId(), command.getStartDate(), command.getEndDate(), command.getUserId()));
    }
  }

  private void populateOnlineActivitiesForStudent() {
    if (command.getContentType() == null) {
      contents.addAll(EntityClassContentsDao.fetchAsmtCollScheduledActivitiesForStudent(
          command.getClassId(), command.getStartDate(), command.getEndDate(), command.getUserId()));
    } else if (hasValidContentTypeFilterForOnline()) {
      contents
          .addAll(EntityClassContentsDao.fetchAsmtCollScheduledActivitiesForStudentForContentType(
              command.getClassId(), command.getStartDate(), command.getEndDate(),
              command.getUserId(), DbHelperUtil.toPostgresArrayString(command.getContentType())));
    }
  }

  private void populateOfflineScheduledActivitiesForTeacher(Set<String> classes) {
    if (hasNoFilterOrHasValidContentTypeFilterForOffline()) {
      contents.addAll(EntityClassContentsDao.fetchOfflineScheduledActivitiesForTeacher(
          DbHelperUtil.toPostgresArrayString(classes), command.getStartDate(),
          command.getEndDate()));
    }
  }

  private void populateOnlineScheduledActivitiesForTeacher(Set<String> classes) {
    if (command.getContentType() == null) {
      contents.addAll(EntityClassContentsDao.fetchAsmtCollScheduledActivitiesForTeacher(
          DbHelperUtil.toPostgresArrayString(classes), command.getStartDate(),
          command.getEndDate()));
    } else if (hasValidContentTypeFilterForOnline()) {
      contents
          .addAll(EntityClassContentsDao.fetchAsmtCollScheduledActivitiesForTeacherForContentType(
              DbHelperUtil.toPostgresArrayString(classes), command.getStartDate(),
              command.getEndDate(), DbHelperUtil.toPostgresArrayString(command.getContentType())));
    }
  }

  private boolean hasValidContentTypeFilterForOnline() {
    return command.getContentType() != null && (command.getContentType().contains(COLLECTION)
        || command.getContentType().contains(ASSESSMENT));
  }

  private boolean hasNoFilterOrHasValidContentTypeFilterForOffline() {
    return command.getContentType() == null || (command.getContentType() != null
        && command.getContentType().contains(OFFLINE_ACTIVITY));
  }
  
  @Override
  public Long fetchTotalContentCount() {
    if (!contentFetchDone) {
      throw new IllegalStateException("Count fetch without fetching content");
    }
    return contents != null ? (long) contents.size() : 0;
  }
}
