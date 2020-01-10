package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;


import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("tenant_setting")
public class AJEntityTenantSetting extends Model {
  public static final String KEY = "key";
  public static final String VALUE = "value";
  public static final String VALUE_ON = "on";
  public static final String KEY_ALLOW_MULTI_GRADE_CLASS = "allow_multi_grade_class";
  public static final String FETCH_TENANT_SETTING_BY_ID_AND_KEY = "id = ?::uuid and key = ?";
}
