package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.onlinescheduled;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author ashish.
 */

public class ListOnlineScheduledActivityCommand {

  private final boolean isStudent;
  private final String userId;
  private final String startDateString;
  private final String endDateString;
  private LocalDate startDate;
  private LocalDate endDate;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final String classId;
  private boolean isValid = false;

  private static final String START_DATE = "start_date";
  private static final String END_DATE = "end_date";

  ListOnlineScheduledActivityCommand(ProcessorContext context, boolean isStudent) {
    userId = context.userId();
    classId = context.classId();
    startDateString = DbHelperUtil.readRequestParam(START_DATE, context);
    endDateString = DbHelperUtil.readRequestParam(END_DATE, context);
    this.isStudent = isStudent;
  }


  public void validate() {
    try {
      if (startDateString == null || endDateString == null) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("date.format.invalid")));
      }
      startDate = LocalDate.parse(startDateString);
      endDate = LocalDate.parse(endDateString);
      if (startDate.isAfter(endDate)) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("start.end.date.sequence")));
      }
      isValid = true;
    } catch (DateTimeParseException e) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("date.format.invalid")));
    }
  }

  public boolean isStudent() {
    return isStudent;
  }

  public String getUserId() {
    return userId;
  }

  public String getClassId() {
    return classId;
  }

  public LocalDate getStartDate() {
    if (!isValid) {
      validate();
    }
    return startDate;
  }

  public LocalDate getEndDate() {
    if (!isValid) {
      validate();
    }
    return endDate;
  }
}
