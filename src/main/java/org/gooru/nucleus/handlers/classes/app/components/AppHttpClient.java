package org.gooru.nucleus.handlers.classes.app.components;

import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.classes.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.handlers.classes.bootstrap.startup.Initializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

public final class AppHttpClient implements Initializer, Finalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppHttpClient.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private volatile boolean initialized = false;

    private HttpClient httpClient;

    private static final String KEY_RESCOPE_URI = "rescope.uri";
    private static final String KEY_ROUTE0_URI = "route0.uri";
    private static final String KEY_LP_BASELINE_URI = "lpbaseline.uri";
    private static final String KEY_MAX_POOLSIZE = "http.conn.poolsize";
    private static final String KEY_HTTP_TIMEOUT = "http.conn.timeout";
    private static final String KEY_EVENT_CONFIG = "rescope.event.publisher.config";

    private static final int DEFAULT_POOLSIZE = 20;
    private static final int DEFAULT_TIMEOUT = 60000;

    private String rescopeUri;
    private String route0Uri;
    private String lpbaselineUri;
    private int maxPoolsize;
    private int timeout;

    private AppHttpClient() {
    }

    public static AppHttpClient getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void initializeComponent(Vertx vertx, JsonObject config) {
        LOGGER.debug("Initializing called for http client");
        if (!initialized) {
            LOGGER.debug("may have to initialize http client");
            synchronized (Holder.INSTANCE) {
                if (!initialized) {
                    LOGGER.debug("initializing http client after double checking");
                    JsonObject eventConfig = config.getJsonObject(KEY_EVENT_CONFIG);
                    if (eventConfig == null || eventConfig.isEmpty()) {
                        LOGGER.warn(RESOURCE_BUNDLE.getString("event.config.not.found"));
                        throw new AssertionError(RESOURCE_BUNDLE.getString("event.config.not.found"));
                    }

                    this.maxPoolsize = eventConfig.getInteger(KEY_MAX_POOLSIZE, DEFAULT_POOLSIZE);
                    this.timeout = eventConfig.getInteger(KEY_HTTP_TIMEOUT, DEFAULT_TIMEOUT);

                    this.rescopeUri = eventConfig.getString(KEY_RESCOPE_URI);
                    if (this.rescopeUri == null || this.rescopeUri.isEmpty()) {
                        LOGGER.warn(RESOURCE_BUNDLE.getString("rescope.uri.missing"));
                        throw new AssertionError(RESOURCE_BUNDLE.getString("rescope.uri.missing"));
                    }
                    this.route0Uri = eventConfig.getString(KEY_ROUTE0_URI);
                    if (this.route0Uri == null || this.route0Uri.isEmpty()) {
                        LOGGER.warn(RESOURCE_BUNDLE.getString("route0.uri.missing"));
                        throw new AssertionError(RESOURCE_BUNDLE.getString("route0.uri.missing"));
                    }
                    this.lpbaselineUri = eventConfig.getString(KEY_LP_BASELINE_URI);
                    if (this.lpbaselineUri == null || this.lpbaselineUri.isEmpty()) {
                        LOGGER.warn(RESOURCE_BUNDLE.getString("lpbaseline.uri.missing"));
                        throw new AssertionError(RESOURCE_BUNDLE.getString("lpbaseline.uri.missing"));
                    }
                    this.httpClient = vertx.createHttpClient(
                        new HttpClientOptions().setMaxPoolSize(this.maxPoolsize).setConnectTimeout(this.timeout));
                    initialized = true;
                    LOGGER.debug("App Http Client initialized successfully");
                }
            }
        }
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public String rescopeUri() {
        return rescopeUri;
    }

    public String route0Uri() {
        return route0Uri;
    }
    
    public String lpbaselineUri() {
        return lpbaselineUri;
    }
    
    @Override
    public void finalizeComponent() {
        this.httpClient.close();
    }
    

    private static final class Holder {
        private static final AppHttpClient INSTANCE = new AppHttpClient();
    }
}
