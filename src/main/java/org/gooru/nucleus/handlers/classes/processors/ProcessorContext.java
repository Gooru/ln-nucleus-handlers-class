package org.gooru.nucleus.handlers.classes.processors;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 7/1/16.
 */
public final class ProcessorContext {

    private final String userId;
    private final JsonObject session;
    private final JsonObject request;
    private final String classId;
    private final String courseId;
    private final String classCode;
    private final String studentId;
    private final String studentEmail;
    private final MultiMap requestHeaders;
    private final TenantContext tenantContext;
    private final String accessToken;

    private ProcessorContext(String userId, JsonObject session, JsonObject request, String classId, String courseId,
        String classCode, String studentId, String studentEmail, MultiMap headers, String accessToken) {
        if (session == null || userId == null || session.isEmpty() || headers == null || headers.isEmpty()) {
            throw new IllegalStateException("Processor Context creation failed because of invalid values");
        }
        this.courseId = courseId;
        this.userId = userId;
        this.session = session.copy();
        this.request = request != null ? request.copy() : null;
        this.classId = classId;
        this.classCode = classCode;
        this.studentEmail = studentEmail;
        this.studentId = studentId;
        this.requestHeaders = headers;
        this.tenantContext = new TenantContext(session);
        this.accessToken = accessToken;
    }

    public String userId() {
        return this.userId;
    }

    public JsonObject session() {
        return this.session.copy();
    }

    public JsonObject request() {
        return this.request;
    }

    public String classId() {
        return this.classId;
    }

    public String courseId() {
        return this.courseId;
    }

    public String classCode() {
        return this.classCode;
    }

    public String studentId() {
        return this.studentId;
    }

    public String studentEmail() {
        return this.studentEmail;
    }

    public MultiMap requestHeaders() {
        return this.requestHeaders;
    }

    public String tenant() {
        return this.tenantContext.tenant();
    }

    public String tenantRoot() {
        return this.tenantContext.tenantRoot();
    }
    
    public String accessToken() {
        return this.accessToken;
    }


    public static class ProcessorContextBuilder {
        private final String userId;
        private final JsonObject session;
        private final JsonObject request;
        private final String classId;
        private final MultiMap requestHeaders;
        private String courseId;
        private String studentId;
        private String studentEmail;
        private final String classCode;
        private boolean built = false;
        private String accessToken;

        ProcessorContextBuilder(String userId, JsonObject session, JsonObject request, String classId, String classCode,
            MultiMap headers) {
            if (session == null || userId == null || session.isEmpty() || headers == null || headers.isEmpty()) {
                throw new IllegalStateException("Processor Context creation failed because of invalid values");
            }
            this.userId = userId;
            this.session = session.copy();
            this.request = request != null ? request.copy() : null;
            this.classId = classId;
            this.classCode = classCode;
            this.requestHeaders = headers;
        }

        ProcessorContextBuilder setCourseId(String courseId) {
            this.courseId = courseId;
            return this;
        }

        ProcessorContextBuilder setStudentId(String studentId) {
            this.studentId = studentId;
            return this;
        }

        ProcessorContextBuilder setStudentEmail(String email) {
            this.studentEmail = email;
            return this;
        }
        
        ProcessorContextBuilder setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        ProcessorContext build() {
            if (this.built) {
                throw new IllegalStateException("Tried to build again");
            } else {
                this.built = true;
                return new ProcessorContext(userId, session, request, classId, courseId, classCode, studentId,
                    studentEmail, requestHeaders, accessToken);
            }
        }
    }

    private static class TenantContext {
        private static final String TENANT = "tenant";
        private static final String TENANT_ID = "tenant_id";
        private static final String TENANT_ROOT = "tenant_root";

        private final String tenantId;
        private final String tenantRoot;

        TenantContext(JsonObject session) {
            JsonObject tenantJson = session.getJsonObject(TENANT);
            if (tenantJson == null || tenantJson.isEmpty()) {
                throw new IllegalStateException("Tenant Context invalid");
            }
            this.tenantId = tenantJson.getString(TENANT_ID);
            if (tenantId == null || tenantId.isEmpty()) {
                throw new IllegalStateException("Tenant Context with invalid tenant");
            }
            this.tenantRoot = tenantJson.getString(TENANT_ROOT);
        }

        public String tenant() {
            return this.tenantId;
        }

        public String tenantRoot() {
            return this.tenantRoot;
        }
    }

}
