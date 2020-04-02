package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitycontentmeetingsetup;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;

public interface ContentMeetingSetupInClassService {

  void setupMeeting();

  static ContentMeetingSetupInClassService build(AJEntityClass entityClass,
      AJEntityClassContents classContents, ProcessorContext context) {
    return new ContentMeetingSetupInClassServiceImpl(entityClass, classContents, context);
  }

}
