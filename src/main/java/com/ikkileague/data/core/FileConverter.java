package com.ikkileague.data.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ikkileague.data.exception.ConversionException;
import com.ikkileague.data.formatter.DataFormatter;
import com.ikkileague.data.model.ColumnDefinition;
import com.ikkileague.data.parser.FixedRecordParser;
import com.ikkileague.data.parser.MetadataParser;
import com.ikkileague.data.writer.CsvWriter;

public class FileConverter {

    private static final Logger logger = LoggerFactory.getLogger(FileConverter.class);

    private final MetadataParser metadataParser;
    private final FixedRecordParser fixedRecordParser;
    private final DataFormatter dataFormatter;

    public FileConverter(MetadataParser metadataParser, FixedRecordParser fixedRecordParser,
            DataFormatter dataFormatter) {
        this.metadataParser = metadataParser;
        this.fixedRecordParser = fixedRecordParser;
        this.dataFormatter = dataFormatter;
    }

    public FileConverter() {
        this(new MetadataParser(), new FixedRecordParser(), new DataFormatter());
    }

    public void convert(Path fixedFilePath, Path metadataFilePath, Path outputFilePath) throws ConversionException {
        logger.info("Début de la conversion : Fichier fixe '{}' + Métadonnées '{}' -> CSV '{}'",
                fixedFilePath, metadataFilePath, outputFilePath);

        // 1. Lecture du fichier de métadonnées pour récupérer les définitions de
        // colonnes
        List<ColumnDefinition> columnDefinitions = metadataParser.parse(metadataFilePath);
        if (columnDefinitions.isEmpty()) {
            throw new ConversionException(
                    "Aucune définition de colonne trouvée dans le fichier de métadonnées : " + metadataFilePath);
        }
        logger.debug("Définitions de {} colonnes extraites depuis les métadonnées.", columnDefinitions.size());
        logger.trace("Définitions de colonnes : {}", columnDefinitions);

        // Extraction des en-têtes pour le fichier CSV
        List<String> headers = columnDefinitions.stream()
                .map(ColumnDefinition::getName)
                .collect(Collectors.toList());

        // Vérifie que le fichier d'entrée existe et est un fichier régulier
        if (fixedFilePath == null || !Files.exists(fixedFilePath) || !Files.isRegularFile(fixedFilePath)) {
            String message = "Fichier d'entrée manquant ou non valide: " + fixedFilePath;
            throw new ConversionException(message);
        }

        // 2. Lecture ligne par ligne du fichier fixe et écriture dans le fichier CSV
        try (BufferedReader fixedFileReader = Files.newBufferedReader(fixedFilePath, StandardCharsets.UTF_8);
                CsvWriter csvWriter = new CsvWriter(outputFilePath, headers)) { // CsvWriter écrit les en-têtes
                                                                                // automatiquement ici

            String fixedLine;
            long lineNumber = 0;
            while ((fixedLine = fixedFileReader.readLine()) != null) {
                lineNumber++;
                if (fixedLine.trim().isEmpty()) {
                    logger.debug("Ligne vide ignorée à la ligne {} du fichier source.", lineNumber);
                    continue;
                }

                // Découpage de la ligne fixe en champs bruts
                List<String> rawFields = fixedRecordParser.parseLine(fixedLine, columnDefinitions, lineNumber);
                List<String> formattedFields = new ArrayList<>();
                logger.trace("Ligne {} parsée en champs bruts : {}", lineNumber, rawFields);

                // Formatage de chaque champ selon sa définition
                for (int i = 0; i < rawFields.size(); i++) {
                    String rawField = rawFields.get(i);
                    ColumnDefinition definition = columnDefinitions.get(i);
                    try {
                        String formattedField = dataFormatter.format(rawField, definition.getType());
                        formattedFields.add(formattedField);
                        logger.trace("Champ '{}' (type {}) formaté en '{}'", rawField, definition.getType(),
                                formattedField);
                    } catch (ConversionException e) {
                        // Enrichissement du message d’erreur avec la ligne et la colonne concernées
                        String errorMessage = String.format(
                                "Erreur lors du traitement du champ pour la colonne '%s' (type %s) à la ligne %d : %s",
                                definition.getName(), definition.getType(), lineNumber, e.getMessage());
                        logger.error(errorMessage, e); // Log de l’erreur avec trace complète
                        throw new ConversionException(errorMessage, e);
                    }
                }
                csvWriter.writeRecord(formattedFields);
                logger.debug("Ligne {} traitée et écrite avec succès dans le CSV.", lineNumber);
            }
            logger.info("Fichier fixe entièrement traité. Nombre total de lignes : {}", lineNumber);

        } catch (IOException e) {
            String errorMessage = "Une erreur d'entrée/sortie est survenue lors de la conversion : " + e.getMessage();
            logger.error(errorMessage, e); // Log de l’erreur I/O avec trace complète
            throw new ConversionException(errorMessage, e);
        }
    }
}
