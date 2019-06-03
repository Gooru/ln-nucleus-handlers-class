package org.gooru.nucleus.handlers.classes.processors.postprocessors;

import io.vertx.core.json.JsonObject;

/**
 * @author ashish.
 */

public final class PostProcessorEventBuilderFactory {

  private PostProcessorEventBuilderFactory() {
    throw new AssertionError();
  }

  public static JsonObject buildOACompletionEvent(OACompletionPostProcessorPayload payload) {
    return new JsonObject().put(PostProcessorEventAttributes.PPEVENT_KEY,
        PostProcessorEventAttributes.PPEVENT_OA_COMPLETE)
        .put(PostProcessorEventAttributes.PPEVENT_PAYLOAD, payload.createPayload());
  }

}
