package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitycontentactivation;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;

public interface ContentActivator {

  void activateContent();

  static ContentActivator buildForActivationFlow(AJEntityClass entityClass,
      AJEntityClassContents classContents, ProcessorContext context) {
    return new ContentActivatorImpl(entityClass, classContents, context);
  }

}
