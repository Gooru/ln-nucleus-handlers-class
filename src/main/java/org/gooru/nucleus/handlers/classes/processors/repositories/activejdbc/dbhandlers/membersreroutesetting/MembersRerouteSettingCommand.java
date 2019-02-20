package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContextHelper;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author ashish.
 */

class MembersRerouteSettingCommand {

  private final UUID classId;
  private final List<UserSettingCommand> userSetting;
  private boolean classUpperBoundUpdateNeeded;
  private Long classUpperBound;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  static MembersRerouteSettingCommand build(ProcessorContext context) {
    UUID classId = UUID.fromString(context.classId());

    List<UserSettingCommand> users = initializeUsers(context);
    validate(classId, users);
    return new MembersRerouteSettingCommand(classId, users);
  }

  private static List<UserSettingCommand> initializeUsers(ProcessorContext context) {
    JsonArray users = context.request().getJsonArray(MembersRerouteSettingRequestAttributes.USERS);
    if (users == null || users.isEmpty()) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")));
    }
    int size = users.size();
    List<UserSettingCommand> userSettings = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      JsonObject userJson = users.getJsonObject(i);
      String user = userJson.getString(MembersRerouteSettingRequestAttributes.USER_ID);
      if (!ProcessorContextHelper.validateUuid(user)) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.user")));
      }

      Long gradeLowerBound =
          userJson.getLong(MembersRerouteSettingRequestAttributes.GRADE_LOWER_BOUND);
      Long gradeUpperBound =
          userJson.getLong(MembersRerouteSettingRequestAttributes.GRADE_UPPER_BOUND);
      userSettings
          .add(new UserSettingCommand(UUID.fromString(user), gradeLowerBound, gradeUpperBound));
    }
    return Collections.unmodifiableList(userSettings);
  }

  private static void validate(UUID classId, List<UserSettingCommand> users) {
    new RouteSettingCommandSanityValidator(classId, users).validate();

  }

  MembersRerouteSettingCommand(UUID classId, List<UserSettingCommand> users) {
    this.classId = classId;
    this.userSetting = users;
  }

  public UUID getClassId() {
    return classId;
  }

  public List<UserSettingCommand> getUsers() {
    return userSetting;
  }

  public boolean isClassUpperBoundUpdateNeeded() {
    return classUpperBoundUpdateNeeded;
  }

  public void setClassUpperBoundUpdateNeeded(boolean classUpperBoundUpdateNeeded) {
    this.classUpperBoundUpdateNeeded = classUpperBoundUpdateNeeded;
  }

  public Long getClassUpperBound() {
    return classUpperBound;
  }

  public void setClassUpperBound(Long classUpperBound) {
    this.classUpperBound = classUpperBound;
  }

  static class MembersRerouteSettingRequestAttributes {

    static final String GRADE_LOWER_BOUND = "grade_lower_bound";
    static final String GRADE_UPPER_BOUND = "grade_upper_bound";
    static final String USER_ID = "user_id";
    static final String USERS = "users";

    private MembersRerouteSettingRequestAttributes() {
      throw new AssertionError();
    }
  }
}
