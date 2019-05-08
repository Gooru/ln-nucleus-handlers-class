package org.gooru.nucleus.handlers.classes.processors.repositories;

import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;

/**
 * Created by ashish on 28/1/16.
 */
public interface ClassRepo {

  MessageResponse createClass();

  MessageResponse updateClass();

  MessageResponse fetchClass();

  MessageResponse fetchClassMembers();

  MessageResponse fetchClassesForCourse();

  MessageResponse fetchClassesForUser();

  MessageResponse joinClassByStudent();

  MessageResponse inviteStudentToClass();

  MessageResponse deleteClass();

  MessageResponse associateCourseWithClass();

  MessageResponse updateCollaboratorForClass();

  MessageResponse setContentVisibility();

  MessageResponse removeInviteForStudentFromClass();

  MessageResponse removeStudentFromClass();

  MessageResponse getVisibleContent();

  MessageResponse addContentInClass();

  MessageResponse listClassContent();

  MessageResponse enableContentInClass();

  MessageResponse archiveClass();

  MessageResponse deleteContentFromClass();

  MessageResponse updateClassRerouteSetting();

  MessageResponse updateProfileBaselineForSpecifiedStudents();
  
  MessageResponse updateProfileBaselineForStudent();

  MessageResponse updateClassMembersRerouteSetting();

  MessageResponse addClassContentUsers();

  MessageResponse listClassContentUsers();

  MessageResponse classMembersDeactivate();

  MessageResponse classMembersActivate();

  MessageResponse updateClassPreference();

  MessageResponse updateClassLanguage();
  
  MessageResponse updateClassContentMasteryAccrual();

  MessageResponse classContentComplete();
}
