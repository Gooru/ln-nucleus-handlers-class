package org.gooru.nucleus.handlers.classes.processors.utils;

import java.util.List;

import org.gooru.nucleus.handlers.classes.app.components.AppHttpClient;
import org.gooru.nucleus.handlers.classes.constants.HttpConstants;
import org.gooru.nucleus.handlers.classes.processors.repositories.activejdbc.entities.AJEntityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public final class AppHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppHelper.class);
    private static final String TOKEN = "Token ";
    private static final String CLASS_ID = "classId";
    private static final String MEMBER_IDS = "memberIds";
    private static final String SOURCE = "source";

    private AppHelper() {
        throw new AssertionError();
    }

    public static void publishEventForRescopeAndRoute0(AJEntityClass entityClass, String accessToken, String classId, String source, String studentId) {
        final String setting = entityClass.getString(AJEntityClass.SETTING);
        final JsonObject classSettings = setting == null ? null : new JsonObject(setting);
        if (classSettings != null && classSettings.containsKey(AJEntityClass.COURSE_PREMIUM)
            && classSettings.getBoolean(AJEntityClass.COURSE_PREMIUM)) {
            final String authHeader = TOKEN + accessToken;
            final JsonObject data = new JsonObject();
            data.put(CLASS_ID, classId);
            if (studentId != null) {
                JsonArray memberIds = new JsonArray();
                memberIds.add(studentId);
                data.put(MEMBER_IDS, memberIds);
            }
            data.put(SOURCE, source);
            postRescopeEvent(data.toString(), authHeader);
            postRoute0Event(data.toString(), authHeader);
        }
    }
    
    public static void doLpBaselineSave(AJEntityClass entityClass, String accessToken, String classId,
        String courseId, List<String> memberIds) {
        final String setting = entityClass.getString(AJEntityClass.SETTING);
        final JsonObject classSettings = setting == null ? null : new JsonObject(setting);
        if (classSettings == null || (classSettings != null && !(classSettings.containsKey(AJEntityClass.COURSE_PREMIUM)
            && classSettings.getBoolean(AJEntityClass.COURSE_PREMIUM)))) {
            final String authHeader = TOKEN + accessToken;
            memberIds.forEach(memberId -> {
                AppHttpClient httpClient = AppHttpClient.getInstance();
                String uri = httpClient.lpbaselineUri() + "?userId=" + memberId + "&classId=" + classId + "&courseId=" + courseId ;
                executeHTTPClientGet(authHeader, httpClient, uri);
            });
        }
    }
    
    private static void postRescopeEvent(String data, String authHeader) {
            AppHttpClient httpClient = AppHttpClient.getInstance();
            String uri = httpClient.rescopeUri();
            executeHTTPClientPost(data, authHeader, httpClient, uri);
    }
    
    private static void postRoute0Event(String data, String authHeader) {
            AppHttpClient httpClient = AppHttpClient.getInstance();
            String uri = httpClient.route0Uri();
            executeHTTPClientPost(data, authHeader, httpClient, uri);
    }

    private static void executeHTTPClientPost(String data, String authHeader, AppHttpClient httpClient, String uri) {
        try {
            HttpClientRequest eventRequest = httpClient.getHttpClient().postAbs(uri, responseHandler -> {
                if (responseHandler.statusCode() == HttpConstants.HttpStatus.SUCCESS.getCode()) {
                    LOGGER.info("event posted successfully");
                } else {
                    LOGGER.warn("event post failed with status code: {}, event data: {}", responseHandler.statusCode(),
                        data);
                }
            });
            eventRequest.putHeader(HttpConstants.HEADER_CONTENT_TYPE, HttpConstants.CONTENT_TYPE_JSON);
            eventRequest.putHeader(HttpConstants.HEADER_CONTENT_LENGTH, String.valueOf(data.getBytes().length));
            eventRequest.putHeader(HttpConstants.HEADER_AUTH, authHeader);
            eventRequest.write(data);
            eventRequest.end();
        } catch (Throwable t) {
            LOGGER.error("error while posting event with uri : {} Exception : {}", uri, t);
        }
    }
    
    private static void executeHTTPClientGet(String authHeader, AppHttpClient httpClient, String uri) {
        try {
            HttpClientRequest eventRequest = httpClient.getHttpClient().getAbs(uri, responseHandler -> {
                if (responseHandler.statusCode() == HttpConstants.HttpStatus.SUCCESS.getCode()) {
                    LOGGER.info("api communication successfull");
                } else {
                    LOGGER.warn("api communication failed with status code: {}", responseHandler.statusCode());
                }
            });
            eventRequest.putHeader(HttpConstants.HEADER_CONTENT_TYPE, HttpConstants.CONTENT_TYPE_JSON);
            eventRequest.putHeader(HttpConstants.HEADER_AUTH, authHeader);
            eventRequest.end();
        } catch (Throwable t) {
            LOGGER.error("error while communication with uri: {} Exception: {}", uri, t);
        }
    }
}
