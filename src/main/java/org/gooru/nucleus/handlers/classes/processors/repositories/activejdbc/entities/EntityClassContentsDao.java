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

  public static LazyList<AJEntityClassContents> fetchAllContentsForStudent(String classId,
      int forMonth, int forYear, String userId) {

    LocalDate fromDate, toDate;
    fromDate = LocalDate.of(forYear, forMonth, 1);
    toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());
    return AJEntityClassContents
        .where(SELECT_CLASS_CONTENTS_FLT_NOT_ACTIVATED, classId, fromDate.toString(),
            toDate.toString(), userId).orderBy("activation_date desc, id desc");
  }

  public static LazyList<AJEntityClassContents> fetchAllContentsForTeacher(String classId,
      int forMonth, int forYear) {

    return AJEntityClassContents
        .where(SELECT_CLASS_CONTENTS, classId, forYear, forMonth)
        .orderBy("dca_added_date desc nulls first, created_at desc");
  }

  public static LazyList<AJEntityClassContents> fetchClassContentsByContentTypeForStudent(
      String classId, String contentType, int forMonth, int forYear, String userId) {

    LocalDate fromDate, toDate;
    fromDate = LocalDate.of(forYear, forMonth, 1);
    toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());

    return AJEntityClassContents
        .where(SELECT_CLASS_CONTENTS_GRP_BY_TYPE_FLT_NOT_ACTIVATED, classId, contentType,
            fromDate.toString(), toDate.toString(), userId)
        .orderBy("activation_date desc, id desc");
  }

  public static LazyList<AJEntityClassContents> fetchClassContentsByContentTypeForTeacher(
      String classId, String contentType, int forMonth, int forYear) {

    return AJEntityClassContents
        .where(SELECT_CLASS_CONTENTS_GRP_BY_TYPE, classId, forYear, forMonth, contentType)
        .orderBy("dca_added_date desc nulls first, created_at desc");
  }


  private static final String SELECT_CLASS_CONTENTS_FLT_NOT_ACTIVATED =
      "class_id = ?::uuid AND activation_date BETWEEN ?::date AND ?::date and (?::text = any(users) OR users is null)";

  private static final String SELECT_CLASS_CONTENTS =
      "class_id = ?::uuid AND for_year = ? AND for_month = ?";

  private static final String SELECT_CLASS_CONTENTS_GRP_BY_TYPE =
      "class_id = ?::uuid AND for_year = ? and for_month = ? and content_type = ? ";

  private static final String SELECT_CLASS_CONTENTS_GRP_BY_TYPE_FLT_NOT_ACTIVATED =
      "class_id = ?::uuid AND content_type = ? AND activation_date BETWEEN ?::date AND ?::date AND (?::text = any(users) OR users is null)";


}
