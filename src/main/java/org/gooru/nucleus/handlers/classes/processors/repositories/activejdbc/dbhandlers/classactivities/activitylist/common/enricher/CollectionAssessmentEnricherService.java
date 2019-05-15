package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.enricher;

import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityContent;

/**
 * @author ashish.
 */

class CollectionAssessmentEnricherService {

  private JsonObject idToEnrichmentDataMap;
  private List<String> collectionIds;
  private boolean enrichmentDone = false;

  CollectionAssessmentEnricherService(List<String> collectionIds) {
    this.collectionIds = collectionIds;
  }

  void enrichCollectionInfo() {
    if (!enrichmentDone) {
      idToEnrichmentDataMap = new JsonObject();
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

      List<Map> oeQuestionCountFromDB = AJEntityContent
          .fetchOEQuestionsCount(collectionArrayString);

      oeQuestionCountFromDB.forEach(data -> {
        idToEnrichmentDataMap
            .getJsonObject(data.get(AJEntityContent.COLLECTION_ID).toString())
            .put(AJEntityContent.OE_QUESTION_COUNT, data.get(AJEntityContent.OE_QUESTION_COUNT));
      });
      enrichmentDone = true;
    }
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


  JsonObject getEnrichmentInfo(String collectionId) {
    if (!enrichmentDone) {
      throw new IllegalStateException("Enrichment not done, while asking for enrichment data");
    }
    return idToEnrichmentDataMap.getJsonObject(collectionId);
  }
}
