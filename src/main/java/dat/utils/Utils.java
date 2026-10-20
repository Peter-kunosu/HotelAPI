package dat.utils;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dat.security.exceptions.ApiException;
import io.javalin.http.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Purpose: Utility class to read properties from a file
 * Author: Thomas Hartmann
 */
public class Utils {
    public static void main(String[] args) {
        System.out.println(getPropertyValue("db.name", "properties-from-pom.properties"));
    }


    public static String getPropertyValue(String propName, String resourceName) {

        // First try environment variables (Docker / deployment)
        String envValue = System.getenv(propName);

        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        // Otherwise read config.properties (local development)
        try (InputStream is = Utils.class.getClassLoader()
                .getResourceAsStream(resourceName)) {

            if (is == null) {
                throw new ApiException(
                        500,
                        "Configuration file not found: " + resourceName
                                + ". Missing environment variable: " + propName
                );
            }

            Properties prop = new Properties();
            prop.load(is);

            String value = prop.getProperty(propName);

            if (value != null && !value.isBlank()) {
                return value.trim();
            }

            throw new ApiException(
                    500,
                    "Property " + propName + " not found in " + resourceName
            );

        } catch (IOException ex) {
            throw new ApiException(
                    500,
                    "Could not read property " + propName
            );
        }
    }


    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignore unknown properties in JSON
        objectMapper.registerModule(new JavaTimeModule()); // Serialize and deserialize java.time objects
        objectMapper.writer(new DefaultPrettyPrinter());
        return objectMapper;
    }

    public static String convertToJsonMessage(Context ctx, String property, String message) {
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put(property, message);  // Put the message in the map
        msgMap.put("status", String.valueOf(ctx.status()));  // Put the status in the map
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(msgMap);  // Convert the map to JSON
        } catch (Exception e) {
            return "{\"error\": \"Could not convert  message to JSON\"}";
        }
    }
}
