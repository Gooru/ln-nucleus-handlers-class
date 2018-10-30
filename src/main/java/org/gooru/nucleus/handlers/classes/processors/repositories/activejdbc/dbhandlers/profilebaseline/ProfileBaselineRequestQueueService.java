package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.profilebaseline;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

class ProfileBaselineRequestQueueService {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ProfileBaselineRequestQueueService.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private static final String PROFILE_BASELINE_ENQUEUE_QUERY =
      "insert into profile_baseline_queue(user_id, course_id, class_id, priority, status) values (?::uuid, ?::uuid, ?::uuid, ?, ?)";
  private static final int RQ_STATUS_QUEUED = 0;
  private static final int DEFAULT_QUEUE_PRIORITY = 4;
  private final ProfileBaselineCommand command;
  private final String classId;
  private final String courseId;

  ProfileBaselineRequestQueueService(ProfileBaselineCommand command, String classId,
      String courseId) {
    this.command = command;
    this.classId = classId;
    this.courseId = courseId;
  }

  ExecutionResult<MessageResponse> enqueue() {
    try (PreparedStatement ps = Base.startBatch(PROFILE_BASELINE_ENQUEUE_QUERY)) {

      for (String userId : command.getUsersList()) {
        Base.addBatch(ps, userId, classId, courseId, DEFAULT_QUEUE_PRIORITY, RQ_STATUS_QUEUED);
      }
      Base.executeBatch(ps);
      return new ExecutionResult<>(MessageResponseFactory
          .createNoContentResponse(RESOURCE_BUNDLE.getString("queued")),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);

    } catch (DBException | SQLException dbe) {
      LOGGER.error("Error trying to queue requests", dbe);
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
  }
}
