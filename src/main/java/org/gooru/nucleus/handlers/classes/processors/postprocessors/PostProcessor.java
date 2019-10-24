package org.gooru.nucleus.handlers.classes.processors.postprocessors;

import io.vertx.core.Future;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.classes.app.components.AppConfiguration;
import org.gooru.nucleus.handlers.classes.constants.HttpConstants;
import org.gooru.nucleus.handlers.classes.processors.AsyncMessageProcessor;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PostProcessor implements AsyncMessageProcessor {

  private JsonObject postProcessingEventData;
  private static final Logger LOGGER = LoggerFactory.getLogger(PostProcessor.class);
  private final Future<MessageResponse> result;

  PostProcessor(JsonObject postProcessingEventData) {
    this.postProcessingEventData = postProcessingEventData;
    this.result = Future.future();
  }

  @Override
  public Future<MessageResponse> process() {
    try {
      if (postProcessingEventData == null || postProcessingEventData.isEmpty()) {
        LOGGER.warn("Empty payload for post processing");
        result.complete(MessageResponseFactory.createNoContentResponse(""));
      }
      String eventName = postProcessingEventData
          .getString(PostProcessorEventAttributes.PPEVENT_KEY);
      if (PostProcessorEventAttributes.PPEVENT_OA_COMPLETE.equals(eventName)) {
        postProcessOACompleteEvent();
      }
    } catch (Throwable t) {
      LOGGER.warn("Error while post processing: ", t);
      result.fail(t);
    }
    return result;
  }

  private void postProcessOACompleteEvent() {
    JsonObject payload = postProcessingEventData
        .getJsonObject(PostProcessorEventAttributes.PPEVENT_PAYLOAD);
    relayOACompletionMessage(payload);
  }

  private void relayOACompletionMessage(JsonObject payload) {
    String uri = AppConfiguration.getInstance().getOACompletionInformEndpoint();
    HttpClientRequest req = PostProcessingHttpClient.getInstance().getHttpClient()
        .postAbs(uri, response -> response.bodyHandler(buffer -> {
          if (response.statusCode() != HttpConstants.HttpStatus.SUCCESS.getCode()) {
            LOGGER.warn("OA completion signal failed, status code: '{}'", response.statusCode());
          } else {
            LOGGER.debug("OA completion signal successful");
          }
          result.complete(MessageResponseFactory.createNoContentResponse(""));
        })).exceptionHandler(ex -> {
          LOGGER.warn("OA completion request payload : {}", payload.toString());
          LOGGER.warn("Error while communicating with remote server: ", ex);
          result.fail(ex);
        });
    req.end(payload.toString());
  }

}
