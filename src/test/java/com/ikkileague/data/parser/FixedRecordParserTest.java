package com.ikkileague.data.parser;

import com.ikkileague.data.exception.ConversionException;
import com.ikkileague.data.model.ColumnDefinition;
import com.ikkileague.data.model.ColumnType; // Ensure this import matches your ColumnType enum definition
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach; // Import BeforeEach
import org.junit.jupiter.api.Test;
import org.slf4j.Logger; // Import SLF4J Logger
import org.slf4j.LoggerFactory; // Import SLF4J LoggerFactory

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//mvn -Dtest=FixedRecordParserTest test

class FixedRecordParserTest {

    private static final Logger logger = LoggerFactory.getLogger(FixedRecordParserTest.class);

    private FixedRecordParser parser; // Declare the parser here

    @BeforeEach
    void setUp() {
        // Initialize the parser before each test to ensure a clean state
        parser = new FixedRecordParser();
        logger.info("--- Setting up FixedRecordParserTest for a new test ---");
    }

    @Test
    @DisplayName("Should correctly parse a fixed-format line based on definitions")
    void parseLine_validLine_returnsCorrectFields() throws ConversionException {
        logger.info("Starting test: parseLine_validLine_returnsCorrectFields");

        // Given
        List<ColumnDefinition> definitions = Arrays.asList(
                new ColumnDefinition("Date", 10, ColumnType.DATE),
                new ColumnDefinition("FullName", 15, ColumnType.STRING), // Use STRING or CHAIN based on your ColumnType
                new ColumnDefinition("Weight", 5, ColumnType.NUMERIC) // Use NUMBER or NUMERIC based on your ColumnType
        );
        String fixedLine = "1970-01-01John Smith     81.5 "; // Total length = 10 + 15 + 5 = 30
        logger.debug("Given definitions: {}", definitions);
        logger.debug("Given fixed line: '{}'", fixedLine);

        // When
        List<String> fields = parser.parseLine(fixedLine, definitions, 1);
        logger.debug("Parsed fields: {}", fields);

        // Then
        assertNotNull(fields);
        assertEquals(3, fields.size());
        assertEquals("1970-01-01", fields.get(0));
        assertEquals("John Smith     ", fields.get(1));
        assertEquals("81.5 ", fields.get(2));
        logger.info("Test passed: Successfully parsed valid line.");
    }

    @Test
    @DisplayName("Should handle UTF-8 characters correctly")
    void parseLine_withUtf8Chars_returnsCorrectFields() throws ConversionException {
        logger.info("Starting test: parseLine_withUtf8Chars_returnsCorrectFields");

        // Given
        List<ColumnDefinition> definitions = Arrays.asList(
                new ColumnDefinition("City", 10, ColumnType.STRING), // Use STRING or CHAIN
                new ColumnDefinition("Name", 10, ColumnType.STRING) // Use STRING or CHAIN
        );
        String fixedLine = "éèçà$ïî   Rémi      "; // Total length = 10 + 10 = 20
        logger.debug("Given definitions: {}", definitions);
        logger.debug("Given fixed line with UTF-8: '{}'", fixedLine);

        // When
        List<String> fields = parser.parseLine(fixedLine, definitions, 1);
        logger.debug("Parsed fields with UTF-8: {}", fields);

        // Then
        assertNotNull(fields);
        assertEquals(2, fields.size());
        assertEquals("éèçà$ïî   ", fields.get(0)); // Note: the space is part of the 10 chars
        assertEquals("Rémi      ", fields.get(1));
        logger.info("Test passed: Handled UTF-8 characters correctly.");
    }

    @Test
    @DisplayName("Should throw exception if line is shorter than expected total length")
    void parseLine_lineTooShort_throwsException() {
        logger.info("Starting test: parseLine_lineTooShort_throwsException");

        // Given
        List<ColumnDefinition> definitions = Arrays.asList(
                new ColumnDefinition("Name", 10, ColumnType.STRING), // Use STRING or CHAIN
                new ColumnDefinition("Age", 3, ColumnType.NUMERIC) // Use NUMBER or NUMERIC
        );
        String fixedLine = "NGUYEN 20"; // Expected 13, got 9
        logger.debug("Given definitions: {}", definitions);
        logger.debug("Given short fixed line: '{}'", fixedLine);

        // When / Then
        ConversionException thrown = assertThrows(ConversionException.class,
                () -> parser.parseLine(fixedLine, definitions, 5));
        logger.debug("Exception caught: {}", thrown.getMessage());

        assertTrue(thrown.getMessage().contains("la longueur (9) ne correspond pas à la longueur attendue (13)"));
        assertTrue(thrown.getMessage().contains("Ligne 5"));
        logger.info("Test passed: Correctly threw exception for line too short.");
    }

    @Test
    @DisplayName("Should throw exception if line is longer than expected total length")
    void parseLine_lineTooLong_throwsException() {
        logger.info("Starting test: parseLine_lineTooLong_throwsException");

        // Given
        List<ColumnDefinition> definitions = Arrays.asList(
                new ColumnDefinition("Nom", 5, ColumnType.STRING), // Use STRING or CHAIN
                new ColumnDefinition("Prenom", 10, ColumnType.STRING) // Use STRING or CHAIN
        );
        String fixedLine = "Rémi            Martin Martin"; // Total expected 15, given 29
        logger.debug("Given definitions: {}", definitions);
        logger.debug("Given fixed line for column length exceed: '{}'", fixedLine);

        // When / Then
        ConversionException thrown = assertThrows(ConversionException.class,
                () -> parser.parseLine(fixedLine, definitions, 3));
        logger.debug("Exception lineTooLong caught: {}", thrown.getMessage());

        assertTrue(thrown.getMessage().contains("la longueur (29) ne correspond pas à la longueur attendue (15)"));
        assertTrue(thrown.getMessage().contains("Ligne 3"));
        logger.info("Test passed: Correctly threw exception for line too short.");
    }

    @Test
    @DisplayName("Should handle empty definitions list")
    void parseLine_emptyDefinitions_returnsEmptyList() throws ConversionException {
        logger.info("Starting test: parseLine_emptyDefinitions_returnsEmptyList");

        // Given
        List<ColumnDefinition> definitions = Arrays.asList();
        String fixedLine = "";
        logger.debug("Given empty definitions list. Fixed line: '{}'", fixedLine);

        // When
        List<String> fields = parser.parseLine(fixedLine, definitions, 1);
        logger.debug("Parsed fields: {}", fields);

        // Then
        assertNotNull(fields);
        assertTrue(fields.isEmpty());
        logger.info("Test passed: Handled empty definitions list correctly.");
    }
}