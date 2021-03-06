package org.gooru.nucleus.handlers.classes.processors.commands;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContextHelper;
import org.gooru.nucleus.handlers.classes.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author ashish on 2/1/17.
 */
class ClassCourseAssociationProcessor extends AbstractCommandProcessor {

  public ClassCourseAssociationProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {

  }

  @Override
  protected MessageResponse processCommand() {
    if (!ProcessorContextHelper.validateContextWithCourse(context)) {
      return MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class.or.course"));
    }
    return RepoBuilder.buildClassRepo(context).associateCourseWithClass();
  }
}
