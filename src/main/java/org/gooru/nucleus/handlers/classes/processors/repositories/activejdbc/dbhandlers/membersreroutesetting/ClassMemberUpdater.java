package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting;

import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;

/**
 * @author ashish.
 */

class ClassMemberUpdater {

  private MembersRerouteSettingCommand command;

  ClassMemberUpdater(MembersRerouteSettingCommand command) {
    this.command = command;
  }

  void update() {
    if (command.getGradeLowerBound() != null && command.getGradeUpperBound() != null) {
      updateClassMembersLowerBoundAndUpperBound();
    } else if (command.getGradeLowerBound() != null) {
      updateClassMembersLowerBound();
    } else {
      updateClassMemberUpperBound();
    }
  }

  private void updateClassMemberUpperBound() {
    AJClassMember.updateClassMemberUpperBoundForSpecifiedUsers(command.getClassId().toString(),
        command.getGradeUpperBound(),
        Utils.convertListToPostgresArrayStringRepresentation(command.getUsers()));
  }

  private void updateClassMembersLowerBound() {
    AJClassMember.updateClassMemberLowerBoundForSpecifiedUsers(command.getClassId().toString(),
        command.getGradeLowerBound(),
        Utils.convertListToPostgresArrayStringRepresentation(command.getUsers()));
  }

  private void updateClassMembersLowerBoundAndUpperBound() {
    updateClassMembersLowerBound();
    updateClassMemberUpperBound();
  }
}
