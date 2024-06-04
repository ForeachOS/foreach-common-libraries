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
import com.foreach.common.spring.localization.MyLanguage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TestLocalizedText extends AbstractLocalizationTest
{
	@Test
	public void createdIsNotSetAfterConstruction() {
		LocalizedText text = new LocalizedText();
		assertNull( text.getCreated() );
		assertNull( text.getUpdated() );
	}

	@Test
	public void equalOnApplicationGroupAndLabel() {
		LocalizedText left = new LocalizedText();
		LocalizedText right = new LocalizedText();

		equal( left, right );

		left.setApplication( "app left" );
		different( left, right );

		right.setApplication( "app right" );
		different( left, right );

		right.setApplication( left.getApplication() );
		equal( left, right );

		left.setGroup( "group left" );
		different( left, right );

		right.setGroup( "group right" );
		different( left, right );

		right.setGroup( left.getGroup() );
		equal( left, right );

		left.setLabel( "label left" );
		different( left, right );

		right.setLabel( "label right" );
		different( left, right );

		right.setLabel( left.getLabel() );
		equal( left, right );
	}

	@Test
	public void builderCreatesTheSameLocalizedText() {
		LocalizedText noBuilder = new LocalizedText();
		noBuilder.setGroup( "group" );
		noBuilder.setLabel( "label" );
		LocalizedTextFields localizedTextFields = new LocalizedTextFields( MyLanguage.EN );
		localizedTextFields.setText( "text" );
		noBuilder.setFieldsAsCollection( Arrays.asList( localizedTextFields ) );

		LocalizedText withBuilder =
				LocalizedText.builder().group( "group" ).label( "label" ).field( MyLanguage.EN, "text" ).build();

		equal( noBuilder, withBuilder );
	}

	@Test
	public void builderSupportsMoreThenOneLanguage(){
		LocalizedText withBuilder =
				LocalizedText.builder().group( "group" ).label( "label" ).field( MyLanguage.EN, "text" ).field( MyLanguage.FR,"text-fr" ).build();

		assertEquals( 2,withBuilder.getFieldsAsCollection().size() );
		assertEquals( "text", withBuilder.getFieldsForLanguage( MyLanguage.EN ).getText());
		assertEquals( "text-fr", withBuilder.getFieldsForLanguage( MyLanguage.FR ).getText());
	}

	private void equal( LocalizedText left, LocalizedText right ) {
		assertEquals( left, right );
		assertEquals( right, left );
		assertEquals( left.hashCode(), right.hashCode() );
	}

	private void different( LocalizedText left, LocalizedText right ) {
		assertNotEquals( left, right );
		assertNotEquals( right, left );
	}

}
