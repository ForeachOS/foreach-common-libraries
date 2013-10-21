package com.foreach.synchronizer.text;

import com.foreach.spring.localization.text.LocalizedText;
import com.foreach.spring.localization.text.LocalizedTextFields;

import java.util.Map;

public class LocalizedTextWrapper {
    private LocalizedText localizedText;
    private boolean changed;

    public LocalizedTextWrapper( LocalizedText localizedText ) {
        this.localizedText = localizedText;
        changed = false;
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

//        String thisApplication = localizedText.getApplication();
//        String thisGroup = localizedText.getGroup();
//        String thatApplication = thatText.getApplication();
//        String thatGroup = thatText.getGroup();
//
//        if( thisApplication != null ? !thisApplication.equals( thatApplication ) : thatApplication != null ) {
//            return false;
//        }
//        if( thisGroup != null ? !thisGroup.equals( thatGroup ) : thatGroup != null ) {
//            return false;
//        }
//        return true;
    }

    @Override
    public int hashCode() {
        return localizedText.hashCode();
//        if( localizedText != null ) {
//            String x = localizedText.getApplication() + localizedText.getGroup();
//            return x.hashCode();
//        }
//        return (new LocalizedTextWrapper( new LocalizedText() )).hashCode();
    }

    public void merge( LocalizedText newText ) {
        if( newText == null ) {
            return;
        }
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
}
