
package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.preference;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author szgooru Created On 03-Jan-2019
 */
public class ClassPreferenceCommandSanityValidator {
  private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  private final String subject;
  private final String framework;

  public ClassPreferenceCommandSanityValidator(String subject, String framework) {
    this.subject = subject;
    this.framework = framework;
  }

  void validate() {
    validateSubject();
  }


  private void validateSubject() {
    if (subject == null || subject.trim().isEmpty()) {
      throw new MessageResponseWrapperException(MessageResponseFactory.createInvalidRequestResponse(
          RESOURCE_BUNDLE.getString("invalid.class.preference.subject.fw")));
    }
  }
}
