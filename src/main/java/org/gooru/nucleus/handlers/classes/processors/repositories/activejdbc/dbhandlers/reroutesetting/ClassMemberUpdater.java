package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting;

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

  private final boolean lowerBoundUpdated;
  private final boolean gradeWasSet;
  private RerouteSettingCommand command;

  private final static Logger LOGGER = LoggerFactory.getLogger(ClassMemberUpdater.class);

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

    // send for post processing to baseline, route0 and rescope based on the what grades has been
    // updated
    RerouteSettingPostProcessorCommand postProcessorCommand = RerouteSettingPostProcessorCommand
        .build(this.command.getGradeLowerBound(), this.command.getGradeCurrent(), command.getClassId().toString(), null);
    ExecutionResult<MessageResponse> result =
        new RerouteSettingPostProcessor(postProcessorCommand).process();
    if (!result.isSuccessful()) {
      LOGGER.warn("post processing has failed due to: {}", result.result().reply());
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
