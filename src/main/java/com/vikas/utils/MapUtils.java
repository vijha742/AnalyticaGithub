package com.vikas.utils;

import java.util.Map;

public class MapUtils {

    /**
     * Extracts an integer value from a map with a default value if the key is not found or the value is not a Number.
     *
     * @param map The map to extract from
     * @param key The key to look up
     * @param defaultValue The default value to return if key not found or value is not a Number
     * @return The integer value or the default value
     */
    public static int extractIntFromMap(Map<String, Object> map, String key, int defaultValue) {
        if (map == null || key == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * Extracts a string value from a map with a default value if the key is not found or the value is not a String.
     *
     * @param map The map to extract from
     * @param key The key to look up
     * @param defaultValue The default value to return if key not found or value is not a String
     * @return The string value or the default value
     */
    public static String extractStringFromMap(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || key == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    /**
     * Extracts an integer from a nested map (e.g., followers.totalCount).
     *
     * @param map The parent map
     * @param nestedKey The key to get the nested map
     * @param key The key within the nested map
     * @param defaultValue The default value
     * @return The integer value or the default value
     */
    public static int extractIntFromNestedMap(Map<String, Object> map, String nestedKey, String key, int defaultValue) {
        if (map == null || nestedKey == null) {
            return defaultValue;
        }
        Object nestedObj = map.get(nestedKey);
        if (nestedObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedMap = (Map<String, Object>) nestedObj;
            return extractIntFromMap(nestedMap, key, defaultValue);
        }
        return defaultValue;
    }
}
