package org.gooru.nucleus.handlers.classes.processors.events;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 19/1/16.
 */
public final class EventBuilderFactory {

  private static final String EVT_CLASS_CREATE = "event.class.create";
  private static final String EVT_CLASS_UPDATE = "event.class.update";
  private static final String EVT_CLASS_DELETE = "event.class.delete";
  private static final String EVT_CLASS_STUDENT_INVITE = "event.class.student.invite";
  private static final String EVT_CLASS_STUDENT_JOIN = "event.class.student.join";
  private static final String EVT_CLASS_COLLABORATOR_UPDATE = "event.class.collaborator.join";
  private static final String EVT_CLASS_COURSE_ASSIGNED = "event.class.course.assigned";
  private static final String EVT_CLASS_CONTENT_VISIBLE = "event.class.content.visible";
  private static final String EVENT_NAME = "event.name";
  private static final String EVENT_BODY = "event.body";
  private static final String CLASS_ID = "id";

  private EventBuilderFactory() {
    throw new AssertionError();
  }

  public static EventBuilder getDeleteClassEventBuilder(String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_DELETE).put(EVENT_BODY, new JsonObject().put(CLASS_ID, classId));
  }

  public static EventBuilder getCreateClassEventBuilder(String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_CREATE).put(EVENT_BODY, new JsonObject().put(CLASS_ID, classId));
  }

  public static EventBuilder getUpdateClassEventBuilder(String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_UPDATE).put(EVENT_BODY, new JsonObject().put(CLASS_ID, classId));
  }

  // TODO: Decide on how to pass on students' ID
  public static EventBuilder getStudentInvitedEventBuilder(String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_STUDENT_INVITE).put(EVENT_BODY, new JsonObject().put(CLASS_ID, classId));
  }

  // TODO: Decided on how to pass students' ID
  public static EventBuilder getStudentJoinedEventBuilder(String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_STUDENT_JOIN).put(EVENT_BODY, new JsonObject().put(CLASS_ID, classId));
  }

  public static EventBuilder getCollaboratorUpdatedEventBuilder(String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_COLLABORATOR_UPDATE).put(EVENT_BODY, new JsonObject().put(CLASS_ID, classId));
  }

  // TODO: Decide on how to pass multiple class id
  public static EventBuilder getCourseAssignedEventBuilder(String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_COURSE_ASSIGNED).put(EVENT_BODY, new JsonObject().put(CLASS_ID, classId));
  }

  // TODO: Decide on how to pass the content's structure
  public static EventBuilder getContentVisibleEventBuilder(String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_CONTENT_VISIBLE).put(EVENT_BODY, new JsonObject().put(CLASS_ID, classId));
  }

}
