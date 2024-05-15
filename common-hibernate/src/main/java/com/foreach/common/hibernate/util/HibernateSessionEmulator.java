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
package com.foreach.common.hibernate.util;

import jakarta.persistence.FlushModeType;
import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This class will simulate the open session in view patterns(OpenSessionInViewFilter).
 * The methods in this class can be used in Tasks which needs to perform transactional events in hibernate.
 */
public final class HibernateSessionEmulator
{
	private HibernateSessionEmulator() {
		// utility classes should not have public/default constructor, so provide default private constructor
	}

	/**
	 * Begins a hibernate request by creating new hibernate session with flush mode set to AUTO
	 *
	 * @param sessionFactory
	 */
	public static void beginRequest( SessionFactory sessionFactory ) {
		beginRequest( sessionFactory, false );
	}

	/**
	 * Begins a hibernate request by creating new hibernate session with flush mode set by the parameter setFlushModeToManual
	 *
	 * @param sessionFactory
	 * @param setFlushModeToManual true - for FlushMode.AUTO, false - FlushMode.MANUAL
	 */
	public static void beginRequest( SessionFactory sessionFactory, boolean setFlushModeToManual ) {
		// Bind single session
		Session session = sessionFactory.openSession();

		if ( setFlushModeToManual ) {
			session.setFlushMode( FlushModeType.COMMIT );
		}

		if ( !TransactionSynchronizationManager.hasResource( sessionFactory ) ) {
			TransactionSynchronizationManager.bindResource( sessionFactory, new SessionHolder( session ) );
		}
	}

	/**
	 * Ends a hibernate request and closes the hibernate session.
	 * This method doesn't do explicit flushing of data, hibernate will handle the flushing.
	 *
	 * @param sessionFactory
	 */
	public static void endRequest( SessionFactory sessionFactory ) {
		endRequest( sessionFactory, false );
	}

	/**
	 * Ends a hibernate request and closes the hibernate session.
	 * This method flush the data based on the given doNotFlush flag.
	 *
	 * @param sessionFactory
	 * @param doNotFlush     true - no flushing is done, false - data is flushed explicitly
	 */
	public static void endRequest( SessionFactory sessionFactory, boolean doNotFlush ) {
		SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource( sessionFactory );
		if ( holder != null ) {
			Session session = holder.getSession();

			if ( !doNotFlush ) {
				session.flush();
			}

			TransactionSynchronizationManager.unbindResource( sessionFactory );

			// we ran into session is closed problem when we execute editor queries inside mme transaction(readonly=false)
			// the editor session is registered in TransactionSynchronizationManager.synchronizations when we do factory.getCurrentSession call
			// during commit of mme transaction, the editor session is also commited as it is registered in synchronizations of TransactionSynchronizationManager
			// to commit editor session, the session should be open, so we dont close the editor session if the transaction is active
			// when the http request ends, this editor session will also be closed by OpenSessionInViewFilter
			if ( !TransactionSynchronizationManager.isActualTransactionActive() ) {
				SessionFactoryUtils.closeSession( session );
			}
		}
	}

	/**
	 * Represents a caching strategy. The cache process synchronizes
	 * database state with session state by detecting state changes
	 * and executing SQL statements.
	 *
	 * @param sessionFactory
	 * @param cacheMode
	 */
	public static void setCacheMode( SessionFactory sessionFactory, CacheMode cacheMode ) {
		SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource( sessionFactory );
		if ( holder != null ) {
			holder.getSession().setCacheMode( cacheMode );
		}
	}
}
