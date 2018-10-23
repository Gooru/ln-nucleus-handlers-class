package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc;

import java.util.Iterator;
import java.util.List;

/**
 * Created by ashish on 4/3/16.
 */
public final class Utils {

  public static String convertListToPostgresArrayStringRepresentation(List<String> input) {
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

  public static String convertListToPostgresArrayLongRepresentation(List<Long> input) {
    int approxSize = ((input.size() + 1) * 36); // Length of UUID is around
    // 36 chars
    Iterator<Long> it = input.iterator();
    if (!it.hasNext()) {
      return "{}";
    }

    StringBuilder sb = new StringBuilder(approxSize);
    sb.append('{');
    boolean appended = false;
    for (; ; ) {
      Long s = it.next();
      if (s != null) {
        appended = true;
        sb.append(s);
      }
      if (!it.hasNext()) {
        return sb.append('}').toString();
      }
      if (appended) {
        sb.append(',');
        appended = false;
      }
    }

  }


  private Utils() {
    throw new AssertionError();
  }
}
