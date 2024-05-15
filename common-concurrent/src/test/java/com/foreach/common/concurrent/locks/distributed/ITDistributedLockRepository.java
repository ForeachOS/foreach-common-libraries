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
package com.foreach.common.concurrent.locks.distributed;

import com.foreach.common.concurrent.locks.CloseableObjectLock;
import com.foreach.common.concurrent.locks.ObjectLockRepository;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.foreach.common.concurrent.locks.ExecutorBatch.Status;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = ITDistributedLockRepository.Config.class)
public class ITDistributedLockRepository
{
	private static final int BATCHES = 5;
	private static final int LOCKS_PER_BATCH = 20;
	private static final int EXECUTORS_PER_LOCK = 30;

	private static final AtomicInteger REPOSITORY_COUNTER = new AtomicInteger();

	private final ExecutorService singleThread = Executors.newSingleThreadExecutor();

	@Autowired
	@Qualifier("real")
	private JdbcTemplate realJdbcTemplate;

	@Autowired
	@Qualifier("spy")
	private JdbcTemplate spyJdbcTemplate;

	private Set<SqlBasedDistributedLockManager> lockManagers;

	private SqlBasedDistributedLockConfiguration configuration;

	private final Map<String, Integer> resultsByLock = Collections.synchronizedMap(
			new HashMap<String, Integer>() );

	@Before
	public void setup() {
		configuration = new SqlBasedDistributedLockConfiguration( "test_locks" );
		configuration.setVerifyInterval( 100 );
		configuration.setMaxIdleBeforeSteal( 500 );
		configuration.setRetryInterval( 50 );
		configuration.setCleanupInterval( 100 );
		configuration.setCleanupAge( 60000 );

		resultsByLock.clear();
		reset( spyJdbcTemplate );
		lockManagers = new HashSet<>();
	}

	@After
	public void shutdown() throws InterruptedException {
		for ( SqlBasedDistributedLockManager lockManager : lockManagers ) {
			lockManager.close();
		}
		cleanupTable();
	}

	@Test
	public void testSynchronization() throws Exception {
		int batchSize = BATCHES;
		int totalLocks = LOCKS_PER_BATCH;
		int resultsPerLock = EXECUTORS_PER_LOCK * BATCHES;

		Map<String, Integer> resultValues = new HashMap<>();

		Set<ObjectLockRepository<String>> repositories = new HashSet<>();

		for ( int i = 0; i < batchSize; i++ ) {
			repositories.add( createRepository() );
		}

		int locksBeforeStart = lockCount();

		Collection<Status> results =
				com.foreach.common.concurrent.locks.ExecutorBatch.execute(
						repositories,
						resultValues,
						5,
						LOCKS_PER_BATCH,
						EXECUTORS_PER_LOCK
				);

		assertEquals( batchSize, results.size() );
		for ( Status status : results ) {
			assertEquals( LOCKS_PER_BATCH * EXECUTORS_PER_LOCK, status.getSucceeded() );
		}

		assertEquals( totalLocks + locksBeforeStart, lockCount() );

		// Check synchronization was correct
		assertEquals( totalLocks, resultValues.size() );
		for ( Integer value : resultValues.values() ) {
			assertEquals( Integer.valueOf( resultsPerLock ), value );
		}
	}

	@Test
	public void tryLockShouldReturnImmediately() throws Exception {
		String localRepositoryName = "local-" + REPOSITORY_COUNTER.incrementAndGet();

		DistributedLockRepository lockRepository = createRepository( localRepositoryName );
		DistributedLockRepository otherRepositoryInSameJvm = createRepository( localRepositoryName );
		DistributedLockRepository externalRepository = createRepository(
				"external-" + REPOSITORY_COUNTER.incrementAndGet() );

		final DistributedLock lock = lockRepository.getLock( UUID.randomUUID().toString() );
		final DistributedLock otherLock = otherRepositoryInSameJvm.getLock( lock.getKey() );
		final DistributedLock externalLock = externalRepository.getLock( lock.getKey() );

		boolean locked = lock.tryLock();
		assertTrue( locked );

		final AtomicLong duration = new AtomicLong( 0 );

		// Same lock but from a different thread should fail
		Future<Boolean> sameLockByOtherThreadLocked = singleThread.submit( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception {
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();

				boolean success = lock.tryLock();

				duration.set( stopWatch.getTime() );

				return success;
			}
		} );

		assertFalse( sameLockByOtherThreadLocked.get() );
		assertTrue( duration.get() < 100 );

		// Other lock instances but same thread should work
		assertTrue( otherLock.tryLock() );

