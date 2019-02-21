package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting;

import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;

/**
 * @author ashish.
 */

class ClassMemberUpdater {

  private final boolean gradeWasSet;
  private RerouteSettingCommand command;

  ClassMemberUpdater(RerouteSettingCommand command, boolean gradeWasSet) {
    this.command = command;
    this.gradeWasSet = gradeWasSet;
  }

  void update() {
    if (gradeWasSet) {
      // Only set the high grades of the students as we do not want to infer the student origin from
      // class origin.
      updateClassMemberUpperBound();

      // There is no post processing after the student destination has been set because student
      // origin will not be setup
    }
  }

  private void updateClassMemberUpperBound() {
    AJClassMember.updateClassMemberUpperBoundAsDefault(command.getClassId().toString(),
        command.getGradeCurrent());
  }

}
