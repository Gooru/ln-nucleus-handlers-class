package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityContent;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.libs.tenant.TenantTree;
import org.gooru.nucleus.libs.tenant.TenantTreeBuilder;
import org.gooru.nucleus.libs.tenant.contents.ContentTenantAuthorization;
import org.gooru.nucleus.libs.tenant.contents.ContentTenantAuthorizationBuilder;
import org.gooru.nucleus.libs.tenant.contents.ContentTreeAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 10/1/17.
 */
class TenantReadContentAuthorizer implements Authorizer<AJEntityContent> {

  private final ProcessorContext context;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private static final Logger LOGGER = LoggerFactory
      .getLogger(TenantReadContentAuthorizer.class);

  public TenantReadContentAuthorizer(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> authorize(AJEntityContent model) {
    TenantTree userTenantTree = TenantTreeBuilder.build(context.tenant(), context.tenantRoot());
    TenantTree contentTenantTree = TenantTreeBuilder
        .build(model.getTenant(), model.getTenantRoot());

    ContentTenantAuthorization authorization = ContentTenantAuthorizationBuilder
        .build(contentTenantTree, userTenantTree, ContentTreeAttributes.build(model.isPublished()));

    if (authorization.canRead()) {
      return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }
    return new ExecutionResult<>(
        MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
        ExecutionResult.ExecutionStatus.FAILED);
  }
}
