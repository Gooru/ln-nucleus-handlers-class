package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.enricher;

import io.vertx.core.json.JsonObject;
import java.util.List;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCollection;

/**
 * @author ashish.
 */

class OfflineActivitiesEnricherService {

  private JsonObject idToEnrichmentDataMap;
  private List<String> offlineActivityIds;
  private boolean enrichmentDone = false;

  OfflineActivitiesEnricherService(List<String> offlineActivityIds) {
    this.offlineActivityIds = offlineActivityIds;
  }

  // NOTE: Currently enrichment is done at par with Collection. If and when needed this logic needs
  // to be changed to do offline specific enrichment
  void enrichOfflineActivitiesInfo() {
    if (!enrichmentDone) {
      idToEnrichmentDataMap = new JsonObject();
      String collectionArrayString = DbHelperUtil.toPostgresArrayString(offlineActivityIds);
      List<AJEntityCollection> collections = AJEntityCollection
          .findCollectionsForSpecifiedIds(collectionArrayString);

      collections.forEach(content -> {
        JsonObject data = createOfflineDetailJson(content);
        idToEnrichmentDataMap.put(content.getString(MessageConstants.ID), data);
      });
      enrichmentDone = true;
    }
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
