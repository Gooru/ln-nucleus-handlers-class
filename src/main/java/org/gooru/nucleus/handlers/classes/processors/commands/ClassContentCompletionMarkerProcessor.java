package org.gooru.nucleus.handlers.classes.processors.commands;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;

/**
 * @author ashish.
 */

class ClassContentCompletionMarkerProcessor extends AbstractCommandProcessor {

  ClassContentCompletionMarkerProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  public MessageResponse processCommand() {
    return RepoBuilder.buildClassRepo(context).classContentComplete();

  }

  @Override
  protected void setDeprecatedVersions() {

  }
}
