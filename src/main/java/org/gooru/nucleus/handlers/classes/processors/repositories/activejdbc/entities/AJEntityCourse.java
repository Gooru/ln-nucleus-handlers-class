package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("course")
public class AJEntityCourse extends Model {

  public static final String TABLE_COURSE = "course";
  private static final String TENANT = "tenant";
  private static final String TENANT_ROOT = "tenant_root";
  private static final String PUBLISH_STATUS_TYPE_PUBLISHED = "published";
  private static final String PUBLISH_STATUS = "publish_status";
  public static final String ID = "id";
  public static final String TITLE = "title";
  public static final String VERSION = "version";
  public static final String COURSE_TITLE = "course_title";
  public static final String COURSE_VERSION = "course_version";
  private static final String SUBJECT_BUCKET = "subject_bucket";
  private static final String PRIMARY_LANGUAGE = "primary_language";

  public static final String SELECT_COURSE_TO_AUTHORIZE =
      "SELECT id, owner_id, collaborator, tenant, tenant_root, publish_status FROM course WHERE id = ?::uuid AND is_deleted = false";
  public static final String COURSE_VERSION_FETCH_QUERY =
      "select version from course where id = ?::uuid and is_deleted = false";
  public static final String COURSE_SUBJECT_BUCKET_FETCH_QUERY =
      "select subject_bucket from course where id = ?::uuid and is_deleted = false";
  public static final String COURSE_ASSOCIATION_FILTER = "id = ?::uuid and is_deleted = false";

  public static final String SELECT_COURSE_TITLE_VERSION =
      "SELECT id, title, version FROM course where id = ANY(?::uuid[]) AND is_deleted = false";

  public String getTenant() {
    return this.getString(TENANT);
  }

  public String getTenantRoot() {
    return this.getString(TENANT_ROOT);
  }

  public boolean isCoursePublished() {
    String publishStatus = this.getString(PUBLISH_STATUS);
    return PUBLISH_STATUS_TYPE_PUBLISHED.equalsIgnoreCase(publishStatus);
  }

  public String getTitle() {
    return this.getString(TITLE);
  }

  public String getVersion() {
    return this.getString(VERSION);
  }

  public String getSubjectBucket() {
    return this.getString(SUBJECT_BUCKET);
  }

  public int getPrimaryLanguage() {
    return this.getInteger(PRIMARY_LANGUAGE);
  }

}
