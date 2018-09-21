package org.gooru.nucleus.handlers.classes.app.components;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.gooru.nucleus.handlers.classes.bootstrap.startup.Initializer;

/**
 * @author ashish.
 */

public class VersionInfo implements Initializer  {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionInfo.class);

    @Override
    public void initializeComponent(Vertx vertx, JsonObject config) {
        InputStream resource = null;
        try {
            resource = getClass().getClassLoader().getResourceAsStream("version.properties");
            if (resource != null) {
                String version = readFromInputStream(resource);
                LOGGER.info("Versioning Information: ");
                LOGGER.info("\n" + version);
            } else {
                LOGGER.warn("No versioning information found");
            }
        } catch (IOException e) {
            LOGGER.warn("Not able to get versioning information");
            e.printStackTrace();
        } finally {
            if (resource != null) {
                try {
                    resource.close();
                } catch (IOException e) {
                    LOGGER.warn("Not able to close stream for versioning information.", e);
                }
            }
        }
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
