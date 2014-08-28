package com.foreach.synchronizer.text.io;

import com.foreach.common.spring.localization.Language;
import com.foreach.common.spring.localization.LanguageConfigurator;
import com.foreach.common.spring.localization.text.LocalizedText;
import com.foreach.common.spring.localization.text.LocalizedTextFields;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xml.internal.utils.PrefixResolverDefault;
import org.apache.commons.lang3.time.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class ExcelLocalizedTextReader implements LocalizedTextReader {

    InputStream inputStream;

    public ExcelLocalizedTextReader( InputStream inputStream ) {
        this.inputStream = inputStream;
    }

    public Collection<LocalizedText> read() {
        Collection<LocalizedText> list = new ArrayList<LocalizedText>();
        try {
            DocumentBuilderFactory xmlFact = DocumentBuilderFactory.newInstance();
            xmlFact.setNamespaceAware( true );
            DocumentBuilder builder = xmlFact.newDocumentBuilder();
            Document doc = builder.parse( inputStream );

            final PrefixResolver resolver = new PrefixResolverDefault( doc.getDocumentElement() );
            NamespaceContext ctx = new NamespaceContext() {
                public String getNamespaceURI( String prefix ) {
                    return resolver.getNamespaceForPrefix( prefix );
                }

                // Dummy implementation - not used!
                public Iterator getPrefixes( String val ) {
                    return null;
                }

                // Dummy implemenation - not used!
                public String getPrefix( String uri ) {
                    return null;
                }
            };

            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext( ctx );
            String expression = "//ss:Workbook/ss:Worksheet/ss:Table/ss:Row";
            NodeList nodes = ( NodeList ) xpath.evaluate( expression, doc, XPathConstants.NODESET );

            for( int i = 1; i < nodes.getLength(); i++ ) {
                Node node = nodes.item( i );

                NodeList cells = ( NodeList ) xpath.evaluate( "ss:Cell/ss:Data", node, XPathConstants.NODESET );
                list.add( readLocalizedEntity( cells ) );
            }
        } catch ( Exception e ) {
            throw new RuntimeException( "Illegal Excel XML format", e );
        }
        return list;
    }

    public void close() throws IOException {
        inputStream.close();
    }

    protected LocalizedText readLocalizedEntity( NodeList cells ) throws ParseException {
        LocalizedText entity = new LocalizedText();
        int index = 0;
        entity.setApplication( valueAsStringOrNullIfEmpty( cells.item( index++ ).getTextContent() )  );
        entity.setGroup( valueAsStringOrNullIfEmpty( cells.item( index++ ).getTextContent() ) );
        entity.setLabel( valueAsStringOrNullIfEmpty( cells.item( index++ ).getTextContent() ) );

        for( Language language : LanguageConfigurator.getLanguages() ) {
            LocalizedTextFields localizedTextFields = new LocalizedTextFields( language );
            localizedTextFields.setText( valueAsStringOrNullIfEmpty( cells.item( index++ ).getTextContent() ) );
            entity.addFields( localizedTextFields );
        }
        entity.setUsed( valueAsbooleanOrFalseIfEmpty( cells.item( index++ ).getTextContent() ) );
        entity.setAutoGenerated( valueAsbooleanOrFalseIfEmpty( cells.item( index++ ).getTextContent() ) );

        entity.setCreated( valueAsDateOrNullIfEmpty( cells.item( index++ ).getTextContent()) );
        entity.setUpdated( valueAsDateOrNullIfEmpty( cells.item( index++ ).getTextContent()) );

        return entity;
    }

    private Date valueAsDateOrNullIfEmpty(String input) throws ParseException {
        return input.isEmpty() ? null : DateUtils.parseDate( input, "yyyy-MM-dd HH:mm:ss" );
    }

    private boolean valueAsbooleanOrFalseIfEmpty(String input)
    {
        return input.isEmpty()?false:Boolean.parseBoolean( input);
    }

    private String valueAsStringOrNullIfEmpty( String input )
    {
        return input.isEmpty()?null:input;
    }
}
