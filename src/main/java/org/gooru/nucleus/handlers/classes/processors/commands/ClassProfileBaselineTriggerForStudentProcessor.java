package org.gooru.nucleus.handlers.classes.processors.commands;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContextHelper;
import org.gooru.nucleus.handlers.classes.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author szgooru Created On 04-Feb-2019
 */
public class ClassProfileBaselineTriggerForStudentProcessor extends AbstractCommandProcessor {

  protected ClassProfileBaselineTriggerForStudentProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    // NOOP
  }

  @Override
  protected MessageResponse processCommand() {
    if (!ProcessorContextHelper.validateContext(context)) {
      return MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class"));
    }
    return RepoBuilder.buildClassRepo(context).updateProfileBaselineForStudent();
  }

}
