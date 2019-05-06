package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.classactivities.activitycontentactivation;

import io.vertx.core.json.JsonObject;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClassContents;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author ashish.
 */

class FlowDeterminer {

  private final boolean scheduledForDayFlow;
  private final boolean activationFlow;
  private final String inputDateAsString;
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  FlowDeterminer(JsonObject request) {
    this.scheduledForDayFlow = request.getString(AJEntityClassContents.DCA_ADDED_DATE) != null;
    this.activationFlow = request.getString(AJEntityClassContents.ACTIVATION_DATE) != null;

    if (activationFlow == scheduledForDayFlow) {
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("activation.dca.both.dates.not.allowed")));
    }
    if (activationFlow) {
      inputDateAsString = request.getString(AJEntityClassContents.ACTIVATION_DATE);
    } else {
      inputDateAsString = request.getString(AJEntityClassContents.DCA_ADDED_DATE);
    }
  }

  public boolean isScheduledForDayFlow() {
    return scheduledForDayFlow;
  }

  public boolean isActivationFlow() {
    return activationFlow;
  }

  public String getInputDateAsString() {
    return inputDateAsString;
  }

}
