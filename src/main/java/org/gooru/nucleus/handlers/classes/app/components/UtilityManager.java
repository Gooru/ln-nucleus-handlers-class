package org.gooru.nucleus.handlers.classes.app.components;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.classes.bootstrap.shutdown.Finalizer;
import org.gooru.nucleus.handlers.classes.bootstrap.startup.Initializer;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers.LanguageValidator;
import org.gooru.nucleus.libs.tenant.bootstrap.TenantInitializer;

public class UtilityManager implements Initializer, Finalizer {

  private static final UtilityManager ourInstance = new UtilityManager();

  public static UtilityManager getInstance() {
    return ourInstance;
  }

  private UtilityManager() {
  }

  @Override
  public void finalizeComponent() {

  }

  @Override
  public void initializeComponent(Vertx vertx, JsonObject config) {
    TenantInitializer.initialize(DataSourceRegistry.getInstance().getDefaultDataSource());
    LanguageValidator.initialize();
  }

}
