package org.gooru.nucleus.handlers.classes.processors.events;

import io.vertx.core.json.JsonArray;
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
  private static final String EVT_CLASS_STUDENT_INVITE_REMOVAL = "event.class.student.invite.remove";
  private static final String EVT_CLASS_STUDENT_REMOVAL = "event.class.student.remove";
  private static final String EVT_CLASS_COLLABORATOR_UPDATE = "event.class.collaborator.update";
  private static final String EVT_CLASS_COURSE_ASSIGNED = "event.class.course.assigned";
  private static final String EVT_CLASS_CONTENT_VISIBLE = "event.class.content.visible";
  private static final String EVT_CLASS_CONTENT_CREATE = "event.class.content.create";
  private static final String EVT_CLASS_CONTENT_ENABLE = "event.class.content.enable";
  private static final String EVT_CLASS_ARCHIVE = "event.class.archive";
  private static final String EVT_CLASS_CONTENT_DELETE = "event.class.content.delete";
  private static final String EVENT_NAME = "event.name";
  private static final String EVENT_BODY = "event.body";
  private static final String CLASS_ID = "id";
  private static final String STUDENT_ID = "studentId";
  private static final String COURSE_ID = "courseId";
  private static final String INVITEES = "invitees";
  private static final String EMAIL = "email";
  private static final String CONTENT_ID = "content_id";
  private static final String CONTENT_TYPE = "content_type";
  private static final String CLASS_CONTENT_ID = "id";
  private static final String CTX_COURSE_ID = "ctx_course_id";
  private static final String CTX_UNIT_ID = "ctx_unit_id";
  private static final String CTX_LESSON_ID = "ctx_lesson_id";
  private static final String CTX_COLLECTION_ID = "ctx_collection_id";
  private static final String ID_CLASS = "class_id";

  private EventBuilderFactory() {
    throw new AssertionError();
  }

  public static EventBuilder getDeleteClassEventBuilder(String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_DELETE).put(EVENT_BODY,
        new JsonObject().put(CLASS_ID, classId));
  }

  public static EventBuilder getCreateClassEventBuilder(String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_CREATE).put(EVENT_BODY,
        new JsonObject().put(CLASS_ID, classId));
  }

  public static EventBuilder getUpdateClassEventBuilder(String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_UPDATE).put(EVENT_BODY,
        new JsonObject().put(CLASS_ID, classId));
  }

  public static EventBuilder getStudentInvitedEventBuilder(String classId, JsonArray invitees) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_STUDENT_INVITE).put(EVENT_BODY,
        new JsonObject().put(CLASS_ID, classId).put(INVITEES, invitees));
  }

  public static EventBuilder getStudentJoinedEventBuilder(String classId, String studentId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_STUDENT_JOIN).put(EVENT_BODY,
        new JsonObject().put(CLASS_ID, classId).put(STUDENT_ID, studentId));
  }

  public static EventBuilder getCollaboratorUpdatedEventBuilder(String classId,
      JsonObject collaborators) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_COLLABORATOR_UPDATE).put(EVENT_BODY,
        collaborators.put(CLASS_ID, classId));
  }

  public static EventBuilder getCourseAssignedEventBuilder(String classId, String courseId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_COURSE_ASSIGNED).put(EVENT_BODY,
        new JsonObject().put(CLASS_ID, classId).put(COURSE_ID, courseId));
  }

  public static EventBuilder getInviteRemovalEventBuilder(String classId, String email) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_STUDENT_INVITE_REMOVAL).put(EVENT_BODY,
        new JsonObject().put(CLASS_ID, classId).put(EMAIL, email));
  }

  public static EventBuilder getStudentRemovalEventBuilder(String classId, String studentId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_STUDENT_REMOVAL).put(EVENT_BODY,
        new JsonObject().put(CLASS_ID, classId).put(STUDENT_ID, studentId));
  }

  // TODO: Decide on how to pass the content's structure
  public static EventBuilder getContentVisibleEventBuilder(String classId,
      JsonObject visibleContents) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_CONTENT_VISIBLE).put(EVENT_BODY,
        visibleContents.put(CLASS_ID, classId));
  }

  public static EventBuilder getCreateClassContentEventBuilder(Object id, String classId,
      String contentId,
      String contentType) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_CONTENT_CREATE).put(EVENT_BODY,
        new JsonObject().put(CLASS_CONTENT_ID, id).put(ID_CLASS, classId).put(CONTENT_ID, contentId)
            .put(CONTENT_TYPE, contentType));
  }

  public static EventBuilder getClassContentEnableEventBuilder(Object id, String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_CONTENT_ENABLE).put(EVENT_BODY,
        new JsonObject().put(CLASS_CONTENT_ID, id).put(ID_CLASS, classId));
  }

  public static EventBuilder getArchiveClassEventBuilder(String classId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_ARCHIVE).put(EVENT_BODY,
        new JsonObject().put(CLASS_ID, classId));
  }

  public static EventBuilder getDeleteClassContentEventBuilder(Object id, String classId,
      String contentId, String contentType) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_CLASS_CONTENT_DELETE)
        .put(EVENT_BODY, new JsonObject().put(CLASS_CONTENT_ID, id).put(ID_CLASS, classId)
            .put(CONTENT_ID, contentId).put(CONTENT_TYPE, contentType));
  }

}
