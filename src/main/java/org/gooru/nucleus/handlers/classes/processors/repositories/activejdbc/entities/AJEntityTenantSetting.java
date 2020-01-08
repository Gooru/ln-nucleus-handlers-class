package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;


import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("tenant_setting")
public class AJEntityTenantSetting extends Model {
  public static final String KEY = "key";
  public static final String VALUE = "value";
  public static final String VALUE_ON = "on";
  public static final String MULI_GRADE_KEY = "allow_multi_grade_class";
  public static final String SELECT_TENANT_SETTING_BY_ID = "id = ?::uuid and key = ?";
}
