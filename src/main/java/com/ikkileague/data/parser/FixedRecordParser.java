package com.ikkileague.data.parser;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ikkileague.data.exception.ConversionException;
import com.ikkileague.data.model.ColumnDefinition;

public class FixedRecordParser {

    private static final Logger logger = LoggerFactory.getLogger(FixedRecordParser.class);

    /**
     * Analyse une ligne de données au format fixe en fonction des définitions de
     * colonnes.
     *
     * @param line              La ligne à analyser
     * @param columnDefinitions La liste des colonnes avec leur position et longueur
     * @param lineNumber        Le numéro de ligne dans le fichier (utile pour les
     *                          erreurs)
     * @return Une liste de champs extraits
     * @throws ConversionException si la ligne ne correspond pas à la structure
     *                             attendue
     */
    public List<String> parseLine(String line, List<ColumnDefinition> columnDefinitions, long lineNumber)
            throws ConversionException {

        List<String> fields = new ArrayList<>();
        int currentPosition = 0;

        // Calcule la longueur totale attendue de la ligne en fonction des définitions
        int expectedTotalLength = columnDefinitions.stream().mapToInt(ColumnDefinition::getLength).sum();

        logger.debug("parseLine line {} : {}", line.length(), line);
        logger.debug("parseLine columnDefinitions {} : {}", columnDefinitions.size(), columnDefinitions.toArray());

        // Vérifie si la longueur de la ligne correspond bien à la somme des longueurs
        // des colonnes
        if (line.length() != expectedTotalLength) {
            String errorMessage = String.format(
                    "Ligne %d : la longueur (%d) ne correspond pas à la longueur attendue (%d). Ligne : '%s'",
                    lineNumber, line.length(), expectedTotalLength, line);
            logger.error(errorMessage);
            throw new ConversionException(errorMessage);
        }

        // Extraction de chaque champ selon sa position et longueur définies
        for (int i = 0; i < columnDefinitions.size(); i++) {
            ColumnDefinition definition = columnDefinitions.get(i);
            int length = definition.getLength();

            // Découpe le champ à partir de la position actuelle
            String field = line.substring(currentPosition, currentPosition + length);
            logger.debug("Champ extrait de longueur {} : {}", field.length(), field);

            fields.add(field);
            logger.trace("Ligne {} : champ extrait pour '{}' (longueur {}) à partir de '{}' -> '{}'",
                    lineNumber, definition.getName(), length, field, field);

            // Mise à jour de la position pour le champ suivant
            currentPosition += length;
        }

        return fields;
    }
}
