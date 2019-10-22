package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.offlinecompleted;

import java.util.ResourceBundle;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author ashish.
 */

public class ListActivityOfflineCompletedCommand {

  private final boolean isStudent;
  private final int offset;
  private final int limit;
  private final String userId;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final String classId;
  private final Set<String> secondaryClasses;

  ListActivityOfflineCompletedCommand(ProcessorContext context, boolean isStudent) {
    limit = DbHelperUtil.getLimitFromContext(context);
    offset = DbHelperUtil.getOffsetFromContext(context);
    userId = context.userId();
    classId = context.classId();
    this.isStudent = isStudent;
    secondaryClasses = DbHelperUtil.getSecondaryClasses(context);
  }


  public void validate() {
    if (offset < 0 || limit < 0) {
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.limit.offset")));
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

  public int getOffset() {
    return offset;
  }

  public int getLimit() {
    return limit;
  }

  public Set<String> getSecondaryClasses() {
    return secondaryClasses;
  }
  
}
