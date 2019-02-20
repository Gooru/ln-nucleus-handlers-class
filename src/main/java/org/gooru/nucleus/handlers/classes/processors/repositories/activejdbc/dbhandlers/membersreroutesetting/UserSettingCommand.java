
package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting;

import java.util.UUID;

/**
 * @author szgooru Created On 19-Feb-2019
 */
public class UserSettingCommand {
  private final UUID userId;
  private final Long gradeLowerBound;
  private final Long gradeUpperBound;

  public UserSettingCommand(UUID userId, Long gradeLowerBound, Long gradeUpperBound) {
    this.userId = userId;
    this.gradeLowerBound = gradeLowerBound;
    this.gradeUpperBound = gradeUpperBound;
  }

  public UUID getUserId() {
    return userId;
  }

  public Long getGradeLowerBound() {
    return gradeLowerBound;
  }

  public Long getGradeUpperBound() {
    return gradeUpperBound;
  }

}
