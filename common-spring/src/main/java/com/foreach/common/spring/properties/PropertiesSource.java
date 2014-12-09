package com.foreach.common.spring.properties;

import java.util.Map;

/**
 * Interface that proxies access to a map of properties.
 * Useful to enforce a TypedPropertyMap to be able to use instances where the underlying map reference changes.
 *
 * @param <T> Type of the map keys.
 */
public interface PropertiesSource<T> {
    /**
     * @return The underlying map of properties.
     */
    Map<T, ?> getProperties();
}
