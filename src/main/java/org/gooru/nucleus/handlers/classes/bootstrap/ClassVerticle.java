package org.gooru.nucleus.handlers.classes.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.classes.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.handlers.classes.bootstrap.shutdown.Finalizers;
import org.gooru.nucleus.handlers.classes.bootstrap.startup.Initializer;
import org.gooru.nucleus.handlers.classes.bootstrap.startup.Initializers;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.constants.MessagebusEndpoints;
import org.gooru.nucleus.handlers.classes.processors.ProcessorBuilder;
import org.gooru.nucleus.handlers.classes.processors.postprocessors.PostProcessingProcessorBuilder;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 25/12/15.
 */
public class ClassVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClassVerticle.class);

  @Override
  public void start(Future<Void> voidFuture) {

    EventBus eb = vertx.eventBus();

    vertx.executeBlocking(blockingFuture -> {
      startApplication();
      blockingFuture.complete();
    }, startApplicationFuture -> {
      if (startApplicationFuture.succeeded()) {
        eb.consumer(MessagebusEndpoints.MBEP_CLASS, message -> {
          LOGGER.debug("Received message: '{}'", message.body());
          vertx.executeBlocking(future -> {
            MessageResponse result = ProcessorBuilder.build(message).process();
            future.complete(result);
          }, res -> {
            MessageResponse result = (MessageResponse) res.result();
            message.reply(result.reply(), result.deliveryOptions());
            JsonObject eventData = result.event();
            if (eventData != null) {
              String sessionToken =
                  ((JsonObject) message.body()).getString(MessageConstants.MSG_HEADER_TOKEN);
              if (sessionToken != null && !sessionToken.isEmpty()) {
                // Check if there are multiple events present, if so iterate and send
                if (eventData.containsKey(MessageConstants.EVENTS)) {
                  JsonArray events = eventData.getJsonArray(MessageConstants.EVENTS);
                  for (Object event : events) {
                    JsonObject eventJson = (JsonObject) event;
                    eventJson.put(MessageConstants.MSG_HEADER_TOKEN, sessionToken);
                    eb.send(MessagebusEndpoints.MBEP_EVENT, eventJson);
                  }
                } else {
                  eventData.put(MessageConstants.MSG_HEADER_TOKEN, sessionToken);
                  eb.send(MessagebusEndpoints.MBEP_EVENT, eventData);
                }
              } else {
                LOGGER.warn("Invalid session token received");
              }
            }
            JsonObject postProcessingEventData = result.postProcessingEvent();
            if (postProcessingEventData != null && !postProcessingEventData.isEmpty()) {
              vertx.executeBlocking(postProcessingFuture -> {
                Future<MessageResponse> resultFuture = PostProcessingProcessorBuilder
                    .build(postProcessingEventData).process();
                resultFuture.setHandler(postProcessingInterimResult -> {
                  postProcessingFuture.complete();
                });
              }, postProcessingResult -> {
                // Nothing to do after post processing, right now
              });
            }
          });
        }).completionHandler(result -> {
          if (result.succeeded()) {
            LOGGER.info("Class end point ready to listen");
            voidFuture.complete();
          } else {
            LOGGER.error("Error registering the class handler. Halting the Class machinery");
            voidFuture.fail(result.cause());
            Runtime.getRuntime().halt(1);
          }
        });
      } else {
        LOGGER.warn("Not able to initialize Class machinery properly.",
            startApplicationFuture.cause());
        voidFuture.fail(startApplicationFuture.cause());
      }
    });

  }

  @Override
  public void stop() throws Exception {
    shutDownApplication();
    super.stop();
  }

  private void startApplication() {
    Initializers initializers = new Initializers();
    try {
      for (Initializer initializer : initializers) {
        initializer.initializeComponent(vertx, config());
      }
    } catch (IllegalStateException ie) {
      LOGGER.error("Error initializing application", ie);
      Runtime.getRuntime().halt(1);
    }
  }

  private void shutDownApplication() {
    Finalizers finalizers = new Finalizers();
    for (Finalizer finalizer : finalizers) {
      finalizer.finalizeComponent();
    }

  }

}
