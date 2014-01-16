package com.foreach.spring.util;

import org.springframework.core.convert.ConversionService;

import java.util.*;

/**
 * <p>A TypedPropertyMap is proxy for a source list of key/value pairs, that allows getting and setting
 * of properties in a strongly typed fashion.  Usually the source is a simple map with both String key and value.</p>
 * <p>For this to be possible, the properties must be defined.  Meaning that every property key must
 * have an associated strong typed to map to.</p>
 * <p>Additionally a ConversionService must be present that can perform the type conversion for every property.</p>
 * <p><strong>Note:</strong> the standard implementation does not cache the fetched values and will always read from
 * the backing source directly.  See also {@link com.foreach.spring.util.CachingTypedPropertyMap}.</p>
 * <p>This class also implements the Map interface to facilitate use in JSP/JSTL.  Note that the semantics for the
 * general Map interface might be somewhat dodgy and slower than a regular map.</p>
 *
 * @param <T> The type of the property key (most often String).
 * @see com.foreach.spring.util.CachingTypedPropertyMap
 * @see com.foreach.spring.util.PropertyTypeRegistry
 * @see com.foreach.spring.util.PropertiesSource
 * @see org.springframework.core.convert.ConversionService
 */
public class TypedPropertyMap<T> implements Map<T, Object> {
    private final PropertyTypeRegistry<T> propertyTypeRegistry;
    private final ConversionService conversionService;
    private final Class sourceValueClass;

    private final PropertiesSource source;

    /**
     * Construct a new TypedPropertyMap.
     *
     * @param propertyTypeRegistry Registry that contains the property keys with their corresponding type.
     * @param conversionService    ConversionService implementation that will be used to convert types.
     * @param source               Backing source map containing the stored values.
     * @param sourceValueClass     Class to use when setting values on the source map.
     */
    public TypedPropertyMap( PropertyTypeRegistry<T> propertyTypeRegistry, ConversionService conversionService,
                             Map<T, ?> source, Class sourceValueClass ) {
        this.propertyTypeRegistry = propertyTypeRegistry;
        this.conversionService = conversionService;
        this.source = new DirectPropertiesSource<T>( source );
        this.sourceValueClass = sourceValueClass;
    }

    /**
     * Construct a new TypedPropertyMap.
     *
     * @param propertyTypeRegistry Registry that contains the property keys with their corresponding type.
     * @param conversionService    ConversionService implementation that will be used to convert types.
     * @param source               Backing source proxy containing the stored values.
     * @param sourceValueClass     Class to use when setting values on the source map.
     * @see com.foreach.spring.util.PropertiesSource
     */
    public TypedPropertyMap( PropertyTypeRegistry<T> propertyTypeRegistry, ConversionService conversionService,
                             PropertiesSource source, Class sourceValueClass ) {
        this.propertyTypeRegistry = propertyTypeRegistry;
        this.conversionService = conversionService;
        this.source = source;
        this.sourceValueClass = sourceValueClass;
    }

    /**
     * Allows a strong typed property to be fetched from the source map.  This method will lookup the specific type
     * of the property requested.  Use the conversionService to convert the source value to the typed value, and then
     * case the result to the type requested.
     *
     * @param property Key of the property.
     * @param <O>      Strong type value to return, the must registered type for the property must be able to cast to this type!
     * @return Strong typed instance of the property.
     */
    @SuppressWarnings("unchecked")
    public <O> O getValue( T property ) {
        Class actualType = propertyTypeRegistry.getClassForProperty( property );

        return ( O ) getValue( property, actualType );
    }

    /**
     * Fetches a property from the source map and converts it to the type expected.  This circumvents the registry but
     * forces a conversion to the requested target type.
     *
     * @param property     Key of the property.
     * @param expectedType Type the value should be converted to and will be returned.
     * @param <O>          Strong type value to return.
     * @return Strong typed instance of the property.
     */
    @SuppressWarnings("unchecked")
    public <O> O getValue( T property, Class<O> expectedType ) {
        if( source.getProperties().containsKey( property ) ) {
            Object originalValue = source.getProperties().get( property );

            return conversionService.convert( originalValue, expectedType );
        } else {
            return ( O ) propertyTypeRegistry.getDefaultValueForProperty( property );
        }
    }

    /**
     * Stores the property value in the source map by converting it to the sourceValueClass first.
     *
     * @param property Key of the property.
     * @param value    Strong type value to set the property to.
     */
    @SuppressWarnings("unchecked")
    public Object set( T property, Object value ) {
        Object convertedValue = conversionService.convert( value, sourceValueClass );

        return source.getProperties().put( property, convertedValue );
    }

    public int size() {
        return source.getProperties().size();
    }

    public boolean isEmpty() {
        return source.getProperties().isEmpty();
    }

    public boolean containsKey( Object key ) {
        return source.getProperties().containsKey( key );
    }

    public boolean containsValue( Object value ) {
        return values().contains( value );
    }

    @SuppressWarnings( "unchecked" )
    public Object get( Object key ) {
        return getValue( ( T ) key );
    }

    public Object put( T key, Object value ) {
        return set( key, value );
    }

    public Object remove( Object key ) {
        return source.getProperties().remove( key );
    }

    public void putAll( Map<? extends T, ?> m ) {
        for( Entry<? extends T, ?> entry : m.entrySet() ) {
            put( entry.getKey(), entry.getValue() );
        }
    }

    public void clear() {
        source.getProperties().clear();
    }

    public Set<T> keySet() {
        Set<Entry<T, Object>> entries = entrySet();
        Set<T> list = new HashSet<T>();

        for( Entry<T, Object> entry : entries ) {
            list.add( entry.getKey() );
        }

        return list;
    }

    public Collection<Object> values() {
        Set<Entry<T, Object>> entries = entrySet();
        Collection<Object> list = new ArrayList<Object>( entries.size() );

        for( Entry<T, Object> entry : entries ) {
            list.add( entry.getValue() );
        }

        return list;
    }

    public Set<Entry<T, Object>> entrySet() {
        Set<Entry<T, Object>> entries = new TreeSet<Entry<T, Object>>();

        final TypedPropertyMap<T> myself = this;

        for( final Object key : source.getProperties().keySet() ) {
            entries.add( new Entry<T, Object>() {
                public T getKey() {
                    return ( T ) key;
                }

                public Object getValue() {
                    return myself.getValue( ( T ) key );
                }

                public Object setValue( Object value ) {
                    return myself.set( getKey(), value );
                }
            } );
        }

        return entries;
    }
}
