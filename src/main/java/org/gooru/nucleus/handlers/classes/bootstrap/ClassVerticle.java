package org.gooru.nucleus.handlers.classes.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.classes.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.handlers.classes.bootstrap.shutdown.Finalizers;
import org.gooru.nucleus.handlers.classes.bootstrap.startup.Initializer;
import org.gooru.nucleus.handlers.classes.bootstrap.startup.Initializers;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.constants.MessagebusEndpoints;
import org.gooru.nucleus.handlers.classes.processors.ProcessorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 25/12/15.
 */
public class ClassVerticle extends AbstractVerticle {

  static final Logger LOGGER = LoggerFactory.getLogger(ClassVerticle.class);

  @Override
  public void start(Future<Void> voidFuture) throws Exception {

    vertx.executeBlocking(blockingFuture -> {
      startApplication();
    }, future -> {
      if (future.succeeded()) {
        voidFuture.complete();
      } else {
        voidFuture.fail("Not able to initialize the Class machinery properly");
      }
    });

    EventBus eb = vertx.eventBus();

    eb.consumer(MessagebusEndpoints.MBEP_CLASS, message -> {

      LOGGER.debug("Received message: " + message.body());

      vertx.executeBlocking(future -> {
        JsonObject result = new ProcessorBuilder(message).build().process();
        future.complete(result);
      }, res -> {
        JsonObject result = (JsonObject) res.result();
        DeliveryOptions options = new DeliveryOptions().addHeader(MessageConstants.MSG_OP_STATUS, result.getString(MessageConstants.MSG_OP_STATUS));
        message.reply(result.getJsonObject(MessageConstants.RESP_CONTAINER_MBUS), options);

        JsonObject eventData = result.getJsonObject(MessageConstants.RESP_CONTAINER_EVENT);
        if (eventData != null) {
          eb.publish(MessagebusEndpoints.MBEP_EVENT, eventData);
        }

      });


    }).completionHandler(result -> {
      if (result.succeeded()) {
        LOGGER.info("Class end point ready to listen");
      } else {
        LOGGER.error("Error registering the class handler. Halting the Class machinery");
        Runtime.getRuntime().halt(1);
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
    } catch(IllegalStateException ie) {
      LOGGER.error("Error initializing application", ie);
      Runtime.getRuntime().halt(1);
    }
  }

  private void shutDownApplication() {
    Finalizers finalizers = new Finalizers();
    for (Finalizer finalizer : finalizers ) {
      finalizer.finalizeComponent();
    }

  }


}