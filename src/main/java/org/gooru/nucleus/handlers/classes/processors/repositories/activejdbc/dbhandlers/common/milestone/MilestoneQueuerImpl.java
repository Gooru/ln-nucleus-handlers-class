package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.common.milestone;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import org.gooru.nucleus.handlers.classes.app.components.AppConfiguration;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityCourse;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

public class MilestoneQueuerImpl implements MilestoneQueuer {

  private static final int STATUS_QUEUED = 0;
  private final boolean override;
  private UUID courseId;
  private Long gradeCurrent;
  private String fwCode;
  private static final Logger LOGGER = LoggerFactory.getLogger(MilestoneQueuer.class);
  private static String MILESTONE_QUEUE_QUERY = "insert into milestone_queue(course_id, fw_code, override, status) values (?::uuid, ?, ?, ?)";
  private static final String DEFAULT_FW = "GUT";


  MilestoneQueuerImpl(boolean override) {
    this.override = override;
  }

  @Override
  public void enqueue(UUID courseId, Long gradeCurrent) {
    this.courseId = courseId;
    this.gradeCurrent = gradeCurrent;
    doQueue();
  }


  private void doQueue() {
    fwCode = fetchFwCodeFromGrade();
    if (fwCode == null) {
      fwCode = DEFAULT_FW;
    }
    LOGGER.info("Queueing for milestone calculation. Course: '{}' and FW: '{}'", courseId, fwCode);
    queueInDb();
  }

  private void queueInDb() {
    try {
      Base.exec(MILESTONE_QUEUE_QUERY, courseId, fwCode, override, STATUS_QUEUED);
    } catch (DBException dbe) {
      LOGGER.error("Error trying to queue requests", dbe);
      if (dbe.getCause() != null && dbe.getCause() instanceof SQLException) {
        handleSqlException((SQLException) dbe.getCause());
      } else {
        throw dbe;
      }
    }
  }

  private boolean isCoursePremium(AJEntityCourse entityCourse) {
    return Objects.equals(AppConfiguration.getInstance().getCourseVersionForPremiumContent(),
        entityCourse.getVersion());
  }

  private void handleSqlException(SQLException e) {
    String message =
        (e.getNextException() != null) ? e.getNextException().getMessage() : e.getMessage();
    LOGGER.error("SqlException: State: '{}', message: '{}'", e.getSQLState(), message);
    if (e.getSQLState().equals("23505")) {
      return;
    }
    throw new DBException(e);
  }

  private String fetchFwCodeFromGrade() {
    Object fwCodeObject = Base
        .firstCell("select fw_code from grade_master where id = ?", gradeCurrent);
    return String.valueOf(fwCodeObject);
  }


}
