package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.contentfetcher;

import java.util.List;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.unscheduled.ListActivityUnscheduledCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassContentsDao;

/**
 * @author ashish.
 */

class ContentFetcherForUnscheduledActivities implements ActivityFetcher {

  private final ListActivityUnscheduledCommand command;
  private List<AJEntityClassContents> contents;
  private boolean contentFetchDone = false;
  private String ASSESSMENT = "assessment";
  private String COLLECTION = "collection";

  ContentFetcherForUnscheduledActivities(ListActivityUnscheduledCommand command) {
    this.command = command;
  }

  @Override
  public List<AJEntityClassContents> fetchContents() {
    if (!contentFetchDone) {
      // Get secondary classes from command and add primary class id to filter the data for all
      // classes including primary class. No need to check null on the set object as we are always
      // returning non null.
      Set<String> classes = command.getSecondaryClasses();
      classes.add(command.getClassId());
      if (command.getContentType() != null && (command.getContentType().contains(COLLECTION)
              || command.getContentType().contains(ASSESSMENT))) {
        contents = EntityClassContentsDao.fetchUnscheduledActivitiesForTeacherForContentType(
            DbHelperUtil.toPostgresArrayString(classes), command.getForMonth(),
            command.getForYear(), DbHelperUtil.toPostgresArrayString(command.getContentType()));
      } else {
        contents = EntityClassContentsDao.fetchUnscheduledActivitiesForTeacher(
            DbHelperUtil.toPostgresArrayString(classes), command.getForMonth(),
            command.getForYear());
      }
      contentFetchDone = true;
    }
    return contents;
  }

  @Override
  public Long fetchTotalContentCount() {
    if (!contentFetchDone) {
      throw new IllegalStateException("Count fetch without fetching content");
    }
    return contents != null ? (long) contents.size() : 0;
  }
}
