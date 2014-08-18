package com.foreach.synchronizer.text.io;

import com.foreach.common.spring.localization.Language;

import java.util.Locale;

public enum TestLanguage implements Language {
    EN( "en", "English", Locale.ENGLISH ),
    NL( "nl", "Nederlands", Locale.US ),
    FR( "fr", "Frans", Locale.FRANCE ),
    DE( "de", "Duits", Locale.GERMANY );

    private String code, name;
    private Locale locale;

    TestLanguage( String code, String name, Locale locale ) {
        this.code = code;
        this.name = name;
        this.locale = locale;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Locale getLocale() {
        return locale;
    }
}
