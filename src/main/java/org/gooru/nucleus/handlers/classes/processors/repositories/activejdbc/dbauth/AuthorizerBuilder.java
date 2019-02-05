package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth;

import io.vertx.core.json.JsonArray;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;

/**
 * Created by ashish on 29/1/16.
 */
public final class AuthorizerBuilder {

  private AuthorizerBuilder() {
    throw new AssertionError();
  }

  public static Authorizer<AJEntityClass> buildAssociateCourseWithClassAuthorizer(
      ProcessorContext context) {
    return new AssociateCourseWithClassAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildDeleteAuthorizer(ProcessorContext context) {
    return new ClassOwnerAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildArchiveAuthorizer(ProcessorContext context) {
    return new ClassOwnerAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildFetchClassesForCourseAuthorizer(
      ProcessorContext context) {
    // Course owner should be calling this API
    return new CourseOwnerAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildFetchClassesForUserAuthorizer(
      ProcessorContext context) {
    // As long as session token is valid and user is not anonymous, which is
    // the case as we are, we should be fine
    return model -> new ExecutionResult<>(null,
        ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  public static Authorizer<AJEntityClass> buildFetchClassAuthorizer(ProcessorContext context) {
    return new TenantReadClassAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildFetchClassMembersAuthorizer(
      ProcessorContext context) {
    // User should be a member (which is either teacher or collaborator or
    // student of that class.
    return new ClassMemberAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildClassMembersAuthorizer(ProcessorContext context) {
    // User should be a member (which is either teacher or collaborator or
    // student of that class.
    return new ClassMemberAuthorizer(context);
  }
  
  public static Authorizer<AJEntityClass> buildClassStudentAuthorizer(ProcessorContext context) {
    return new ClassStudentAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildInviteStudentToClassAuthorizer(
      ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildRemoveInviteAuthorizer(ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildRemoveStudentAuthorizer(ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildJoinClassByStudentAuthorizer(
      ProcessorContext context,
      AJClassMember membership) {
    return new OpenClassOrInvitedStudentAuthorizer(context, membership);
  }

  public static Authorizer<AJEntityClass> buildUpdateClassAuthorizer(ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildUpdateRerouteSettingAuthorizer(
      ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildUpdateMembersRerouteSettingAuthorizer(
      ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildUpdateProfileBaselineAuthorizer(
      ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildUpdateCollaboratorAuthorizer(
      ProcessorContext context) {
    return new ClassOwnerAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildCreateClassAuthorizer(ProcessorContext context) {
    // As long as session token is valid and user is not anonymous, which is
    // the case as we are, we should be fine
    return model -> new ExecutionResult<>(null,
        ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  public static Authorizer<AJEntityClass> buildContentVisibilityAuthorizer(
      ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildVisibleContentAuthorizer(ProcessorContext context) {
    return new ClassMemberAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildVisibleContentStatsAuthorizer(
      ProcessorContext context) {
    // FIXME: No idea as to how to authorize as this API is expensive and
    // may be potentially heavy on DB
    // and currently it is just pass through
    return model -> new ExecutionResult<>(null,
        ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  public static Authorizer<AJEntityClass> buildTenantJoinAuthorizer(ProcessorContext context) {
    return new TenantJoinAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildTenantCollaboratorAuthorizer(
      ProcessorContext context,
      JsonArray collaborators) {
    return new TenantCollaboratorAuthorizer(context, collaborators);
  }

  public static Authorizer<AJEntityClass> buildClassContentAuthorizer(ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }

  public static Authorizer<AJEntityCollection> buildTenantReadCollectionAuthorizer(
      ProcessorContext context) {
    return new TenantReadCollectionAuthorizer(context);
  }

  public static Authorizer<AJEntityCourse> buildTenantReadCourseAuthorizer(
      ProcessorContext context) {
    return new TenantReadCourseAuthorizer(context);
  }

  public static Authorizer<AJEntityContent> buildTenantReadContentAuthorizer(
      ProcessorContext context) {
    return new TenantReadContentAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildListClassContentUsersAuthorizer(
      ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildAddClassContentUsersAuthorizer(
      ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildClassMemberDeactivateAuthorizer(
      ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }

  public static Authorizer<AJEntityClass> buildClassMemberActivateAuthorizer(
      ProcessorContext context) {
    return new ClassOwnerOrCollaboratorAuthorizer(context);
  }


}
