package com.foreach.spring.localization.text;

import com.foreach.spring.localization.AbstractLocalizationTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestLocalizedText extends AbstractLocalizationTest
{
	@Test
	public void createdIsSetAfterConstruction()
	{
		LocalizedText text = new LocalizedText();
		assertNotNull( text.getCreated() );
		assertNull( text.getUpdated() );
	}

	@Test
	public void equalOnApplicationGroupAndLabel()
	{
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

	private void equal( LocalizedText left, LocalizedText right )
	{
		assertEquals( left, right );
		assertEquals( right, left );
		assertEquals( left.hashCode(), right.hashCode() );
	}

	private void different( LocalizedText left, LocalizedText right )
	{
		assertFalse( left.equals( right ) );
		assertFalse( right.equals( left ) );
	}
}
