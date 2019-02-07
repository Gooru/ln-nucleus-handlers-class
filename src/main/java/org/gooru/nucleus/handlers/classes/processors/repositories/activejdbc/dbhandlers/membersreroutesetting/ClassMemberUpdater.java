package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting;

import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting.postprocessor.RerouteSettingPostProcessor;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting.postprocessor.RerouteSettingPostProcessorCommand;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

class ClassMemberUpdater {

  private final static Logger LOGGER = LoggerFactory.getLogger(ClassMemberUpdater.class);
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

    // send for post processing to baseline, route0 and rescope based on the what grades has been
    // updated
    RerouteSettingPostProcessorCommand postProcessorCommand =
        RerouteSettingPostProcessorCommand.build(this.command.getGradeLowerBound(),
            this.command.getGradeUpperBound(), command.getClassId().toString(), command.getUsers());
    ExecutionResult<MessageResponse> result =
        new RerouteSettingPostProcessor(postProcessorCommand).process();
    if (!result.isSuccessful()) {
      LOGGER.warn("post processing has failed due to: {}", result.result().reply());
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
