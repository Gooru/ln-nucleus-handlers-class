
package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting.postprocessor;

import java.util.List;

public class RerouteSettingPostProcessorForClassCommand {
  private String classId;
  private Long gradeLowerBound;
  private Long gradeUpperBound;
  private List<String> users;

  public Long getGradeLowerBound() {
    return gradeLowerBound;
  }

  public Long getGradeUpperBound() {
    return gradeUpperBound;
  }

  public List<String> getUsers() {
    return users;
  }

  public String getClassId() {
    return classId;
  }

  public static RerouteSettingPostProcessorForClassCommand build(Long gradeLowerBound,
      Long gradeUpperBound, String classId, List<String> users) {
    RerouteSettingPostProcessorForClassCommand command =
        new RerouteSettingPostProcessorForClassCommand();
    command.gradeLowerBound = gradeLowerBound;
    command.gradeUpperBound = gradeUpperBound;
    command.users = users;
    command.classId = classId;

    return command;
  }
}
