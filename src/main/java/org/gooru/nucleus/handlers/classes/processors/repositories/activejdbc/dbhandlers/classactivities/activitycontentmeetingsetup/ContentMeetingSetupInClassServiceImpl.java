package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitycontentmeetingsetup;

import java.time.format.DateTimeFormatter;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;


class ContentMeetingSetupInClassServiceImpl implements ContentMeetingSetupInClassService {

  private final AJEntityClassContents classContents;
  private final ProcessorContext context;
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ContentMeetingSetupInClassServiceImpl.class);

  ContentMeetingSetupInClassServiceImpl(AJEntityClass entityClass,
      AJEntityClassContents classContents, ProcessorContext context) {
    this.classContents = classContents;
    this.context = context;
  }


  @Override
  public void setupMeeting() {
    populateMeetingDetails();
    boolean result = this.classContents.save();
    if (!result) {
      if (classContents.hasErrors()) {
        LOGGER.warn("error in meeting setup for content in class");
        throw new MessageResponseWrapperException(
            MessageResponseFactory.createValidationErrorResponse(getModelErrors()));
      }
    }
  }

  private void populateMeetingDetails() {
    JsonObject request = this.context.request();
    String meetingId = request.getString(AJEntityClassContents.MEETING_ID);
    String meetingUrl = request.getString(AJEntityClassContents.MEETING_URL);
    String meetingStartDatetime = request.getString(AJEntityClassContents.MEETING_START_TIME);
    String meetingEndDatetime = request.getString(AJEntityClassContents.MEETING_END_TIME);
    String meetingTimeZone = request.getString(AJEntityClassContents.MEETING_TIME_ZONE);
    this.classContents.set(AJEntityClassContents.MEETING_ID, meetingId);
    this.classContents.set(AJEntityClassContents.MEETING_URL, meetingUrl);
    this.classContents.set(AJEntityClassContents.MEETING_START_TIME, FieldConverter
        .convertFieldToTimestampWithFormat(meetingStartDatetime, DateTimeFormatter.ISO_DATE_TIME));
    this.classContents.set(AJEntityClassContents.MEETING_END_TIME, FieldConverter
        .convertFieldToTimestampWithFormat(meetingEndDatetime, DateTimeFormatter.ISO_DATE_TIME));
    this.classContents.set(AJEntityClassContents.MEETING_TIME_ZONE, meetingTimeZone);
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.classContents.errors().entrySet()
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }



}
