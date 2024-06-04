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
package com.foreach.common.concurrent.locks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestObjectLockRepository
{
	private ObjectLockRepository<String> repository;

	@BeforeEach
	public void setUp() {
		repository = new ReentrantObjectLockRepository<>();
	}

	@Test
	public void sameLockInstanceShouldBeReturned() {
		ObjectLock<String> lock = repository.getLock( "1" );

		assertNotNull( lock );
		assertEquals( "1", lock.getKey() );

		ObjectLock<String> other = repository.getLock( "1" );
		assertSame( lock, other );

		ObjectLockRepository<String> otherRepository = new ReentrantObjectLockRepository<String>();
		ObjectLock<String> fromOtherRepo = otherRepository.getLock( "1" );
		assertNotSame( lock, fromOtherRepo );

		assertNotNull( fromOtherRepo );
		assertEquals( "1", fromOtherRepo.getKey() );

		assertSame( fromOtherRepo, otherRepository.getLock( "1" ) );
	}

	@Test
	public void afterGCADifferentLockInstanceShouldBeReturned() {
		ObjectLock<String> lock = fetch( "1" );

		assertNotNull( lock );
		int hashOne = System.identityHashCode( lock );

		gc();

		// Garbage collection with a hard reference is fine
		lock = fetch( "1" );
		assertNotNull( lock );
		assertEquals( hashOne, System.identityHashCode( lock ) );

		// Remove the hard reference
		lock = null;

		gc();

		lock = fetch( "1" );
		assertNotNull( lock );
		assertNotEquals( hashOne, System.identityHashCode( lock ) );
	}

	@Test
	public void internalCheckThatGCRemovesTheLockKey() {
		assertTrue( internalLocksMap().isEmpty() );

		ObjectLock<String> lock = fetch( "1" );
		assertFalse( internalLocksMap().isEmpty() );

		gc();

		assertFalse( internalLocksMap().isEmpty() );

		lock = null;

		gc();

		assertTrue( internalLocksMap().isEmpty() );
	}

	/**
	 * Method that ensures that the key is called as a new reference.
	 * Necessary to verify garbage collection and weak reference behavior.
	 */
	@SuppressWarnings("all")
	private ObjectLock<String> fetch( String key ) {
		return repository.getLock( new String( key ) );
	}

	private void gc() {
		Runtime runtime = Runtime.getRuntime();

		runtime.gc();
		runtime.runFinalization();
	}

	private Map internalLocksMap() {
		try {
			Field fld = ReentrantObjectLockRepository.class.getDeclaredField( "locks" );
			fld.setAccessible( true );

			return (WeakHashMap) fld.get( repository );
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	@Test
	public void lockIsReentrantAndCanBeUsedAgain() {
		ObjectLock<String> one = repository.getLock( "1" );
		one.lock();
		one.lock();

		assertTrue( one.isLocked() );
		assertTrue( one.isHeldByCurrentThread() );

		one.unlock();

		assertTrue( one.isLocked() );
		assertTrue( one.isHeldByCurrentThread() );

		one.unlock();

		assertFalse( one.isLocked() );
		assertFalse( one.isHeldByCurrentThread() );

		one.lock();

		assertTrue( one.isLocked() );
		assertTrue( one.isHeldByCurrentThread() );

		one.unlock();
	}

	@Test
	public void autoClosableLocking() {
		ObjectLock<String> otherLock = repository.getLock( "123" );
		assertFalse( otherLock.isHeldByCurrentThread() );

		try (CloseableObjectLock<String> lock = repository.lock( "123" )) {
			assertTrue( lock.isHeldByCurrentThread() );
			assertTrue( otherLock.isHeldByCurrentThread() );
		}

		assertFalse( otherLock.isHeldByCurrentThread() );
	}

	@Test
	public void multiThreadLocking() throws Exception {
		// Execute thread batches
		executeBatches();

		// After a GC, locks should be empty
		gc();
		assertTrue( internalLocksMap().isEmpty() );
	}

	private void executeBatches() throws Exception {
		Map<String, Integer> resultValues = new HashMap<>();

		Collection<ExecutorBatch.Status> results = ExecutorBatch.execute(
				Arrays.asList( repository ),
				resultValues,
				5,
				5,
				300
		);

		for ( ExecutorBatch.Status status : results ) {
			assertEquals( 5 * 300, status.getSucceeded() );
			System.out.println( status.getAverageDuration() );
		}

		assertEquals( 5, resultValues.size() );
		for ( Integer resultValue : resultValues.values() ) {
			assertEquals( Integer.valueOf( 300 ), resultValue );
		}
	}
}
