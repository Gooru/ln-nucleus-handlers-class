package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.fetchclassmembers;

import java.util.Objects;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;

/**
 * @author ashish.
 */

class MembershipInfo {

  private final boolean isActive;
  private final boolean profileBaselineDone;
  private final long createdAt;

  private MembershipInfo(boolean isActive, boolean profileBaselineDone, long createdAt) {
    this.isActive = isActive;
    this.profileBaselineDone = profileBaselineDone;
    this.createdAt = createdAt;
  }

  boolean isActive() {
    return isActive;
  }

  boolean isProfileBaselineDone() {
    return profileBaselineDone;
  }

  public long getCreatedAt() {
    return createdAt;
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
        profileBaselineDone == that.profileBaselineDone &&
        createdAt == that.createdAt;
  }

  @Override
  public int hashCode() {
    return Objects.hash(isActive, profileBaselineDone);
  }

  static MembershipInfo build(AJClassMember member) {
    return new MembershipInfo(member.getIsActive(), member.getProfileBaselineDone(),
        member.getCreatedAtAsLong());
  }
}
