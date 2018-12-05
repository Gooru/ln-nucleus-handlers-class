package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.profilebaseline;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult.ExecutionStatus;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.LazyList;
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
  private List<String> userList;

  ProfileBaselineRequestQueueService(ProfileBaselineCommand command, String classId,
      String courseId) {
    this.command = command;
    this.classId = classId;
    this.courseId = courseId;
  }

  ExecutionResult<MessageResponse> enqueue() {
    try {
      initializeUsers();
      if (userList == null || userList.isEmpty()) {
        returnSuccess();
      }
      return doQueueing();
    } catch (MessageResponseWrapperException mwe) {
      return new ExecutionResult<>(mwe.getMessageResponse(), ExecutionStatus.FAILED);
    }
  }

  private ExecutionResult<MessageResponse> doQueueing() {
    try (PreparedStatement ps = Base.startBatch(PROFILE_BASELINE_ENQUEUE_QUERY)) {

      for (String userId : userList) {
        Base.addBatch(ps, userId, courseId, classId, DEFAULT_QUEUE_PRIORITY, RQ_STATUS_QUEUED);
      }
      Base.executeBatch(ps);
      return returnSuccess();

    } catch (DBException dbe) {
      LOGGER.error("Error trying to queue requests", dbe);
      if (dbe.getCause() != null && dbe.getCause() instanceof SQLException) {
        return handleSqlException((SQLException) dbe.getCause());
      }
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")),
          ExecutionStatus.FAILED);
    } catch (SQLException dbe) {
      LOGGER.error("Error trying to queue requests", dbe);
      return handleSqlException(dbe);
    }
  }

  private ExecutionResult<MessageResponse> returnSuccess() {
    return new ExecutionResult<>(MessageResponseFactory
        .createNoContentResponse(RESOURCE_BUNDLE.getString("queued")),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private void initializeUsers() {
    if (command.doingBaselineForAll()) {
      try {
        LazyList<AJClassMember> allMembers =
            AJClassMember.where(AJClassMember.FETCH_ALL_JOINED_ACTIVE_USERS_FILTER, classId);
        if (!allMembers.isEmpty()) {
          userList = new ArrayList<>();
          for (AJClassMember member : allMembers) {
            userList.add(member.getUserId());
          }
        } else {
          userList = Collections.emptyList();
        }
      } catch (Exception e) {
        throw new MessageResponseWrapperException(
            MessageResponseFactory
                .createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")));
      }
    } else {
      userList = command.getUsersList();
    }
  }

  private ExecutionResult<MessageResponse> handleSqlException(SQLException e) {
    String message =
        (e.getNextException() != null) ? e.getNextException().getMessage() : e.getMessage();
    LOGGER.error("SqlException: State: '{}', message: '{}'", e.getSQLState(), message);
    if (e.getSQLState().equals("23505")) {
      return returnSuccess();
    }
    return new ExecutionResult<>(
        MessageResponseFactory
            .createInternalErrorResponse(RESOURCE_BUNDLE.getString("error.from.store")),
        ExecutionResult.ExecutionStatus.FAILED);
  }
}
