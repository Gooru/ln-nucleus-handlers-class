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
   * If the supplied fwCode is null, then default of GUT is used
   */
  void enqueue(UUID courseId, String fwCode);

  /*
   * This variation validates the presence of course and it bring premium. It also deduces the
   * framework from subject bucket.
   */
  void enqueue(UUID courseId);

  static MilestoneQueuer build() {
    return new MilestoneQueuerImpl(false);
  }

  static MilestoneQueuer build(boolean override) {
    return new MilestoneQueuerImpl(override);
  }

}
