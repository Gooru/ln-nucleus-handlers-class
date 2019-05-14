package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.enricher;

import io.vertx.core.json.JsonObject;
import java.util.List;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityContent;

/**
 * @author ashish.
 */

class ResourceQuestionEnricherService {

  private List<String> contentIds;
  private JsonObject idToEnrichmentDataMap;
  private boolean enrichmentDone = false;

  ResourceQuestionEnricherService(List<String> contentIds) {

    this.contentIds = contentIds;
  }

  void enrichContentInfo() {
    if (!enrichmentDone) {
      idToEnrichmentDataMap = new JsonObject();
      String contentArrayString = DbHelperUtil.toPostgresArrayString(contentIds);
      List<AJEntityContent> contents = AJEntityContent.fetchContentDetails(contentArrayString);

      contents.forEach(content -> {
        JsonObject data = new JsonObject();
        data.put(MessageConstants.TITLE, content.getString(MessageConstants.TITLE));
        data.put(MessageConstants.THUMBNAIL, content.getString(MessageConstants.THUMBNAIL));
        String taxonomyString = content.getString(MessageConstants.TAXONOMY);
        JsonObject taxonomy =
            (taxonomyString == null || taxonomyString.isEmpty()) ? new JsonObject()
                : new JsonObject(taxonomyString);
        data.put(MessageConstants.TAXONOMY, taxonomy);
        idToEnrichmentDataMap.put(content.getString(MessageConstants.ID), data);
      });
      enrichmentDone = true;
    }
  }


  JsonObject getEnrichmentInfo(String contentId) {
    if (!enrichmentDone) {
      throw new IllegalStateException("Enrichment not done, while asking for enrichment data");
    }
    return idToEnrichmentDataMap.getJsonObject(contentId);
  }
}
