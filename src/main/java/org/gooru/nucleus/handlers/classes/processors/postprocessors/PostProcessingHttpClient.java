package org.gooru.nucleus.handlers.classes.processors.postprocessors;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.classes.app.components.AppConfiguration;
import org.gooru.nucleus.handlers.classes.bootstrap.startup.Initializer;

public class PostProcessingHttpClient implements Initializer {


  public static PostProcessingHttpClient getInstance() {
    return Holder.ourInstance;
  }

  private transient boolean initialized = false;
  private HttpClient httpClient;


  private PostProcessingHttpClient() {
  }

  @Override
  public void initializeComponent(Vertx vertx, JsonObject config) {
    if (!initialized) {
      synchronized (Holder.ourInstance) {
        if (!initialized) {
          httpClient = vertx.createHttpClient(
              new HttpClientOptions().setConnectTimeout(getTimeout())
                  .setMaxPoolSize(getPoolSize()));

        }
      }
    }
  }

  private int getPoolSize() {
    return AppConfiguration.getInstance().getHttpClientPoolSize();
  }

  private int getTimeout() {
    return AppConfiguration.getInstance().getHttpClientTimeout();
  }

  public HttpClient getHttpClient() {
    return this.httpClient;
  }

  private static final class Holder {

    private static final PostProcessingHttpClient ourInstance = new PostProcessingHttpClient();
  }

}
