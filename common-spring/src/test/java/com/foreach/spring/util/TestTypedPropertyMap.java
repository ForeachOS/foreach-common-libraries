package com.foreach.spring.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = TestTypedPropertyMap.Config.class )
@DirtiesContext
public class TestTypedPropertyMap {

    @Autowired
    protected ConversionService conversionService;

    protected PropertyTypeRegistry<String> registry;
    protected Map<String, String> source;
    protected TypedPropertyMap<String> map;

    @Before
    public void reset() {
        registry = new PropertyTypeRegistry<String>();

        source = new HashMap<String, String>();
        source.put( "date", "345790800000" );
        source.put( "decimal", "128.50" );
        source.put( "number", "1981561" );
        source.put( "text", "some text" );

        map = createMap();
    }

    protected TypedPropertyMap<String> createMap() {
        return new TypedPropertyMap<String>( registry, conversionService, source, String.class );
    }

    @Test
    public void propertyValuesReturnedAsStringIfNotSpecified() {
        assertEquals( "345790800000", map.getValue( "date" ) );
        assertEquals( "128.50", map.getValue( "decimal" ) );
        assertEquals( "1981561", map.getValue( "number" ) );
        assertEquals( "some text", map.getValue( "text" ) );
    }

    @Test
    public void propertyValuesReturnedAsTypeRegistered() {
        registry.register( "date", Date.class );
        registry.register( "decimal", BigDecimal.class );
        registry.register( "number", Integer.class );
        registry.register( "text", String.class );

        Date date = map.getValue( "date" );
        assertEquals( "1980-12-16", new SimpleDateFormat( "yyyy-MM-dd" ).format( date ) );
        assertEquals( date, map.get( "date" ) );

        BigDecimal decimal = map.getValue( "decimal" );
        assertEquals( new BigDecimal( "128.50" ), decimal );
        assertEquals( decimal, map.get( "decimal" ) );

        Integer number = map.getValue( "number" );
        assertEquals( 1981561, number.intValue() );
        assertEquals( number, map.get( "number" ) );

        String text = map.getValue( "text" );
        assertEquals( "some text", text );
        assertEquals( text, map.get( "text" ) );
    }

    @Test
    public void propertyValuesConvertedOnGet() {
        registry.register( "date", Date.class );
        registry.register( "decimal", BigDecimal.class );
        registry.register( "number", Integer.class );

        assertEquals( Long.valueOf( "345790800000" ), map.getValue( "date", Long.class ) );
        assertEquals( Double.valueOf( "128.50" ), map.getValue( "decimal", Double.class ) );
        assertEquals( "1981561", map.getValue( "number", String.class ) );
    }

    @Test
    public void propertyValuesAreSentToSourceAndAvailableOnRead() {
        registry.register( "date", Date.class );
        registry.register( "decimal", BigDecimal.class );
        registry.register( "number", Integer.class );
        registry.register( "text", String.class );

        map.put( "decimal", new BigDecimal( "333.33" ) );
        map.put( "number", 987654321 );
        map.put( "text", null );
        map.put( "date", new Date( 1286683200000L ) );

        assertEquals( "333.33", source.get( "decimal" ) );
        assertEquals( "987654321", source.get( "number" ) );
        assertEquals( null, source.get( "text" ) );
        assertEquals( "1286683200000", source.get( "date" ) );

        Date date = map.getValue( "date" );
        assertEquals( 1286683200000L, date.getTime() );
        assertEquals( date, map.get( "date" ) );

        BigDecimal decimal = map.getValue( "decimal" );
        assertEquals( new BigDecimal( "333.33" ), decimal );
        assertEquals( decimal, map.get( "decimal" ) );

        Integer number = map.getValue( "number" );
        assertEquals( 987654321, number.intValue() );
        assertEquals( number, map.get( "number" ) );

        String text = map.getValue( "text" );
        assertNull( text );
        assertNull( map.get( "text" ) );
    }

    @Test
    public void valueChangedDirectlyInSourceMap() {
        registry.register( "number", Integer.class );

        Integer number = map.getValue( "number" );
        assertEquals( 1981561, number.intValue() );
        assertEquals( number, map.get( "number" ) );

        source.remove( "number" );
        assertNull( map.getValue( "number" ) );

        source.put( "number", "100" );

        number = map.getValue( "number" );
        assertEquals( 100, number.intValue() );
        assertEquals( number, map.get( "number" ) );
    }

    @Configuration
    public static class Config {
        @Bean
        public ConversionService conversionService() {
            DefaultConversionService service = new DefaultConversionService();
            service.addConverter( new Converter<String, Date>() {
                public Date convert( String source ) {
                    return new Date( Long.valueOf( source ) );
                }
            } );

            service.addConverter( new Converter<Date, String>() {
                public String convert( Date source ) {
                    return "" + source.getTime();
                }
            } );
            return service;
        }
    }
}
