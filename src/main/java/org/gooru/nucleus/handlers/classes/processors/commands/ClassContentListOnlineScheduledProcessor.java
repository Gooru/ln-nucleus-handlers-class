package org.gooru.nucleus.handlers.classes.processors.commands;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;


class ClassContentListOnlineScheduledProcessor extends AbstractCommandProcessor {

  public ClassContentListOnlineScheduledProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {

  }

  @Override
  protected MessageResponse processCommand() {
    return RepoBuilder.buildClassRepo(context).listClassContentListOnlineScheduledProcessor();
  }
}
