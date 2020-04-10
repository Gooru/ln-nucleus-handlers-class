package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitycontentmeetingcancel;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;


class ContentMeetingCancelInClassServiceImpl implements ContentMeetingCancelInClassService {

  private final AJEntityClassContents classContents;
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ContentMeetingCancelInClassServiceImpl.class);

  ContentMeetingCancelInClassServiceImpl(AJEntityClass entityClass,
      AJEntityClassContents classContents, ProcessorContext context) {
    this.classContents = classContents;
  }


  @Override
  public void cancelMeeting() {
    cancelMeetingInClass();
    boolean result = this.classContents.save();
    if (!result) {
      if (classContents.hasErrors()) {
        LOGGER.warn("error in meeting setup for content in class");
        throw new MessageResponseWrapperException(
            MessageResponseFactory.createValidationErrorResponse(getModelErrors()));
      }
    }
  }

  private void cancelMeetingInClass() {
    this.classContents.set(AJEntityClassContents.MEETING_ID, null);
    this.classContents.set(AJEntityClassContents.MEETING_URL, null);
    this.classContents.set(AJEntityClassContents.MEETING_START_TIME, null);
    this.classContents.set(AJEntityClassContents.MEETING_END_TIME, null);
    this.classContents.set(AJEntityClassContents.MEETING_TIME_ZONE, null);
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.classContents.errors().entrySet()
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }



}
