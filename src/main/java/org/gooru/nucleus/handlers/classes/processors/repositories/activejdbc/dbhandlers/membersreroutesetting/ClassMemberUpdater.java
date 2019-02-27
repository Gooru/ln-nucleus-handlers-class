package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting;

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
    this.command.getUserSettings().forEach(user -> {
      if (user.isLowerBoundChanged() && user.isUpperBoundChanged()) {
        updateClassMembersLowerBoundAndUpperBound(user);
      } else if (user.isLowerBoundChanged()) {
        updateClassMembersLowerBound(user);
      } else {
        updateClassMemberUpperBound(user);
      }  
    });
    
    // send for post processing to baseline, route0 and rescope based on the what grades has been
    // updated
    RerouteSettingPostProcessorCommand postProcessorCommand =
        RerouteSettingPostProcessorCommand.build(command.getClassId().toString(), command.getUserSettings());
    ExecutionResult<MessageResponse> result =
        new RerouteSettingPostProcessor(postProcessorCommand).process();
    if (!result.isSuccessful()) {
      LOGGER.warn("post processing has failed due to: {}", result.result().reply());
    }
  }

  private void updateClassMemberUpperBound(UserSettingCommand user) {
    AJClassMember.updateClassMemberUpperBoundForSpecifiedUsers(command.getClassId(),
        user.getGradeUpperBound(), user.getUserId());
  }

  private void updateClassMembersLowerBound(UserSettingCommand user) {
    AJClassMember.updateClassMemberLowerBoundForSpecifiedUsers(command.getClassId(),
        user.getGradeLowerBound(), user.getUserId());
  }

  private void updateClassMembersLowerBoundAndUpperBound(UserSettingCommand user) {
    updateClassMembersLowerBound(user);
    updateClassMemberUpperBound(user);
  }

}
