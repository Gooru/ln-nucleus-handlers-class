package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.transactions.exceptionhandlers;

import org.gooru.nucleus.handlers.classes.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponse;

public interface ExceptionHandler {
  
  ExecutionResult<MessageResponse> handleError(Throwable e);

}
