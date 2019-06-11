package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.enricher;

import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassContentsDao;

/**
 * @author ashish.
 */

class OfflineActivitiesEnricherService {

  private JsonObject idToEnrichmentDataMap;
  private List<String> offlineActivityIds;
  private boolean enrichmentDone = false;
  private static final String TASK_COUNT = "task_count" ;
  private static final String OA_ID = "oa_id" ;

  OfflineActivitiesEnricherService(List<String> offlineActivityIds) {
    this.offlineActivityIds = offlineActivityIds;
  }

  // NOTE: Currently enrichment is done at par with Collection. If and when needed this logic needs
  // to be changed to do offline specific enrichment
  void enrichOfflineActivitiesInfo() {
    if (!enrichmentDone) {
      idToEnrichmentDataMap = new JsonObject();
      String collectionArrayString = DbHelperUtil.toPostgresArrayString(offlineActivityIds);
      enrichWithOAInfo(collectionArrayString);
      enrichWithTasksInfo(collectionArrayString);
      enrichmentDone = true;
    }
  }

  private void enrichWithTasksInfo(String collectionArrayString) {
    List<Map> tasksCountListMap = EntityClassContentsDao.fetchTaskCount(collectionArrayString);

    if (tasksCountListMap == null || tasksCountListMap.isEmpty()) {
      return;
    }

    tasksCountListMap.forEach(data -> {
      idToEnrichmentDataMap.getJsonObject(data.get(OA_ID).toString())
          .put(TASK_COUNT, data.get(TASK_COUNT));
    });
  }

  private void enrichWithOAInfo(String collectionArrayString) {
    List<AJEntityCollection> collections = AJEntityCollection
        .findCollectionsForSpecifiedIds(collectionArrayString);

    collections.forEach(content -> {
      JsonObject data = createOfflineDetailJson(content);
      idToEnrichmentDataMap.put(content.getString(MessageConstants.ID), data);
    });
  }

  private JsonObject createOfflineDetailJson(AJEntityCollection content) {
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
