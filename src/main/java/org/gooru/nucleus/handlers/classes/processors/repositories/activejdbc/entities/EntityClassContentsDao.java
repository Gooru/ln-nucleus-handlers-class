package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import java.time.LocalDate;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

public final class EntityClassContentsDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityClassContentsDao.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

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

  public static LazyList<AJEntityClassContents> fetchAllNonOfflineContentsForStudent(String classId,
      int forMonth, int forYear, String userId) {

    LocalDate fromDate, toDate;
    fromDate = LocalDate.of(forYear, forMonth, 1);
    toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
    return AJEntityClassContents
        .where(SELECT_NON_OFFLINE_FOR_STUDENTS, classId, fromDate.toString(),
            toDate.toString(), userId).orderBy("activation_date desc, id desc");
  }

  public static LazyList<AJEntityClassContents> fetchAllNonOfflineContentsForTeacher(String classId,
      int forMonth, int forYear) {

    return AJEntityClassContents
        .where(SELECT_NON_OFFLINE_FOR_TEACHERS, classId, forYear, forMonth)
        .orderBy("dca_added_date desc nulls first, created_at desc");
  }

  public static LazyList<AJEntityClassContents> fetchClassContentsByContentTypeForStudent(
      String classId, String contentType, int forMonth, int forYear, String userId) {

    LocalDate fromDate, toDate;
    fromDate = LocalDate.of(forYear, forMonth, 1);
    toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());

    return AJEntityClassContents
        .where(SELECT_FOR_SPECIFIC_TYPE_FOR_STUDENT, classId, contentType,
            fromDate.toString(), toDate.toString(), userId)
        .orderBy("activation_date desc, id desc");
  }

  public static LazyList<AJEntityClassContents> fetchClassContentsByContentTypeForTeacher(
      String classId, String contentType, int forMonth, int forYear) {

    return AJEntityClassContents
        .where(SELECT_FOR_SPECIFIC_TYPE_FOR_TEACHER, classId, forYear, forMonth, contentType)
        .orderBy("dca_added_date desc nulls first, created_at desc");
  }

  public static LazyList<AJEntityClassContents> fetchOfflineActivitiesForStudent(String classId,
      int forMonth, int forYear, String userId) {
    // TODO: Implement this
    return null;
  }

  public static LazyList<AJEntityClassContents> fetchOfflineActivitiesForTeacher(String classId,
      int forMonth, int forYear) {
    // TODO: Implement this
    return null;
  }

  private static final String SELECT_OFFLINE_FOR_STUDENTS =
      "class_id = ?::uuid AND end_date BETWEEN ?::date AND ?::date and (?::text = any(users) OR users is null) "
          + " and content_type == 'offline-activity' and activation_date is not null";

  private static final String SELECT_OFFLINE_FOR_TEACHERS =
      "class_id = ?::uuid AND for_year = ? AND for_month = ? and content_type == 'offline-activity'";


  private static final String SELECT_NON_OFFLINE_FOR_STUDENTS =
      "class_id = ?::uuid AND activation_date BETWEEN ?::date AND ?::date and (?::text = any(users) OR users is null) "
          + " and content_type != 'offline-activity'";

  private static final String SELECT_NON_OFFLINE_FOR_TEACHERS =
      "class_id = ?::uuid AND for_year = ? AND for_month = ? and content_type != 'offline-activity'";

  private static final String SELECT_FOR_SPECIFIC_TYPE_FOR_TEACHER =
      "class_id = ?::uuid AND for_year = ? and for_month = ? and content_type = ? ";

  private static final String SELECT_FOR_SPECIFIC_TYPE_FOR_STUDENT =
      "class_id = ?::uuid AND content_type = ? AND activation_date BETWEEN ?::date AND ?::date AND (?::text = any(users) OR users is null)";


}
