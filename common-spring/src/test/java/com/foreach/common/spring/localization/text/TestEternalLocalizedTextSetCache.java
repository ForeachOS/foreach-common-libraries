/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.common.spring.localization.text;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestEternalLocalizedTextSetCache
{
	private LocalizedTextSetCache cache;

	@Before
	public void setUp() {
		cache = new EternalLocalizedTextSetCache();
	}

	@Test
	public void putAndGet() {
		assertNull( cache.getLocalizedTextSet( "myapp", "mygroup" ) );

		LocalizedTextSet set = mockTextSet( "myapp", "mygroup" );
		cache.storeLocalizedTextSet( set );

		assertSame( set, cache.getLocalizedTextSet( "myapp", "mygroup" ) );
	}

	@Test
	public void doublePutReplaces() {
		assertNull( cache.getLocalizedTextSet( "myapp", "mygroup" ) );

		LocalizedTextSet set = mockTextSet( "myapp", "mygroup" );
		cache.storeLocalizedTextSet( set );

		LocalizedTextSet other = mockTextSet( "myapp", "mygroup" );
		cache.storeLocalizedTextSet( other );

		assertSame( other, cache.getLocalizedTextSet( "myapp", "mygroup" ) );
	}

	@Test
	public void nullGetDoesntBreak() {
		assertNull( cache.getLocalizedTextSet( null, null ) );
		assertNull( cache.getLocalizedTextSet( null, "mygroup" ) );
		assertNull( cache.getLocalizedTextSet( "myapp", null ) );
	}

	@Test
	public void nullPutDoesntBreak() {
		cache.storeLocalizedTextSet( null );

		assertNull( cache.getLocalizedTextSet( "myapp", "mygroup" ) );
	}

	@Test
	public void clear() {
		cache.storeLocalizedTextSet( mockTextSet( "myapp", "mygroup" ) );
		assertNotNull( cache.getLocalizedTextSet( "myapp", "mygroup" ) );

		cache.clear();
		assertNull( cache.getLocalizedTextSet( "myapp", "mygroup" ) );
	}

	@Test
	public void reloadAll() {
		LocalizedTextSet first = mockTextSet( "myapp1", "group1" );
		LocalizedTextSet second = mockTextSet( "myapp1", "group2" );

		cache.storeLocalizedTextSet( first );
		cache.storeLocalizedTextSet( second );

		cache.reload();

		verify( first, times( 1 ) ).reload();
		verify( second, times( 1 ) ).reload();
	}

	@Test
	public void reloadSingle() {
		LocalizedTextSet first = mockTextSet( "myapp1", "group1" );
		LocalizedTextSet second = mockTextSet( "myapp1", "group2" );

		cache.storeLocalizedTextSet( first );
		cache.storeLocalizedTextSet( second );

		cache.reload( "myapp1", "group2" );

		verify( first, never() ).reload();
		verify( second, times( 1 ) ).reload();
	}

	@Test
	public void size() {
		assertEquals( 0, cache.size() );

		cache.storeLocalizedTextSet( mockTextSet( "myapp1", "group1" ) );
		assertEquals( 1, cache.size() );

		cache.storeLocalizedTextSet( mockTextSet( "myapp2", "group1" ) );
		assertEquals( 2, cache.size() );

		cache.storeLocalizedTextSet( mockTextSet( "myapp2", "group2" ) );
		assertEquals( 3, cache.size() );

		// Double put
		cache.storeLocalizedTextSet( mockTextSet( "myapp1", "group1" ) );
		assertEquals( 3, cache.size() );

		cache.clear();
		assertEquals( 0, cache.size() );
	}

	@Test
	public void getCachedSets() {
		assertNotNull( cache.getCachedTextSets() );
		assertEquals( 0, cache.getCachedTextSets().size() );

		LocalizedTextSet first = mockTextSet( "myapp1", "group1" );
		LocalizedTextSet second = mockTextSet( "myapp1", "group2" );

		cache.storeLocalizedTextSet( first );
		cache.storeLocalizedTextSet( second );

		Set<LocalizedTextSet> setsInCache = cache.getCachedTextSets();
		assertEquals( 2, setsInCache.size() );
		assertTrue( setsInCache.contains( first ) );
		assertTrue( setsInCache.contains( second ) );
	}

	private LocalizedTextSet mockTextSet( String application, String group ) {
		LocalizedTextSet set = mock( LocalizedTextSet.class );
		when( set.getApplication() ).thenReturn( application );
		when( set.getGroup() ).thenReturn( group );

		return set;
	}
}
