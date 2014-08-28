package com.foreach.common.spring.util;

import org.springframework.core.convert.ConversionService;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Extends the TypedPropertyMap by caching the values instead of converting all the time.
 * Changes directly in the source map will not be detected unless an explicit {@link #refresh()} is done.
 * Changes through the CachingTypedPropertyMap should not be a problem.
 * </p>
 * <p><strong>Note:</strong> Only call for the registered type will be cached, that means that direct
 * calls to {@link #getValue(Object, Class)} will always use the conversionService.</p>
 *
 * @see com.foreach.common.spring.util.TypedPropertyMap
 */
public class CachingTypedPropertyMap<T> extends TypedPropertyMap<T> {

    private Map<T, Object> cachedValues = new HashMap<T, Object>();

    public CachingTypedPropertyMap( PropertyTypeRegistry<T> propertyTypeRegistry, ConversionService conversionService, Map<T, ?> source, Class sourceValueClass ) {
        super( propertyTypeRegistry, conversionService, source, sourceValueClass );
    }

    @Override
    public <O> O getValue( T property ) {
        if( !cachedValues.containsKey( property ) ) {
            Object value = super.getValue( property );
            cachedValues.put( property, value );
        }

        return ( O ) cachedValues.get( property );
    }

    @Override
    public Object set( T property, Object value ) {
        cachedValues.remove( property );
        return super.set( property, value );
    }

    @Override
    public void clear() {
        cachedValues.clear();
        super.clear();
    }

    @Override
    public Object remove( Object key ) {
        cachedValues.remove( key );
        return super.remove( key );
    }

    /**
     * Clears all cached values, ensuring that on the next request the properties will be converted again.
     */
    public void refresh() {
        cachedValues.clear();
    }

    /**
     * Creates a duplicate of the TypedPropertyMap with its own property source, but the
     * same registry and conversionService.
     *
     * @return Detached duplicate of the current map.
     */
    @Override
    public TypedPropertyMap<T> detach() {
        Map<T, Object> sourceCopy = new HashMap<T, Object>();
        for( Entry<T, Object> entry : entrySet() ) {
            sourceCopy.put( entry.getKey(), entry.getValue() );
        }

        return new CachingTypedPropertyMap<T>( propertyTypeRegistry, conversionService, sourceCopy, sourceValueClass );
    }
}
