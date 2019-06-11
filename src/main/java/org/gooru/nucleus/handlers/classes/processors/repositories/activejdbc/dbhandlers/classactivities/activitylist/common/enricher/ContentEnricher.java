package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.enricher;

import io.vertx.core.json.JsonArray;
import java.util.List;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;

/**
 * @author ashish.
 */

public interface ContentEnricher {

  JsonArray enrichContent();

  static ContentEnricher buildContentEnricherForOnlineScheduledActivities(
      List<AJEntityClassContents> classContents, boolean isStudent) {

    return new ContentEnricherForAllTypesOfActivities(classContents, isStudent);
  }

  static ContentEnricher buildContentEnricherForOfflineActiveActivities(
      List<AJEntityClassContents> classContents, boolean isStudent) {

    // Here we can optimize a bit to provide only offline type of enricher
    return new ContentEnricherForAllTypesOfActivities(classContents, isStudent);
  }

  static ContentEnricher buildContentEnricherForOfflineCompletedActivities(
      List<AJEntityClassContents> classContents, boolean isStudent) {

    // Here we can optimize a bit to provide only offline type of enricher
    return new ContentEnricherForAllTypesOfActivities(classContents, isStudent);
  }

  static ContentEnricher buildContentEnricherForUnscheduledActivities(
      List<AJEntityClassContents> classContents) {

    return new ContentEnricherForAllTypesOfActivities(classContents, false);
  }

}
