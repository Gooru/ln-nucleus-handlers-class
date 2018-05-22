package org.gooru.nucleus.handlers.classes.processors.commands;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContextHelper;
import org.gooru.nucleus.handlers.classes.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;


class ClassUpdateRoute0SettingProcessor extends AbstractCommandProcessor {
    public ClassUpdateRoute0SettingProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {

    }

    @Override
    protected MessageResponse processCommand() {
        if (!ProcessorContextHelper.validateContext(context)) {
            return MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.class"));
        }
        return RepoBuilder.buildClassRepo(context).updateRoute0ClassSetting();
    }
}
