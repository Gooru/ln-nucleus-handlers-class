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

    private ProcessorContext(String userId, JsonObject session, JsonObject request, String classId, String courseId,
        String classCode, String studentId, String studentEmail, MultiMap headers) {
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

        ProcessorContext build() {
            if (this.built) {
                throw new IllegalStateException("Tried to build again");
            } else {
                this.built = true;
                return new ProcessorContext(userId, session, request, classId, courseId, classCode, studentId,
                    studentEmail, requestHeaders);
            }
        }
    }

}
