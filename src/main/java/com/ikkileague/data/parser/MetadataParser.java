package com.ikkileague.data.parser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ikkileague.data.exception.ConversionException;
import com.ikkileague.data.model.ColumnDefinition;
import com.ikkileague.data.model.ColumnType;

public class MetadataParser {
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT; // Format CSV par défaut (RFC 4180)
    private static final Logger logger = LoggerFactory.getLogger(MetadataParser.class);

    /**
     * Analyse le fichier de métadonnées et retourne une liste d'objets ColumnDefinition.
     *
     * @param metadataFilePath Le chemin du fichier CSV de métadonnées.
     * @return Une liste d'objets ColumnDefinition.
     * @throws ConversionException En cas d'erreur de lecture ou de format incorrect.
     */
    public List<ColumnDefinition> parse(Path metadataFilePath) throws ConversionException {
        List<ColumnDefinition> definitions = new ArrayList<>();

        // Vérifie si le fichier existe et est bien un fichier standard
        if (metadataFilePath == null || !Files.exists(metadataFilePath) || !Files.isRegularFile(metadataFilePath)) {
            String errorMessage = "Le fichier de métadonnées n'existe pas ou n'est pas un fichier valide : " + metadataFilePath;
            logger.error(errorMessage);
            throw new ConversionException(errorMessage);
        }

        try (Reader reader = Files.newBufferedReader(metadataFilePath, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSV_FORMAT)) {

            int lineNumber = 0;
            for (CSVRecord csvRecord : csvParser) {
                lineNumber++;
                // Ignore les lignes vides (Apache Commons CSV peut les retourner)
                if (csvRecord.size() == 0) {
                    continue;
                }
                definitions.add(parseRecord(csvRecord, lineNumber));
            }

            // Vérifie que le fichier contient bien au moins une définition
            if (definitions.isEmpty()) {
                String errorMessage = "Le fichier de métadonnées est vide ou ne contient aucune définition valide : "
                        + metadataFilePath;
                logger.error(errorMessage);
                throw new ConversionException(errorMessage);
            }

        } catch (IOException e) {
            throw new ConversionException("Erreur lors de la lecture du fichier de métadonnées : " + metadataFilePath, e);
        }
        return definitions;
    }

    /**
     * Analyse une ligne du fichier CSV et crée une instance de ColumnDefinition.
     *
     * @param csvRecord La ligne CSV à analyser.
     * @param lineNumber Le numéro de ligne dans le fichier (pour les messages d’erreur).
     * @return Une instance de ColumnDefinition.
     * @throws ConversionException En cas d’erreur de format ou de données invalides.
     */
    private ColumnDefinition parseRecord(CSVRecord csvRecord, int lineNumber) throws ConversionException {
        if (csvRecord.size() != 3) {
            throw new ConversionException(String.format(
                    "Format de ligne invalide à la ligne %d : 3 éléments attendus (nom, longueur, type), %d trouvés. Ligne : '%s'",
                    lineNumber, csvRecord.size(), csvRecord.toList()));
        }

        String name = csvRecord.get(0).trim();
        int length;
        try {
            length = Integer.parseInt(csvRecord.get(1).trim());
            if (length <= 0) {
                throw new ConversionException(String.format(
                        "Longueur de colonne invalide à la ligne %d : la longueur doit être un entier strictement positif. Ligne : '%s'",
                        lineNumber, csvRecord.toList()));
            }
        } catch (NumberFormatException e) {
            throw new ConversionException(String.format(
                    "Longueur de colonne invalide à la ligne %d : valeur non numérique. Ligne : '%s'",
                    lineNumber, csvRecord.toList()), e);
        }

        ColumnType type;
        try {
            type = ColumnType.fromName(csvRecord.get(2).trim());
        } catch (Exception e) {
            throw new ConversionException(String.format(
                    "Type de colonne invalide à la ligne %d : type inconnu '%s'. Types attendus : '%s'. Ligne : '%s'",
                    lineNumber, csvRecord.get(2).trim(), ColumnType.getAllNamesAsString(), csvRecord.toList()), e);
        }

        return new ColumnDefinition(name, length, type);
    }
}
