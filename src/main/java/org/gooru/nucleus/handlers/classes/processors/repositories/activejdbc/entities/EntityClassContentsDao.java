package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

public final class EntityClassContentsDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityClassContentsDao.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private static final String SELECT_TASKS_COUNT_BY_OA =
      "SELECT count(id) as task_count, oa_id FROM oa_tasks WHERE"
          + " oa_id = ANY(?::uuid[]) GROUP BY oa_id";
  private static final String SELECT_ACTIVE_CLASS_USERS = "select user_id::text from class_member where class_id = ?::uuid and is_active = true";
  private static final String FETCH_STUDENT_RUBRIC_FOR_COLLECTION = "select id from rubric where collection_id = ?::uuid and is_deleted = false and is_rubric = true and grader = 'Self'";

  public static AJEntityClassContents fetchActivityByIdAndClass(String contentId, String classId) {
    LazyList<AJEntityClassContents> classContents =
        AJEntityClassContents
            .where(AJEntityClassContents.FETCH_CLASS_CONTENT, contentId, classId);
    if (classContents.isEmpty()) {
      LOGGER.warn("content {} not add to this class  {}", contentId, classId);
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")));
    }
    return classContents.get(0);
  }

  private EntityClassContentsDao() {
    throw new AssertionError();
  }

  public static List<String> fetchUsersForSpecifiedOA(AJEntityClassContents classContents) {
    List<String> users = classContents.getUsers();
    if (users == null) {
      users = Base.firstColumn(SELECT_ACTIVE_CLASS_USERS, classContents.getClassId());
    }
    return users;
  }


  public static List<AJEntityClassContents> fetchAllOnlineScheduledActivitiesForStudent(
      String classId, LocalDate startDate, LocalDate endDate, String userId) {

    return AJEntityClassContents
        .where(SELECT_ONLINE_SCHEDULED_FOR_STUDENTS, classId, startDate.toString(),
            endDate.toString(), userId).orderBy("activation_date desc, id desc");
  }

  public static List<AJEntityClassContents> fetchAllOnlineScheduledActivitiesForTeacher(
      String classId, LocalDate startDate, LocalDate endDate) {

    return AJEntityClassContents
        .where(SELECT_ONLINE_SCHEDULED_FOR_TEACHERS, classId, startDate.toString(),
            endDate.toString()).orderBy("dca_added_date desc nulls first, created_at desc");
  }

  public static List<AJEntityClassContents> fetchUnscheduledActivitiesForTeacher(String classId,
      int forMonth, int forYear) {
    return AJEntityClassContents
        .where(SELECT_UNSCHEDULED_FOR_TEACHERS, classId, forMonth, forYear);
  }

  public static List<AJEntityClassContents> fetchOfflineCompletedActivitiesForStudent(
      String classId, int offset, int limit, String userId) {
    return AJEntityClassContents
        .where(SELECT_ALL_OFFLINE_COMPLETED_FOR_STUDENTS, classId, userId)
        .orderBy("end_date desc").offset(offset).limit(limit);

  }

  public static List<AJEntityClassContents> fetchOfflineCompletedActivitiesForTeacher(
      String classId, int offset, int limit) {
    return AJEntityClassContents
        .where(SELECT_ALL_OFFLINE_COMPLETED_FOR_TEACHERS, classId)
        .orderBy("end_date desc").offset(offset).limit(limit);
  }

  public static List<AJEntityClassContents> fetchOfflineActiveActivitiesForStudent(String classId,
      int offset, int limit, String userId) {
    return AJEntityClassContents
        .where(SELECT_ALL_OFFLINE_ACTIVE_FOR_STUDENTS, classId, userId)
        .orderBy("end_date desc").offset(offset).limit(limit);
  }

  public static List<AJEntityClassContents> fetchOfflineActiveActivitiesForTeacher(String classId,
      int offset, int limit) {
    return AJEntityClassContents
        .where(SELECT_ALL_OFFLINE_ACTIVE_FOR_TEACHERS, classId)
        .orderBy("end_date desc").offset(offset).limit(limit);
  }

  public static Long fetchOfflineCompletedActivitiesCountForStudent(String classId, String userId) {
    return AJEntityClassContents.count(SELECT_ALL_OFFLINE_COMPLETED_FOR_STUDENTS, classId, userId);
  }

  public static Long fetchOfflineCompletedActivitiesCountForTeacher(String classId) {
    return AJEntityClassContents.count(SELECT_ALL_OFFLINE_COMPLETED_FOR_TEACHERS, classId);
  }

  public static Long fetchOfflineActiveActivitiesCountForStudent(String classId, String userId) {
    return AJEntityClassContents.count(SELECT_ALL_OFFLINE_ACTIVE_FOR_STUDENTS, classId, userId);
  }

  public static Long fetchOfflineActiveActivitiesCountForTeacher(String classId) {
    return AJEntityClassContents.count(SELECT_ALL_OFFLINE_ACTIVE_FOR_TEACHERS, classId);
  }

  private static final String SELECT_ONLINE_SCHEDULED_FOR_STUDENTS =
      "class_id = ?::uuid AND activation_date BETWEEN ?::date AND ?::date and (?::text = any(users) OR users is null) "
          + " and content_type != 'offline-activity'";

  private static final String SELECT_ONLINE_SCHEDULED_FOR_TEACHERS =
      "class_id = ?::uuid AND dca_added_date BETWEEN ?::date AND ?::date and content_type != 'offline-activity'";

  private static final String SELECT_UNSCHEDULED_FOR_TEACHERS =
      "class_id = ?::uuid AND for_month = ? and for_year = ? and dca_added_date is null";

  private static final String SELECT_ALL_OFFLINE_COMPLETED_FOR_TEACHERS =
      "class_id = ?::uuid AND is_completed = true and content_type = 'offline-activity'";

  private static final String SELECT_ALL_OFFLINE_ACTIVE_FOR_TEACHERS =
      "class_id = ?::uuid AND is_completed = false and dca_added_date is not null and content_type = 'offline-activity'";

  private static final String SELECT_ALL_OFFLINE_ACTIVE_FOR_STUDENTS =
      "class_id = ?::uuid AND is_completed = false and activation_date is not null and content_type = 'offline-activity' "
          + " and (?::text = any(users) OR users is null) ";

  private static final String SELECT_ALL_OFFLINE_COMPLETED_FOR_STUDENTS =
      "class_id = ?::uuid AND is_completed = true and content_type = 'offline-activity' "
          + " and (?::text = any(users) OR users is null) ";

  public static List<Map> fetchTaskCount(String offlineActivityIdListString) {
    return Base.findAll(SELECT_TASKS_COUNT_BY_OA, offlineActivityIdListString);
  }
  
  public static String fetchStudentRubricForSpecifiedOA(String contentId) {
    Object studentRubricId = Base.firstCell(FETCH_STUDENT_RUBRIC_FOR_COLLECTION, contentId);
    return studentRubricId == null ? null : String.valueOf(studentRubricId);
  }

}
