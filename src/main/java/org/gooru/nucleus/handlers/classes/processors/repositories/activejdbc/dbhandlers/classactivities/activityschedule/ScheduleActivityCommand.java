package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activityschedule;

import io.vertx.core.json.JsonObject;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author ashish.
 */

class ScheduleActivityCommand {

  private LocalDate dcaAddedDate;
  private LocalDate endDate;
  private final String dcaAddedDateAsString;
  private final String endDateAsString;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  ScheduleActivityCommand(JsonObject payload) {
    if (payload != null && !payload.isEmpty()) {
      dcaAddedDateAsString = payload.getString(AJEntityClassContents.DCA_ADDED_DATE);
      endDateAsString = payload.getString(AJEntityClassContents.END_DATE);
    } else {
      dcaAddedDateAsString = null;
      endDateAsString = null;
    }
  }

  void validate() {
    if (dcaAddedDateAsString == null || endDateAsString == null) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("schedule.end.date.mandatory")));
    }
    try {
      dcaAddedDate = LocalDate.parse(dcaAddedDateAsString);
      endDate = LocalDate.parse(endDateAsString);
      if (dcaAddedDate.isAfter(endDate)) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("schedule.end.date.sequence")));
      }
      if (dcaAddedDate.isBefore(LocalDate.now())) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("schedule.past")));
      }
    } catch (DateTimeParseException dte) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("date.format.invalid")));
    }
  }

  // Make sure that accessors are called after call of validate
  public LocalDate getDcaAddedDate() {
    return dcaAddedDate;
  }

  // Make sure that accessors are called after call of validate
  public LocalDate getEndDate() {
    return endDate;
  }

  public String getDcaAddedDateAsString() {
    return dcaAddedDateAsString;
  }

  public String getEndDateAsString() {
    return endDateAsString;
  }
}
