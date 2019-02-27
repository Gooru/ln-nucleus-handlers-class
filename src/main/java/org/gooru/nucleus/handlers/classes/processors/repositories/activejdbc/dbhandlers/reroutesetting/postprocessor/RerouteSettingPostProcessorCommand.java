
package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting.postprocessor;

import java.util.List;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting.UserSettingCommand;

/**
 * @author szgooru Created On 07-Feb-2019
 */
public class RerouteSettingPostProcessorCommand {
  private String classId;
  private List<UserSettingCommand> users;

  public List<UserSettingCommand> getUsers() {
    return users;
  }

  public String getClassId() {
    return classId;
  }

  public static RerouteSettingPostProcessorCommand build(String classId,
      List<UserSettingCommand> users) {
    RerouteSettingPostProcessorCommand command = new RerouteSettingPostProcessorCommand();
    command.users = users;
    command.classId = classId;

    return command;
  }
}
