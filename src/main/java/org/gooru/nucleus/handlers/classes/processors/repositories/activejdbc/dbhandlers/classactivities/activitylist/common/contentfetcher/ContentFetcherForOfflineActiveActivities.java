package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.contentfetcher;

import java.util.List;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.offlineactive.ListActivityOfflineActiveCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassContentsDao;

/**
 * @author ashish.
 */

class ContentFetcherForOfflineActiveActivities implements ActivityFetcher {

  private final ListActivityOfflineActiveCommand command;
  private List<AJEntityClassContents> contents;
  private boolean contentFetchDone = false;
  private Long count = 0L;

  ContentFetcherForOfflineActiveActivities(ListActivityOfflineActiveCommand command) {
    this.command = command;
  }

  @Override
  public List<AJEntityClassContents> fetchContents() {
    if (!contentFetchDone) {
      if (command.isStudent()) {
        contents = EntityClassContentsDao.fetchOfflineActiveActivitiesForStudent(
            command.getClassId(), command.getOffset(), command.getLimit(), command.getUserId());
        count = EntityClassContentsDao
            .fetchOfflineActiveActivitiesCountForStudent(command.getClassId(), command.getUserId());
      } else {
        // Get secondary classes from command and add primary class id to filter the data for all
        // classes including primary class. No need to check null on the set object as we are always
        // returning non null.
        Set<String> classes = command.getSecondaryClasses();
        classes.add(command.getClassId());
        contents = EntityClassContentsDao.fetchOfflineActiveActivitiesForTeacher(
            DbHelperUtil.toPostgresArrayString(classes), command.getOffset(), command.getLimit());
        count = EntityClassContentsDao.fetchOfflineActiveActivitiesCountForTeacher(
            DbHelperUtil.toPostgresArrayString(classes));
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
    return count;
  }
}
