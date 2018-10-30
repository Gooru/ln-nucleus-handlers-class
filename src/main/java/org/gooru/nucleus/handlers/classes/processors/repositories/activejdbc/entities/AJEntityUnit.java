package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("unit")
public class AJEntityUnit extends Model {

  public static final String SELECT_UNIT_TO_VALIDATE =
      "SELECT unit_id, course_id FROM unit WHERE unit_id = ?::uuid AND course_id = ?::uuid AND is_deleted = false";

}
