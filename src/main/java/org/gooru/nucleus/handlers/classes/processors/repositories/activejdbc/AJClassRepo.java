package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.ClassRepo;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;

/**
 * Created by ashish on 28/1/16.
 */
class AJClassRepo implements ClassRepo {

  private final ProcessorContext context;

  public AJClassRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse createClass() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildCreateClassHandler(context));
  }

  @Override
  public MessageResponse updateClass() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildUpdateClassHandler(context));
  }

  @Override
  public MessageResponse fetchClass() {
    return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildFetchClassHandler(context));
  }

  @Override
  public MessageResponse fetchClassMembers() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildFetchClassMembersHandler(context));
  }

  @Override
  public MessageResponse fetchClassesForCourse() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildFetchClassesForCourseHandler(context));
  }

  @Override
  public MessageResponse fetchClassesForUser() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildFetchClassesForUserHandler(context));
  }

  @Override
  public MessageResponse joinClassByStudent() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildJoinClassByStudentHandler(context));
  }

  @Override
  public MessageResponse deleteClass() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildDeleteClassHandler(context));
  }

  @Override
  public MessageResponse associateCourseWithClass() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildAssociateCourseWithClassHandler(context));
  }

  @Override
  public MessageResponse setContentVisibility() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildSetContentVisibilityHandler(context));
  }

  @Override
  public MessageResponse updateCollaboratorForClass() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildUpdateCollaboratorForClassHandler(context));
  }

  @Override
  public MessageResponse removeStudentFromClass() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildRemoveStudentHandler(context));
  }

  @Override
  public MessageResponse getVisibleContent() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildGetVisibleContentHandler(context));
  }

  @Override
  public MessageResponse addContentInClass() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildAddContentInClassHandler(context));
  }

  @Override
  public MessageResponse listClassContentUnscheduled() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildListClassContentUnscheduledHandler(context));
  }

  @Override
  public MessageResponse enableContentInClass() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildEnableContentInClassHandler(context));
  }

  @Override
  public MessageResponse scheduleContentInClass() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildScheduleContentInClassHandler(context));
  }

  @Override
  public MessageResponse archiveClass() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildArchiveClassHandler(context));
  }

  @Override
  public MessageResponse deleteContentFromClass() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildDeleteClassContentHandler(context));
  }

  @Override
  public MessageResponse updateClassRerouteSetting() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildUpdateClassRerouteSettingHandler(context));
  }

  @Override
  public MessageResponse updateProfileBaselineForSpecifiedStudents() {
    return TransactionExecutor
        .executeTransaction(
            DBHandlerBuilder.buildUpdateProfileBaselineForSpecifiedStudentsHandler(context));
  }

  @Override
  public MessageResponse updateClassMembersRerouteSetting() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildUpdateClassMembersRerouteSettingHandler(context));
  }

  @Override
  public MessageResponse addClassContentUsers() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildAddClassContentUsersHandler(context));
  }

  @Override
  public MessageResponse listClassContentUsers() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildListClassContentUsersHandler(context));
  }

  @Override
  public MessageResponse classMembersDeactivate() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildClassMembersDeactivateHandler(context));
  }

  @Override
  public MessageResponse classMembersActivate() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildClassMembersActivateHandler(context));
  }

  @Override
  public MessageResponse updateClassPreference() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildClassPreferenceUpdateHandler(context));
  }

  @Override
  public MessageResponse updateClassLanguage() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildClassLanguageUpdateHandler(context));
  }

  @Override
  public MessageResponse updateProfileBaselineForStudent() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildUpdateProfileBaselineForStudentHandler(context));
  }

  @Override
  public MessageResponse updateClassContentMasteryAccrual() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildUpdateClassContentMasteryAccrualHandler(context));
  }

  @Override
  public MessageResponse classContentComplete() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildClassContentCompletionMarkerHandler(context));
  }

  @Override
  public MessageResponse listClassContentListOfflineActiveProcessor() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildClassContentListOfflineActiveHandler(context));
  }

  @Override
  public MessageResponse listClassContentListOfflineCompletedProcessor() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildClassContentListOfflineCompletedHandler(context));
  }

  @Override
  public MessageResponse listClassContentListOnlineScheduledProcessor() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildClassContentListOnlineScheduledHandler(context));
  }

  @Override
  public MessageResponse addStudentsToClass() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildClassStudentsAddHandler(context));
  }

  @Override
  public MessageResponse findSecondaryClasses() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildClassSecondaryClassesFindHandler(context));
  }
  
  @Override
  public MessageResponse listClassContentListScheduledProcessor() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildClassContentListScheduledHandler(context));
  }
}
