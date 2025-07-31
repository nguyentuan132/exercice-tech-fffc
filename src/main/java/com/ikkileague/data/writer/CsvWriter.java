package com.ikkileague.data.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ikkileague.data.exception.ConversionException;

public class CsvWriter implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(CsvWriter.class);

    private final CSVPrinter csvPrinter;
    private final BufferedWriter writer;



    public CsvWriter(Path outputPath, List<String> headers) throws ConversionException {
        logger.info("Initializing CSV writer for output file: {}", outputPath);
        logger.debug("CSV Headers: {}", headers);
        try {
            this.writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);

            // Configuration du format CSV : séparateur ',', séparateur de ligne CRLF (RFC 4180)
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader(headers.toArray(new String[0])) // remplace withHeader
                    .setRecordSeparator("\r\n") // CRLF
                    .setDelimiter(',') // virgule
                    .build();

            this.csvPrinter = new CSVPrinter(writer, format);
        } catch (IOException e) {
            throw new ConversionException("Error initializing CSV writer for file: " + outputPath, e);
        }
    }

    public void writeRecord(List<String> record) throws ConversionException {
        try {
            csvPrinter.printRecord(record);
            logger.trace("Record written: {}", record);
        } catch (IOException e) {
            logger.error("Error writing record to CSV file: {}. Message: {}", record, e.getMessage(), e);
            throw new ConversionException("Error writing record to CSV file: " + record, e);
        }
    }

    @Override
    public void close() throws IOException {
        if (csvPrinter != null) {
            logger.info("Closing CSV writer.");
            csvPrinter.close();
        }
    }
}