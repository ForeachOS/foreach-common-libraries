package com.foreach.spring.concurrent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TestObjectLockRepository {
    private ObjectLockRepository<Integer> repository;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        repository = new ObjectLockRepository<Integer>();
    }

    @Test
    public void sameLockInstanceShouldBeReturned() {
        ObjectLock<Integer> lock = repository.getLock( 1 );

        assertNotNull( lock );
        assertEquals( Integer.valueOf( 1 ), lock.getKey() );

        ObjectLock<Integer> other = repository.getLock( 1 );
        assertSame( lock, other );

        ObjectLockRepository<String> otherRepository = new ObjectLockRepository<String>();
        ObjectLock<String> fromOtherRepo = otherRepository.getLock( "1" );
        assertNotSame( lock, fromOtherRepo );

        assertNotNull( fromOtherRepo );
        assertEquals( "1", fromOtherRepo.getKey() );

        assertSame( fromOtherRepo, otherRepository.getLock( "1" ) );
    }

    @Test
    public void onlyAfterReleaseShouldLockBeDestroyed() {
        ObjectLock<Integer> one = repository.getLock( 1 );
        assertEquals( 1, repository.size() );

        ObjectLock<Integer> oneAgain = repository.getLock( 1 );
        assertEquals( 1, repository.size() );

        ObjectLock<Integer> two = repository.getLock( 2 );
        assertEquals( 2, repository.size() );

        two.release();
        assertEquals( 1, repository.size() );

        one.lock();
        one.unlock();
        assertEquals( 1, repository.size() );

        one.lock();
        oneAgain.release();
        assertEquals( 1, repository.size() );

        one.release();
        assertEquals( 0, repository.size() );
    }

    @Test
    public void releasedLockCantBeUsedAgain() {
        expectedException.expect( RuntimeException.class );
        expectedException.expectMessage( "ObjectLock has been destroyed" );

        ObjectLock<Integer> one = repository.getLock( 1 );
        one.release();

        one.lock();
    }

    @Test
    public void multiThreadLocking() throws Exception {
        ExecutorService threads = Executors.newFixedThreadPool( 25 );

        final int[] count = new int[5];

        for( int i = 0; i < 500; i++ ) {
            final int index = i % 5;
            threads.submit( new Runnable() {
                public void run() {
                    ObjectLock<Integer> lock = repository.getLock( index );

                    try {
                        lock.lock();

                        // Do something
                        int current = count[index];

                        try {
                            Thread.sleep( 10 );
                        } catch ( InterruptedException ie ) {

                        }

                        count[index] = current + 1;

                    } finally {
                        lock.release();
                    }
                }
            } );
        }

        threads.shutdown();
        threads.awaitTermination( 5, TimeUnit.SECONDS );

        // Counters should add up exactly to
        for( int aCount : count ) {
            assertEquals( 100, aCount );
        }

        assertEquals( 0, repository.size() );
    }
}
