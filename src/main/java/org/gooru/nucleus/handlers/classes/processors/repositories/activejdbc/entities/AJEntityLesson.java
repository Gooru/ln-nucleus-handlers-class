package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("lesson")
public class AJEntityLesson extends Model {
    public static final String SELECT_LESSON_TO_VALIDATE =
        "SELECT lesson_id, unit_id, course_id FROM lesson WHERE lesson_id = ?::uuid AND unit_id = ?::uuid AND course_id = ?::uuid AND is_deleted = false";
}
