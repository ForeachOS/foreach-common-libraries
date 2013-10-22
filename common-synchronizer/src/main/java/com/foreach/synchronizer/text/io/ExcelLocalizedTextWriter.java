package com.foreach.synchronizer.text.io;

import com.foreach.spring.localization.Language;
import com.foreach.spring.localization.LanguageConfigurator;
import com.foreach.spring.localization.text.LocalizedText;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelLocalizedTextWriter implements LocalizedTextWriter {

    private static final String templateResource = "com/foreach/synchronizer/text/template.xml";

    public static final String SS_STYLE_ID = "ss:StyleID";
    public static final String NEWLINE = "\n      ";

    private OutputStream outputStream;

    public ExcelLocalizedTextWriter( OutputStream outputStream ) {
        this.outputStream = outputStream;
    }

    public void write( List<LocalizedText> localizedTexts ) {
        final StringWriter xml = new StringWriter();
        final XMLStreamWriter writer;

        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        try {
            writer = factory.createXMLStreamWriter( xml );

            writer.writeCharacters( NEWLINE );

            writer.writeStartElement( "Table" );
            writer.writeAttribute( "ss:ExpandedColumnCount", "4" );
            writer.writeAttribute( "ss:ExpandedRowCount", "3" );
            writer.writeAttribute( "x:FullColumns", "1" );
            writer.writeAttribute( "x:FullRows", "1" );
            writer.writeAttribute( "ss:DefaultRowHeight", "15" );

            writer.writeCharacters( NEWLINE );
            writer.writeStartElement( "Column" );
            writer.writeAttribute( SS_STYLE_ID, "s79" );
            writer.writeAttribute( "ss:AutoFitWidth", "0" );
            writer.writeAttribute( "ss:Width", "325.5" );
            writer.writeEndElement();

            writer.writeCharacters( NEWLINE );
            writer.writeStartElement( "Column" );
            writer.writeAttribute( SS_STYLE_ID, "s66" );
            writer.writeAttribute( "ss:AutoFitWidth", "0" );
            writer.writeAttribute( "ss:Width", "225" );
            writer.writeAttribute( "ss:Span", "2" );
            writer.writeEndElement();

            writer.writeCharacters( NEWLINE );
            writer.writeStartElement( "Row" );
            writer.writeAttribute( "ss:AutoFitHeight", "0" );
            writer.writeAttribute( "ss:Height", "30.75" );

            writeHeader( writer, "Application" );
            writeHeader( writer, "Group" );
            writeHeader( writer, "Label" );
            for( Language language : LanguageConfigurator.getLanguages() ) {
                writeHeader( writer, language.getName() );
            }

            writer.writeEndElement();

            DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
            symbols.setDecimalSeparator( '.' );

            DecimalFormat formatter = new DecimalFormat( "0.00", symbols );

            for( LocalizedText item : localizedTexts ) {
                writer.writeCharacters( NEWLINE );
                writer.writeStartElement( "Row" );
                writer.writeAttribute( "ss:AutoFitHeight", "0" );
                writer.writeAttribute( "ss:Height", formatter.format( calculateHeight( item ) ) );
                writeLabel( writer, item.getApplication() );
                writeLabel( writer, item.getGroup() );
                writeLabel( writer, item.getLabel() );
                for( Language language : LanguageConfigurator.getLanguages() ) {
                    writeCell( writer, item.getFieldsForLanguage( language ).getText() );
                }
                writer.writeEndElement();
            }

            writer.writeEndElement();

            writer.flush();
            writer.close();
        } catch ( XMLStreamException xmle ) {
            throw new RuntimeException( xmle );
        }

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File templateFile = new File( classLoader.getResource( templateResource ).getFile() );
            String excel = FileUtils.readFileToString( templateFile, "UTF-8" );

            Pattern pattern = Pattern.compile( "<Worksheet ss:Name=\\\"Sheet1\\\">(.*)<WorksheetOptions",
                    Pattern.MULTILINE | Pattern.DOTALL );
            Matcher matcher = pattern.matcher( excel );

            if( matcher.find() ) {
                String original = matcher.group( 1 );
                excel = excel.replace( original, xml.toString() );
                excel = excel.replaceAll( "ss:ExpandedRowCount=\".{1,5}\"",
                        "ss:ExpandedRowCount=\"" + (localizedTexts.size() + 1) + "\"" );
            }

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter( outputStream, "UTF-8" );
            outputStreamWriter.write( excel );
            outputStreamWriter.close();
        } catch ( IOException ioe ) {
            throw new RuntimeException( "Error while writing to output stream", ioe );
        }
    }

    public void close() throws IOException {
        IOUtils.closeQuietly( outputStream );
    }

    private void writeHeader( XMLStreamWriter xml, String text ) throws XMLStreamException {
        xml.writeStartElement( "Cell" );
        xml.writeAttribute( SS_STYLE_ID, "s76" );

        xml.writeStartElement( "Data" );
        xml.writeAttribute( "ss:Type", "String" );
        xml.writeCharacters( text );
        xml.writeEndElement();

        xml.writeEndElement();
    }

    private void writeLabel( XMLStreamWriter xml, String text ) throws XMLStreamException {
        xml.writeStartElement( "Cell" );

        xml.writeStartElement( "Data" );
        xml.writeAttribute( "ss:Type", "String" );
        xml.writeCharacters( (text == null) ? "" : text );
        xml.writeEndElement();

        xml.writeEndElement();
    }

    private void writeCell( XMLStreamWriter xml, String text ) throws XMLStreamException {
        xml.writeStartElement( "Cell" );
        xml.writeAttribute( SS_STYLE_ID, "s67" );

        xml.writeStartElement( "Data" );
        xml.writeAttribute( "ss:Type", "String" );
        xml.writeCharacters( (text == null) ? "" : text );
        xml.writeEndElement();

        xml.writeEndElement();
    }

    private BigDecimal calculateHeight( LocalizedText item ) {
        int textLength = 0;
        for( Language language : LanguageConfigurator.getLanguages() ) {
            textLength = Math.max( textLength, StringUtils.length( item.getFieldsForLanguage( language ).getText() ) );
        }
        double multiplier = 1;
        if( textLength > 50 ) {
            multiplier = Math.ceil( textLength / 50D );
        }
        return new BigDecimal( "20.25" ).multiply( new BigDecimal( multiplier ) );
    }
}
