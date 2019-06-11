package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author szgooru Created On 12-Dec-2018
 */
@Table("taxonomy_subject")
public class AJEntityTaxonomySubject extends Model {

  public static final String TABLE = "taxonomy_subject";

  public static final String FETCH_SUBJECT_BY_ID = "id = ?";
  public static final String FETCH_SUBJECT_BY_GUT = "id = ? AND default_taxonomy_subject_id IS NULL";
  public static final String FETCH_SUBJECT_BY_ID_FW = "default_taxonomy_subject_id = ? AND standard_framework_id = ?";

  public static final String RESP_KEY_FRAMEWORK = "framework";
  public static final String RESP_KEY_SUBJECT = "subject";

}
