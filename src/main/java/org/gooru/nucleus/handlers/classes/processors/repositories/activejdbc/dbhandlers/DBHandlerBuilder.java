package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.activityaddusers.AddClassContentUsersHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.activitylistusers.ListClassContentUsersHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classmembersactivate.ClassMembersActivateHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classmembersdeactivate.ClassMembersDeactivateHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.fetchclassmembers.FetchClassMembersHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting.UpdateClassMembersRerouteSettingHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.profilebaseline.UpdateProfileBaselineForSpecifiedStudentsHandler;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting.UpdateClassRerouteSettingHandler;

/**
 * Created by ashish on 11/1/16.
 */
public final class DBHandlerBuilder {

  private DBHandlerBuilder() {
    throw new AssertionError();
  }

  public static DBHandler buildCreateClassHandler(ProcessorContext context) {
    return new CreateClassHandler(context);
  }

  public static DBHandler buildUpdateClassHandler(ProcessorContext context) {
    return new UpdateClassHandler(context);
  }

  public static DBHandler buildFetchClassHandler(ProcessorContext context) {
    return new FetchClassHandler(context);
  }

  public static DBHandler buildFetchClassMembersHandler(ProcessorContext context) {
    return new FetchClassMembersHandler(context);
  }

  public static DBHandler buildFetchClassesForCourseHandler(ProcessorContext context) {
    return new FetchClassesForCourseHandler(context);
  }

  public static DBHandler buildFetchClassesForUserHandler(ProcessorContext context) {
    return new FetchClassesForUserHandler(context);
  }

  public static DBHandler buildJoinClassByStudentHandler(ProcessorContext context) {
    return new JoinClassByStudentHandler(context);
  }

  public static DBHandler buildInviteStudentToClassHandler(ProcessorContext context) {
    return new InviteStudentToClassHandler(context);
  }

  public static DBHandler buildDeleteClassHandler(ProcessorContext context) {
    return new DeleteClassHandler(context);
  }

  public static DBHandler buildAssociateCourseWithClassHandler(ProcessorContext context) {
    return new AssociateCourseWithClassHandler(context);
  }

  public static DBHandler buildUpdateCollaboratorForClassHandler(ProcessorContext context) {
    return new UpdateCollaboratorForClassHandler(context);
  }

  public static DBHandler buildSetContentVisibilityHandler(ProcessorContext context) {
    return new ContentVisibilityHandler(context);
  }

  public static DBHandler buildRemoveInviteHandler(ProcessorContext context) {
    return new RemoveInviteHandler(context);
  }

  public static DBHandler buildRemoveStudentHandler(ProcessorContext context) {
    return new RemoveStudentHandler(context);
  }

  public static DBHandler buildGetVisibleContentHandler(ProcessorContext context) {
    return new VisibleContentHandler(context);
  }

  public static DBHandler buildAddContentInClassHandler(ProcessorContext context) {
    return new AddContentInClassHandler(context);
  }

  public static DBHandler buildListClassContentHandler(ProcessorContext context) {
    return new ListClassContentHandler(context);
  }

  public static DBHandler buildEnableContentInClassHandler(ProcessorContext context) {
    return new EnableContentInClassHandler(context);
  }

  public static DBHandler buildArchiveClassHandler(ProcessorContext context) {
    return new ArchiveClassHandler(context);
  }

  public static DBHandler buildDeleteClassContentHandler(ProcessorContext context) {
    return new DeleteClassContentHandler(context);
  }

  public static DBHandler buildUpdateClassRerouteSettingHandler(ProcessorContext context) {
    return new UpdateClassRerouteSettingHandler(context);
  }

  public static DBHandler buildUpdateProfileBaselineForSpecifiedStudentsHandler(
      ProcessorContext context) {
    return new UpdateProfileBaselineForSpecifiedStudentsHandler(context);
  }

  public static DBHandler buildUpdateClassMembersRerouteSettingHandler(ProcessorContext context) {
    return new UpdateClassMembersRerouteSettingHandler(context);
  }

  public static DBHandler buildAddClassContentUsersHandler(ProcessorContext context) {
    return new AddClassContentUsersHandler(context);
  }

  public static DBHandler buildListClassContentUsersHandler(ProcessorContext context) {
    return new ListClassContentUsersHandler(context);
  }

  public static DBHandler buildClassMembersActivateHandler(ProcessorContext context) {
    return new ClassMembersActivateHandler(context);
  }

  public static DBHandler buildClassMembersDeactivateHandler(ProcessorContext context) {
    return new ClassMembersDeactivateHandler(context);
  }

  public static DBHandler buildClassPreferenceUpdateHandler(ProcessorContext context) {
    return new ClassPreferenceUpdateHandler(context);
  }
}
