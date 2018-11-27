package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCollection;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to provide utility functions for getting visible contents and associated statistics
 * Created by ashish on 16/5/16.
 */
public final class VisibleContentHelper {

  private VisibleContentHelper() {
    throw new AssertionError();
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(VisibleContentHelper.class);

  /**
   * Based on the provided class Id, courseId and default class visibility, find out all the
   * assessments/collections that are visible for that class. This includes both assessments and
   * external assessments
   *
   * @param classId Id of the class for which visibility is needed
   * @param courseId The course associated with specified class
   * @param result JsonObject which will get populated with all assessments/collections which are
   * visible
   */
  public static void populateVisibleItems(String classId, String courseId, String defaultVisibility,
      JsonObject result) {
    LazyList<AJEntityCollection> items =
        AJEntityCollection.findBySQL(AJEntityCollection.FETCH_VISIBLE_ITEMS_QUERY, courseId);

    Map<String, Set<String>> unitLessonMap = new HashMap<>();
    Map<String, JsonArray> collectionsByLesson = new HashMap<>();
    Map<String, JsonArray> assessmentsByLesson = new HashMap<>();

    items.forEach(collection -> {
      String id = collection.getString(AJEntityCollection.ID);
      String unitId = collection.getString(AJEntityCollection.UNIT_ID);
      String lessonId = collection.getString(AJEntityCollection.LESSON_ID);

      if (unitLessonMap.containsKey(unitId)) {
        unitLessonMap.get(unitId).add(collection.getString(AJEntityCollection.LESSON_ID));
      } else {
        Set<String> lessons = new HashSet<>();
        lessons.add(collection.getString(AJEntityCollection.LESSON_ID));
        unitLessonMap.put(unitId, lessons);
      }

      String visibility;
      String strClassVisibility = collection.getString(AJEntityCollection.CLASS_VISIBILITY);
      JsonObject classVisibility = strClassVisibility != null && !strClassVisibility.isEmpty()
          ? new JsonObject(strClassVisibility) : new JsonObject();
      if (classVisibility.containsKey(classId)) {
        visibility = classVisibility.getString(classId);
      } else {
        if (AJEntityClass.CONTENT_VISIBILITY_TYPE_VISIBLE_ALL.equalsIgnoreCase(defaultVisibility)) {
          visibility = AJEntityCollection.VISIBLE_ON;
        } else if (AJEntityClass.CONTENT_VISIBILITY_TYPE_VISIBLE_COLLECTION
            .equalsIgnoreCase(defaultVisibility)) {
          visibility =
              (collection.isCollection() ? AJEntityCollection.VISIBLE_ON
                  : AJEntityCollection.VISIBLE_OFF);
        } else {
          visibility = AJEntityCollection.VISIBLE_OFF;
        }
      }

      JsonObject visibilityJson = new JsonObject().put(AJEntityCollection.ID, id)
          .put(AJEntityCollection.JSON_KEY_VISIBLE, visibility);

      if (collection.isAssessment() || collection.isAssessmentExternal()) {
        if (assessmentsByLesson.containsKey(lessonId)) {
          assessmentsByLesson.get(lessonId).add(visibilityJson);
        } else {
          JsonArray assessmentsArray = new JsonArray();
          assessmentsArray.add(visibilityJson);
          assessmentsByLesson.put(lessonId, assessmentsArray);
        }
      } else if (collection.isCollection()) {
        if (collectionsByLesson.containsKey(lessonId)) {
          collectionsByLesson.get(lessonId).add(visibilityJson);
        } else {
          JsonArray collectionsArray = new JsonArray();
          collectionsArray.add(visibilityJson);
          collectionsByLesson.put(lessonId, collectionsArray);
        }
      } else {
        LOGGER.warn("Invalid format for collection/assessment id {}", id);
      }
    });

    JsonArray unitArray = new JsonArray();
    for (Map.Entry<String, Set<String>> stringSetEntry : unitLessonMap.entrySet()) {
      Set<String> lessons = stringSetEntry.getValue();
      JsonArray lessonArray = new JsonArray();
      for (String lessonId : lessons) {
        JsonObject lesson = new JsonObject();
        lesson.put(AJEntityCollection.ID, lessonId);
        lesson.put(AJEntityClass.CV_ASSESSMENTS, assessmentsByLesson.get(lessonId));
        lesson.put(AJEntityClass.CV_COLLECTIONS, collectionsByLesson.get(lessonId));
        lessonArray.add(lesson);
      }

      JsonObject unit = new JsonObject();
      unit.put(AJEntityCollection.ID, stringSetEntry.getKey());
      unit.put(AJEntityCollection.LESSONS, lessonArray);

      unitArray.add(unit);
    }
    JsonObject course = new JsonObject();
    course.put(AJEntityCollection.ID, courseId);
    course.put(AJEntityCollection.UNITS, unitArray);

    result.put(AJEntityCollection.COURSE, course);
  }
}
