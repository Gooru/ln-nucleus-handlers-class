package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.cacontentactivation;

import io.vertx.core.json.JsonObject;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
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
class ActivationFlowContentActivator implements ContentActivator {

  private final AJEntityClassContents classContents;
  private final FlowDeterminer flowDeterminer;
  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(ActivationFlowContentActivator.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private String activationDateAsString;
  private LocalDate activationDate;

  ActivationFlowContentActivator(AJEntityClass entityClass,
      AJEntityClassContents classContents, FlowDeterminer flowDeterminer,
      ProcessorContext context) {
    this.classContents = classContents;
    this.flowDeterminer = flowDeterminer;
    this.context = context;
  }


  @Override
  public void validate() {
    validateContentNotAlreadyActivated();
    validateTheDates();
    validateAlreadyActivatedSameContentForThatDay();
  }

  @Override
  public void activateContent() {
    this.classContents.setActivationDateIfNotPresent(activationDate);
    if (this.classContents.getDcaAddedDate() == null) {
      this.classContents.setDcaAddedDateIfNotPresent(activationDate);
    }
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

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.classContents.errors().entrySet()
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }

  private void validateAlreadyActivatedSameContentForThatDay() {
    LazyList<AJEntityClassContents> ajClassContents = AJEntityClassContents
        .findBySQL(AJEntityClassContents.SELECT_CLASS_CONTENTS_TO_VALIDATE_ACTIVATION, context.classId(),
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

  private void validateTheDates() {
    String incomingDateAsString = flowDeterminer.getInputDateAsString();
    try {
      if (incomingDateAsString != null) {
        activationDate = LocalDate.parse(incomingDateAsString);
        // toString() method will extract the date only (yyyy-mm-dd)
        activationDateAsString = activationDate.toString();
        final String dcaAddedDate = Objects.toString(this.classContents.getDcaAddedDate(), null);

        if (dcaAddedDate != null && !activationDateAsString.equals(dcaAddedDate)) {
          LOGGER.warn("Activation date {} should be same as class content creation date {}",
              activationDateAsString, dcaAddedDate);
          throw new MessageResponseWrapperException(MessageResponseFactory
              .createInvalidRequestResponse(
                  RESOURCE_BUNDLE.getString("activation.date.not.same.as.creation.date")));

        } else if (dcaAddedDate == null) {
          if (activationDate.getMonthValue() != this.classContents.getForMonth()
              || activationDate.getYear() != this.classContents.getForYear()) {
            throw new MessageResponseWrapperException(MessageResponseFactory
                .createInvalidRequestResponse(
                    RESOURCE_BUNDLE.getString("activation.date.not.same.as.monthyear")));
          }
        }
      } else {
        LOGGER.warn("Incoming date is null");
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")));
      }
    } catch (DateTimeParseException e) {
      LOGGER.warn("Invalid activation date format {}", incomingDateAsString);
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("invalid.activation.date.format")));
    }
  }
}
