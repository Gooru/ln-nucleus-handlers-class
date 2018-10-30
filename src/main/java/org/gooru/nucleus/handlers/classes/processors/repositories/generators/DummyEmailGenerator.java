package org.gooru.nucleus.handlers.classes.processors.repositories.generators;

import org.gooru.nucleus.handlers.classes.app.components.AppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 2/6/16.
 */
final class DummyEmailGenerator implements Generator<String> {

  private final String userId;
  private static final Logger LOGGER = LoggerFactory.getLogger(DummyEmailGenerator.class);

  public DummyEmailGenerator(String userId) {
    this.userId = userId;
  }

  @Override
  public String generate() {
    String email = null;
    boolean needGeneration = AppConfiguration.getInstance().getPopulateDummyEmail();
    if (needGeneration) {
      String domain = AppConfiguration.getInstance().getDummyEmailDomain();
      if (domain != null) {
        email = userId.concat("@").concat(domain);
      } else {
        LOGGER.error("Dummay email generation is true but dummy email domain is not set");
      }
    }
    // If generation is not needed, need to return null which email is already initialized with
    return email;
  }
}
