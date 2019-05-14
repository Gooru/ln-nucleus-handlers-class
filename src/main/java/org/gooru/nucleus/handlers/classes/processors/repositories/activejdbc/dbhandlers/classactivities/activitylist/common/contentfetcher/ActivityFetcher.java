package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.contentfetcher;

import java.util.List;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.offlineactive.ListActivityOfflineActiveCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.offlinecompleted.ListActivityOfflineCompletedCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.onlinescheduled.ListOnlineScheduledActivityCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.unscheduled.ListActivityUnscheduledCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;

/**
 * @author ashish.
 */

public interface ActivityFetcher {

  List<AJEntityClassContents> fetchContents();

  int fetchTotalContentCount();

  static ActivityFetcher buildContentFetcherForOnlineScheduledActivities(
      ListOnlineScheduledActivityCommand command) {
    return new ContentFetcherForOnlineScheduledActivities(command);
  }

  static ActivityFetcher buildContentFetcherForOfflineActiveActivities(
      ListActivityOfflineActiveCommand command) {
    return new ContentFetcherForOfflineActiveActivities(command);
  }

  static ActivityFetcher buildContentFetcherForOfflineCompletedActivities(
      ListActivityOfflineCompletedCommand command) {
    return new ContentFetcherForOfflineCompletedActivities(command);
  }

  static ActivityFetcher buildContentFetcherForUnscheduledActivities(
      ListActivityUnscheduledCommand command) {
    return new ContentFetcherForUnscheduledActivities(command);
  }


}
