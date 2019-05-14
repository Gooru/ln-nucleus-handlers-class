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

  ContentFetcherForUnscheduledActivities(ListActivityUnscheduledCommand command) {
    this.command = command;
  }

  @Override
  public List<AJEntityClassContents> fetchContents() {
    return EntityClassContentsDao
        .fetchUnscheduledActivitiesForTeacher(command.getClassId(), command.getForMonth(),
            command.getForYear());
  }
}
