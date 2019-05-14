package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.contentfetcher;

import java.util.List;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.onlinescheduled.ListOnlineScheduledActivityCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;

/**
 * @author ashish.
 */

public interface ActivityFetcher {

  List<AJEntityClassContents> fetchContents();

  static ActivityFetcher buildContentFetcherForOnlineScheduledActivities(
      ListOnlineScheduledActivityCommand command) {
    return new ContentFetcherForOnlineScheduledActivities(command);
  }

}
