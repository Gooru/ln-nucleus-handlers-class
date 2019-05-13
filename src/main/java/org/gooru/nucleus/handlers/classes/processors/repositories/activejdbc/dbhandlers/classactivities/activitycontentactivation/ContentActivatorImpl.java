package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitycontentactivation;

import io.vertx.core.json.JsonObject;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish
 */
class ContentActivatorImpl implements ContentActivator {

  private final AJEntityClassContents classContents;
  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ContentActivatorImpl.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final AJEntityClass entityClass;

  ContentActivatorImpl(AJEntityClass entityClass,
      AJEntityClassContents classContents, ProcessorContext context) {
    this.classContents = classContents;
    this.context = context;
    this.entityClass = entityClass;
  }


  @Override
  public void activateContent() {
    validateContentNotAlreadyActivated();
    validateContentIsScheduled();
    validateAlreadyActivatedSameContentForThatDay();

    this.classContents.setActivationDateIfNotPresent(classContents.getDcaAddedDate().toLocalDate());

    boolean result = this.classContents.save();
    if (!result) {
      if (classContents.hasErrors()) {
        LOGGER.warn("error in creating content map for class");
        throw new MessageResponseWrapperException(
            MessageResponseFactory.createValidationErrorResponse(getModelErrors()));
      }
    }
  }

  private void validateContentNotAlreadyActivated() {
    if (this.classContents.getActivationDate() != null) {
      LOGGER.warn("content {} already activated to this class {}", this.classContents.getId(),
          context.classId());
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("already.class.content.activated")));
    }
  }

  private void validateContentIsScheduled() {
    if (this.classContents.getDcaAddedDate() != null) {
      LOGGER.warn("content {} is not scheduled to activate for this class {}",
          this.classContents.getId(), context.classId());
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("not.scheduled")));
    }
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.classContents.errors().entrySet()
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }

  private void validateAlreadyActivatedSameContentForThatDay() {
    String activationDateAsString = classContents.getDcaAddedDate().toString();
    LazyList<AJEntityClassContents> ajClassContents = AJEntityClassContents
        .findBySQL(AJEntityClassContents.SELECT_CLASS_CONTENTS_TO_VALIDATE_ACTIVATION,
            context.classId(),
            this.classContents.getContentId(), activationDateAsString);
    if (!ajClassContents.isEmpty()) {
      LOGGER.warn("For this class {} same content {} already activated for this date {}",
          context.classId(),
          this.classContents.getContentId(), activationDateAsString);
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("same.content.already.activated")));
    }
  }

}
