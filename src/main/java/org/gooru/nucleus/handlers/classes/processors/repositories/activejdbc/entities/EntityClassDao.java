package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

public final class EntityClassDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityClassDao.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  public static AJEntityClass fetchClassById(String classId) {
    LazyList<AJEntityClass> classes = AJEntityClass
        .where(AJEntityClass.FETCH_QUERY_FILTER, classId);
    if (classes.isEmpty()) {
      LOGGER.warn("Not able to find class '{}'", classId);
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")));
    }
    AJEntityClass entityClass = classes.get(0);
    if (!entityClass.isCurrentVersion() || entityClass.isArchived()) {
      LOGGER.warn("Class '{}' is either archived or not of current version", classId);
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(
              RESOURCE_BUNDLE.getString("class.archived.or.incorrect.version")));
    }
    return entityClass;
  }
  
  public static LazyList<AJEntityClass> fetchMultipleClassesByIds(String classIds) {
    return AJEntityClass
        .where(AJEntityClass.FETCH_MULTIPLE_NON_DELETED_NON_ARCHIVED, classIds);
  }

  private EntityClassDao() {
    throw new AssertionError();
  }
}
