package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.contentfetcher;

import java.util.List;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.offlinecompleted.ListActivityOfflineCompletedCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassContentsDao;

/**
 * @author ashish.
 */

class ContentFetcherForOfflineCompletedActivities implements ActivityFetcher {

  private final ListActivityOfflineCompletedCommand command;
  private List<AJEntityClassContents> contents;
  private boolean contentFetchDone = false;
  private Long count = 0L;

  ContentFetcherForOfflineCompletedActivities(ListActivityOfflineCompletedCommand command) {
    this.command = command;
  }

  @Override
  public List<AJEntityClassContents> fetchContents() {
    if (!contentFetchDone) {
      if (command.isStudent()) {
        contents = EntityClassContentsDao
            .fetchOfflineCompletedActivitiesForStudent(command.getClassId(),
                command.getOffset(),
                command.getLimit(), command.getUserId());
        count = EntityClassContentsDao
            .fetchOfflineCompletedActivitiesCountForStudent(command.getClassId(),
                command.getUserId());
      } else {
        contents = EntityClassContentsDao
            .fetchOfflineCompletedActivitiesForTeacher(command.getClassId(),
                command.getOffset(),
                command.getLimit());
        count = EntityClassContentsDao
            .fetchOfflineCompletedActivitiesCountForTeacher(command.getClassId());
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
