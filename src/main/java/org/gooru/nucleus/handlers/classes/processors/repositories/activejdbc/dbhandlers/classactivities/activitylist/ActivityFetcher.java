package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist;

import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.javalite.activejdbc.LazyList;

/**
 * @author ashish.
 */

interface ActivityFetcher {

  LazyList<AJEntityClassContents> fetchContents();

  static ActivityFetcher buildOfflineActivitiesFetcher(ListActivityCommand command) {
    return new OfflineActivitiesFetcher(command);
  }

  static ActivityFetcher buildAllNonOfflineActivitiesFetcher(ListActivityCommand command) {
    return new AllNonOfflineActivitiesFetcher(command);
  }

  static ActivityFetcher buildSpecificContentTypeActivitiesFetcher(ListActivityCommand command) {
    return new SpecificContentTypeActivitiesFetcher(command);
  }

}
