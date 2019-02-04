package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhandlers.profilebaseline;

import io.vertx.core.json.JsonArray;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContextHelper;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

/**
 * @author ashish.
 */

class ProfileBaselineCommand {

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final List<String> usersList;
  private final boolean doBaselineForAll;

  ProfileBaselineCommand(List<String> usersList) {
    this.usersList = Collections.unmodifiableList(usersList);
    doBaselineForAll = this.usersList.isEmpty();
  }

  List<String> getUsersList() {
    return usersList;
  }

  boolean doingBaselineForAll() {
    return doBaselineForAll;
  }

  static ProfileBaselineCommand build(ProcessorContext context, boolean isStudent) {
    List<String> usersList;
    // If the request is for student, we want to initiate the list with single student and return
    if (isStudent) {
      usersList = new ArrayList<>(1);
      usersList.add(context.userId());
      return new ProfileBaselineCommand(usersList);
    }

    JsonArray users = context.request().getJsonArray(RequestAttributes.USERS);
    if (users == null) {
      throw new MessageResponseWrapperException(MessageResponseFactory
          .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.payload")));
    } else if (users.isEmpty()) {
      usersList = Collections.emptyList();
    } else {
      usersList = validateForBeingUuidAndFetchUsers(users);
    }
    return new ProfileBaselineCommand(usersList);
  }

  private static List<String> validateForBeingUuidAndFetchUsers(JsonArray users) {
    int size = users.size();
    List<String> usersList = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      String user = users.getString(i);
      if (!ProcessorContextHelper.validateUuid(user)) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.user")));
      }
      usersList.add(user);
    }
    return usersList;
  }

  static class RequestAttributes {

    static final String USERS = "users";

    private RequestAttributes() {
      throw new AssertionError();
    }
  }
}
