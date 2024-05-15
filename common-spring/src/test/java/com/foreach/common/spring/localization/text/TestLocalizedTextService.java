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

import com.foreach.common.spring.localization.AbstractLocalizationTest;
import com.foreach.common.spring.localization.LanguageConfigurator;
import com.foreach.common.spring.localization.MyLanguage;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestLocalizedTextService extends AbstractLocalizationTest
{
	private LocalizedTextDataStore textDao;
	private AbstractLocalizedTextService textService;

	@Before
	public void setUp() {
		textDao = mock( LocalizedTextDataStore.class );

		textService = new LocalizedTextServiceImpl( textDao );
	}

	@Test
	public void createTextSet() {
		LocalizedText existing = new LocalizedText();
		existing.setLabel( "existing" );
		existing.setUsed( true );

		when( textDao.getLocalizedTextForGroup( "myapp", "mygroup" ) ).thenReturn( Arrays.asList( existing ) );

		LocalizedTextSet textSet = textService.getLocalizedTextSet( "myapp", "mygroup" );

		assertTrue( textSet instanceof LocalizedTextSetImpl );
		assertNotNull( textSet );
		assertEquals( 1, textSet.size() );
		assertEquals( "myapp", textSet.getApplication() );
		assertEquals( "mygroup", textSet.getGroup() );
		assertSame( textService, ( (LocalizedTextSetImpl) textSet ).getLocalizedTextService() );
		assertTrue( textSet.exists( "existing" ) );
	}

	@Test
	public void textSetShouldComeFromCacheIfPossible() {
		LocalizedTextSetCache cache = mock( LocalizedTextSetCache.class );
		LocalizedTextSet expected = mock( LocalizedTextSet.class );

		textService.setTextSetCache( cache );

		when( cache.getLocalizedTextSet( "myapp", "mygroup" ) ).thenReturn( expected );

		LocalizedTextSet textSet = textService.getLocalizedTextSet( "myapp", "mygroup" );
		assertSame( expected, textSet );

		verifyNoInteractions( textDao );
	}

	@Test
	public void textSetShouldBeStoredInCacheIfCreated() {
		LocalizedTextSetCache cache = mock( LocalizedTextSetCache.class );
		textService.setTextSetCache( cache );

		LocalizedText existing = new LocalizedText();
		existing.setLabel( "existing" );
		existing.setUsed( true );

		when( textDao.getLocalizedTextForGroup( "myapp", "mygroup" ) ).thenReturn( Arrays.asList( existing ) );

		LocalizedTextSet textSet = textService.getLocalizedTextSet( "myapp", "mygroup" );

		verify( cache, times( 1 ) ).storeLocalizedTextSet( textSet );
	}

	@Test
	public void textSetFetchingShouldBeQueuedFromMultipleThreads() throws InterruptedException {
		LocalizedTextSetCache cache = new LocalizedTextSetCache()
		{
			private LocalizedTextSet saved = null;

			public LocalizedTextSet getLocalizedTextSet( String application, String group ) {
				return saved;
			}

			public void storeLocalizedTextSet( LocalizedTextSet textSet ) {
				saved = textSet;
			}

			public int size() {
				return 0;
			}

			public void clear() {
			}

			public void reload() {
			}

			public void reload( String application, String group ) {
			}

			public Set<LocalizedTextSet> getCachedTextSets() {
				return null;
			}
		};

		textService.setTextSetCache( cache );

		ExecutorService threads = Executors.newFixedThreadPool( 5 );

		final LocalizedTextSet[] fetched = new LocalizedTextSet[5];

		for ( int i = 0; i < 5; i++ ) {
			final int index = i;
			threads.submit( new Runnable()
			{
				public void run() {
					LocalizedTextSet text = textService.getLocalizedTextSet( "myapp", "mygroup" );

					fetched[index] = text;
				}
			} );
		}

		threads.shutdown();
		threads.awaitTermination( 5, TimeUnit.SECONDS );

		LocalizedTextSet found = null;

		verify( textDao, times( 1 ) ).getLocalizedTextForGroup( "myapp", "mygroup" );

		for ( int i = 0; i < 5; i++ ) {
			if ( found == null ) {
				found = fetched[i];
			}

			assertNotNull( fetched[i] );
			assertSame( found, fetched[i] );
		}
	}

	@Test
	public void settingNullCacheShouldNotBreakThings() {
		textService.setTextSetCache( null );

		createTextSet();

		reset( textDao );
		saveItemThatDoesExist();

		reset( textDao );
		saveItemThatDoesNotYetExist();
	}

	@Test
	public void ifItemExistsItWillBeReturnedInsteadOfCreated() {
		LocalizedText existing = new LocalizedText();

		when( textDao.getLocalizedText( "myapp", "mygroup", "mylabel" ) ).thenReturn( existing );

		LocalizedText text = textService.saveDefaultText( "myapp", "mygroup", "mylabel", "default" );

		assertSame( existing, text );
	}

	@Test
	public void createNewItem() {
		LocalizedText text = textService.saveDefaultText( "myapp", "mygroup", "mylabel", "default" );

		assertNotNull( text );
		assertEquals( "myapp", text.getApplication() );
		assertEquals( "mygroup", text.getGroup() );
		assertEquals( "mylabel", text.getLabel() );
		assertTrue( text.isUsed() );
		assertTrue( text.isAutoGenerated() );
		assertNotNull( text.getCreated() );
		assertEquals( "default", text.getFieldsForLanguage( MyLanguage.EN ).getText() );
		assertEquals( "default", text.getFieldsForLanguage( MyLanguage.FR ).getText() );

		verify( textDao, times( 1 ) ).insertLocalizedText( text );
	}

	@Test
	public void getExistingItem() {
		when( textDao.getLocalizedText( "myapp", "mygroup", "mylabel" ) ).thenReturn( null );

		LocalizedText expected = new LocalizedText();
		when( textDao.getLocalizedText( "myapp2", "mygroup", "mylabel" ) ).thenReturn( expected );

		assertNull( textService.getLocalizedText( "myapp", "mygroup", "mylabel" ) );
		assertSame( expected, textService.getLocalizedText( "myapp2", "mygroup", "mylabel" ) );
	}

	@Test
	public void savingNullItemShouldNotBreak() {
		LocalizedTextSetCache cache = mock( LocalizedTextSetCache.class );
		textService.setTextSetCache( cache );

		textService.saveLocalizedText( null );

		verifyNoInteractions( textDao, cache );
	}

	@Test
	public void saveItemShouldUseTheCache() {
		LocalizedTextSetCache cache = mock( LocalizedTextSetCache.class );
		textService.setTextSetCache( cache );

		saveItemThatDoesNotYetExist();
		verify( cache, times( 1 ) ).reload( "myapp1", "mygroup" );

		reset( cache, textDao );

		saveItemThatDoesExist();
		verify( cache, times( 1 ) ).reload( "myapp1", "mygroup" );
	}

	@Test
	public void saveItemShouldUseExecutorServiceForCache() {
		LocalizedTextSetCache cache = mock( LocalizedTextSetCache.class );
		textService.setTextSetCache( cache );

		ExecutorService executorService = mock( ExecutorService.class );
		textService.setExecutorService( executorService );

		saveItemThatDoesNotYetExist();

		// Cache interaction should have been suppressed
		verifyNoInteractions( cache );
		verify( executorService, times( 1 ) ).submit( any( Runnable.class ) );

		reset( cache, textDao, executorService );

		saveItemThatDoesExist();

		verifyNoInteractions( cache );
		verify( executorService, times( 1 ) ).submit( any( Runnable.class ) );
	}

	@Test
	public void saveItemThatDoesNotYetExist() {
		LocalizedText text = new LocalizedText();
		text.setApplication( "myapp1" );
		text.setGroup( "mygroup" );
		text.setLabel( "mylabel" );

		when( textDao.getLocalizedText( "myapp1", "mygroup", "mylabel" ) ).thenReturn( null );

		textService.saveLocalizedText( text );

		verify( textDao, times( 1 ) ).insertLocalizedText( text );
		verify( textDao, never() ).updateLocalizedText( text );
	}

	@Test
	public void saveItemThatDoesExist() {
		LocalizedText text = new LocalizedText();
		text.setApplication( "myapp1" );
		text.setGroup( "mygroup" );
		text.setLabel( "mylabel" );

		when( textDao.getLocalizedText( "myapp1", "mygroup", "mylabel" ) ).thenReturn( new LocalizedText() );

		textService.saveLocalizedText( text );

		verify( textDao, times( 1 ) ).updateLocalizedText( text );
		verify( textDao, never() ).insertLocalizedText( text );
	}

	@Test
	public void deleteNullItemShouldDoNothing() {
		LocalizedTextSetCache cache = mock( LocalizedTextSetCache.class );
		textService.setTextSetCache( cache );

		textService.deleteLocalizedText( null );

		verifyNoInteractions( textDao, cache );
	}

	@Test
	public void deleteItemShouldUseTheCache() {
		LocalizedTextSetCache cache = mock( LocalizedTextSetCache.class );
		textService.setTextSetCache( cache );

		deleteItem();
		verify( cache, times( 1 ) ).reload( "myapp1", "mygroup" );
	}

	@Test
	public void deleteItemShouldUseExecutorServiceForCache() {
		LocalizedTextSetCache cache = mock( LocalizedTextSetCache.class );
		textService.setTextSetCache( cache );

		ExecutorService executorService = mock( ExecutorService.class );
		textService.setExecutorService( executorService );

		deleteItem();

		// Cache interaction should have been suppressed
		verifyNoInteractions( cache );
		verify( executorService, times( 1 ) ).submit( any( Runnable.class ) );
	}

	@Test
	public void deleteItem() {
		LocalizedText text = new LocalizedText();
		text.setApplication( "myapp1" );
		text.setGroup( "mygroup" );
		text.setLabel( "mylabel" );

		textService.deleteLocalizedText( text );

		verify( textDao, times( 1 ) ).deleteLocalizedText( text );
	}

	@Test
	public void createShouldFailSilently() {
		doThrow( new RuntimeException( "Insert failed" ) ).when( textDao ).insertLocalizedText(
				any( LocalizedText.class ) );

		LocalizedText text = textService.saveDefaultText( "myapp", "mygroup", "mylabel", "default" );

		assertNotNull( text );
		assertEquals( "myapp", text.getApplication() );
		assertEquals( "mygroup", text.getGroup() );
		assertEquals( "mylabel", text.getLabel() );
		assertTrue( text.isUsed() );
		assertTrue( text.isAutoGenerated() );
		assertNotNull( text.getCreated() );
	}

	@Test
	public void flagAsUsed() {
		LocalizedText text = new LocalizedText();
		assertFalse( text.isUsed() );
		assertNull( text.getUpdated() );

		textService.flagAsUsed( text );

		assertTrue( text.isUsed() );
		assertNotNull( text.getUpdated() );

		verify( textDao, times( 1 ) ).flagAsUsed( text );
	}

	@Test
	public void flagAsUsedShouldUseTheExecutorService() {
		ExecutorService executorService = mock( ExecutorService.class );
		textService.setExecutorService( executorService );

		LocalizedText text = new LocalizedText();
		assertFalse( text.isUsed() );
		assertNull( text.getUpdated() );

		textService.flagAsUsed( text );

		// Properties should still be set
		assertTrue( text.isUsed() );
		assertNotNull( text.getUpdated() );

		// Dao should never have been called, but the executorService should have been
		verify( textDao, never() ).flagAsUsed( text );
		verify( executorService, times( 1 ) ).submit( any( Runnable.class ) );
	}

	@Test
	public void settingNullExecutorServiceShouldNotBreakThings() {
		textService.setExecutorService( null );

		// Flag as used behavior should be same as default even with null ExecutorService
		flagAsUsed();
	}

	@Test
	public void getApplications() {
		List<String> expected = Arrays.asList( "myapp", "myotherapp" );
		when( textDao.getApplications() ).thenReturn( expected );

		List<String> applications = textService.getApplications();

		assertSame( expected, applications );
	}

	@Test
	public void getGroups() {
		List<String> expected = Arrays.asList( "mygroup", "myothergroup" );
		when( textDao.getGroups( "myapp" ) ).thenReturn( expected );

		List<String> groups = textService.getGroups( "myapp" );

		assertSame( expected, groups );
	}

	@Test
	public void searchLocalizedText() {
		List<LocalizedText> expected = Arrays.asList( new LocalizedText() );
		when( textDao.searchLocalizedText( "some text" ) ).thenReturn( expected );

		List<LocalizedText> found = textService.searchLocalizedTextItemsForText( "some text" );

		assertSame( expected, found );
	}

	@Test
	public void testEmptyConverstion() {
		LocalizedTextSet set = mock( LocalizedTextSet.class );
		Collection<LocalizedText> translations = new HashSet<LocalizedText>();
		when( set.getItems() ).thenReturn( translations );

		Map<String, String> actualObject =
				textService.getLanguageMap( set, LanguageConfigurator.getLanguageByCode( "en" ) );

		assertNotNull( actualObject );
		assertEquals( 0, actualObject.entrySet().size() );
	}

	@Test
	public void testCorrectConversionOneLabel() {
		LocalizedTextSet set = mock( LocalizedTextSet.class );
		Collection<LocalizedText> translations = new HashSet<LocalizedText>();
		LocalizedText t1 =
				createLocalizedText( "eurosong.belgium.label", "Belgique douze points",
				                     "Belgium twelve points" );
		translations.add( t1 );
		when( set.getItems() ).thenReturn( translations );

		Map<String, String> actualObject =
				textService.getLanguageMap( set, LanguageConfigurator.getLanguageByCode( "fr" ) );

		assertNotNull( actualObject );
		assertEquals( 1, actualObject.entrySet().size() );
		assertEquals( "Belgique douze points", actualObject.get( "eurosong.belgium.label" ) );
	}

	@Test
	public void testCorrectConversionMultipleLabelsAndMultipleLanguages() {
		LocalizedTextSet set = mock( LocalizedTextSet.class );
		Collection<LocalizedText> translations = new HashSet<LocalizedText>();
		LocalizedText t1 =
				createLocalizedText( "eurosong.belgium.label.1", "Belgique douze points",
				                     "Belgium twelve points" );
		translations.add( t1 );
		LocalizedText t2 =
				createLocalizedText( "eurosong.belgium.label.2", "Belgique dix points",
				                     "Belgium ten points" );
		translations.add( t2 );
		LocalizedText t3 =
				createLocalizedText( "eurosong.belgium.label.3", "Belgique huit points",
				                     "Belgium eight points" );
		translations.add( t3 );
		when( set.getItems() ).thenReturn( translations );

		Map<String, String> actualObject =
				textService.getLanguageMap( set, LanguageConfigurator.getLanguageByCode( "fr" ) );

		assertNotNull( actualObject );
		assertEquals( 3, actualObject.entrySet().size() );
		assertEquals( "Belgique douze points", actualObject.get( "eurosong.belgium.label.1" ) );
		assertEquals( "Belgique dix points", actualObject.get( "eurosong.belgium.label.2" ) );
		assertEquals( "Belgique huit points", actualObject.get( "eurosong.belgium.label.3" ) );

		actualObject = textService.getLanguageMap( set, LanguageConfigurator.getLanguageByCode( "en" ) );

		assertNotNull( actualObject );
		assertEquals( 3, actualObject.entrySet().size() );
		assertEquals( "Belgium twelve points", actualObject.get( "eurosong.belgium.label.1" ) );
		assertEquals( "Belgium ten points", actualObject.get( "eurosong.belgium.label.2" ) );
		assertEquals( "Belgium eight points", actualObject.get( "eurosong.belgium.label.3" ) );

	}

	private LocalizedText createLocalizedText( String expectedLabel, String frString, String enString ) {
		LocalizedText t1 = new LocalizedText();
		t1.setLabel( expectedLabel );
		HashSet<LocalizedTextFields> allFields = new HashSet<>();
		LocalizedTextFields frField = new LocalizedTextFields( LanguageConfigurator.getLanguageByCode( "fr" ) );
		frField.setText( frString );
		allFields.add( frField );
		LocalizedTextFields enField = new LocalizedTextFields( LanguageConfigurator.getLanguageByCode( "en" ) );
		enField.setText( enString );
		allFields.add( enField );
		t1.setFieldsAsCollection( allFields );
		return t1;
	}

	private class LocalizedTextServiceImpl extends AbstractLocalizedTextService
	{
		private LocalizedTextServiceImpl( LocalizedTextDataStore localizedTextDao ) {
			super( localizedTextDao );
		}
	}
}
