package org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("course")
public class AJEntityCourse extends Model {
    public static final String TENANT = "tenant";
    public static final String TENANT_ROOT = "tenant_root";
    private static final String PUBLISH_STATUS_TYPE_PUBLISHED = "published";
    private static final String PUBLISH_STATUS = "publish_status";

    public static final String SELECT_COURSE_TO_AUTHORIZE =
        "SELECT id, owner_id, collaborator, tenant, tenant_root FROM course WHERE id = ?::uuid AND is_deleted = false AND"
            + " (owner_id = ?::uuid OR collaborator ?? ?)";
   
    public String getTenant() {
        return this.getString(TENANT);
    }

    public String getTenantRoot() {
        return this.getString(TENANT_ROOT);
    }
    
    public boolean isCoursePublished() {
        String publishStatus = this.getString(PUBLISH_STATUS);
        return PUBLISH_STATUS_TYPE_PUBLISHED.equalsIgnoreCase(publishStatus);
    }

}
