package org.gooru.nucleus.handlers.classes.processors.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.Processor;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 2/1/17.
 */
public enum CommandProcessorBuilder {

  DEFAULT("default") {
    private final Logger LOGGER = LoggerFactory.getLogger(CommandProcessorBuilder.class);
    private final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

    @Override
    public Processor build(ProcessorContext context) {
      return () -> {
        LOGGER.error("Invalid operation type passed in, not able to handle");
        return MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.operation"));
      };
    }
  },
  CLASS_CREATE(MessageConstants.MSG_OP_CLASS_CREATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassCreateProcessor(context);
    }
  },
  CLASS_GET(MessageConstants.MSG_OP_CLASS_GET) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassGetProcessor(context);
    }
  },
  CLASS_UPDATE(MessageConstants.MSG_OP_CLASS_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassUpdateProcessor(context);
    }
  },
  CLASS_COLLABORATORS_UPDATE(MessageConstants.MSG_OP_CLASS_COLLABORATORS_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassCollaboratorUpdateProcessor(context);
    }
  },
  CLASS_COURSE_ASSOCIATION(MessageConstants.MSG_OP_CLASS_COURSE_ASSOCIATION) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassCourseAssociationProcessor(context);
    }
  },
  CLASS_DELETE(MessageConstants.MSG_OP_CLASS_DELETE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassDeleteProcessor(context);
    }
  },
  CLASS_INVITE(MessageConstants.MSG_OP_CLASS_INVITE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassInviteProcessor(context);
    }
  },
  CLASS_JOIN(MessageConstants.MSG_OP_CLASS_JOIN) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassJoinProcessor(context);
    }
  },
  CLASS_LIST(MessageConstants.MSG_OP_CLASS_LIST) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassListProcessor(context);
    }
  },
  CLASS_LIST_FOR_COURSE(MessageConstants.MSG_OP_CLASS_LIST_FOR_COURSE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassListForCourseProcessor(context);
    }
  },
  CLASS_MEMBERS_GET(MessageConstants.MSG_OP_CLASS_MEMBERS_GET) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassMembersGetProcessor(context);
    }
  },
  CLASS_MEMBERS_ACTIVATE(MessageConstants.MSG_OP_CLASS_MEMBERS_ACTIVATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassMembersActivateProcessor(context);
    }
  },
  CLASS_MEMBERS_DEACTIVATE(MessageConstants.MSG_OP_CLASS_MEMBERS_DEACTIVATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassMembersDeactivateProcessor(context);
    }
  },
  CLASS_SET_CONTENT_VISIBILITY(MessageConstants.MSG_OP_CLASS_SET_CONTENT_VISIBILITY) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentVisibilitySetProcessor(context);
    }
  },
  CLASS_GET_CONTENT_VISIBILITY(MessageConstants.MSG_OP_CLASS_GET_CONTENT_VISIBILITY) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentVisibilityGetProcessor(context);
    }
  },
  CLASS_REMOVE_STUDENT(MessageConstants.MSG_OP_CLASS_REMOVE_STUDENT) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassRemoveStudentProcessor(context);
    }
  },
  CLASS_INVITE_REMOVE(MessageConstants.MSG_OP_CLASS_INVITE_REMOVE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassInviteRemoveProcessor(context);
    }
  },
  CLASS_CONTENT_ADD(MessageConstants.MSG_OP_CLASS_CONTENT_ADD) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentAddProcessor(context);
    }
  },
  CLASS_CONTENT_USERS_ADD(MessageConstants.MSG_OP_CLASS_CONTENT_USERS_ADD) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentUsersAddProcessor(context);
    }
  },
  CLASS_CONTENT_LIST_UNSCHEDULED(MessageConstants.MSG_OP_CLASS_CONTENT_LIST_UNSCHEDULED) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentListUnscheduledProcessor(context);
    }
  },
  CLASS_CONTENT_LIST_OFFLINE_ACTIVE(MessageConstants.MSG_OP_CLASS_CONTENT_LIST_OFFLINE_ACTIVE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentListOfflineActiveProcessor(context);
    }
  },
  CLASS_CONTENT_LIST_OFFLINE_COMPLETED(
      MessageConstants.MSG_OP_CLASS_CONTENT_LIST_OFFLINE_COMPLETED) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentListOfflineCompletedProcessor(context);
    }
  },
  CLASS_CONTENT_LIST_ONLINE_SCHEDULED(MessageConstants.MSG_OP_CLASS_CONTENT_LIST_ONLINE_SCHEDULED) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentListOnlineScheduledProcessor(context);
    }
  },
  CLASS_CONTENT_USERS_LIST(MessageConstants.MSG_OP_CLASS_CONTENT_USERS_LIST) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentUsersListProcessor(context);
    }
  },
  CLASS_CONTENT_ENABLE(MessageConstants.MSG_OP_CLASS_CONTENT_ENABLE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentEnableProcessor(context);
    }
  },
  CLASS_CONTENT_SCHEDULE(MessageConstants.MSG_OP_CLASS_CONTENT_SCHEDULE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentScheduleProcessor(context);
    }
  },
  CLASS_ARCHIVE(MessageConstants.MSG_OP_CLASS_ARCHIVE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassArchiveProcessor(context);
    }
  },
  CLASS_CONTENT_DELETE(MessageConstants.MSG_OP_CLASS_CONTENT_DELETE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentDeleteProcessor(context);
    }
  },
  MSG_OP_CLASS_REROUTE_SETTINGS_UPDATE(MessageConstants.MSG_OP_CLASS_REROUTE_SETTINGS_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassRerouteSettingsUpdateProcessor(context);
    }
  },
  MSG_OP_CLASS_LPBASELINE_TRIGGER(MessageConstants.MSG_OP_CLASS_LPBASELINE_TRIGGER) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassProfileBaselineTriggerProcessor(context);
    }
  },
  MSG_OP_CLASS_MEMBERS_REROUTE_SETTINGS_UPDATE(
      MessageConstants.MSG_OP_CLASS_MEMBERS_REROUTE_SETTINGS_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassMembersRerouteSettingsUpdateProcessor(context);
    }
  },
  MSG_OP_CLASS_PREFERENCE_UPDATE(MessageConstants.MSG_OP_CLASS_PREFERENCE_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassPreferenceUpdateProcessor(context);
    }
  },
  MSG_OP_CLASS_LANGUAGE_UPDATE(MessageConstants.MSG_OP_CLASS_LANGUAGE_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassLanguageUpdateProcessor(context);
    }
  },
  MSG_OP_CLASS_LPBASELINE_STUDENT_TRIGGER(
      MessageConstants.MSG_OP_CLASS_LPBASELINE_STUDENT_TRIGGER) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassProfileBaselineTriggerForStudentProcessor(context);
    }
  },
  CLASS_CONTENT_MASTERY_ACCRUAL_UPDATE(
      MessageConstants.MSG_OP_CLASS_CONTENT_MASTERY_ACCRUAL_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentMasteryAccrualUpdateProcessor(context);
    }
  },
  MSG_OP_CLASS_CONTENT_COMPLETION(MessageConstants.MSG_OP_CLASS_CONTENT_COMPLETION) {
    @Override
    public Processor build(ProcessorContext context) {
      return new ClassContentCompletionMarkerProcessor(context);
    }
  };
  private String name;

  CommandProcessorBuilder(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  private static final Map<String, CommandProcessorBuilder> LOOKUP = new HashMap<>();

  static {
    for (CommandProcessorBuilder builder : values()) {
      LOOKUP.put(builder.getName(), builder);
    }
  }

  public static CommandProcessorBuilder lookupBuilder(String name) {
    CommandProcessorBuilder builder = LOOKUP.get(name);
    if (builder == null) {
      return DEFAULT;
    }
    return builder;
  }

  public abstract Processor build(ProcessorContext context);
}
