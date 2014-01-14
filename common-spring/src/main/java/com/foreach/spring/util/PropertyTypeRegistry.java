package com.foreach.spring.util;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>Maps defined properties by name to the class they are supposed to be.
 * If no property is defined with that name, the registry will return the default class.</p>
 * <p>A PropertyDefinitionRegistry is used as the configuration for a TypedPropertyMap.</p>
 */
public class PropertyTypeRegistry<T> {
    private Class classForUnknownProperties = String.class;

    private final Map<T, Class> definitions = new TreeMap<T, Class>();

    public PropertyTypeRegistry() {
    }

    public PropertyTypeRegistry( Class classForUnknownProperties ) {
        this.classForUnknownProperties = classForUnknownProperties;
    }

    public void register( T propertyKey, Class propertyClass ) {
        definitions.put( propertyKey, propertyClass );
    }

    public void unregister( T propertyKey ) {
        definitions.remove( propertyKey );
    }

    public Class getClassForProperty( T propertyKey ) {
        Class actual = definitions.get( propertyKey );

        if( actual == null ) {
            actual = getClassForUnknownProperties();
        }

        return actual;
    }

    public Class getClassForUnknownProperties() {
        if( classForUnknownProperties == null ) {
            throw new RuntimeException( "No class registered for unknown properties." );
        }
        return classForUnknownProperties;
    }

    public void setClassForUnknownProperties( Class classForUnknownProperties ) {
        this.classForUnknownProperties = classForUnknownProperties;
    }

    public boolean isRegistered( T propertyKey ) {
        return definitions.containsKey( propertyKey );
    }

    public Map<T, Class> getRegisteredProperties() {
        return Collections.unmodifiableMap( definitions );
    }

    public boolean isEmpty() {
        return definitions.isEmpty();
    }

    public void clear() {
        definitions.clear();
    }
}
