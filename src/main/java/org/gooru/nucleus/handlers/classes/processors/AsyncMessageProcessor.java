package org.gooru.nucleus.handlers.classes.processors;

import io.vertx.core.Future;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;

/**
 * @author ashish.
 */

public interface AsyncMessageProcessor {

  Future<MessageResponse> process();

}
