package org.gooru.nucleus.handlers.classes.processors.commands;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;

/**
 * @author ashish.
 */

class ClassContentUsersAddProcessor extends AbstractCommandProcessor {

  ClassContentUsersAddProcessor(ProcessorContext context) {
    super(context);
  }


  @Override
  protected void setDeprecatedVersions() {

  }

  @Override
  protected MessageResponse processCommand() {
    return RepoBuilder.buildClassRepo(context).addClassContentUsers();
  }

}
