package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityUser;
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
  private final List<String> classIdList = new ArrayList<>();
  private JsonArray classIds = null;

  FetchClassDetailsHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
 // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty() || context.userId()
        .equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      LOGGER.warn("Anonymous or invalid user attempting to create class");
      return new ExecutionResult<>(
          MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Payload should not be empty
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("Empty payload supplied to create class");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("empty.payload")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
     
    this.classIds = (JsonArray) context.request().getValue(MessageConstants.CLASS);
    if(classIds == null || classIds.isEmpty()) {
      LOGGER.warn("Invalid params to fetch class details");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse("Invalid params to fetch class details"),
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
    for (Object id : classIds){ 
      classIdList.add(id.toString());
     } 
    LazyList<AJEntityClass> classes = AJEntityClass.where(AJEntityClass.FETCH_MULTIPLE_QUERY_FILTER,
        Utils.convertListToPostgresArrayStringRepresentation(classIdList));
    JsonArray classDetails = new JsonArray(JsonFormatterBuilder
        .buildSimpleJsonFormatter(false, AJEntityClass.FETCH_QUERY_FIELD_LIST)
        .toJson(classes));
    result.put(RESPONSE_BUCKET_CLASSES, classDetails);
    return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(result),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    // TODO Auto-generated method stub
    return false;
  }

}
