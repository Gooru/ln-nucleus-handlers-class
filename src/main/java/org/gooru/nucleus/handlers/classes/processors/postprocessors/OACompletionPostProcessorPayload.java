package org.gooru.nucleus.handlers.classes.processors.postprocessors;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ashish.
 */

public class OACompletionPostProcessorPayload {

  public static final String DEFAULT_CONTENT_SOURCE = "dailyclassactivity";
  public static final int DEFAULT_PATH_ID = 0;
  private String classId;
  private Long oaDcaId;
  private String oaId;
  private String contentSource = DEFAULT_CONTENT_SOURCE;
  private int pathId = DEFAULT_PATH_ID;
  private String pathType = null;
  private List<String> users = new ArrayList<>();

  public OACompletionPostProcessorPayload setClassId(String classId) {
    this.classId = classId;
    return this;
  }

  public OACompletionPostProcessorPayload setOADcaId(Long oaDcaId) {
    this.oaDcaId = oaDcaId;
    return this;
  }

  public OACompletionPostProcessorPayload setOAId(String oaId) {
    this.oaId = oaId;
    return this;
  }

  public OACompletionPostProcessorPayload setContentSource(String contentSource) {
    this.contentSource = contentSource;
    return this;
  }

  public OACompletionPostProcessorPayload setPathId(int pathId) {
    this.pathId = pathId;
    return this;
  }

  public OACompletionPostProcessorPayload setPathType(String pathType) {
    this.pathType = pathType;
    return this;
  }

  public OACompletionPostProcessorPayload setUsers(List<String> users) {
    this.users = users;
    return this;
  }

  public JsonObject createPayload() {
    JsonArray usersArray = new JsonArray();
    if (users != null && !users.isEmpty()) {
      for (String user : users) {
        usersArray.add(user);
      }
    }

    return new JsonObject().put("class_id", classId).put("oa_dca_id", oaDcaId).put("oa_id", oaId)
        .put("content_source", contentSource).put("path_id", pathId).put("path_type", pathType)
        .put("users", usersArray);
  }

}
