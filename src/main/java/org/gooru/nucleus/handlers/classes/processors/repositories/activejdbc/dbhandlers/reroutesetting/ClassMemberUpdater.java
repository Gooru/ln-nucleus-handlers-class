package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting;

import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;

/**
 * @author ashish.
 */

class ClassMemberUpdater {

  private final boolean lowerBoundUpdated;
  private final boolean gradeWasSet;
  private RerouteSettingCommand command;

  ClassMemberUpdater(RerouteSettingCommand command, boolean lowerBoundUpdated,
      boolean gradeWasSet) {
    this.command = command;
    this.lowerBoundUpdated = lowerBoundUpdated;
    this.gradeWasSet = gradeWasSet;
  }

  void update() {
    if (lowerBoundUpdated && gradeWasSet) {
      updateClassMembersLowerBoundAndUpperBound();
    } else if (lowerBoundUpdated) {
      updateClassMembersLowerBound();
    } else {
      updateClassMemberUpperBound();
    }
  }

  private void updateClassMemberUpperBound() {
    AJClassMember.updateClassMemberUpperBoundAsDefault(command.getClassId().toString(),
        command.getGradeCurrent());
  }

  private void updateClassMembersLowerBound() {
    AJClassMember.updateClassMemberLowerBoundAsDefault(command.getClassId().toString(),
        command.getGradeLowerBound());
  }

  private void updateClassMembersLowerBoundAndUpperBound() {
    updateClassMembersLowerBound();
    updateClassMemberUpperBound();
  }
}
