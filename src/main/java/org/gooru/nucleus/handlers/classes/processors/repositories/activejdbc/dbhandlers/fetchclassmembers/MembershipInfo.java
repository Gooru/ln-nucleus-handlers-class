package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.fetchclassmembers;

import java.util.Objects;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;

/**
 * @author ashish.
 */

class MembershipInfo {

  private final boolean isActive;
  private final boolean profile_baseline_done;

  private MembershipInfo(boolean isActive, boolean profile_baseline_done) {
    this.isActive = isActive;
    this.profile_baseline_done = profile_baseline_done;
  }

  boolean isActive() {
    return isActive;
  }

  boolean isProfile_baseline_done() {
    return profile_baseline_done;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MembershipInfo that = (MembershipInfo) o;
    return isActive == that.isActive &&
        profile_baseline_done == that.profile_baseline_done;
  }

  @Override
  public int hashCode() {
    return Objects.hash(isActive, profile_baseline_done);
  }

  static MembershipInfo build(AJClassMember member) {
    return new MembershipInfo(member.getIsActive(), member.getProfileBaselineDone());
  }
}
