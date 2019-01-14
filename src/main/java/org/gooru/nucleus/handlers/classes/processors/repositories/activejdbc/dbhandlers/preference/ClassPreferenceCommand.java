
package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.preference;

import java.io.IOException;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On 03-Jan-2019
 */
public class ClassPreferenceCommand {

  private final static Logger LOGGER = LoggerFactory.getLogger(ClassPreferenceCommand.class);
  private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  private String subject;
  private String framework;

  static ClassPreferenceCommand build(ProcessorContext context) {
    JsonObject preference = context.request().getJsonObject("preference");

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      ClassPreferenceCommand command =
          objectMapper.readValue(preference.toString(), ClassPreferenceCommand.class);
      validate(command.subject, command.framework);
      return command;
    } catch (IOException e) {
      LOGGER.error("Invalid format of the request", e);
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")));
    }
  }

  private static void validate(String subject, String framework) {
    new ClassPreferenceCommandSanityValidator(subject, framework).validate();
  }

  public JsonObject toJson() {
    return new JsonObject().put(ClassPreferenceAttributes.SUBJECT, subject)
        .put(ClassPreferenceAttributes.FRAMEWORK, framework);
  }
  
  public String getSubject() {
    return subject;
  }

  public String getFramework() {
    return framework;
  }

  static class ClassPreferenceAttributes {
    private ClassPreferenceAttributes() {
      throw new AssertionError();
    }

    static final String SUBJECT = "subject";
    static final String FRAMEWORK = "framework";
  }

}
