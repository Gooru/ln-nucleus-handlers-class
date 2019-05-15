package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;

/**
 * @author ashish.
 */

class MembersRerouteSettingCommand {

  private final UUID classId;
  private final List<UserSettingCommand> userSetting;
  private boolean classUpperBoundUpdateNeeded;
  private Long classUpperBound;

  static MembersRerouteSettingCommand build(ProcessorContext context,
      Map<String, AJClassMember> existingGrades) {
    UUID classId = UUID.fromString(context.classId());

    List<UserSettingCommand> users = initializeUsers(context, existingGrades);
    return new MembersRerouteSettingCommand(classId, users);
  }

  private static List<UserSettingCommand> initializeUsers(ProcessorContext context,
      Map<String, AJClassMember> existingGrades) {
    JsonArray users = context.request().getJsonArray(MembersRerouteSettingRequestAttributes.USERS);

    int size = users.size();
    List<UserSettingCommand> userSettings = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      JsonObject userJson = users.getJsonObject(i);
      String userId = userJson.getString(MembersRerouteSettingRequestAttributes.USER_ID);

      Long gradeLowerBound =
          userJson.getLong(MembersRerouteSettingRequestAttributes.GRADE_LOWER_BOUND);
      Long gradeUpperBound =
          userJson.getLong(MembersRerouteSettingRequestAttributes.GRADE_UPPER_BOUND);

      // Compare the incoming grades with the existing grades of the user and consider the members
      // for processing for which the grades are actually updated
      AJClassMember member = existingGrades.get(userId);
      Boolean lowerBoundChanged = false;
      Boolean upperBoundChanged = false;
      if (gradeLowerBound != null && gradeLowerBound != member.getGradeLowerBound()) {
        lowerBoundChanged = true;
      }

      if (gradeUpperBound != null && gradeUpperBound != member.getGradeUpperBound()) {
        upperBoundChanged = true;
      }

      if (lowerBoundChanged || upperBoundChanged) {
        userSettings.add(new UserSettingCommand(UUID.fromString(userId), gradeLowerBound,
            gradeUpperBound, lowerBoundChanged, upperBoundChanged));
      }
    }
    return Collections.unmodifiableList(userSettings);
  }

  MembersRerouteSettingCommand(UUID classId, List<UserSettingCommand> users) {
    this.classId = classId;
    this.userSetting = users;
  }

  public UUID getClassId() {
    return classId;
  }

  public List<UserSettingCommand> getUserSettings() {
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
}
