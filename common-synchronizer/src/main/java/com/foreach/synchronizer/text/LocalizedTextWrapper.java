package com.foreach.synchronizer.text;

import com.foreach.common.spring.localization.text.LocalizedText;
import com.foreach.common.spring.localization.text.LocalizedTextFields;

import java.util.Map;

public class LocalizedTextWrapper {
    private LocalizedText localizedText;
    private boolean changed;

    private int amountOfEnvironmentsPresent;

    public LocalizedTextWrapper( LocalizedText localizedText ) {
        this.localizedText = localizedText;
        changed = false;
        amountOfEnvironmentsPresent = 1;
    }

    public LocalizedText getLocalizedText() {
        return localizedText;
    }

    public boolean hasChanged() {
        return changed;
    }

    @SuppressWarnings( "all" )
    @Override
    public boolean equals( Object o ) {
        if( this == o ) {
            return true;
        }
        if( o == null || getClass() != o.getClass() ) {
            return false;
        }
        if( localizedText == null ) {
            return false;
        }
        LocalizedTextWrapper thatWrapper = ( LocalizedTextWrapper ) o;
        LocalizedText thatText = thatWrapper.getLocalizedText();
        return localizedText.equals( thatText );
    }

    @Override
    public int hashCode() {
        return localizedText.hashCode();
    }

    public void merge( LocalizedText newText ) {
        if( newText == null ) {
            return;
        }
        amountOfEnvironmentsPresent++;
        if( updatedEquals( newText ) ) {
            //dates are the same, so the current one has priority.
            if( !hasEqualValues( newText ) ) {
                changed = true;
            }
        } else {
            changed = true;
            if( !currentIsMostRecentOrEqual( newText ) ) {
                localizedText = newText;
            }
        }
    }

    public boolean shouldUpdate( int amountOfEnvironments ) {
        return (amountOfEnvironments != amountOfEnvironmentsPresent || changed);
    }

    private boolean hasEqualValues( LocalizedText text ) {
        Map<String, LocalizedTextFields> theirFields = text.getFields();
        Map<String, LocalizedTextFields> myFields = localizedText.getFields();

        if( theirFields.size() != myFields.size() ) {
            return false;
        }
        for( LocalizedTextFields myField : myFields.values() ) {
            if( !theirFields.values().contains( myField ) ) {
                return false;
            }
        }
        return true;
    }

    private boolean updatedEquals( LocalizedText text ) {
        return (localizedText.getUpdated() == null && text.getUpdated() == null) || localizedText.getUpdated() != null && localizedText.getUpdated().equals( text.getUpdated() );
    }

    private boolean currentIsMostRecentOrEqual( LocalizedText text ) {
        if( localizedText.getUpdated() == null || text.getUpdated() == null ) {
            return text.getUpdated() == null;
        } else {
            return !localizedText.getUpdated().before( text.getUpdated() );
        }
    }

    public int getAmountOfEnvironmentsPresent() {
        return amountOfEnvironmentsPresent;
    }
}
