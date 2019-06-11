package org.gooru.nucleus.handlers.classes.app.components;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.classes.bootstrap.startup.Initializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 5/5/16.
 */
public final class AppConfiguration implements Initializer {

  private static final String APP_CONFIG_KEY = "app.configuration";
  private static final String KEY = "__KEY__";
  private static final String CLASS_END_DATE_KEY = "class.end.date";
  private static final JsonObject configuration = new JsonObject();
  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfiguration.class);
  private static final String POPULATE_DUMMY_EMAIL_KEY = "populate.dummy.email";
  private static final String DUMMY_EMAIL_DOMAIN_KEY = "dummy.email.domain";
  private static final String LIMIT_DEFAULT = "limit.default";
  private static final String LIMIT_MAX = "limit.max";
  private static final String COURSE_VERSION_FOR_ALTERNATE_VISIBILITY = "course.version.for.alternate.visibility";
  private static final String COURSE_VERSION_FOR_PREMIUM_CONTENT = "course.version.for.premium.content";

  public static AppConfiguration getInstance() {
    return Holder.INSTANCE;
  }

  private volatile boolean initialized = false;

  private AppConfiguration() {
  }

  @Override
  public void initializeComponent(Vertx vertx, JsonObject config) {
    if (!initialized) {
      synchronized (Holder.INSTANCE) {
        if (!initialized) {
          JsonObject appConfiguration = config.getJsonObject(APP_CONFIG_KEY);
          if (appConfiguration == null || appConfiguration.isEmpty()) {
            LOGGER.warn("App configuration is not available");
          } else {
            configuration.put(KEY, appConfiguration);
            initialized = true;
          }
        }
      }
    }
  }

  public String getClassEndDate() {
    return configuration.getJsonObject(KEY).getString(CLASS_END_DATE_KEY);
  }

  public boolean getPopulateDummyEmail() {
    return configuration.getJsonObject(KEY).getBoolean(POPULATE_DUMMY_EMAIL_KEY);
  }

  public String getDummyEmailDomain() {
    return configuration.getJsonObject(KEY).getString(DUMMY_EMAIL_DOMAIN_KEY);
  }

  public int getDefaultLimit() {
    return configuration.getJsonObject(KEY).getInteger(LIMIT_DEFAULT, 20);
  }

  public int getMaxLimit() {
    return configuration.getJsonObject(KEY).getInteger(LIMIT_MAX, 50);
  }

  public String getCourseVersionForAlternateVisibility() {
    return configuration.getJsonObject(KEY).getString(COURSE_VERSION_FOR_ALTERNATE_VISIBILITY);
  }

  public String getCourseVersionForPremiumContent() {
    return configuration.getJsonObject(KEY).getString(COURSE_VERSION_FOR_PREMIUM_CONTENT);
  }

  public String getOACompletionInformEndpoint() {
    return configuration.getJsonObject(KEY).getString("oa.completion.informer.endpoint");
  }

  public int getHttpClientPoolSize() {
    return configuration.getJsonObject(KEY).getInteger("httpclient.pool.size");
  }

  public int getHttpClientTimeout() {
    return configuration.getJsonObject(KEY).getInteger("httpclient.timeout");
  }

  private static final class Holder {

    private static final AppConfiguration INSTANCE = new AppConfiguration();
  }

}
