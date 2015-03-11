package com.foreach.common.spring.properties.support;

import com.foreach.common.spring.properties.PropertyFactory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * @author Arne Vandamme
 */
public class TestInstancePropertyFactory
{
	@Test
	public void createListInstances() {
		PropertyFactory<String, ArrayList> factory = InstancePropertyFactory.forClass( ArrayList.class );

		List one = factory.create( null, null );
		List two = factory.create( null, null );

		assertNotNull( one );
		assertNotNull( two );
		assertNotSame( one, two );
	}
}
