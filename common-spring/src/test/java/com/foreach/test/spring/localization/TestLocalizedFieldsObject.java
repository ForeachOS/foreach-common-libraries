package com.foreach.test.spring.localization;

import com.foreach.spring.localization.AbstractLocalizedFieldsObject;
import com.foreach.spring.localization.BaseLocalizedFields;
import com.foreach.spring.localization.Language;
import org.junit.Test;

import java.util.Collection;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestLocalizedFieldsObject
{
	@Test
	public void verifyListCollection()
	{
		MyLocalizedText text = new MyLocalizedText();

		Collection<MyFields> fields = text.getFieldsAsCollection();
		assertNotNull( fields );
		assertEquals( 1, fields.size() );

		text.getFields( MyLanguage.EN ).setText( "first" );

		for ( MyFields f : fields ) {
			assertEquals( "first", f.getText() );
		}

		MyFields other = new MyFields( MyLanguage.EN );
		other.setText( "other" );
		fields.add( other );

		assertEquals( "other", text.getFields( MyLanguage.EN ).getText() );
		assertEquals( 1, fields.size() );

		for ( MyFields f : fields ) {
			assertEquals( "other", f.getText() );
		}
	}

	enum MyLanguage implements Language
	{
		EN;

		public String getName()
		{
			return "English";
		}

		public Locale getLocale()
		{
			return Locale.ENGLISH;
		}

		public String getCode()
		{
			return "en";
		}
	}

	class MyFields extends BaseLocalizedFields
	{
		private String text;

		MyFields( Language language )
		{
			super( language );
		}

		public String getText()
		{
			return text;
		}

		public void setText( String text )
		{
			this.text = text;
		}
	}

	class MyLocalizedText extends AbstractLocalizedFieldsObject<MyFields>
	{
		@Override
		protected void createDefaultFields()
		{
			for ( Language language : MyLanguage.values() ) {
				getFields( language );
			}
		}

		@Override
		public MyFields createFields( Language language )
		{
			return new MyFields( language );
		}
	}
}
