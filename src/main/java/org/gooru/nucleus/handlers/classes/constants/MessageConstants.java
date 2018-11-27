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
  public static final String MSG_OP_CLASS_COLLABORATORS_UPDATE = "class.collaborators.update";
  public static final String MSG_OP_CLASS_LIST = "class.list";
  public static final String MSG_OP_CLASS_LIST_FOR_COURSE = "class.list.for.course";
  public static final String MSG_OP_CLASS_JOIN = "class.join";
  public static final String MSG_OP_CLASS_INVITE = "class.invite.user";
  public static final String MSG_OP_CLASS_COURSE_ASSOCIATION = "class.course.association";
  public static final String MSG_OP_CLASS_SET_CONTENT_VISIBILITY = "class.content.visibility.set";
  public static final String MSG_OP_CLASS_GET_CONTENT_VISIBILITY = "class.content.visibility.get";
  public static final String MSG_OP_CLASS_INVITE_REMOVE = "class.invite.user.remove";
  public static final String MSG_OP_CLASS_REMOVE_STUDENT = "class.join.removal";
  public static final String MSG_OP_CLASS_CONTENT_ADD = "class.content.add";
  public static final String MSG_OP_CLASS_CONTENT_LIST = "class.content.list";
  public static final String MSG_OP_CLASS_CONTENT_ENABLE = "class.content.enable";
  public static final String MSG_OP_CLASS_ARCHIVE = "class.archive";
  public static final String MSG_OP_CLASS_CONTENT_DELETE = "class.content.delete";

  public static final String MSG_OP_CLASS_REROUTE_SETTINGS_UPDATE = "class.reroute.settings.update";
  public static final String MSG_OP_CLASS_LPBASELINE_TRIGGER = "class.lb.baseline.trigger";
  public static final String MSG_OP_CLASS_MEMBERS_REROUTE_SETTINGS_UPDATE = "class.members.reroute.settings.update";

  // Containers for different responses
  public static final String RESP_CONTAINER_MBUS = "mb.container";
  public static final String RESP_CONTAINER_EVENT = "mb.event";

  public static final String CLASS_ID = "classId";
  public static final String COURSE_ID = "courseId";
  public static final String STUDENT_ID = "studentId";
  public static final String CLASS_CODE = "classCode";
  public static final String USER_ID = "userId";
  public static final String EMAIL = "email";
  public static final String CLASS_CONTENTS = "class_contents";
  public static final String TITLE = "title";
  public static final String THUMBNAIL = "thumbnail";
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
