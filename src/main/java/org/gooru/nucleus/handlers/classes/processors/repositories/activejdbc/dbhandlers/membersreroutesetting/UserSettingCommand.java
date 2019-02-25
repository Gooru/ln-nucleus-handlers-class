
package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.membersreroutesetting;

import java.util.UUID;

/**
 * @author szgooru Created On 19-Feb-2019
 */
public class UserSettingCommand {
  private final UUID userId;
  private final Long gradeLowerBound;
  private final Long gradeUpperBound;
  private final Boolean lowerBoundChanged;
  private final Boolean upperBoundChanged;

  public UserSettingCommand(UUID userId, Long gradeLowerBound, Long gradeUpperBound,
      Boolean lowerBoundChanged, Boolean upperBoundChanged) {
    this.userId = userId;
    this.lowerBoundChanged = lowerBoundChanged;
    this.gradeLowerBound = gradeLowerBound;
    this.upperBoundChanged = upperBoundChanged;
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

  public Boolean isLowerBoundChanged() {
    return lowerBoundChanged;
  }

  public Boolean isUpperBoundChanged() {
    return upperBoundChanged;
  }

}
