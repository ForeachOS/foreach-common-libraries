package com.foreach.common.spring.properties.support;

import com.foreach.common.spring.properties.PropertiesSource;

import java.util.Map;

/**
 * Simple PropertieSource that proxies access to an underlying Map implementation.
 *
 * @param <T> Type of the map keys.
 */
public class DirectPropertiesSource<T> implements PropertiesSource<T>
{
    private final Map<T, ?> map;

    public DirectPropertiesSource( Map<T, ?> map ) {
        this.map = map;
    }

    public Map<T, ?> getProperties() {
        return map;
    }
}
