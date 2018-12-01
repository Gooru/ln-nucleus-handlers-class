package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import java.util.List;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.Utils;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * @author ashish.
 */
@Table("grade_master")
public class AJEntityGradeMaster extends Model {

  private static final String ID = "id";
  private static final String GRADE = "grade";
  private static final String GRADE_SEQ = "grade_seq";

  private static final String FETCH_MULTIPLE_QUERY_FILTER = "id = ANY(?::bigint[])";

  public Long getId() {
    return this.getLong(ID);
  }

  public String getGradeName() {
    return this.getString(GRADE);
  }

  public Integer getGradeSeq() {
    return this.getInteger(GRADE_SEQ);
  }

  public static List<AJEntityGradeMaster> getAllByIds(List<Long> idList) {
    return AJEntityGradeMaster
        .find(FETCH_MULTIPLE_QUERY_FILTER,
            Utils.convertListToPostgresArrayLongRepresentation(idList));
  }
}
