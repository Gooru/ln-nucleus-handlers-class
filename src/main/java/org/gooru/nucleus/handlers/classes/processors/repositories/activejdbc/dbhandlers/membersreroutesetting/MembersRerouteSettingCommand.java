package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting;

import io.vertx.core.json.JsonArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContextHelper;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author ashish.
 */

class MembersRerouteSettingCommand {

  private final UUID classId;
  private final Long gradeLowerBound;
  private final Long gradeUpperBound;
  private final List<String> users;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  static MembersRerouteSettingCommand build(ProcessorContext context) {
    UUID classId = UUID.fromString(context.classId());
    Long gradeLowerBound = context.request()
        .getLong(MembersRerouteSettingRequestAttributes.GRADE_LOWER_BOUND);
    Long gradeUpperBound = context.request()
        .getLong(MembersRerouteSettingRequestAttributes.GRADE_UPPER_BOUND);

    List<String> users = initializeUsers(context);
    validate(classId, gradeLowerBound, gradeUpperBound, users);
    return new MembersRerouteSettingCommand(classId, gradeLowerBound, gradeUpperBound, users);
  }

  private static List<String> initializeUsers(ProcessorContext context) {
    JsonArray users = context.request().getJsonArray(MembersRerouteSettingRequestAttributes.USERS);
    if (users == null || users.isEmpty()) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")));
    }
    int size = users.size();
    Set<String> uniqueUsers = new HashSet<>(size);
    for (int i = 0; i < size; i++) {
      String user = users.getString(i);
      if (!ProcessorContextHelper.validateUuid(user)) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.user")));
      }
      uniqueUsers.add(user);
    }
    return Collections.unmodifiableList(new ArrayList<>(uniqueUsers));
  }

  private static void validate(UUID classId, Long gradeLowerBound, Long gradeUpperBound,
      List<String> users) {
    new RouteSettingCommandSanityValidator(classId, gradeLowerBound, gradeUpperBound, users).validate();

  }

  MembersRerouteSettingCommand(UUID classId, Long gradeLowerBound, Long gradeUpperBound,
      List<String> users) {
    this.classId = classId;
    this.gradeLowerBound = gradeLowerBound;
    this.gradeUpperBound = gradeUpperBound;
    this.users = users;
  }

  public UUID getClassId() {
    return classId;
  }

  public Long getGradeLowerBound() {
    return gradeLowerBound;
  }

  public Long getGradeUpperBound() {
    return gradeUpperBound;
  }

  public List<String> getUsers() {
    return users;
  }

  static class MembersRerouteSettingRequestAttributes {

    static String GRADE_LOWER_BOUND = "grade_lower_bound";
    static String GRADE_UPPER_BOUND = "grade_upper_bound";
    static String USERS = "users";

    private MembersRerouteSettingRequestAttributes() {
      throw new AssertionError();
    }
  }
}