		// Other lock instance in another thread should also fail
		Future<Boolean> otherLockByOtherThreadLocked = singleThread.submit( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception {
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();

				boolean success = otherLock.tryLock();

				duration.set( stopWatch.getTime() );

				return success;
			}
		} );

		assertFalse( otherLockByOtherThreadLocked.get() );
		assertTrue( duration.get() < 100 );

		// Same thread but an "external" repository should fail
		assertFalse( externalLock.tryLock() );
	}

	@Test
	public void tryLockWithTimeout() {
		DistributedLockRepository lockRepository = createRepository( "local-" + REPOSITORY_COUNTER.incrementAndGet() );
		DistributedLockRepository externalRepository = createRepository(
				"external-" + REPOSITORY_COUNTER.incrementAndGet() );

		DistributedLock lock = lockRepository.getLock( UUID.randomUUID().toString() );
		DistributedLock externalLock = externalRepository.getLock( lock.getKey() );

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		assertTrue( lock.tryLock( 3, TimeUnit.SECONDS ) );
		assertTrue( stopWatch.getTime() < 150 );

		stopWatch.reset();
		stopWatch.start();

		assertFalse( externalLock.tryLock( 3, TimeUnit.SECONDS ) );
		assertTrue( stopWatch.getTime() >= 3000 );
	}

	@Test
	public void lockIsStolenIfIdleForTooLong() {
		DistributedLockRepository lockRepository = createRepository( "local-" + REPOSITORY_COUNTER.incrementAndGet() );
		DistributedLockRepository externalRepository = createRepository(
				"external-" + REPOSITORY_COUNTER.incrementAndGet() );

		DistributedLock lock = lockRepository.getLock( UUID.randomUUID().toString() );
		DistributedLock externalLock = externalRepository.getLock( lock.getKey() );

		assertTrue( lock.tryLock() );
		assertTrue( lock.isLocked() );
		assertTrue( lock.isHeldByCurrentThread() );

		assertFalse( externalLock.tryLock() );
		assertTrue( externalLock.isLocked() );
		assertFalse( externalLock.isHeldByCurrentThread() );

		updateIdleTime( lock, System.currentTimeMillis() - 30000 );

		assertTrue( externalLock.tryLock() );
		assertTrue( externalLock.isLocked() );
		assertTrue( externalLock.isHeldByCurrentThread() );

		assertFalse( lock.tryLock() );
		assertTrue( lock.isLocked() );
		assertFalse( lock.isHeldByCurrentThread() );
	}

	@Test
	public void stolenLockCallback() {
		DistributedLockRepository lockRepository = createRepository( "local-" + REPOSITORY_COUNTER.incrementAndGet() );

		final DistributedLock lock = lockRepository.createSharedLock( "owner-one", UUID.randomUUID().toString() );
		final DistributedLock otherLock = lockRepository.createSharedLock( "owner-two", lock.getKey() );

		final AtomicBoolean callbackExecuted = new AtomicBoolean( false );

		DistributedLock.LockStolenCallback callback = new DistributedLock.LockStolenCallback()
		{
			@Override
			public void stolen( String lockId, String ownerId, DistributedLock stolenLock ) {
				assertEquals( lock.getKey(), lockId );
				assertEquals( "owner-one", ownerId );
				assertSame( lock, stolenLock );

				callbackExecuted.set( true );
			}
		};

		lock.setStolenCallback( callback );

		assertTrue( lock.tryLock() );
		assertFalse( otherLock.tryLock() );

		updateIdleTime( lock, System.currentTimeMillis() - 30000 );

		assertTrue( otherLock.tryLock() );
		assertFalse( lock.isHeldByCurrentThread() );
		assertTrue( callbackExecuted.get() );
	}

	@Test
	public void lockBecomesUnstableIfDatabaseConnectionIsLost() throws InterruptedException {
		DistributedLockRepository lockRepository = createRepository( "local-" + REPOSITORY_COUNTER.incrementAndGet() );

		final DistributedLock lock = lockRepository.createSharedLock( "owner", UUID.randomUUID().toString() );
		final DistributedLock otherLock = lockRepository.createSharedLock( "otherOwner", lock.getKey() );

		final AtomicBoolean callbackExecuted = new AtomicBoolean( false );

		DistributedLock.LockUnstableCallback callback = new DistributedLock.LockUnstableCallback()
		{
			@Override
			public void unstable( String lockKey,
			                      String ownerId,
			                      DistributedLock unstableLock,
			                      long lastVerified,
			                      Throwable t ) {
				assertEquals( lock.getKey(), lockKey );
				assertEquals( "owner", ownerId );
				assertSame( lock, unstableLock );
				assertTrue( System.currentTimeMillis() - lastVerified > configuration.getVerifyInterval() * 2 );
				assertTrue( t instanceof DistributedLockException );

				callbackExecuted.set( true );
			}

		};

		lock.setUnstableCallback( callback );

		assertTrue( lock.tryLock() );
		assertFalse( otherLock.tryLock() );

		// Disable the queries - make sure they throw an exception
		assertTrue( lock.isHeldByCurrentThread() );
		doThrow( new DataAccessResourceFailureException( "Datasource broken" ) )
				.when( spyJdbcTemplate )
				.update( any( String.class ), any( PreparedStatementSetter.class ) );

		doThrow( new DataAccessResourceFailureException( "Datasource broken" ) )
				.when( spyJdbcTemplate )
				.queryForObject( any( String.class ), any( RowMapper.class ), any( Object[].class ) );

		assertTrue( "Lock still held even though database interactions fail", lock.isHeldByCurrentThread() );

		Thread.sleep( configuration.getVerifyInterval() + 20 );
		assertTrue( "Lock should still be held after single verification failure", lock.isHeldByCurrentThread() );
		assertFalse( "A single verification failure should not have resulted in callback", callbackExecuted.get() );

		Thread.sleep( configuration.getVerifyInterval() * 2 );
		assertTrue(
				"Lock checking is still possible without exception after 2 verification failures",
				lock.isHeldByCurrentThread() );
		assertTrue( "Two subsequent verification failures should have triggered the callback",
		            callbackExecuted.get() );

		Thread.sleep( configuration.getMaxIdleBeforeSteal() - ( 2 * configuration.getVerifyInterval() ) );

		boolean exceptionCaught = false;

		try {
			lock.isHeldByCurrentThread();
		}
		catch ( DistributedLockException dle ) {
			exceptionCaught = true;
		}

		assertTrue(
				"After the steal time has passed the lock hold check should go straight to database and exception should be bubbled as DistributedLockException",
				exceptionCaught );

		// Releasing the lock should work without exception
		lock.unlock();

		// Re-enable the jdbc template
		reset( spyJdbcTemplate );

		assertTrue( "Lock is still held because no other thread has tried to take it, " +
				            "the original thread simply dropped its interest in the lock... allowing other " +
				            "threads to steal the lock if idle time is too long",
		            lock.isHeldByCurrentThread() );

		assertTrue( "Even though the lock is still held, another can now steal it", otherLock.tryLock() );
		assertFalse( lock.isHeldByCurrentThread() );
		assertTrue( otherLock.isHeldByCurrentThread() );
	}

	@Test
	public void lockManagerShouldNotifyLocksInUse() throws InterruptedException {
		DistributedLockRepository lockRepository = createRepository( "local-" + REPOSITORY_COUNTER.incrementAndGet() );
		DistributedLock lock = lockRepository.getLock( UUID.randomUUID().toString() );

		lock.lock();

		long creation = lastUpdated( lock );
		Thread.sleep( configuration.getVerifyInterval() + 20 );

		long updated = lastUpdated( lock );
		assertTrue( updated > creation );

		creation = updated;
		Thread.sleep( configuration.getVerifyInterval() + 20 );

		assertTrue( lastUpdated( lock ) > creation );
	}

	@Test
	public void distributedLockIsReentrant() {
		DistributedLockRepository lockRepository = createRepository();
		DistributedLock lock = lockRepository.getLock( "somelock" );

		lock.lock();

		assertTrue( lock.isHeldByCurrentThread() );

		try (CloseableObjectLock<String> sameLock = lockRepository.lock( "somelock" )) {
			assertTrue( sameLock.isHeldByCurrentThread() );
			assertTrue( lock.isHeldByCurrentThread() );
		}

		assertTrue( lock.isHeldByCurrentThread() );

		try {
			lock.lock();

			assertTrue( lock.isHeldByCurrentThread() );
		}
		finally {
			lock.unlock();
		}

		assertTrue( lock.isHeldByCurrentThread() );

		lock.unlock();

		assertFalse( lock.isLocked() );
		assertFalse( lock.isHeldByCurrentThread() );
	}

	//a variant on the stolenLockCallback callback above, that steals the lock via direct database access, and initially causes some errors during stolen lock verification
	@Test(timeout = 500L)
	public void lockMonitorShouldHandleDatabaseExceptionsGracefully() throws InterruptedException {
		configuration.setVerifyInterval( 10L );

		DistributedLockRepository lockRepository = createRepository( "local-" + REPOSITORY_COUNTER.incrementAndGet() );

		final DistributedLock lock = lockRepository.createSharedLock( "owner-one", UUID.randomUUID().toString() );
		final DistributedLock otherLock = lockRepository.createSharedLock( "owner-two", lock.getKey() );

		final AtomicBoolean callbackExecuted = new AtomicBoolean( false );

		assertEquals( 0, lockCount() );

		DistributedLock.LockStolenCallback callback = new DistributedLock.LockStolenCallback()
		{
			@Override
			public void stolen( String lockId, String ownerId, DistributedLock stolenLock ) {
				assertEquals( lock.getKey(), lockId );
				assertEquals( "owner-one", ownerId );
				assertSame( lock, stolenLock );

				callbackExecuted.set( true );
			}
		};

		lock.setStolenCallback( callback );

		assertTrue( lock.tryLock() );
		assertFalse( otherLock.tryLock() );

		DelegatingJdcbUpdateAnswer answer = new DelegatingJdcbUpdateAnswer( true );

		String sqlVerifyLock = (String) ReflectionTestUtils.getField( lockManagers.iterator().next(), "sqlVerifyLock" );
		assertNotNull( sqlVerifyLock );
		String lockId = lock.getKey();

		doAnswer( answer ).when( spyJdbcTemplate ).update(
				eq( sqlVerifyLock ),
				anyLong(), eq( lockId ), anyString()
		);

		//Thread.sleep( 100 );
		doAnswer( answer ).when( spyJdbcTemplate ).update( anyString(), anyLong(), anyString(), anyString() );

		assertFalse( "otherLock shouldn't be active before we start messing with the data",
		             otherLock.isHeldByCurrentThread() );
		assertTrue( "lock should be active before we start messing with the data", lock.isHeldByCurrentThread() );
		assertFalse( "callback shouldn't be invoked before we start messing with the data", callbackExecuted.get() );

		stealLock( otherLock );

		Thread.sleep( 100L );

		assertFalse( "otherLock shouldn't be active while the monitor can't verify",
		             otherLock.isHeldByCurrentThread() );
		assertTrue( "lock should be active while the monitor can't verify", lock.isHeldByCurrentThread() );
		assertFalse( "callback shouldn't be invoked while the monitor can't verify", callbackExecuted.get() );

		int numberOfLockVerificationAttemptsBeforeItStartsWorkingAgain = answer.getNrAttempts();
		assertTrue(
				"Should have done at least 5 attempts before it starts working again, but did " + numberOfLockVerificationAttemptsBeforeItStartsWorkingAgain,
				numberOfLockVerificationAttemptsBeforeItStartsWorkingAgain > 5 );

		answer.setShouldFail( false );

		Thread.sleep( 100L );

		assertTrue( "otherLock should be active now that the monitor can verify", otherLock.isHeldByCurrentThread() );
		assertFalse( "lock shouldn't be active now that the monitor can verify", lock.isHeldByCurrentThread() );
		assertTrue( "callback should be invoked now that the monitor can verify", callbackExecuted.get() );

		int numberOfLockVerificationAttemptsAfterItStartsWorkingAgain =
				answer.getNrAttempts() - numberOfLockVerificationAttemptsBeforeItStartsWorkingAgain;
		assertEquals( "Should have done 1 attempt after it started working again",
		              numberOfLockVerificationAttemptsAfterItStartsWorkingAgain, 1 );
	}

	@Test
	public void cleanupMonitorShouldHandleDatabaseExceptionsGracefully() throws InterruptedException, ExecutionException {
		configuration.setCleanupInterval( 30 );
		configuration.setVerifyInterval( 15 );

		DistributedLockRepository lockRepository = createRepository( "local-" + REPOSITORY_COUNTER.incrementAndGet() );

		final DistributedLock lock = lockRepository.createSharedLock( "owner", UUID.randomUUID().toString() );
		lock.tryLock();

		DelegatingJdcbUpdateAnswer answer = new DelegatingJdcbUpdateAnswer( true );
		doAnswer( answer ).when( spyJdbcTemplate ).update(
				eq( (String) ReflectionTestUtils.getField( lockManagers.iterator().next(), "sqlCleanup" ) ), anyLong()
		);

		assertEquals( "should have 1 lock before we start messing with the data", 1, lockCount( lock ) );

		releaseLock( lock.getKey(), System.currentTimeMillis() - 2 * configuration.getCleanupAge() );

		Thread.sleep( 300 );

		assertEquals( "should have 1 lock the monitor can't cleanup", 1, lockCount( lock ) );

		int numberOfCleanupAttemptsBeforeItStartsWorkingAgain = answer.getNrAttempts();
		assertTrue(
				"Should have done at least 5 attempts before it starts working again, but did " + numberOfCleanupAttemptsBeforeItStartsWorkingAgain,
				numberOfCleanupAttemptsBeforeItStartsWorkingAgain > 5 );

		answer.setShouldFail( false );

		Thread.sleep( 300 );

		assertEquals( "should have 0 locks now that the monitor can cleanup", 0, lockCount( lock ) );

		int numberOfCleanupAttemptsAfterItStartsWorkingAgain =
				answer.getNrAttempts() - numberOfCleanupAttemptsBeforeItStartsWorkingAgain;
		assertTrue(
				"Should have done at least 5 attempts after it started working again, but did " + numberOfCleanupAttemptsAfterItStartsWorkingAgain,
				numberOfCleanupAttemptsAfterItStartsWorkingAgain > 5 );
	}

	private class DelegatingJdcbUpdateAnswer implements Answer<Integer>
	{
		private int nrAttempts = 0;
		private boolean shouldFail;

		public DelegatingJdcbUpdateAnswer( boolean shouldFail ) {
			setShouldFail( shouldFail );
		}

		public void setShouldFail( boolean shouldFail ) {
			this.shouldFail = shouldFail;
		}

		public int getNrAttempts() {
			return nrAttempts;
		}

		@Override
		public Integer answer( InvocationOnMock invocationOnMock ) throws Throwable {
			nrAttempts++;
			if ( shouldFail ) {
				throw new RuntimeException();
			}
			final String query = (String) invocationOnMock.getArguments()[0];
			final Object[] args =
					Arrays.copyOfRange( invocationOnMock.getArguments(), 1, invocationOnMock.getArguments().length );
			return realJdbcTemplate.update( query, args );
		}
	}

	private void stealLock( DistributedLock lock ) {
		realJdbcTemplate.update( "UPDATE test_locks SET owner_id = ?, updated = ? WHERE lock_id = ?", lock.getOwnerId(),
		                         System.currentTimeMillis(), lock.getKey() );
	}

	private void releaseLock( String lockId, long lastUpdated ) {
		realJdbcTemplate.update( "UPDATE test_locks SET owner_id = NULL, updated = ? WHERE lock_id = ?", lastUpdated,
		                         lockId );
	}

	private void updateIdleTime( DistributedLock lock, long updated ) {
		realJdbcTemplate.update( "UPDATE test_locks SET updated = ? WHERE lock_id = ?", updated, lock.getKey() );
	}

	private int lockCount() {
		return realJdbcTemplate.queryForObject( "SELECT count(*) FROM test_locks", Integer.class );
	}

	private int lockCount( DistributedLock lock ) {
		return realJdbcTemplate.queryForObject( "SELECT count(*) FROM test_locks where lock_id = ?", Integer.class,
		                                        lock.getKey() );
	}

	private long lastUpdated( DistributedLock lock ) {
		return realJdbcTemplate.queryForObject( "SELECT updated FROM test_locks WHERE lock_id = ?", Long.class,
		                                        lock.getKey() );
	}

	private void cleanupTable() {
		realJdbcTemplate.update( "TRUNCATE TABLE test_locks" );
	}

	private DistributedLockRepository createRepository() {
		return createRepository( "local" );
	}

	private DistributedLockRepository createRepository( String defaultOwnerName ) {
		SqlBasedDistributedLockManager lockManager =
				new SqlBasedDistributedLockManager( spyJdbcTemplate, configuration );
		lockManagers.add( lockManager );

		return new DistributedLockRepositoryImpl( lockManager,
		                                          defaultOwnerName );
	}

	@Configuration
	protected static class Config
	{
		@Bean
		public DataSource dataSource() {
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
			dataSource.setUrl( "jdbc:hsqldb:mem:/hsql-mem/distributed-lock" );
			dataSource.setUsername( "sa" );
			dataSource.setPassword( "" );

			return dataSource;
		}

		@Bean(name = "real")
		public JdbcTemplate realJdbcTemplate() {
			return new JdbcTemplate( dataSource() );
		}

		@Bean(name = "spy")
		public JdbcTemplate spyJdbcTemplate() {
			return spy( new JdbcTemplate( dataSource() ) );
		}

		@Bean
		public SpringLiquibase createSchema() {
			SpringLiquibase springLiquibase = new SpringLiquibase();
			springLiquibase.setDataSource( dataSource() );
			springLiquibase.setChangeLog(
					"classpath:TestSchemaDistributedLocking.xml" );

			return springLiquibase;
		}
	}
}
