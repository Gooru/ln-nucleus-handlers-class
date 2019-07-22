package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * Created by ashish on 8/2/16.
 */
class OpenClassOrInvitedStudentAuthorizer implements Authorizer<AJEntityClass> {

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;

  OpenClassOrInvitedStudentAuthorizer(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> authorize(AJEntityClass model) {
    if (checkClassTypeOpen(model)) {
      return AuthorizerBuilder.buildTenantJoinAuthorizer(context).authorize(model);
    }
    return new ExecutionResult<>(
        MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.allowed")),
        ExecutionResult.ExecutionStatus.FAILED);

  }

  private boolean checkClassTypeOpen(AJEntityClass model) {
    return AJEntityClass.CLASS_SHARING_TYPE_OPEN
        .equalsIgnoreCase(model.getString(AJEntityClass.CLASS_SHARING));
  }

}
