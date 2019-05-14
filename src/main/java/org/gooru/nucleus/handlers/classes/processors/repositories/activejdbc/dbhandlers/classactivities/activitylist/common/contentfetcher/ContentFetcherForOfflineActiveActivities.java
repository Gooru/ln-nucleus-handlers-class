package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.contentfetcher;

import java.util.List;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.offlineactive.ListActivityOfflineActiveCommand;
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
        contents = EntityClassContentsDao
            .fetchOfflineActiveActivitiesForStudent(command.getClassId(),
                command.getOffset(),
                command.getLimit(), command.getUserId());
        count = EntityClassContentsDao
            .fetchOfflineActiveActivitiesCountForStudent(command.getClassId(), command.getUserId());
      } else {
        contents = EntityClassContentsDao
            .fetchOfflineActiveActivitiesForTeacher(command.getClassId(),
                command.getOffset(),
                command.getLimit());
        count = EntityClassContentsDao
            .fetchOfflineActiveActivitiesCountForTeacher(command.getClassId());
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
