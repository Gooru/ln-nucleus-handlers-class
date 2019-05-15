package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.enricher;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;

/**
 * Take the activity list and fetch details for each activity to enrich it and render it
 *
 * @author ashish.
 */

class ContentEnricherForAllTypesOfActivities implements ContentEnricher {

  private final List<AJEntityClassContents> classContents;
  private final boolean isStudent;

  private List<String> contentIds = new ArrayList<>();
  private List<String> collectionIds = new ArrayList<>();
  private List<String> offlineActivityIds = new ArrayList<>();


  private JsonArray unenrichedActivities;
  private JsonArray enrichedActivities;
  private CollectionAssessmentEnricherService collectionAssessmentEnricherService;
  private ResourceQuestionEnricherService resourceQuestionEnricherService;
  private OfflineActivitiesEnricherService offlineActivitiesEnricherService;

  ContentEnricherForAllTypesOfActivities(List<AJEntityClassContents> classContents,
      boolean isStudent) {
    this.classContents = classContents;
    this.isStudent = isStudent;
  }

  @Override
  public JsonArray enrichContent() {
    initializeUnenrichedActivities();
    enrichedActivities = new JsonArray();

    doEnrichment();
    return enrichedActivities;
  }

  private void doEnrichment() {
    if (unenrichedActivities.size() > 0) {
      initializeIdsForContentTypes(unenrichedActivities);

      if (contentIds.size() > 0) {
        resourceQuestionEnricherService = new ResourceQuestionEnricherService(contentIds);
        resourceQuestionEnricherService.enrichContentInfo();
      }

      if (collectionIds.size() > 0) {
        collectionAssessmentEnricherService = new CollectionAssessmentEnricherService(
            collectionIds);
        collectionAssessmentEnricherService.enrichCollectionInfo();
      }

      if (offlineActivityIds.size() > 0) {
        offlineActivitiesEnricherService = new OfflineActivitiesEnricherService(offlineActivityIds);
        offlineActivitiesEnricherService.enrichOfflineActivitiesInfo();
      }

      for (Object result : unenrichedActivities) {
        JsonObject data = ((JsonObject) result);
        JsonObject enrichmentInfo = fetchEnrichmentInfo(data);
        if (enrichmentInfo != null && !enrichmentInfo.isEmpty()) {
          data.mergeIn(enrichmentInfo);
        }
        enrichedActivities.add(data);
      }
    }
  }

  private JsonObject fetchEnrichmentInfo(JsonObject data) {
    String contentType = data.getString(AJEntityClassContents.CONTENT_TYPE);
    String contentId = data.getString(AJEntityClassContents.CONTENT_ID);

    if (checkContentTypeIsCollection(contentType)) {
      if (collectionAssessmentEnricherService != null) {
        return collectionAssessmentEnricherService.getEnrichmentInfo(contentId);
      }
    } else if (checkContentTypeIsContent(contentType)) {
      if (resourceQuestionEnricherService != null) {
        return resourceQuestionEnricherService.getEnrichmentInfo(contentId);
      }
    } else if (checkContentTypeIsOfflineActivity(contentType)) {
      if (offlineActivitiesEnricherService != null) {
        return offlineActivitiesEnricherService.getEnrichmentInfo(contentId);
      }
    }
    return null;
  }

  private void initializeUnenrichedActivities() {
    if (isStudent) {
      unenrichedActivities = new JsonArray(JsonFormatterBuilder
          .buildSimpleJsonFormatter(false, AJEntityClassContents.RESPONSE_FIELDS_FOR_STUDENT)
          .toJsonFromList(classContents));
    } else {
      unenrichedActivities = new JsonArray(JsonFormatterBuilder
          .buildSimpleJsonFormatter(false, AJEntityClassContents.RESPONSE_FIELDS_FOR_TEACHER)
          .toJsonFromList(classContents));
    }
  }


  private void initializeIdsForContentTypes(JsonArray results) {
    for (Object content : results) {
      JsonObject classContent = (JsonObject) content;
      String contentType = classContent.getString(AJEntityClassContents.CONTENT_TYPE);
      String contentId = classContent.getString(AJEntityClassContents.CONTENT_ID);

      if (checkContentTypeIsCollection(contentType)) {
        collectionIds.add(contentId);
      } else if (checkContentTypeIsContent(contentType)) {
        contentIds.add(contentId);
      } else if (checkContentTypeIsOfflineActivity(contentType)) {
        offlineActivityIds.add(contentId);
      }
    }
  }

  private boolean checkContentTypeIsCollection(String contentType) {
    return (AJEntityClassContents.ASSESSMENT_TYPES.matcher(contentType).matches()
        || AJEntityClassContents.COLLECTION_TYPES.matcher(contentType).matches());
  }

  private boolean checkContentTypeIsContent(String contentType) {
    return (contentType.equalsIgnoreCase(AJEntityClassContents.RESOURCE)
        || contentType.equalsIgnoreCase(AJEntityClassContents.QUESTION));
  }

  private boolean checkContentTypeIsOfflineActivity(String contentType) {
    return (contentType.equalsIgnoreCase(AJEntityClassContents.OFFLINE_ACTIVITY));
  }


}
