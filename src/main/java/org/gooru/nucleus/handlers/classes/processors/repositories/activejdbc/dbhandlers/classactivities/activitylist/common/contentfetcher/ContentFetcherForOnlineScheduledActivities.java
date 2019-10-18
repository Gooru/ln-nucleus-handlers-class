package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.contentfetcher;

import java.util.List;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.onlinescheduled.ListOnlineScheduledActivityCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
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
        // Get secondary classes from command and add primary class id to filter the data for all
        // classes including primary class. No need to check null on the set object as we are always
        // returning non null.
        Set<String> classes = command.getSecondaryClasses();
        classes.add(command.getClassId());
        contents = EntityClassContentsDao
            .fetchAllOnlineScheduledActivitiesForTeacher(DbHelperUtil.toPostgresArrayString(classes),
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
