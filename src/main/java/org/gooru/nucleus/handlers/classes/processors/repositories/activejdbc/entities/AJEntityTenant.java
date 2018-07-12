package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import java.util.Objects;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("tenant")
public class AJEntityTenant extends Model {

    public static final String CONTENT_VISIBILITY = "content_visibility";
    
    public static final String SELECT_TENANT = "id = ?::uuid and status = 'active'";
    
    private static final String CONTENT_VISIBILITY_GLOBAL = "global";

    private static final String CONTENT_VISIBILITY_TENANT = "tenant";

    public String getContentVisibility() {
        return this.getString(CONTENT_VISIBILITY);
    }
    
    public boolean isContentVisibilityGlobal() {
        return Objects.equals(this.getString(CONTENT_VISIBILITY), CONTENT_VISIBILITY_GLOBAL);
    }

    public boolean isContentVisibilityTenant() {
        return Objects.equals(this.getString(CONTENT_VISIBILITY), CONTENT_VISIBILITY_TENANT);
    }
}
