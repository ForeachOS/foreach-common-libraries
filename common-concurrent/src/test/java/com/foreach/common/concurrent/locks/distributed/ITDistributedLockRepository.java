package com.foreach.common.concurrent.locks.distributed;

import com.foreach.common.concurrent.locks.CloseableObjectLock;
import com.foreach.common.concurrent.locks.ObjectLockRepository;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.foreach.common.concurrent.locks.ExecutorBatch.Status;
import static org.junit.Assert.*;

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

	private ExecutorService singleThread = Executors.newSingleThreadExecutor();

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;
	private Set<SqlBasedDistributedLockManager> lockManagers;

	private final Map<String, Integer> resultsByLock = Collections.synchronizedMap(
			new HashMap<String, Integer>() );

	@Before
	public void setup() {
		resultsByLock.clear();

		jdbcTemplate = new JdbcTemplate( dataSource );
		lockManagers = new HashSet<>();
	}

	@After
	public void shutdown() {
		for ( SqlBasedDistributedLockManager lockManager : lockManagers ) {
			lockManager.close();
		}
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
		assertTrue( stopWatch.getTime() < 100 );

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
	public void lockManagerShouldNotifyLocksInUse() throws InterruptedException {
		DistributedLockRepository lockRepository = createRepository( "local-" + REPOSITORY_COUNTER.incrementAndGet() );
		DistributedLock lock = lockRepository.getLock( UUID.randomUUID().toString() );

		lock.lock();

		long creation = lastUpdated( lock );
		Thread.sleep( SqlBasedDistributedLockConfiguration.DEFAULT_VERIFY_INTERVAL + 500 );

		long updated = lastUpdated( lock );
		assertTrue( updated > creation );

		creation = updated;
		Thread.sleep( SqlBasedDistributedLockConfiguration.DEFAULT_VERIFY_INTERVAL + 500 );

		assertTrue( lastUpdated( lock ) > creation );
	}

	@Test
	public void distributedLockIsReentrant() {
		DistributedLockRepository lockRepository = createRepository();
		DistributedLock lock = lockRepository.getLock( "somelock" );

		lock.lock();

		assertTrue( lock.isHeldByCurrentThread() );

		try (CloseableObjectLock sameLock = lockRepository.lock( "somelock" )) {
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

	private void updateIdleTime( DistributedLock lock, long updated ) {
		jdbcTemplate.update( "UPDATE test_locks SET updated = ? WHERE lock_id = ?", updated, lock.getKey() );
	}

	private int lockCount() {
		return jdbcTemplate.queryForObject( "SELECT count(*) FROM test_locks", Integer.class );
	}

	private long lastUpdated( DistributedLock lock ) {
		return jdbcTemplate.queryForObject( "SELECT updated FROM test_locks WHERE lock_id = ?", Long.class,
		                                    lock.getKey() );
	}

	private DistributedLockRepository createRepository() {
		return createRepository( "local" );
	}

	private DistributedLockRepository createRepository( String defaultOwnerName ) {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
		dataSource.setUrl( "jdbc:hsqldb:mem:/hsql-mem/distributed-lock" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );

		SqlBasedDistributedLockConfiguration configuration = new SqlBasedDistributedLockConfiguration( "test_locks" );
		SqlBasedDistributedLockManager lockManager = new SqlBasedDistributedLockManager( dataSource, configuration );
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
