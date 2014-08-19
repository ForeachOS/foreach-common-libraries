package com.foreach.synchronizer.text;

import com.foreach.spring.localization.Language;
import com.foreach.spring.localization.LanguageConfigurator;
import com.foreach.spring.localization.text.LocalizedText;
import com.foreach.spring.localization.text.LocalizedTextFields;
import com.foreach.synchronizer.text.io.TestLanguage;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class TestLocalizedTextWrapper {

    @Before
    public void setup() {
        LanguageConfigurator languageConfigurator = new LanguageConfigurator( TestLanguage.class );
    }

    @Test
    public void testMerge_unchanged() {
        LocalizedText localizedText = getLocalizedText();
        LocalizedTextWrapper localizedTextWrapper = new LocalizedTextWrapper( localizedText );

        localizedTextWrapper.merge( localizedText );

        assertEquals( false, localizedTextWrapper.hasChanged() );
    }

    @Test
    public void testMerge_updatedOlder() {
        LocalizedText localizedTextInitial = getLocalizedText();
        LocalizedText localizedTextToMergeWithOlderDate = getLocalizedText();

        Date initialDate = getLocalizedText().getUpdated();
        Date olderDate = DateUtils.addDays( initialDate, -1 );
        localizedTextToMergeWithOlderDate.setUpdated( olderDate );

        LocalizedTextWrapper localizedTextWrapper = new LocalizedTextWrapper( localizedTextToMergeWithOlderDate );

        localizedTextWrapper.merge( localizedTextInitial );

        assertEquals( true, localizedTextWrapper.hasChanged() );
        assertEquals(DateUtils.truncate( initialDate, Calendar.SECOND), DateUtils.truncate( localizedTextWrapper.getLocalizedText().getUpdated(), Calendar.SECOND) );
    }

    @Test
    public void testMerge_updatedNewer() {
        LocalizedText localizedTextInitial = getLocalizedText();
        LocalizedText localizedTextToMergeWithNewerDate = getLocalizedText();

        Date initialDate = getLocalizedText().getUpdated();
        Date newerDate = DateUtils.addDays( initialDate, 1 );
        localizedTextToMergeWithNewerDate.setUpdated( newerDate );

        LocalizedTextWrapper localizedTextWrapper = new LocalizedTextWrapper( localizedTextToMergeWithNewerDate );

        localizedTextWrapper.merge( localizedTextInitial );

        assertEquals( true, localizedTextWrapper.hasChanged() );
        assertEquals( newerDate, localizedTextWrapper.getLocalizedText().getUpdated() );
    }

    @Test
    public void testMerge_updatedNewerAndNewValue() {
        LocalizedText localizedTextInitial = getLocalizedText();
        LocalizedText localizedTextToMergeWithNewerDate = getLocalizedText();

        Date initialDate = getLocalizedText().getUpdated();
        Date newerDate = DateUtils.addDays( initialDate, 1 );
        localizedTextToMergeWithNewerDate.setUpdated( newerDate );
        localizedTextToMergeWithNewerDate.getFieldsForLanguage( TestLanguage.NL ).setText( "new NL text" );

        LocalizedTextWrapper localizedTextWrapper = new LocalizedTextWrapper( localizedTextToMergeWithNewerDate );

        localizedTextWrapper.merge( localizedTextInitial );

        assertEquals( true, localizedTextWrapper.hasChanged() );
        assertEquals( newerDate, localizedTextWrapper.getLocalizedText().getUpdated() );
        assertEquals( "new NL text", localizedTextToMergeWithNewerDate.getFieldsForLanguage( TestLanguage.NL ).getText() );
    }

    @Test
    public void testMerge_newValueSameDate() {
        LocalizedText localizedTextInitial = getLocalizedText();
        LocalizedText localizedTextToMergeWithNewerDate = getLocalizedText();

        Date initialDate = getLocalizedText().getUpdated();
        localizedTextToMergeWithNewerDate.setUpdated( initialDate );
        localizedTextToMergeWithNewerDate.getFieldsForLanguage( TestLanguage.NL ).setText( "new NL text" );

        LocalizedTextWrapper localizedTextWrapper = new LocalizedTextWrapper( localizedTextToMergeWithNewerDate );

        localizedTextWrapper.merge( localizedTextInitial );

        assertEquals( true, localizedTextWrapper.hasChanged() );
        assertEquals( initialDate, localizedTextWrapper.getLocalizedText().getUpdated() );
        assertEquals( "new NL text", localizedTextToMergeWithNewerDate.getFieldsForLanguage( TestLanguage.NL ).getText() );
    }

    private LocalizedText getLocalizedText() {
        LocalizedText localizedText = new LocalizedText();
        localizedText.setApplication( "application" );
        localizedText.setGroup( "group" );
        localizedText.setLabel( "label" );
        localizedText.setUpdated( new Date() );
        Language nl = TestLanguage.NL;
        LocalizedTextFields localizedTextFieldsNl = new LocalizedTextFields( nl );
        localizedTextFieldsNl.setText( "NL text" );
        localizedText.addFields( localizedTextFieldsNl );
        Language fr = TestLanguage.FR;
        LocalizedTextFields localizedTextFieldsFr = new LocalizedTextFields( fr );
        localizedTextFieldsFr.setText( "FR text" );
        localizedText.addFields( localizedTextFieldsFr );
        return localizedText;
    }
}
