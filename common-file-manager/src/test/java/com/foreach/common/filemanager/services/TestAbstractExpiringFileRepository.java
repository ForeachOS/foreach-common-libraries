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
package com.foreach.common.filemanager.services;

import com.foreach.common.filemanager.services.AbstractExpiringFileRepository;
import com.foreach.common.filemanager.services.ExpiringFileResource;
import com.foreach.common.filemanager.services.FileManagerImpl;
import com.foreach.common.filemanager.services.FileRepository;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static com.foreach.common.filemanager.services.AbstractExpiringFileRepository.timeBasedExpirationStrategy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
class TestAbstractExpiringFileRepository
{
	@Test
	void timeBasedExpiration() {
		assertExpires( 0, 0, -1000, -2000 ).isFalse();
		assertExpires( 1000, 0, -2000, -2000 ).isTrue();
		assertExpires( 1000, 0, -100, -2000 ).isFalse();

		assertExpires( 0, 1000, -2000, -2000 ).isTrue();
		assertExpires( 0, 1000, -2000, -100 ).isFalse();
		assertExpires( 0, 1000, -2000, 0 ).isFalse();

		assertExpires( 1000, 1000, -2000, -2000 ).isTrue();
	}

	private AbstractBooleanAssert<?> assertExpires( long maxUnusedDuration, long maxAge, long lastAccessTime, long cacheCreationTime ) {
		Function<ExpiringFileResource, Boolean> expirationStrategy = timeBasedExpirationStrategy( maxUnusedDuration, maxAge );

		ExpiringFileResource resource = mock( ExpiringFileResource.class, withSettings().lenient() );
		when( resource.getLastAccessTime() ).thenReturn( System.currentTimeMillis() + lastAccessTime );
		when( resource.getCreationTime() ).thenReturn( System.currentTimeMillis() + cacheCreationTime );

		return assertThat( expirationStrategy.apply( resource ) );
	}

	@Test
	void expireTrackedItemsInRegistry() {
		FileManagerImpl fileManager = new FileManagerImpl();
		AbstractExpiringFileRepository cachingOne = mock( AbstractExpiringFileRepository.class );
		when( cachingOne.getRepositoryId() ).thenReturn( "one" );
		AbstractExpiringFileRepository cachingTwo = mock( AbstractExpiringFileRepository.class );
		when( cachingTwo.getRepositoryId() ).thenReturn( "two " );
		FileRepository nonCaching = mock( FileRepository.class );
		when( nonCaching.getRepositoryId() ).thenReturn( "three " );

		fileManager.registerRepository( cachingOne );
		fileManager.registerRepository( nonCaching );
		fileManager.registerRepository( cachingTwo );

		AbstractExpiringFileRepository.expireTrackedItems( fileManager );

		verify( cachingOne ).expireTrackedItems();
		verify( cachingTwo ).expireTrackedItems();
	}
}
