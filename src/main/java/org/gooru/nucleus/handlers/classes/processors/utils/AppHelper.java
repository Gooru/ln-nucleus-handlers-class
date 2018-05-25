package org.gooru.nucleus.handlers.classes.processors.utils;

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

    public static void publishEventForRescope(AJEntityClass entityClass, String accessToken, String classId, String source, String studentId) {
        final String setting = entityClass.getString(AJEntityClass.SETTING);
        final JsonObject classSettings = setting == null ? null : new JsonObject(setting);
        if (classSettings != null && classSettings.getBoolean(AJEntityClass.RESCOPE)) {
            final String authHeader = TOKEN + accessToken;
            final JsonObject data = new JsonObject();
            data.put(CLASS_ID, classId);
            if (studentId != null) {
                JsonArray memberIds = new JsonArray();
                memberIds.add(studentId);
                data.put(MEMBER_IDS, memberIds);
            }
            data.put(SOURCE, source);
            executeHTTPClientPost(data.toString(), authHeader);
        }
    }

    private static void executeHTTPClientPost(String data, String authHeader) {
        try {
            AppHttpClient httpClient = AppHttpClient.getInstance();
            HttpClientRequest eventRequest = httpClient.getHttpClient().post(httpClient.endpoint(), responseHandler -> {
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
            LOGGER.error("error while posting event", t);
        }
    }
}
