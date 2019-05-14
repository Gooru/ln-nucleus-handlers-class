package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.unscheduled;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author ashish.
 */

class ListActivityUnscheduledCommand {
  // TODO: Implement this with correct payload

  private final String contentType;
  private final boolean isStudent;
  private final int forMonth;
  private final int forYear;
  private final String userId;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final String classId;

  ListActivityUnscheduledCommand(ProcessorContext context, boolean isStudent) {
    contentType = DbHelperUtil.readRequestParam(AJEntityClassContents.CONTENT_TYPE, context);
    forMonth = DbHelperUtil.getForMonth(context);
    forYear = DbHelperUtil.getForYear(context);
    userId = context.userId();
    classId = context.classId();
    this.isStudent = isStudent;
  }


  public void validate() {
    if (contentType != null && !AJEntityClassContents.ACCEPT_CONTENT_TYPES.contains(contentType)) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("content.type.invalid")));
    }
  }

  public String getContentType() {
    return contentType;
  }

  public boolean isStudent() {
    return isStudent;
  }

  public int getForMonth() {
    return forMonth;
  }

  public int getForYear() {
    return forYear;
  }

  public String getUserId() {
    return userId;
  }

  public String getClassId() {
    return classId;
  }

  public boolean fetchingAllContentTypesButOffline() {
    return contentType == null;
  }


  public boolean fetchingOfflineContentType() {
    return (contentType != null && AJEntityClassContents.OFFLINE_ACTIVITY
        .equalsIgnoreCase(contentType));
  }

  public boolean fetchingSpecificContentType() {
    return (contentType != null && !AJEntityClassContents.OFFLINE_ACTIVITY
        .equalsIgnoreCase(contentType));
  }

}
