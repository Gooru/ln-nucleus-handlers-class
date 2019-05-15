package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activityadd;

import io.vertx.core.json.JsonObject;
import java.time.LocalDate;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

class AddActivityCommand {

  private final JsonObject payload;
  private String contentType;
  private String contentId;
  private int forYear;
  private int forMonth;
  private String dcaDateString;
  private String endDateString;
  private LocalDate dcaAddedDate;
  private LocalDate endDate;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private static final Logger LOGGER = LoggerFactory.getLogger(AddActivityCommand.class);

  AddActivityCommand(JsonObject payload) {
    if (payload != null && !payload.isEmpty()) {
      dcaDateString = payload.getString(AJEntityClassContents.DCA_ADDED_DATE);
      endDateString = payload.getString(AJEntityClassContents.END_DATE);
      contentType = payload.getString(AJEntityClassContents.CONTENT_TYPE);
      contentId = payload.getString(AJEntityClassContents.CONTENT_ID);
      forMonth = payload.getInteger(AJEntityClassContents.FOR_MONTH);
      forYear = payload.getInteger(AJEntityClassContents.FOR_YEAR);
    }
    this.payload = payload;
  }

  void validate() {
    validateContextRequestFields();
  }

  public String getContentType() {
    return contentType;
  }

  public String getContentId() {
    return contentId;
  }

  public int getForYear() {
    return forYear;
  }

  public int getForMonth() {
    return forMonth;
  }

  public String getDcaDateString() {
    return dcaDateString;
  }

  public String getEndDateString() {
    return endDateString;
  }

  public LocalDate getDcaAddedDate() {
    return dcaAddedDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  private void validateContextRequestFields() {
    JsonObject errors = new DefaultPayloadValidator()
        .validatePayload(payload, AJEntityClassContents.createFieldSelector(),
            AJEntityClassContents.getValidatorRegistry());
    if (errors != null && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createValidationErrorResponse(errors));
    }
    validateDependentFields();
  }

  private void validateDependentFields() {

    validatePastMonthSchedule();
    validateDcaAddedDateAndEndDateTuple();
    initializeAddedDateEndDate();
    validateAddedDateWithMonthYear();
    validateAddedDateWithEndDate();
  }

  private void validateAddedDateWithEndDate() {
    if (dcaAddedDate != null && endDate != null) {
      if (dcaAddedDate.isAfter(endDate)) {
        throw new MessageResponseWrapperException(
            MessageResponseFactory
                .createInvalidRequestResponse(
                    RESOURCE_BUNDLE.getString("schedule.end.date.sequence")));
      }
    }
  }

  private void initializeAddedDateEndDate() {
    if (dcaDateString != null) {
      dcaAddedDate = LocalDate.parse(dcaDateString);
    }
    if (endDateString != null) {
      endDate = LocalDate.parse(endDateString);
    }
  }

  private void validateAddedDateWithMonthYear() {
    if (dcaAddedDate != null) {
      if (dcaAddedDate.getMonthValue() != forMonth || dcaAddedDate.getYear() != forYear) {
        throw new MessageResponseWrapperException(
            MessageResponseFactory
                .createInvalidRequestResponse(
                    RESOURCE_BUNDLE.getString("dca.monthyear.addeddate.mismatch")));
      }
    }
  }

  private void validateDcaAddedDateAndEndDateTuple() {
    if ((dcaDateString == null && endDateString != null) || (dcaDateString != null
        && endDateString == null)) {
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(
                  RESOURCE_BUNDLE.getString("dcadate.enddate.tuple.invalid")));
    }
  }

  private void validatePastMonthSchedule() {
    LocalDate firstOfThisMonth = LocalDate.now().withDayOfMonth(1);
    LocalDate firstOfSpecifiedMonthYear = LocalDate.of(forYear, forMonth, 1);

    // Do not allow past month-year
    if (firstOfThisMonth.isAfter(firstOfSpecifiedMonthYear)) {
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("dca.past.monthyear")));
    }
  }


  private static class DefaultPayloadValidator implements PayloadValidator {

  }


}
