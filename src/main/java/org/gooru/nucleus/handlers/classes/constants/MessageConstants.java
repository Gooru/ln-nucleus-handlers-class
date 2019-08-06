package org.gooru.nucleus.handlers.classes.constants;

public final class MessageConstants {

  public static final String MSG_HEADER_OP = "mb.operation";
  public static final String MSG_HEADER_TOKEN = "session.token";
  public static final String MSG_OP_STATUS = "mb.operation.status";
  public static final String MSG_KEY_SESSION = "session";
  public static final String MSG_OP_STATUS_SUCCESS = "success";
  public static final String MSG_OP_STATUS_ERROR = "error";
  public static final String MSG_OP_STATUS_VALIDATION_ERROR = "error.validation";
  public static final String MSG_USER_ANONYMOUS = "anonymous";
  public static final String MSG_USER_ID = "user_id";
  public static final String MSG_HTTP_STATUS = "http.status";
  public static final String MSG_HTTP_BODY = "http.body";
  public static final String MSG_HTTP_RESPONSE = "http.response";
  public static final String MSG_HTTP_ERROR = "http.error";
  public static final String MSG_HTTP_VALIDATION_ERROR = "http.validation.error";
  public static final String MSG_HTTP_HEADERS = "http.headers";
  public static final String MSG_MESSAGE = "message";

  // Class operations
  public static final String MSG_OP_CLASS_CREATE = "class.create";
  public static final String MSG_OP_CLASS_UPDATE = "class.update";
  public static final String MSG_OP_CLASS_DELETE = "class.delete";
  public static final String MSG_OP_CLASS_GET = "class.get";
  public static final String MSG_OP_CLASS_MEMBERS_GET = "class.members.get";
  public static final String MSG_OP_CLASS_MEMBERS_ACTIVATE = "class.members.activate";
  public static final String MSG_OP_CLASS_MEMBERS_DEACTIVATE = "class.members.deactivate";
  public static final String MSG_OP_CLASS_COLLABORATORS_UPDATE = "class.collaborators.update";
  public static final String MSG_OP_CLASS_LIST = "class.list";
  public static final String MSG_OP_CLASS_LIST_FOR_COURSE = "class.list.for.course";
  public static final String MSG_OP_CLASS_JOIN = "class.join";
  public static final String MSG_OP_CLASS_COURSE_ASSOCIATION = "class.course.association";
  public static final String MSG_OP_CLASS_SET_CONTENT_VISIBILITY = "class.content.visibility.set";
  public static final String MSG_OP_CLASS_GET_CONTENT_VISIBILITY = "class.content.visibility.get";
  public static final String MSG_OP_CLASS_REMOVE_STUDENT = "class.join.removal";
  public static final String MSG_OP_CLASS_ARCHIVE = "class.archive";
  public static final String MSG_OP_CLASS_STUDENTS_ADD = "class.students.add";

  public static final String MSG_OP_CLASS_CONTENT_ADD = "class.content.add";
  public static final String MSG_OP_CLASS_CONTENT_LIST_UNSCHEDULED = "class.content.list.unscheduled";
  public static final String MSG_OP_CLASS_CONTENT_LIST_OFFLINE_COMPLETED = "class.content.list.offline.completed";
  public static final String MSG_OP_CLASS_CONTENT_LIST_OFFLINE_ACTIVE = "class.content.list.offline.active";
  public static final String MSG_OP_CLASS_CONTENT_LIST_ONLINE_SCHEDULED = "class.content.list.online.scheduled";
  public static final String MSG_OP_CLASS_CONTENT_ENABLE = "class.content.enable";
  public static final String MSG_OP_CLASS_CONTENT_SCHEDULE = "class.content.schedule";
  public static final String MSG_OP_CLASS_CONTENT_DELETE = "class.content.delete";
  public static final String MSG_OP_CLASS_CONTENT_USERS_ADD = "class.content.users.add";
  public static final String MSG_OP_CLASS_CONTENT_USERS_LIST = "class.content.users.list";
  public static final String MSG_OP_CLASS_CONTENT_MASTERY_ACCRUAL_UPDATE = "class.content.mastery.accrual.update";
  public static final String MSG_OP_CLASS_CONTENT_COMPLETION = "class.content.completion";


  public static final String MSG_OP_CLASS_REROUTE_SETTINGS_UPDATE = "class.reroute.settings.update";
  public static final String MSG_OP_CLASS_LPBASELINE_TRIGGER = "class.lb.baseline.trigger";
  public static final String MSG_OP_CLASS_LPBASELINE_STUDENT_TRIGGER = "class.lb.baseline.student.trigger";
  public static final String MSG_OP_CLASS_MEMBERS_REROUTE_SETTINGS_UPDATE = "class.members.reroute.settings.update";

  public static final String MSG_OP_CLASS_PREFERENCE_UPDATE = "class.preference.update";
  public static final String MSG_OP_CLASS_LANGUAGE_UPDATE = "class.language.update";

  // Containers for different responses
  public static final String RESP_CONTAINER_MBUS = "mb.container";
  public static final String RESP_CONTAINER_POSTPROCESSOR = "mb.postprocessor.event";
  public static final String RESP_CONTAINER_EVENT = "mb.event";

  public static final String CLASS_ID = "classId";
  public static final String CLASS_CONTENT_ID = "classContentId";
  public static final String COURSE_ID = "courseId";
  public static final String LANGUAGE_ID = "languageId";
  public static final String STUDENT_ID = "studentId";
  public static final String CLASS_CODE = "classCode";
  public static final String USER_ID = "userId";
  public static final String EMAIL = "email";
  public static final String CLASS_CONTENTS = "class_contents";
  public static final String COUNT = "count";
  public static final String TITLE = "title";
  public static final String THUMBNAIL = "thumbnail";
  public static final String TAXONOMY = "taxonomy";
  public static final String URL = "url";
  public static final String ID = "id";
  public static final String REQ_PARAM_OFFSET = "offset";
  public static final String REQ_PARAM_LIMIT = "limit";
  public static final String FOR_MONTH = "for_month";
  public static final String FOR_YEAR = "for_year";

  private MessageConstants() {
    throw new AssertionError();
  }

}
