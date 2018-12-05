package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.activitylistusers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;

/**
 * @author ashish.
 */

class ResponseBuilder {

  private final String classId;
  private final List<String> users;
  private final List<String> usersInClass = new ArrayList<>();
  private LazyList<AJClassMember> members;
  private final Map<String, Boolean> usersInClassIsActiveMap = new HashMap<>();


  ResponseBuilder(String classId, List<String> users) {
    this.classId = classId;
    // NOTE: users could be null or empty
    // null means assigned to whole class
    // empty means assigned to no one
    this.users = users;
  }

  ExecutionResult<MessageResponse> buildResponse() {
    if (users == null) {
      return fetchAllClassMembers();
    } else if (users.isEmpty()) {
      return noClassMembersResponse();
    } else {
      return fetchSpecifiedMembers();
    }
  }

  private ExecutionResult<MessageResponse> fetchSpecifiedMembers() {
    members = AJClassMember.where(AJClassMember.FETCH_SPECIFIC_USERS_QUERY_FILTER, classId,
        DbHelperUtil.toPostgresArrayString(users));
    if (members.isEmpty()) {
      return noClassMembersResponse();
    }
    initializeUsersInClass();
    return createResponseForSpecifiedMembers();
  }

  private void initializeUsersInClass() {
    for (AJClassMember classMember : members) {
      usersInClass.add(classMember.getUserId());
      usersInClassIsActiveMap.put(classMember.getUserId(), classMember.getIsActive());
    }
  }

  private ExecutionResult<MessageResponse> createResponseForSpecifiedMembers() {
    JsonArray usersDemographics = populateDemographics();
    enrichIsActiveInformation(usersDemographics);

    return specifiedClassMembersResponse(usersDemographics);
  }

  private void enrichIsActiveInformation(JsonArray usersDemographics) {
    for (Object userDemographicObject : usersDemographics) {
      JsonObject userDemographic = (JsonObject) userDemographicObject;
      String userId = userDemographic.getString(AJEntityUser.ID);
      Boolean isActive = usersInClassIsActiveMap.get(userId);
      userDemographic.put(AJClassMember.IS_ACTIVE, isActive);
    }
  }

  private JsonArray populateDemographics() {
    LazyList<AJEntityUser> demographics = AJEntityUser.findBySQL(AJEntityUser.GET_SUMMARY_QUERY,
        Utils.convertListToPostgresArrayStringRepresentation(usersInClass));
    return new JsonArray(JsonFormatterBuilder
        .buildSimpleJsonFormatter(false, AJEntityUser.GET_SUMMARY_QUERY_FIELD_LIST)
        .toJson(demographics));
  }


  private ExecutionResult<MessageResponse> fetchAllClassMembers() {
    members =
        AJClassMember.where(AJClassMember.FETCH_ALL_JOINED_USERS_FILTER, classId);
    if (members.isEmpty()) {
      return noClassMembersResponse();
    }
    initializeUsersInClass();
    return createResponseForSpecifiedMembers();
  }

  private ExecutionResult<MessageResponse> noClassMembersResponse() {
    JsonObject result = new JsonObject().put(AJEntityClassContents.USERS, new JsonArray());
    return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(result),
        ExecutionStatus.SUCCESSFUL);
  }

  private ExecutionResult<MessageResponse> specifiedClassMembersResponse(
      JsonArray usersDemographics) {
    JsonObject result = new JsonObject().put(AJEntityClassContents.USERS, usersDemographics);
    return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(result),
        ExecutionStatus.SUCCESSFUL);
  }

}
