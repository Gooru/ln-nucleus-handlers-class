
package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.preference;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityTaxonomySubject;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author szgooru Created On 03-Jan-2019
 */
public class RequestDataDBValidator {

  private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  private final String subject;
  private final String framework;

  public RequestDataDBValidator(ClassPreferenceCommand command) {
    this.subject = command.getSubject();
    this.framework = command.getFramework();
  }

  void validate() {
    if (this.framework != null && !this.framework.trim().isEmpty()) {
      validateSubjectAndFW();
    } else {
      validateSubject();
    }
  }

  private void validateSubjectAndFW() {
    Long count = AJEntityTaxonomySubject.count(AJEntityTaxonomySubject.FETCH_SUBJECT_BY_ID_FW,
        subject, framework);
    if (count == null || count < 1) {
      throw new MessageResponseWrapperException(MessageResponseFactory.createInvalidRequestResponse(
          RESOURCE_BUNDLE.getString("invalid.class.preference.subject.fw")));
    }
  }

  private void validateSubject() {
    Long count =
        AJEntityTaxonomySubject.count(AJEntityTaxonomySubject.FETCH_SUBJECT_BY_ID, subject);
    if (count == null || count < 1) {
      throw new MessageResponseWrapperException(MessageResponseFactory.createInvalidRequestResponse(
          RESOURCE_BUNDLE.getString("invalid.class.preference.subject")));
    }
  }
}
