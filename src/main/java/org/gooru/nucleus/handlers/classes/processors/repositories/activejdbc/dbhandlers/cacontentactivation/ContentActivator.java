package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.cacontentactivation;

import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;

public interface ContentActivator {

  void validate();

  void activateContent();

  static ContentActivator buildForActivationFlow(AJEntityClass entityClass,
      AJEntityClassContents classContents, FlowDeterminer flowDeterminer,
      ProcessorContext context) {
    return new ActivationFlowContentActivator(entityClass, classContents, flowDeterminer,
        context);
  }

  static ContentActivator buildForScheduleOnDayFlow(AJEntityClass entityClass,
      AJEntityClassContents classContents, FlowDeterminer flowDeterminer,
      ProcessorContext context) {
    return new ScheduleForDayContentActivator(entityClass, classContents, flowDeterminer,
        context);
  }

}
