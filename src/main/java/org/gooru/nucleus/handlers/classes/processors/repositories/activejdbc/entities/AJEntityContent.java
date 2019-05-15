package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import java.util.List;
import java.util.Map;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("content")
public class AJEntityContent extends Model {

  public static final String RESOURCE_COUNT = "resource_count";
  public static final String QUESTION_COUNT = "question_count";
  public static final String OE_QUESTION_COUNT = "oe_question_count";
  public static final String CONTENT_COUNT = "content_count";
  public static final String CONTENT_FORMAT = "content_format";
  public static final String COLLECTION_ID = "collection_id";
  public static final String RESOURCE_FORMAT = "resource";
  public static final String QUESTION_FORMAT = "question";

  private static final String SELECT_CONTENTS = "select id, title, thumbnail, taxonomy from content where id = ANY(?::uuid[])";

  private static final String SELECT_CONTENT_COUNT_BY_COLLECTION =
      "SELECT count(id) as content_count, content_format, collection_id FROM content WHERE"
          + " collection_id = ANY(?::uuid[])  AND is_deleted = false GROUP BY"
          + " collection_id, content_format";

  private static final String SELECT_OE_QUESTION_COUNT =
      "SELECT count(id) as oe_question_count, collection_id FROM content WHERE collection_id = ANY(?::uuid[]) AND"
          + " is_deleted = false AND content_format = 'question' AND"
          + " content_subformat = 'open_ended_question' GROUP BY collection_id";

  public static final String SELECT_CONTENT_TO_AUTHORIZE =
      "select id, collection_id, course_id, creator_id, tenant, "
          + " tenant_root, publish_status from content where id = ?::uuid and is_deleted = false";

  public static List<Map> fetchCollectionContentCount(String collectionArrayString) {
    return Base.findAll(AJEntityContent.SELECT_CONTENT_COUNT_BY_COLLECTION, collectionArrayString);
  }

  public static List<Map> fetchOEQuestionsCount(String collectionIdsArrayAsString) {
    return Base.findAll(AJEntityContent.SELECT_OE_QUESTION_COUNT, collectionIdsArrayAsString);
  }

  public static List<AJEntityContent> fetchContentDetails(String contentArrayString) {
    return AJEntityContent.findBySQL(AJEntityContent.SELECT_CONTENTS, contentArrayString);
  }

  public String getTenant() {
    return this.getString("tenant");
  }

  public String getTenantRoot() {
    return this.getString("tenant_root");
  }

  public boolean isPublished() {
    String publishStatus = this.getString("publish_status");
    return "published".equalsIgnoreCase(publishStatus);
  }

  public String getCourseId() {
    return this.getString("course_id");
  }

  public String getCollectionId() {
    return this.getString("collection_id");
  }
}
