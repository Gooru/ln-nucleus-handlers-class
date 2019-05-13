package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist;

import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassContentsDao;
import org.javalite.activejdbc.LazyList;

/**
 * @author ashish.
 */

class OfflineActivitiesFetcher implements ActivityFetcher {

  private final ListActivityCommand command;

  OfflineActivitiesFetcher(ListActivityCommand command) {
    this.command = command;
  }

  @Override
  public LazyList<AJEntityClassContents> fetchContents() {
    if (command.isStudent()) {
      return EntityClassContentsDao
          .fetchOfflineActivitiesForStudent(command.getClassId(), command.getForMonth(),
              command.getForYear(), command.getUserId());
    } else {
      return EntityClassContentsDao
          .fetchOfflineActivitiesForTeacher(command.getClassId(), command.getForMonth(),
              command.getForYear());
    }
  }
}
