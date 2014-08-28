package com.foreach.common.spring.util;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>Maps defined properties by name to the class they are supposed to be.
 * If no property is defined with that name, the registry will return the default class.</p>
 * <p>A PropertyDefinitionRegistry is used as the configuration for a TypedPropertyMap.</p>
 */
public class PropertyTypeRegistry<T> {
    private Class classForUnknownProperties = String.class;

    private final Map<T, PropertyTypeRecord> definitions = new TreeMap<T, PropertyTypeRecord>();

    private static class PropertyTypeRecord {
        private final Class propertyType;
        private final Object defaultValue;

        private PropertyTypeRecord( Class propertyType, Object defaultValue ) {
            this.propertyType = propertyType;
            this.defaultValue = defaultValue;
        }

        public Class getPropertyType() {
            return propertyType;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }

    public PropertyTypeRegistry() {
    }

    public PropertyTypeRegistry( Class classForUnknownProperties ) {
        this.classForUnknownProperties = classForUnknownProperties;
    }

    public void register( T propertyKey, Class propertyClass ) {
        register( propertyKey, propertyClass, null );
    }

    public <A> void register( T propertyKey, Class<A> propertyClass, A propertyValue ) {
        definitions.put( propertyKey, new PropertyTypeRecord( propertyClass, propertyValue ) );
    }

    public void unregister( T propertyKey ) {
        definitions.remove( propertyKey );
    }

    public Class getClassForProperty( T propertyKey ) {
        PropertyTypeRecord actual = definitions.get( propertyKey );

        if( actual != null ) {
            return actual.getPropertyType();
        }

        return getClassForUnknownProperties();
    }

    public Object getDefaultValueForProperty( T propertyKey ) {
        PropertyTypeRecord actual = definitions.get( propertyKey );

        if( actual != null ) {
            return actual.getDefaultValue();
        }

        return null;
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

    public Collection<T> getRegisteredProperties() {
        return definitions.keySet();
    }

    public boolean isEmpty() {
        return definitions.isEmpty();
    }

    public void clear() {
        definitions.clear();
    }
}
