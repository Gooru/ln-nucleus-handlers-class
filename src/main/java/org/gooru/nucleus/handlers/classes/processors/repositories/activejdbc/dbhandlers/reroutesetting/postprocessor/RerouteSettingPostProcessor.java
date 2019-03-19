
package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.reroutesetting.postprocessor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.DbHelperUtil;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJClassMember;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
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
 * @author szgooru Created On 07-Feb-2019
 */
public class RerouteSettingPostProcessor {

  private final static Logger LOGGER = LoggerFactory.getLogger(RerouteSettingPostProcessor.class);
  private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  private static final String PROFILE_BASELINE_ENQUEUE_QUERY =
      "insert into profile_baseline_queue(user_id, course_id, class_id, priority, status, baseline_override, route0_override, rescope_override)"
          + " values (?::uuid, ?::uuid, ?::uuid, ?, ?, ?, ?, ?)";
  private static final int BASELINE_QUEUED_STATUS = 0;
  private static final int DEFAULT_BASELINE_QUEUE_PRIORITY = 4;

  private final RerouteSettingPostProcessorCommand command;
  private final Map<String, AJClassMember> members;

  public RerouteSettingPostProcessor(RerouteSettingPostProcessorCommand command) {
    this.command = command;
    this.members = initializeClassMembers();
  }

  public ExecutionResult<MessageResponse> process() {
    AJEntityClass entityClass = initializeClass();
    if (entityClass == null) {
      return new ExecutionResult<>(
          MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    // We only need to proceed for post processing if the class is NOT set to force calculate ILP
    if (!entityClass.isForceCalculateILP()) {

      try (PreparedStatement ps = Base.startBatch(PROFILE_BASELINE_ENQUEUE_QUERY)) {
        command.getUsers().forEach(user -> {
          String userId = user.getUserId().toString();
          AJClassMember member = this.members.get(userId);
          if (member.getProfileBaselineDone()) {
            if (user.getGradeLowerBound() != null) {
              // Here we need to set the flags of baseline_override, route0_override and
              // rescope_override and insert into baseline queue table
              Base.addBatch(ps, userId, entityClass.getCourseId(),
                  command.getClassId(), DEFAULT_BASELINE_QUEUE_PRIORITY, BASELINE_QUEUED_STATUS,
                  true, true, true);
            } else {
              // Here we just need to do the rescope
              Base.addBatch(ps, userId, entityClass.getCourseId(),
                  command.getClassId(), DEFAULT_BASELINE_QUEUE_PRIORITY, BASELINE_QUEUED_STATUS,
                  false, false, true);
            }
          } else {
            LOGGER.debug("there is no baseline done for user '{}' -- nothing to do, skipping", userId);
          }
        });

        Base.executeBatch(ps);
        return returnSuccess();
      } catch (DBException dbe) {
        LOGGER.error("Error trying to queue requests", dbe);
        if (dbe.getCause() != null && dbe.getCause() instanceof SQLException) {
          return handleSqlException((SQLException) dbe.getCause());
        }
        return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(
            RESOURCE_BUNDLE.getString("error.from.store")), ExecutionStatus.FAILED);
      } catch (SQLException dbe) {
        LOGGER.error("Error trying to queue requests", dbe);
        return handleSqlException(dbe);
      }
    } else {
      LOGGER.debug("Class is set to force calculate ILP, nothing to process here..");
      return returnSuccess();
    }
  }

  private AJEntityClass initializeClass() {
    LazyList<AJEntityClass> classes =
        AJEntityClass.where(AJEntityClass.FETCH_QUERY_FILTER, command.getClassId());
    if (!classes.isEmpty()) {
      return classes.get(0);
    }

    return null;
  }

  private Map<String, AJClassMember> initializeClassMembers() {
    LazyList<AJClassMember> members;
    List<String> userIds = new ArrayList<>();
    command.getUsers().forEach(user -> {
      userIds.add(user.getUserId().toString());
    });

    Map<String, AJClassMember> classMembers = new HashMap<>();
    members = AJClassMember.where(AJClassMember.FETCH_FOR_MLTIPLE_ACTIVE_USER_QUERY_FILTER,
        command.getClassId(), DbHelperUtil.toPostgresArrayString(userIds));
    members.forEach(member -> {
      classMembers.put(member.getString(AJClassMember.USER_ID), member);
    });
    return classMembers;
  }

  private ExecutionResult<MessageResponse> returnSuccess() {
    return new ExecutionResult<>(
        MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("queued")),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  private ExecutionResult<MessageResponse> handleSqlException(SQLException e) {
    String message =
        (e.getNextException() != null) ? e.getNextException().getMessage() : e.getMessage();
    LOGGER.error("SqlException: State: '{}', message: '{}'", e.getSQLState(), message);
    if (e.getSQLState().equals("23505")) {
      return returnSuccess();
    }
    return new ExecutionResult<>(MessageResponseFactory.createInternalErrorResponse(
        RESOURCE_BUNDLE.getString("error.from.store")), ExecutionResult.ExecutionStatus.FAILED);
  }

}
