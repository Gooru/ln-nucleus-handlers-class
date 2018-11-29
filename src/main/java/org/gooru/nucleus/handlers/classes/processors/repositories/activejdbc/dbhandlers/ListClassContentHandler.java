package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ListClassContentHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListClassContentHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private String contentType;
  private boolean isStudent;
  private int forMonth;
  private int forYear;
  private List<String> contentIds;
  private List<String> collectionIds;
  private JsonObject classContentOtherData;
  private LazyList<AJEntityClassContents> classContents;

  ListClassContentHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      validateUser();
      contentType = DbHelperUtil.readRequestParam(AJEntityClassContents.CONTENT_TYPE, context);
      forMonth = DbHelperUtil.getForMonth(context);
      forYear = DbHelperUtil.getForYear(context);
      validateClassId();
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityClass> classes = AJEntityClass
        .where(AJEntityClass.FETCH_QUERY_FILTER, context.classId());
    if (classes.isEmpty()) {
      LOGGER.warn("Not able to find class '{}'", this.context.classId());
      return new ExecutionResult<>(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    AJEntityClass entityClass = classes.get(0);
    ExecutionResult<MessageResponse> classAuthorization =
        AuthorizerBuilder.buildClassContentAuthorizer(this.context).authorize(entityClass);
    if (!classAuthorization.continueProcessing()) {
      isStudent = checkStudent(entityClass);
      if (!isStudent) {
        return new ExecutionResult<>(
            MessageResponseFactory
                .createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
            ExecutionResult.ExecutionStatus.FAILED);
      } else {
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
      }
    } else {
      return classAuthorization;
    }
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {

    fetchClassContents();

    JsonArray results;
    if (isStudent) {
      results = new JsonArray(JsonFormatterBuilder
          .buildSimpleJsonFormatter(false, AJEntityClassContents.RESPONSE_FIELDS_FOR_STUDENT)
          .toJson(classContents));
    } else {
      results = new JsonArray(JsonFormatterBuilder
          .buildSimpleJsonFormatter(false, AJEntityClassContents.RESPONSE_FIELDS_FOR_TEACHER)
          .toJson(classContents));
    }
    JsonArray resultSet = new JsonArray();

    if (results.size() > 0) {
      initializeCollectionIdsAndContentIds(results);
      classContentOtherData = new JsonObject();

      if (contentIds.size() > 0) {
        enrichContentInfo();
      }

      if (collectionIds.size() > 0) {
        enrichCollectionInfo();
      }

      results.forEach(result -> {
        JsonObject data = ((JsonObject) result);
        if (classContentOtherData.containsKey(data.getString(AJEntityClassContents.CONTENT_ID))) {
          data.mergeIn(classContentOtherData
              .getJsonObject(data.getString(AJEntityClassContents.CONTENT_ID)));
        }
        resultSet.add(data);
      });

    }

    return new ExecutionResult<>(
        MessageResponseFactory
            .createOkayResponse(new JsonObject().put(MessageConstants.CLASS_CONTENTS, resultSet)),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private void fetchClassContents() {
    if (contentType == null) {
      classContents = getAllClassContents();
    } else {
      classContents = getClassContentsByContentType();
    }
  }

  private void enrichCollectionInfo() {
    String collectionArrayString = DbHelperUtil.toPostgresArrayString(collectionIds);
    LazyList<AJEntityCollection> collections =
        AJEntityCollection
            .findBySQL(AJEntityCollection.SELECT_COLLECTION, collectionArrayString);
    collections.forEach(content -> {
      JsonObject data = new JsonObject();
      data.put(MessageConstants.TITLE, content.getString(MessageConstants.TITLE));
      data.put(MessageConstants.THUMBNAIL, content.getString(MessageConstants.THUMBNAIL));
      data.put(MessageConstants.URL, content.getString(MessageConstants.URL));
      String taxonomyString = content.getString(MessageConstants.TAXONOMY);
      JsonObject taxonomy = (taxonomyString == null || taxonomyString.isEmpty()) ? new JsonObject()
          : new JsonObject(taxonomyString);
      data.put(MessageConstants.TAXONOMY, taxonomy);
      classContentOtherData.put(content.getString(MessageConstants.ID), data);
    });
    List<Map> collectionContentCount =
        Base.findAll(AJEntityContent.SELECT_CONTENT_COUNT_BY_COLLECTION, collectionArrayString);
    collectionContentCount.forEach(data -> {
      final String key = ((String) data.get(AJEntityContent.CONTENT_FORMAT))
          .equalsIgnoreCase(AJEntityContent.QUESTION_FORMAT) ? AJEntityContent.QUESTION_COUNT
          : AJEntityContent.RESOURCE_COUNT;
      classContentOtherData.getJsonObject(data.get(AJEntityContent.COLLECTION_ID).toString())
          .put(key,
              data.get(AJEntityContent.CONTENT_COUNT));
    });
    List<Map> oeQuestionCountFromDB =
        Base.findAll(AJEntityContent.SELECT_OE_QUESTION_COUNT, collectionArrayString);
    oeQuestionCountFromDB.forEach(data -> {
      classContentOtherData
          .getJsonObject(data.get(AJEntityContent.COLLECTION_ID).toString())
          .put(AJEntityContent.OE_QUESTION_COUNT, data.get(AJEntityContent.OE_QUESTION_COUNT));
    });
  }

  private void enrichContentInfo() {
    String contentArrayString = DbHelperUtil.toPostgresArrayString(contentIds);
    LazyList<AJEntityContent> contents =
        AJEntityContent.findBySQL(AJEntityContent.SELECT_CONTENTS, contentArrayString);
    contents.forEach(content -> {
      JsonObject data = new JsonObject();
      data.put(MessageConstants.TITLE, content.getString(MessageConstants.TITLE));
      data.put(MessageConstants.THUMBNAIL, content.getString(MessageConstants.THUMBNAIL));
      String taxonomyString = content.getString(MessageConstants.TAXONOMY);
      JsonObject taxonomy = (taxonomyString == null || taxonomyString.isEmpty()) ? new JsonObject()
          : new JsonObject(taxonomyString);
      data.put(MessageConstants.TAXONOMY, taxonomy);
      classContentOtherData.put(content.getString(MessageConstants.ID), data);
    });
  }

  private void initializeCollectionIdsAndContentIds(JsonArray results) {
    contentIds = new ArrayList<>();
    collectionIds = new ArrayList<>();
    results.forEach(content -> {
      JsonObject classContent = (JsonObject) content;
      if (checkContentTypeIsCollection(
          classContent.getString(AJEntityClassContents.CONTENT_TYPE))) {
        collectionIds.add(classContent.getString(AJEntityClassContents.CONTENT_ID));
      } else if (checkContentTypeIsContent(
          classContent.getString(AJEntityClassContents.CONTENT_TYPE))) {
        contentIds.add(classContent.getString(AJEntityClassContents.CONTENT_ID));
      }
    });
  }

  private LazyList<AJEntityClassContents> getClassContentsByContentType() {
    if (isStudent) {
      return AJEntityClassContents
          .fetchClassContentsByContentTypeForStudent(context.classId(), contentType, forMonth,
              forYear, context.userId());
    } else {
      return AJEntityClassContents
          .fetchClassContentsByContentTypeForTeacher(context.classId(), contentType, forMonth,
              forYear);
    }
  }

  private LazyList<AJEntityClassContents> getAllClassContents() {
    if (isStudent) {
      return AJEntityClassContents
          .fetchAllContentsForStudent(context.classId(), forMonth, forYear, context.userId());
    } else {
      return AJEntityClassContents.fetchAllContentsForTeacher(context.classId(), forMonth, forYear);
    }
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private void validateUser() {
    if ((context.userId() == null) || context.userId().isEmpty()
        || MessageConstants.MSG_USER_ANONYMOUS.equalsIgnoreCase(context.userId())) {
      LOGGER.warn("Invalid user");
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")));
    }
  }

  private void validateClassId() {
    if (context.classId() == null || context.classId().isEmpty()) {
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createNotFoundResponse(RESOURCE_BUNDLE.getString("missing.class.id")));
    }
  }

  private boolean checkStudent(AJEntityClass model) {
    LazyList<AJClassMember> members = AJClassMember.where(AJClassMember.FETCH_FOR_USER_QUERY_FILTER,
        this.context.classId(), this.context.userId());
    return !members.isEmpty();
  }

  private boolean checkContentTypeIsCollection(String contentType) {
    return (contentType.equalsIgnoreCase(AJEntityClassContents.ASSESSMENT)
        || contentType.equalsIgnoreCase(AJEntityClassContents.ASSESSMENT_EXTERNAL)
        || contentType.equalsIgnoreCase(AJEntityClassContents.COLLECTION_EXTERNAL)
        || contentType.equalsIgnoreCase(AJEntityClassContents.COLLECTION));
  }

  private boolean checkContentTypeIsContent(String contentType) {
    return (contentType.equalsIgnoreCase(AJEntityClassContents.RESOURCE)
        || contentType.equalsIgnoreCase(AJEntityClassContents.QUESTION));
  }
}
