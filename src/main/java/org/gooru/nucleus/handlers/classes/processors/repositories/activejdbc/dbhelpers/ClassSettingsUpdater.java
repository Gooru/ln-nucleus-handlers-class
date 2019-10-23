
package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.dbhelpers;

import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On 19-Sep-2019
 */
public final class ClassSettingsUpdater {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClassSettingsUpdater.class);

  public static void updateSetting(JsonObject setting, AJEntityClass model, JsonObject request) {

    LOGGER.debug("updating class setting via settings updater '{}'", setting.toString());

    // If there is no setting received in request then no action.
    JsonObject settingsFromRequest = request.getJsonObject(AJEntityClass.SETTING, null);
    if (settingsFromRequest == null || settingsFromRequest.isEmpty()) {
      LOGGER.debug("no settings provided in request");
      return;
    }

    // If the secondary classes is present in the request payload, then set it for update if not
    // null. If it is null then remove the key from the existing setting if present.
    if (settingsFromRequest.containsKey(AJEntityClass.SECONDARY_CLASSES)) {
      JsonObject secondaryClasses =
          settingsFromRequest.getJsonObject(AJEntityClass.SECONDARY_CLASSES);
      // If secondary classes in request is null and the key is present in existing setting, then
      // remove it from existing setting
      if (secondaryClasses == null && setting.containsKey(AJEntityClass.SECONDARY_CLASSES)) {
        setting.remove(AJEntityClass.SECONDARY_CLASSES);
      } else {
        setting.put(AJEntityClass.SECONDARY_CLASSES, secondaryClasses);
      }
    }

    // If class default view key is present in the request payload then add/update it in existing
    // setting
    if (settingsFromRequest.containsKey(AJEntityClass.CLASS_DEFAULT_VIEW)) {
      setting.put(AJEntityClass.CLASS_DEFAULT_VIEW,
          settingsFromRequest.getString(AJEntityClass.CLASS_DEFAULT_VIEW));
    }

    // If content add default view key is present in the request payload then add/update it in
    // existing setting
    if (settingsFromRequest.containsKey(AJEntityClass.CONTENT_ADD_DEFAULT_VIEW)) {
      setting.put(AJEntityClass.CONTENT_ADD_DEFAULT_VIEW,
          settingsFromRequest.getString(AJEntityClass.CONTENT_ADD_DEFAULT_VIEW));
    }
    
    // If mastery applicable key is present in the request payload then add/update it in existing
    // setting
    if (settingsFromRequest.containsKey(AJEntityClass.MASTERY_APPLICABLE)) {
      setting.put(AJEntityClass.MASTERY_APPLICABLE,
          settingsFromRequest.getString(AJEntityClass.MASTERY_APPLICABLE));
    }

    LOGGER.debug("updating setting to '{}'", setting.toString());
    model.setClassSettings(setting);
  }

}
