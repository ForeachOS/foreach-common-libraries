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
import com.foreach.common.spring.localization.Language;
import com.foreach.common.spring.localization.MyLanguage;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TestLocalizedTextSet extends AbstractLocalizationTest
{
	private LocalizedTextSet textSet;
	private LocalizedTextService textService;
	private LocalizedText existingText;

	@Before
	public void setUp() {
		textService = mock( LocalizedTextService.class );

		existingText = new LocalizedText();
		existingText.setLabel( "existing_label" );
		existingText.setGroup( "group" );
		existingText.getFieldsForLanguage( MyLanguage.EN ).setText( "existing_value_en" );
		existingText.setUsed( true );

		when( textService.getLocalizedTextItems( "myapp", "mygroup" ) ).thenReturn( Arrays.asList( existingText ) );

		textSet = new LocalizedTextSetImpl( "myapp", "mygroup", textService );
	}

	@Test
	public void getItems() {
		Collection<LocalizedText> items = textSet.getItems();

		assertEquals( 1, items.size() );
		assertTrue( items.contains( existingText ) );
	}

	@Test
	public void exists() {
		assertTrue( textSet.exists( "existing_label" ) );
		assertFalse( textSet.exists( "non_existing" ) );
	}

	@Test
	public void getExistingTextThatHasBeenUsed() {
		existingText.setUsed( true );

		assertEquals( "existing_value_en", text( "existing_label", MyLanguage.EN ) );
		assertTrue( existingText.isUsed() );

		assertNull( text( "existing_label", MyLanguage.FR ) );
		assertTrue( existingText.isUsed() );

		verify( textService, never() ).flagAsUsed( any( LocalizedText.class ) );
	}

	@Test
	public void getExistingTextThatHasNotBeenUsed() {
		existingText.setUsed( false );

		assertEquals( "existing_value_en", text( "existing_label", MyLanguage.EN ) );
		assertTrue( existingText.isUsed() );

		assertNull( text( "existing_label", MyLanguage.FR ) );
		assertTrue( existingText.isUsed() );

		verify( textService, times( 1 ) ).flagAsUsed( existingText );
	}

	@Test
	public void getNonExistingTextWithoutDefault() {
		assertEquals( 1, textSet.size() );

		LocalizedText created = new LocalizedText();
		created.setGroup( "mygroup" );
		created.setLabel( "non_existing_label" );
		created.getFieldsForLanguage( MyLanguage.FR ).setText( "non_existing_label fr" );
		created.getFieldsForLanguage( MyLanguage.EN ).setText( "non_existing_label en" );
		created.setUsed( true );

		when( textService.saveDefaultText( "myapp", "mygroup", "non_existing_label", null ) ).thenReturn( created );

		assertEquals( "non_existing_label fr", text( "non_existing_label", MyLanguage.FR ) );
		assertEquals( "non_existing_label en", text( "non_existing_label", MyLanguage.EN ) );

		assertEquals( 2, textSet.size() );

		verify( textService, times( 1 ) ).saveDefaultText( "myapp", "mygroup", "non_existing_label", null );
		verify( textService, never() ).flagAsUsed( any( LocalizedText.class ) );
	}

	@Test
	public void getNonExistingTextWithDefault() {
		assertEquals( 1, textSet.size() );

		LocalizedText created = new LocalizedText();
		created.setGroup( "mygroup" );
		created.setLabel( "non_existing_label" );
		created.getFieldsForLanguage( MyLanguage.FR ).setText( "default value" );
		created.getFieldsForLanguage( MyLanguage.EN ).setText( "default value" );
		created.setUsed( true );

		when( textService.saveDefaultText( "myapp", "mygroup", "non_existing_label", "default value" ) ).thenReturn(
				created );

		assertEquals( "default value", text( "non_existing_label", MyLanguage.FR, "default value" ) );
		assertEquals( "default value", text( "non_existing_label", MyLanguage.EN, "default value" ) );

		assertEquals( 2, textSet.size() );

		verify( textService, times( 1 ) ).saveDefaultText( "myapp", "mygroup", "non_existing_label", "default value" );
		verify( textService, never() ).flagAsUsed( any( LocalizedText.class ) );
	}

	@Test
	public void reload() {
		LocalizedText updated = new LocalizedText();
		updated.setLabel( "other_label" );
		updated.setGroup( "othergroup" );
		updated.getFieldsForLanguage( MyLanguage.EN ).setText( "existing_value_en" );
		updated.setUsed( true );

		when( textService.getLocalizedTextItems( "myapp", "mygroup" ) ).thenReturn( Arrays.asList( updated ) );

		textSet.reload();

		assertEquals( 1, textSet.size() );
		assertTrue( textSet.exists( "other_label" ) );
	}

	private String text( String label, Language language ) {
		return textSet.getText( label, language );
	}

	private String text( String label, Language language, String defaultValue ) {
		return textSet.getText( label, language, defaultValue );
	}
}
