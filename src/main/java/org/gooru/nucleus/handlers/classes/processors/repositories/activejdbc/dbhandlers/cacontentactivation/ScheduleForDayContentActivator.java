package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.cacontentactivation;

import io.vertx.core.json.JsonObject;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ScheduleForDayContentActivator implements ContentActivator {

  private final AJEntityClass entityClass;
  private final AJEntityClassContents classContents;
  private final FlowDeterminer flowDeterminer;
  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ScheduleForDayContentActivator.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private LocalDate dcaAddedDate;
  private String dcaAddedDateAsString;

  ScheduleForDayContentActivator(AJEntityClass entityClass,
      AJEntityClassContents classContents, FlowDeterminer flowDeterminer,
      ProcessorContext context) {
    this.entityClass = entityClass;
    this.classContents = classContents;
    this.flowDeterminer = flowDeterminer;
    this.context = context;
  }


  @Override
  public void validate() {
    validateContentNotAlreadyScheduled();
    validateTheDates();
    validateAlreadyScheduledSameContentForThatDay();
  }

  @Override
  public void activateContent() {
    this.classContents.setDcaAddedDateIfNotPresent(dcaAddedDate);
    boolean result = this.classContents.save();
    if (!result) {
      if (classContents.hasErrors()) {
        LOGGER.warn("error in creating content map for class");
        throw new MessageResponseWrapperException(
            MessageResponseFactory.createValidationErrorResponse(getModelErrors()));
      }
    }
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.classContents.errors().entrySet()
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }

  private void validateAlreadyScheduledSameContentForThatDay() {
    LazyList<AJEntityClassContents> ajClassContents = AJEntityClassContents
        .findBySQL(AJEntityClassContents.SELECT_CLASS_CONTENTS_TO_VALIDATE_SCHEDULE,
            context.classId(), this.classContents.getContentId(), dcaAddedDateAsString);
    if (!ajClassContents.isEmpty()) {
      LOGGER.warn("For this class {} same content {} already scheduled for this date {}",
          context.classId(),
          this.classContents.getContentId(), dcaAddedDateAsString);
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("same.content.already.scheduled")));
    }
  }

  private void validateContentNotAlreadyScheduled() {
    if (this.classContents.getDcaAddedDate() != null) {
      LOGGER.warn("content {} already scheduled for this class {}", this.classContents.getId(),
          context.classId());
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("already.class.content.scheduled")));
    }
  }

  private void validateTheDates() {
    String incomingDateAsString = flowDeterminer.getInputDateAsString();
    try {
      if (incomingDateAsString != null) {
        dcaAddedDate = LocalDate.parse(incomingDateAsString);
        // toString() method will extract the date only (yyyy-mm-dd)
        dcaAddedDateAsString = dcaAddedDate.toString();
        if (this.dcaAddedDate.getMonthValue() != this.classContents.getForMonth()
            || this.dcaAddedDate.getYear() != this.classContents.getForYear()) {
          throw new MessageResponseWrapperException(MessageResponseFactory
              .createInvalidRequestResponse(
                  RESOURCE_BUNDLE.getString("dca.date.not.same.as.creation.date")));
        }
      } else {
        LOGGER.warn("Incoming date is null");
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")));
      }
    } catch (DateTimeParseException e) {
      LOGGER.warn("Invalid dca added date format {}", incomingDateAsString);
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("invalid.dca.added.date.format")));
    }
  }

}
