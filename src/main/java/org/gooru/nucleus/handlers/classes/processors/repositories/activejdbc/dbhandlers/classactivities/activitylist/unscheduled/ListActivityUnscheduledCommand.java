package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitylist.unscheduled;

import java.util.ResourceBundle;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;

/**
 * @author ashish.
 */

public class ListActivityUnscheduledCommand {

  private final int forMonth;
  private final int forYear;
  private final String userId;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final String classId;
  private final Set<String> secondaryClasses;
  private final Set<String> contentType;

  ListActivityUnscheduledCommand(ProcessorContext context) {
    forMonth = DbHelperUtil.getForMonth(context);
    forYear = DbHelperUtil.getForYear(context);
    userId = context.userId();
    classId = context.classId();
    secondaryClasses = DbHelperUtil.getSecondaryClasses(context); 
    contentType = DbHelperUtil.getContentTypes(context);
  }

  public void validate() {
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
  
  public Set<String> getSecondaryClasses() {
    return secondaryClasses;
  }

  public Set<String> getContentType() {
    return contentType;
  }
}
