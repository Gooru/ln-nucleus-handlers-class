package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.javalite.activejdbc.LazyList;

/**
 * Take the activity list and fetch details for each activity to enrich it and render it
 *
 * @author ashish.
 */

class ContentRenderer {

  private final LazyList<AJEntityClassContents> classContents;
  private final ListActivityCommand command;
  private List<String> contentIds;
  private List<String> collectionIds;
  private JsonObject idToEnrichmentDataMap;
  private JsonArray unenrichedActivities;
  private JsonArray enrichedActivities;

  ContentRenderer(LazyList<AJEntityClassContents> classContents, ListActivityCommand command) {
    this.classContents = classContents;
    this.command = command;
  }

  JsonArray renderContent() {
    initializeUnenrichedActivities();
    enrichedActivities = new JsonArray();

    doEnrichment();
    return enrichedActivities;
  }

  private void doEnrichment() {
    if (unenrichedActivities.size() > 0) {
      initializeCollectionIdsAndContentIds(unenrichedActivities);
      idToEnrichmentDataMap = new JsonObject();

      if (contentIds.size() > 0) {
        enrichContentInfo();
      }

      if (collectionIds.size() > 0) {
        enrichCollectionInfo();
      }

      unenrichedActivities.forEach(result -> {
        JsonObject data = ((JsonObject) result);
        if (idToEnrichmentDataMap.containsKey(data.getString(AJEntityClassContents.CONTENT_ID))) {
          data.mergeIn(idToEnrichmentDataMap
              .getJsonObject(data.getString(AJEntityClassContents.CONTENT_ID)));
        }
        enrichedActivities.add(data);
      });
    }
  }

  private void initializeUnenrichedActivities() {
    if (command.isStudent()) {
      unenrichedActivities = new JsonArray(JsonFormatterBuilder
          .buildSimpleJsonFormatter(false, AJEntityClassContents.RESPONSE_FIELDS_FOR_STUDENT)
          .toJson(classContents));
    } else {
      unenrichedActivities = new JsonArray(JsonFormatterBuilder
          .buildSimpleJsonFormatter(false, AJEntityClassContents.RESPONSE_FIELDS_FOR_TEACHER)
          .toJson(classContents));
    }
  }


  private void enrichCollectionInfo() {
    String collectionArrayString = DbHelperUtil.toPostgresArrayString(collectionIds);
    List<AJEntityCollection> collections = AJEntityCollection
        .findCollectionsForSpecifiedIds(collectionArrayString);

    collections.forEach(content -> {
      JsonObject data = createCollectionDetailJson(content);
      idToEnrichmentDataMap.put(content.getString(MessageConstants.ID), data);
    });

    List<Map> collectionContentCount = AJEntityContent
        .fetchCollectionContentCount(collectionArrayString);

    collectionContentCount.forEach(data -> {
      final String key = ((String) data.get(AJEntityContent.CONTENT_FORMAT))
          .equalsIgnoreCase(AJEntityContent.QUESTION_FORMAT) ? AJEntityContent.QUESTION_COUNT
          : AJEntityContent.RESOURCE_COUNT;
      idToEnrichmentDataMap.getJsonObject(data.get(AJEntityContent.COLLECTION_ID).toString())
          .put(key,
              data.get(AJEntityContent.CONTENT_COUNT));
    });

    List<Map> oeQuestionCountFromDB = AJEntityContent.fetchOEQuestionsCount(collectionArrayString);

    oeQuestionCountFromDB.forEach(data -> {
      idToEnrichmentDataMap
          .getJsonObject(data.get(AJEntityContent.COLLECTION_ID).toString())
          .put(AJEntityContent.OE_QUESTION_COUNT, data.get(AJEntityContent.OE_QUESTION_COUNT));
    });
  }

  private JsonObject createCollectionDetailJson(AJEntityCollection content) {
    JsonObject data = new JsonObject();
    data.put(MessageConstants.TITLE, content.getString(MessageConstants.TITLE));
    data.put(MessageConstants.THUMBNAIL, content.getString(MessageConstants.THUMBNAIL));
    data.put(MessageConstants.URL, content.getString(MessageConstants.URL));
    String taxonomyString = content.getString(MessageConstants.TAXONOMY);
    JsonObject taxonomy = (taxonomyString == null || taxonomyString.isEmpty()) ? new JsonObject()
        : new JsonObject(taxonomyString);
    data.put(MessageConstants.TAXONOMY, taxonomy);
    return data;
  }

  private void enrichContentInfo() {
    String contentArrayString = DbHelperUtil.toPostgresArrayString(contentIds);
    List<AJEntityContent> contents = AJEntityContent.fetchContentDetails(contentArrayString);

    contents.forEach(content -> {
      JsonObject data = new JsonObject();
      data.put(MessageConstants.TITLE, content.getString(MessageConstants.TITLE));
      data.put(MessageConstants.THUMBNAIL, content.getString(MessageConstants.THUMBNAIL));
      String taxonomyString = content.getString(MessageConstants.TAXONOMY);
      JsonObject taxonomy = (taxonomyString == null || taxonomyString.isEmpty()) ? new JsonObject()
          : new JsonObject(taxonomyString);
      data.put(MessageConstants.TAXONOMY, taxonomy);
      idToEnrichmentDataMap.put(content.getString(MessageConstants.ID), data);
    });
  }

  private void initializeCollectionIdsAndContentIds(JsonArray results) {
    contentIds = new ArrayList<>();
    collectionIds = new ArrayList<>();
    results.forEach(content -> {
      JsonObject classContent = (JsonObject) content;
      if (checkContentTypeIsCollection(
          classContent.getString(AJEntityClassContents.CONTENT_TYPE))) {
        collectionIds.add(classContent.getString(AJEntityClassContents.CONTENT_ID));
      } else if (checkContentTypeIsContent(
          classContent.getString(AJEntityClassContents.CONTENT_TYPE))) {
        contentIds.add(classContent.getString(AJEntityClassContents.CONTENT_ID));
      }
    });
  }

  private boolean checkContentTypeIsCollection(String contentType) {
    return (AJEntityClassContents.ASSESSMENT_TYPES.matcher(contentType).matches()
        || AJEntityClassContents.COLLECTION_TYPES.matcher(contentType).matches());
  }

  private boolean checkContentTypeIsContent(String contentType) {
    return (contentType.equalsIgnoreCase(AJEntityClassContents.RESOURCE)
        || contentType.equalsIgnoreCase(AJEntityClassContents.QUESTION));
  }

}
