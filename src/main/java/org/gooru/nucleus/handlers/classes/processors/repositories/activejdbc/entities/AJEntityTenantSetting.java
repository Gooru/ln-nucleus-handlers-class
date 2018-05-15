package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("tenant_setting")
public class AJEntityTenantSetting extends Model {

    public static final String VALUE = "value";
    public static final String TENANT_CLASS_SETTING = "id = ?::uuid and key = 'class_setting'";
}
