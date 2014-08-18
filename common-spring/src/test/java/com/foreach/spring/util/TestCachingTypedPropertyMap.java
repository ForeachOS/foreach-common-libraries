package com.foreach.spring.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = TestTypedPropertyMap.Config.class )
@DirtiesContext
public class TestCachingTypedPropertyMap extends TestTypedPropertyMap {
    @Override
    protected TypedPropertyMap<String> createMap() {
        return new CachingTypedPropertyMap<String>( registry, conversionService, source, String.class );
    }

    @Override
    public void valueChangedDirectlyInSourceMap() {
        // In this case the old value should be returned
        registry.register( "number", Integer.class );

        Integer number = map.getValue( "number" );
        assertEquals( 1981561, number.intValue() );
        assertEquals( number, map.get( "number" ) );

        source.remove( "number" );
        number = map.getValue( "number" );
        assertEquals( 1981561, number.intValue() );
        assertEquals( number, map.get( "number" ) );

        source.put( "number", "100" );
        number = map.getValue( "number" );
        assertEquals( 1981561, number.intValue() );
        assertEquals( number, map.get( "number" ) );
    }

    @Test
    public void aRefreshClearsTheCache() {
        registry.register( "number", Integer.class );

        Integer number = map.getValue( "number" );
        assertEquals( 1981561, number.intValue() );
        assertEquals( number, map.get( "number" ) );

        source.remove( "number" );
        number = map.getValue( "number" );
        assertEquals( 1981561, number.intValue() );
        assertEquals( number, map.get( "number" ) );

        (( CachingTypedPropertyMap ) map).refresh();
        assertNull( map.getValue( "number" ) );

        source.put( "number", "100" );
        assertNull( map.getValue( "number" ) );
        (( CachingTypedPropertyMap ) map).refresh();

        number = map.getValue( "number" );
        assertEquals( 100, number.intValue() );
        assertEquals( number, map.get( "number" ) );
    }
}
