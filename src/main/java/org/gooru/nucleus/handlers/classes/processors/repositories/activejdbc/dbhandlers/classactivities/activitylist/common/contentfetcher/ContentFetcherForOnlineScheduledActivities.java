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

  ContentFetcherForOnlineScheduledActivities(ListOnlineScheduledActivityCommand command) {

    this.command = command;
  }


  @Override
  public List<AJEntityClassContents> fetchContents() {
    if (command.isStudent()) {
      return EntityClassContentsDao
          .fetchAllOnlineScheduledActivitiesForStudent(command.getClassId(), command.getStartDate(),
              command.getEndDate(), command.getUserId());
    } else {
      return EntityClassContentsDao
          .fetchAllOnlineScheduledActivitiesForTeacher(command.getClassId(), command.getStartDate(),
              command.getEndDate());
    }
  }
}
