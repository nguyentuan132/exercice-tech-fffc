package com.ikkileague.data.writer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ikkileague.data.exception.ConversionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// mvn -Dtest=CsvWriterTest test
class CsvWriterTest {
    private static final Logger logger = LoggerFactory.getLogger(CsvWriterTest.class);

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should write header correctly to the CSV file")
    void writeHeader_correctlyWrites() throws IOException, ConversionException {
        // Given
        Path outputFile = tempDir.resolve("output_header.csv");
        List<String> headers = Arrays.asList("Col1", "Col2", "Col3");

        // When
        // The header is written upon CsvWriter initialization
        try (CsvWriter writer = new CsvWriter(outputFile, headers)) {
            // Header écrit automatiquement
        } catch (ConversionException e) {
            logger.error("Erreur lors de la création du CsvWriter : {}", e.getMessage(), e);
        }
        // Then
        String content = Files.readString(outputFile);
        assertEquals("Col1,Col2,Col3\r\n", content); // Verify content including CRLF
    }

    @Test
    @DisplayName("Should write a single record correctly to the CSV file")
    void writeRecord_singleRecord_correctlyWrites() throws IOException, ConversionException {
        // Given
        Path outputFile = tempDir.resolve("output_single_record.csv");
        List<String> headers = Arrays.asList("Name", "Age");
        List<String> recordLine = Arrays.asList("Alice", "30");

        // When
        try (CsvWriter writer = new CsvWriter(outputFile, headers)) {
            writer.writeRecord(recordLine);
        }

        // Then
        String content = Files.readString(outputFile);
        logger.debug("One records content csv : \n{}", content);
        assertEquals("Name,Age\r\nAlice,30\r\n", content);
    }

    @Test
    @DisplayName("Should write multiple records correctly to the CSV file")
    void writeRecord_multipleRecords_correctlyWrites() throws IOException,
            ConversionException {
        // Given
        Path outputFile = tempDir.resolve("output_multiple_records.csv");
        List<String> headers = Arrays.asList("Header1", "Header2");
        List<String> record1 = Arrays.asList("Value1A", "Value1B");
        List<String> record2 = Arrays.asList("Value2A", "Value2B");

        // When
        try (CsvWriter writer = new CsvWriter(outputFile, headers)) {
            writer.writeRecord(record1);
            writer.writeRecord(record2);
        }

        // Then
        String content = Files.readString(outputFile);
        logger.debug("Multiple records content csv : \n{}", content);

        assertEquals("Header1,Header2\r\nValue1A,Value1B\r\nValue2A,Value2B\r\n",
                content);
    }

    @Test
    @DisplayName("Should handle commas in fields by quoting them")
    void writeRecord_fieldWithComma_isQuoted() throws IOException,
            ConversionException {
        // Given
        Path outputFile = tempDir.resolve("output_comma_field.csv");
        List<String> headers = Arrays.asList("Description");
        List<String> record = Arrays.asList("Item, with comma");

        // When
        try (CsvWriter writer = new CsvWriter(outputFile, headers)) {
            writer.writeRecord(record);
        }

        // Then
        String content = Files.readString(outputFile);
        logger.debug("Content for item, with comma  : \n{}", content);

        assertEquals("Description\r\n\"Item, with comma\"\r\n", content);
    }

    @Test
    @DisplayName("Should handle fields with double quotes by escaping them")
    void writeRecord_fieldWithQuote_isEscaped() throws IOException,
            ConversionException {
        // Given
        Path outputFile = tempDir.resolve("output_quote_field.csv");
        List<String> headers = Arrays.asList("Text");
        List<String> record = Arrays.asList("He said \"Hello\"");

        // When
        try (CsvWriter writer = new CsvWriter(outputFile, headers)) {
            writer.writeRecord(record);
        }

        // Then
        String content = Files.readString(outputFile);
        logger.debug("Content for double quotes by escaping them, with comma  : \n{}", content);

        // Apache Commons CSV escapes double quotes by another double quote
        assertEquals("Text\r\n\"He said \"\"Hello\"\"\"\r\n", content);
    }

    @Test
    @DisplayName("Should throw ConversionException if unable to write record")
    void writeRecord_ioException_throwsConversionException() {
        // Given
        Path outputFile = tempDir.resolve("output_fail_write.csv");
        List<String> headers = Arrays.asList("H1");
        List<String> record = Arrays.asList("R1");

        CsvWriter writer = null;
        try {
            writer = new CsvWriter(outputFile, headers);
            writer.close(); // Intentionally close to simulate a write error
        } catch (Exception e) {
            fail("Setup failed: " + e.getMessage());
        }

        // When / Then (combined for exception assertion)
        CsvWriter finalWriter = writer; // Need effectively final variable for lambda
        assertThrows(ConversionException.class, () -> finalWriter.writeRecord(record),
                "Should throw ConversionException on write error");
    }
}
