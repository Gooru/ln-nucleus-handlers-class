package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.unscheduled;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.contentfetcher.ActivityFetcher;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.common.enricher.ContentEnricher;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.common.validators.SanityValidators;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.EntityClassDao;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ListClassContentUnscheduledHandler implements DBHandler {

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private List<AJEntityClassContents> classContents;
  private ListActivityUnscheduledCommand command;

  public ListClassContentUnscheduledHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      SanityValidators.validateUser(context);
      SanityValidators.validateClassId(context);
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    try {
      AJEntityClass entityClass = EntityClassDao.fetchClassById(context.classId());
      command = new ListActivityUnscheduledCommand(context);
      command.validate();
      
      // Verify that the secondary classes exists in db which are not deleted and not archived
      Set<String> inputClasses = command.getSecondaryClasses();
      if (inputClasses != null && !inputClasses.isEmpty()) {
        LazyList<AJEntityClass> classes = EntityClassDao
            .fetchMultipleClassesByIds(DbHelperUtil.toPostgresArrayString(inputClasses));
        if (classes.size() != inputClasses.size()) {
          return new ExecutionResult<>(
              MessageResponseFactory.createInvalidRequestResponse(
                  RESOURCE_BUNDLE.getString("invalid.secondary.classes")),
              ExecutionResult.ExecutionStatus.FAILED);
        }
      }
      
      return AuthorizerBuilder.buildClassContentAuthorizer(this.context).authorize(entityClass);
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {

    fetchClassContents();
 
    // For time being, added only class id in the current response and kept rest of sorting of the
    // response same. Once we have clarity on the UI and response needed we can revisit it.
    JsonArray renderedContent =
        ContentEnricher.buildContentEnricherForUnscheduledActivities(classContents).enrichContent();

    return new ExecutionResult<>(
        MessageResponseFactory
            .createOkayResponse(
                new JsonObject().put(MessageConstants.CLASS_CONTENTS, renderedContent)),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private void fetchClassContents() {
    classContents = ActivityFetcher.buildContentFetcherForUnscheduledActivities(command)
        .fetchContents();
  }


  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
