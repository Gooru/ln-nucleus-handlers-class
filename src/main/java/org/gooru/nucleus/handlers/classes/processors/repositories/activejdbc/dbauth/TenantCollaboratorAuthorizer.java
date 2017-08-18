package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbauth;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.libs.tenant.TenantTree;
import org.gooru.nucleus.libs.tenant.TenantTreeBuilder;
import org.gooru.nucleus.libs.tenant.classes.ClassTenantAuthorization;
import org.gooru.nucleus.libs.tenant.classes.ClassTenantAuthorizationBuilder;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;

/**
 * @author ashish on 20/1/17.
 */
class TenantCollaboratorAuthorizer implements Authorizer<AJEntityClass> {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private final JsonArray collaborators;
    private static final Logger LOGGER = LoggerFactory.getLogger(TenantCollaboratorAuthorizer.class);

    public TenantCollaboratorAuthorizer(ProcessorContext context, JsonArray collaborators) {
        this.context = context;
        this.collaborators = collaborators;
    }

    @Override
    public ExecutionResult<MessageResponse> authorize(AJEntityClass model) {
        if (collaborators == null || collaborators.isEmpty()) {
            return sendSuccess();
        }
        TenantTree classTenantTree = TenantTreeBuilder.build(model.getTenant(), model.getTenantRoot());
        List<TenantTree> collaboratorTenantTrees = getUserTenantTreesForCollaborators();
        if (collaboratorTenantTrees.size() != collaborators.size()) {
            LOGGER.warn("Not all collaborators are present in DB");
            return sendError();
        }
        return authorizeCollaborators(classTenantTree, collaboratorTenantTrees);
    }

    private ExecutionResult<MessageResponse> authorizeCollaborators(TenantTree classTenantTree,
        List<TenantTree> collaboratorTenantTrees) {
        for (TenantTree collaboratorTree : collaboratorTenantTrees) {
            ClassTenantAuthorization authorization =
                ClassTenantAuthorizationBuilder.build(classTenantTree, collaboratorTree);
            if (authorization.canCollaborate()) {
                continue;
            } else {
                return sendError();
            }
        }
        return sendSuccess();
    }

    private List<TenantTree> getUserTenantTreesForCollaborators() {
        LazyList<AJEntityUser> collaboratorsListFromDB = AJEntityUser.getCollaboratorsTenantInfo(this.collaborators);
        List<TenantTree> result = new LinkedList<>();
        for (AJEntityUser collaborator : collaboratorsListFromDB) {
            TenantTree tenantTree = TenantTreeBuilder.build(collaborator.getTenant(), collaborator.getTenantRoot());
            result.add(tenantTree);
        }
        return result;
    }

    private ExecutionResult<MessageResponse> sendError() {
        return new ExecutionResult<>(
            MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
            ExecutionResult.ExecutionStatus.FAILED);
    }

    private ExecutionResult<MessageResponse> sendSuccess() {
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }
}
