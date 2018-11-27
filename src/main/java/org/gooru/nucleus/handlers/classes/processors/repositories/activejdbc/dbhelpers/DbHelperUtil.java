package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers;

import io.vertx.core.json.JsonArray;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.classes.app.components.AppConfiguration;
import org.gooru.nucleus.handlers.classes.constants.MessageConstants;
import org.gooru.nucleus.handlers.classes.processors.ProcessorContext;
import org.gooru.nucleus.handlers.classes.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.classes.processors.responses.MessageResponseFactory;

public final class DbHelperUtil {

  private DbHelperUtil() {
    throw new AssertionError();
  }

  public static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  public static String toPostgresArrayString(Collection<String> input) {
    int approxSize = ((input.size() + 1) * 36); // Length of UUID is around
    // 36 chars
    Iterator<String> it = input.iterator();
    if (!it.hasNext()) {
      return "{}";
    }

    StringBuilder sb = new StringBuilder(approxSize);
    sb.append('{');
    for (; ; ) {
      String s = it.next();
      sb.append('"').append(s).append('"');
      if (!it.hasNext()) {
        return sb.append('}').toString();
      }
      sb.append(',');
    }
  }

  public static Integer getOffsetFromContext(ProcessorContext context) {
    try {
      String offsetFromRequest = readRequestParam(MessageConstants.REQ_PARAM_OFFSET, context);
      return offsetFromRequest != null ? Integer.valueOf(offsetFromRequest) : 0;
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public static Integer getLimitFromContext(ProcessorContext context) {
    try {
      String offsetFromRequest = readRequestParam(MessageConstants.REQ_PARAM_LIMIT, context);
      int offset = offsetFromRequest != null ? Integer.valueOf(offsetFromRequest)
          : AppConfiguration.getInstance().getDefaultLimit();
      if (offset <= AppConfiguration.getInstance().getMaxLimit()) {
        return offset;
      }
      return AppConfiguration.getInstance().getMaxLimit();
    } catch (NumberFormatException nfe) {
      return AppConfiguration.getInstance().getDefaultLimit();
    }
  }

  public static String readRequestParam(String param, ProcessorContext context) {
    JsonArray requestParams = context.request().getJsonArray(param);
    if (requestParams == null || requestParams.isEmpty()) {
      return null;
    }

    String value = requestParams.getString(0);
    return (value != null && !value.isEmpty()) ? value : null;
  }

  public static int getForMonth(ProcessorContext context) {
    String forMonth = readRequestParam(MessageConstants.FOR_MONTH, context);
    if (forMonth != null) {
      try {
        return Integer.valueOf(forMonth);
      } catch (NumberFormatException nme) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.formonth")));
      }
    }
    return LocalDate.now().getMonthValue();
  }

  public static int getForYear(ProcessorContext context) {
    String forYear = readRequestParam(MessageConstants.FOR_YEAR, context);
    if (forYear != null) {
      try {
        return Integer.valueOf(forYear);
      } catch (NumberFormatException e) {
        throw new MessageResponseWrapperException(MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.foryear")));
      }
    }
    return LocalDate.now().getYear();
  }

}
