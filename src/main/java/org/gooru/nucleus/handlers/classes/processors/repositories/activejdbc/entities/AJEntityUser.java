package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;
import java.util.Arrays;
import java.util.List;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Created by ashish on 4/3/16.
 */
@Table("users")
public class AJEntityUser extends Model {

  public static final String GET_SUMMARY_QUERY =
      "select id, first_name, last_name, thumbnail, roster_global_userid, email from users where id = ANY(?::uuid[])";
  public static final String FETCH_TEACHER_DETAILS_QUERY =
      "select id, first_name, last_name, thumbnail from users where id = ANY(select creator_id from "
          + "class where id = ANY(?::uuid[]))";

  private static final String ID = "id";
  private static final String FIRST_NAME = "first_name";
  private static final String LAST_NAME = "last_name";
  private static final String THUMBNAIL = "thumbnail";
  private static final String ROSTER_GLOBAL_USERID = "roster_global_userid";
  private static final String EMAIL = "email";
  public static final List<String> GET_SUMMARY_QUERY_FIELD_LIST =
      Arrays.asList(ID, FIRST_NAME, LAST_NAME, THUMBNAIL, ROSTER_GLOBAL_USERID, EMAIL);

  private static final String TENANT = "tenant_id";
  private static final String TENANT_ROOT = "tenant_root";

  private static final String COLLABORATOR_VALIDATION_QUERY =
      "select tenant_id, tenant_root from users where id = ANY(?::uuid[])";

  public String getTenant() {
    return this.getString(TENANT);
  }

  public String getTenantRoot() {
    return this.getString(TENANT_ROOT);
  }

  public static LazyList<AJEntityUser> getCollaboratorsTenantInfo(JsonArray collaborators) {
    return AJEntityUser.findBySQL(COLLABORATOR_VALIDATION_QUERY,
        Utils.convertListToPostgresArrayStringRepresentation(collaborators.getList()));
  }

}
