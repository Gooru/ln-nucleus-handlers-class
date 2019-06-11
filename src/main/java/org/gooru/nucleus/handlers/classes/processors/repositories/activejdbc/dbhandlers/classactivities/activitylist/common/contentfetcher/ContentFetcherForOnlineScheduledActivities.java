package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.contentfetcher;

import java.util.List;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.onlinescheduled.ListOnlineScheduledActivityCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassContentsDao;

/**
 * @author ashish.
 */

class ContentFetcherForOnlineScheduledActivities implements ActivityFetcher {

  private final ListOnlineScheduledActivityCommand command;
  private List<AJEntityClassContents> contents;
  private boolean contentFetchDone = false;

  ContentFetcherForOnlineScheduledActivities(ListOnlineScheduledActivityCommand command) {

    this.command = command;
  }


  @Override
  public List<AJEntityClassContents> fetchContents() {
    if (!contentFetchDone) {
      if (command.isStudent()) {
        contents = EntityClassContentsDao
            .fetchAllOnlineScheduledActivitiesForStudent(command.getClassId(),
                command.getStartDate(),
                command.getEndDate(), command.getUserId());
      } else {
        contents = EntityClassContentsDao
            .fetchAllOnlineScheduledActivitiesForTeacher(command.getClassId(),
                command.getStartDate(),
                command.getEndDate());
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
