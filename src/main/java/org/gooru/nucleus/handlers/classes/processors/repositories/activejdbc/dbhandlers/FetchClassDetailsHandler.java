package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class FetchClassDetailsHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchClassDetailsHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private static final String RESPONSE_BUCKET_CLASSES = "class_details";
  private static final String MEMBER_COUNT = "member_count";
  private JsonArray classIds = null;

  FetchClassDetailsHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty()
        || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous or invalid user attempting to fetch class details");
      return new ExecutionResult<>(
          MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Payload should not be empty
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("Empty payload supplied to fetch class details");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
          RESOURCE_BUNDLE.getString("empty.payload")), ExecutionResult.ExecutionStatus.FAILED);
    }

    if (!context.request().containsKey(MessageConstants.CLASS_IDS)) {
      LOGGER.warn("Invalid params to fetch class details");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
          "Invalid params to fetch class details"), ExecutionResult.ExecutionStatus.FAILED);
    }

    this.classIds = context.request().getJsonArray(MessageConstants.CLASS_IDS);
    if (classIds == null || classIds.isEmpty()) {
      LOGGER.warn("Empty classIds supplied to fetch class details");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse("Empty classIds supplied to fetch class details"),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }


  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    return AuthorizerBuilder.buildFetchClassesForUserAuthorizer(context).authorize(null);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    JsonObject result = new JsonObject();
    LazyList<AJEntityClass> classes = AJEntityClass.where(AJEntityClass.FETCH_CLASS_DETAILS_BY_IDS,
        Utils.convertListToPostgresArrayStringRepresentation(classIds.getList()));
    JsonArray classDetails = new JsonArray(JsonFormatterBuilder
        .buildSimpleJsonFormatter(false, AJEntityClass.FETCH_QUERY_FIELD_LIST).toJson(classes));
    populateClassMemberCount(classDetails, classIds.getList());

    result.put(RESPONSE_BUCKET_CLASSES, classDetails);
    return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(result),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private static void populateClassMemberCount(JsonArray classDetails, List<String> classIds) {
    if (classDetails != null && !classDetails.isEmpty()) {
      // Group all the class member count as key and value pair, key should be class id and value
      // should be class member count.
      List<Map> classMemberCounts =
          Base.findAll(AJClassMember.FETCH_MEMBERSHIP_COUNT_FOR_CLASSES_QUERY,
              Utils.convertListToPostgresArrayStringRepresentation(classIds));
      Map<String, Object> classMemberCountDetails = new HashMap<>();
      classMemberCounts.forEach(classMemberMap -> {
        classMemberCountDetails.put(classMemberMap.get(AJClassMember.CLASS_ID).toString(),
            classMemberMap.get(AJClassMember.CLASS_MEMBER_COUNT));
      });

      // populate the class member count in class details.
      classDetails.forEach(aclass -> {
        JsonObject classAsJson = (JsonObject) aclass;
        String classId = classAsJson.getString(AJEntityClass.ID);
        Object memberCount = classMemberCountDetails.get(classId);
        classAsJson.put(MEMBER_COUNT, memberCount);
      });

    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
