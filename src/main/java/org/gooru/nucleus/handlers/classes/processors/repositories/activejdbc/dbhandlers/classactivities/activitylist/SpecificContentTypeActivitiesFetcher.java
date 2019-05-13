package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist;

import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassContentsDao;
import org.javalite.activejdbc.LazyList;

/**
 * @author ashish.
 */

class SpecificContentTypeActivitiesFetcher implements ActivityFetcher {

  private final ListActivityCommand command;

  SpecificContentTypeActivitiesFetcher(ListActivityCommand command) {
    this.command = command;
  }

  @Override
  public LazyList<AJEntityClassContents> fetchContents() {
    if (command.isStudent()) {
      return EntityClassContentsDao
          .fetchClassContentsByContentTypeForStudent(command.getClassId(), command.getContentType(),
              command.getForMonth(), command.getForYear(), command.getUserId());
    } else {
      return EntityClassContentsDao
          .fetchClassContentsByContentTypeForTeacher(command.getClassId(), command.getContentType(),
              command.getForMonth(), command.getForYear());
    }
  }
}
