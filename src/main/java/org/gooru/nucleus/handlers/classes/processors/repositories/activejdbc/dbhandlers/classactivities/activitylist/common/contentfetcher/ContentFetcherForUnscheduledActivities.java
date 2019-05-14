package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.contentfetcher;

import java.util.List;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.unscheduled.ListActivityUnscheduledCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassContentsDao;

/**
 * @author ashish.
 */

class ContentFetcherForUnscheduledActivities implements ActivityFetcher {

  private final ListActivityUnscheduledCommand command;
  private List<AJEntityClassContents> contents;
  private boolean contentFetchDone = false;

  ContentFetcherForUnscheduledActivities(ListActivityUnscheduledCommand command) {
    this.command = command;
  }

  @Override
  public List<AJEntityClassContents> fetchContents() {
    if (!contentFetchDone) {
      contents = EntityClassContentsDao
          .fetchUnscheduledActivitiesForTeacher(command.getClassId(), command.getForMonth(),
              command.getForYear());
      contentFetchDone = true;
    }
    return contents;
  }

  @Override
  public int fetchTotalContentCount() {
    if (!contentFetchDone) {
      throw new IllegalStateException("Count fetch without fetching content");
    }
    return contents != null ? contents.size() : 0;
  }
}
