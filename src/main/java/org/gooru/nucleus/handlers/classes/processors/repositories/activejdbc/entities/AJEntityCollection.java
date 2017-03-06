package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * Created by ashish on 16/5/16.
 */
@Table("collection")
public class AJEntityCollection extends Model {

    public static final String ID = "id";
    public static final String COURSE_ID = "course_id";
    public static final String UNIT_ID = "unit_id";
    public static final String LESSON_ID = "lesson_id";
    public static final String CA_COUNT = "count";
    public static final String ASSESSMENT_COUNT = "assessment_count";
    public static final String COLLECTION_COUNT = "collection_count";
    public static final String COURSE = "course";
    public static final String UNITS = "units";
    public static final String LESSONS = "lessons";
    public static final String CLASS_VISIBILITY = "class_visibility";

    // Instead of stating equals assessment we are saying not equals collection
    // because we need to include both
    // assessment and external assessment here
    public static final String FETCH_VISIBLE_ASSESSMENTS_QUERY =
        "select id, course_id, unit_id, lesson_id from collection where course_id = ?::uuid and format != 'collection'::content_container_type and "
            + "is_deleted = false and class_visibility ?? ? group by course_id, unit_id, lesson_id, id";
    // Select both id and type and then in CPU separate them in buckets instead
    // of going to db multiple times
    public static final String FETCH_VISIBLE_ITEMS_QUERY =
        "select id, course_id, unit_id, lesson_id, format, class_visibility from collection where course_id = ?::uuid and"
            + " is_deleted = false";

    public static final String FETCH_STATISTICS_QUERY =
        "select course_id, unit_id, lesson_id, format, count(id) from collection where course_id = ?::uuid and "
            + "is_deleted = false and class_visibility ?? ? group by course_id, unit_id, lesson_id, format";
    public static final String COLLECTIONS_QUERY_FILTER =
        "course_id = ?::uuid and id = ANY(?::uuid[]) and is_deleted = false";
    public static final String FETCH_COLLECTIONS_CLASS_VISIBILITY_BY_ID =
        "SELECT id, class_visibility FROM collection WHERE course_id = ?::uuid AND id = ANY(?::uuid[]) AND is_deleted = false";
    public static final String FETCH_COLLECTIONS_CLASS_VISIBILITY_ALL =
        "SELECT id, class_visibility, format FROM collection WHERE course_id = ?::uuid AND is_deleted = false";
    public static final String TABLE_COLLECTION = "collection";

    public static final String FORMAT_TYPE = "format";
    public static final String FORMAT_TYPE_COLLECTION = "collection";
    public static final String FORMAT_TYPE_ASSESSMENT = "assessment";
    public static final String FORMAT_TYPE_ASSESSMENT_EXT = "assessment-external";
    public static final String VISIBILITY_DML =
        "UPDATE collection SET class_visibility = ?::jsonb WHERE id = ?::uuid AND course_id = ?::uuid AND is_deleted = false";
    public static final String SELECT_CUL_COLLECTION_TO_VALIDATE =
        "SELECT id FROM collection where id = ?::uuid AND is_deleted = false AND lesson_id = ?::uuid AND unit_id = ?::uuid AND course_id = ?::uuid AND format = ?::content_container_type AND subformat not in ('pre-test', 'post-test', 'benchmark')";

    public static final String SELECT_COLLECTION_TO_AUTHORIZE =
        "SELECT id  FROM collection where id = ?::uuid AND is_deleted = false AND subformat not in ('pre-test', 'post-test', 'benchmark') AND"
            + " (publish_status = 'published'::publish_status_type OR owner_id = ?::uuid  OR collaborator ?? ?)";

    public static final String JSON_KEY_VISIBLE = "visible";
    public static final String VISIBLE_ON = "on";
    public static final String VISIBLE_OFF = "off";

    // The model needs to be hydrated with format, else it may fail
    public boolean isAssessment() {
        return FORMAT_TYPE_ASSESSMENT.equalsIgnoreCase(this.getString(FORMAT_TYPE));
    }

    // The model needs to be hydrated with format, else it may fail
    public boolean isCollection() {
        return FORMAT_TYPE_COLLECTION.equalsIgnoreCase(this.getString(FORMAT_TYPE));
    }

    // The model needs to be hydrated with format, else it may fail
    public boolean isAssessmentExternal() {
        return FORMAT_TYPE_ASSESSMENT_EXT.equalsIgnoreCase(this.getString(FORMAT_TYPE));
    }

}
