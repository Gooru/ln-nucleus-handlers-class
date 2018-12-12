package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting;

import java.util.UUID;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;

/**
 * @author ashish.
 */

class RerouteSettingCommand {

  private final UUID classId;
  private final Boolean route0;
  private final Long gradeLowerBound;
  private final Long gradeUpperBound;
  private final Long gradeCurrent;

  static RerouteSettingCommand build(ProcessorContext context) {
    UUID classId = UUID.fromString(context.classId());
    Boolean route0 = context.request().getBoolean(RerouteSettingRequestAttributes.ROUTE0);
    Long gradeLowerBound = context.request()
        .getLong(RerouteSettingRequestAttributes.GRADE_LOWER_BOUND);
    Long gradeUpperBound = context.request()
        .getLong(RerouteSettingRequestAttributes.GRADE_UPPER_BOUND);
    Long gradeCurrent = context.request().getLong(RerouteSettingRequestAttributes.GRADE_CURRENT);
    validate(classId, route0, gradeLowerBound, gradeUpperBound, gradeCurrent);
    return new RerouteSettingCommand(classId, route0, gradeLowerBound, gradeUpperBound,
        gradeCurrent);
  }

  private static void validate(UUID classId, Boolean route0, Long gradeLowerBound,
      Long gradeUpperBound, Long gradeCurrent) {
    new RouteSettingCommandSanityValidator(classId, route0, gradeLowerBound, gradeUpperBound,
        gradeCurrent).validate();

  }

  RerouteSettingCommand(UUID classId, Boolean route0, Long gradeLowerBound, Long gradeUpperBound,
      Long gradeCurrent) {
    this.classId = classId;
    this.route0 = route0;
    this.gradeLowerBound = gradeLowerBound;
    this.gradeUpperBound = gradeUpperBound;
    this.gradeCurrent = gradeCurrent;
  }

  public UUID getClassId() {
    return classId;
  }

  public Boolean getRoute0() {
    return route0;
  }

  public Long getGradeLowerBound() {
    return gradeLowerBound;
  }

  public Long getGradeUpperBound() {
    return gradeUpperBound;
  }

  public Long getGradeCurrent() {
    return gradeCurrent;
  }

  public int validGradesCount() {
    int result = 0;
    if (gradeCurrent != null) {
      result++;
    }
    if (gradeLowerBound != null) {
      result++;
    }
    if (gradeUpperBound != null) {
      result++;
    }
    return result;
  }

  static class RerouteSettingRequestAttributes {

    static final String GRADE_LOWER_BOUND = "grade_lower_bound";
    static final String GRADE_UPPER_BOUND = "grade_upper_bound";
    static final String GRADE_CURRENT = "grade_current";
    static final String ROUTE0 = "route0";

    private RerouteSettingRequestAttributes() {
      throw new AssertionError();
    }
  }
}
