package org.gooru.nucleus.handlers.classes.processors.postprocessors;

/**
 * @author ashish.
 */

public final class PostProcessorEventAttributes {

  private PostProcessorEventAttributes() {
    throw new AssertionError();
  }

  public static final String PPEVENT_OA_COMPLETE = "ppevent.oa.complete";
  public static final String PPEVENT_KEY = "postprocessing.event.name";
  public static final String PPEVENT_PAYLOAD = "postprocessing.event.payload";
}
