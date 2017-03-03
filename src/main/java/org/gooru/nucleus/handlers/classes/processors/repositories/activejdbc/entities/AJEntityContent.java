package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("content")
public class AJEntityContent extends Model {
    public static final String SELECT_CULC_CONTENT_TO_VALIDATE =
        "SELECT id, collection_id, lesson_id, unit_id, course_id FROM content WHERE id = ?::uuid AND collection_id = ?::uuid AND lesson_id = ?::uuid AND unit_id = ?::uuid AND course_id = ?::uuid AND content_format = ?::content_format_type AND is_deleted = false";

    public static final String SELECT_COLLECTION_CONTENT_TO_VALIDATE =
        "select id, collection_id FROM content WHERE id = ?::uuid AND collection_id = ?::uuid AND content_format = ?::content_format_type AND is_deleted = false";

    public static final String SELECT_CONTENT_TO_VALIDATE =
        "select id FROM content WHERE id = ?::uuid  AND content_format = ?::content_format_type AND is_deleted = false";

}
