package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.common.milestone;

import java.util.UUID;

/**
 * There is no validation on entity class.
 *
 * @author ashish.
 */

public interface MilestoneQueuer {

  /*
   * This variation assumes caller has done validation for course being premium/non deleted etc.
   * The fw code is deduced based on destination for the class.
   */
  void enqueue(UUID courseId, Long currentGrade);

  static MilestoneQueuer build() {
    return new MilestoneQueuerImpl(false);
  }

  static MilestoneQueuer build(boolean override) {
    return new MilestoneQueuerImpl(override);
  }

}
