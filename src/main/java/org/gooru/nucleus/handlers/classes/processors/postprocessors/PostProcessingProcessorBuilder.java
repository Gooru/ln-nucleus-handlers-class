package org.gooru.nucleus.handlers.classes.processors.postprocessors;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.classes.processors.AsyncMessageProcessor;

public class PostProcessingProcessorBuilder {

  public static AsyncMessageProcessor build(JsonObject postProcessingEventData) {
    return new PostProcessor(postProcessingEventData);
  }

}
