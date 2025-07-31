package com.ikkileague.data.formatter;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ikkileague.data.exception.ConversionException;
import com.ikkileague.data.model.ColumnType;

public class DataFormatter {

    private static final Logger logger = LoggerFactory.getLogger(DataFormatter.class);
    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Formate un champ brut en fonction de son type de colonne.
     *
     * @param rawField Le champ brut issu du fichier à largeur fixe.
     * @param type     Le type de la colonne (DATE, NUMERIC, STRING).
     * @return Le champ formaté.
     * @throws ConversionException En cas d’erreur de formatage (date invalide,
     *                             nombre incorrect, caractères interdits...).
     */
    public String format(String rawField, ColumnType type) throws ConversionException {
        // La méthode trim() supprime les espaces en début et fin de chaîne (y compris
        // tabulations, retours à la ligne, etc.)
        // C’est souvent nécessaire pour les champs DATE et NUMERIC afin d’éviter les
        // erreurs de parsing.
        String trimmedField = rawField.trim();

        switch (type) {
            case DATE:
                return formatDate(trimmedField);
            case NUMERIC:
                return formatNumeric(trimmedField);
            case STRING:
                return formatString(rawField); // On garde rawField ici pour détecter les caractères interdits avant le
                                               // nettoyage
            default:
                // Cas de sécurité si de nouveaux types sont ajoutés sans être pris en charge
                String errorMessage = "Type de colonne inconnu ou non géré : " + type;
                logger.error(errorMessage);
                throw new ConversionException(errorMessage);
        }
    }

    // Formate une date du format AAAA-MM-JJ vers JJ/MM/AAAA
    private String formatDate(String rawDate) throws ConversionException {
        logger.trace("Formatage de la date : '{}'", rawDate);
        if (rawDate.isEmpty()) {
            return ""; // Retourne une chaîne vide si la date est vide. À adapter selon les règles
                       // métier.
        }
        try {
            return OUTPUT_DATE_FORMAT.format(INPUT_DATE_FORMAT.parse(rawDate));
        } catch (DateTimeParseException e) {
            String errorMessage = "Format de date invalide : '" + rawDate + "'. Format attendu : YYYY-MM-DD.";
            logger.error(errorMessage, e); // Log avec la trace complète
            throw new ConversionException(errorMessage, e);
        }
    }

    // Convertit une chaîne en nombre décimal
    private String formatNumeric(String rawNumber) throws ConversionException {
        logger.trace("Formatage d'un champ numérique : '{}'", rawNumber);

        if (rawNumber == null || rawNumber.isBlank()) {
            return ""; // Retourne une chaîne vide si le champ est vide ou nul
        }

        String trimmed = rawNumber.trim();

        try {
            // Si la chaîne contient un point, on tente une conversion en double
            if (trimmed.contains(".")) {
                return String.valueOf(Double.parseDouble(trimmed));
            } else {
                // Sinon, on tente une conversion en entier
                return String.valueOf(Integer.parseInt(trimmed));
            }
        } catch (NumberFormatException e) {
            String errorMessage = "Format numérique invalide : '" + rawNumber + "'.";
            logger.error(errorMessage, e);
            throw new ConversionException(errorMessage, e);
        }
    }

    // Nettoie une chaîne et vérifie l’absence de caractères interdits
    private String formatString(String rawString) throws ConversionException {
        // Supprime uniquement les espaces en fin de chaîne
        String trimmedString = rawString.stripTrailing(); 

        // Règle métier : les retours à la ligne ne sont pas autorisés dans les champs
        // texte
        if (trimmedString.contains("\r") || trimmedString.contains("\n")) {
            String errorMessage = "Le champ texte contient des caractères interdits (CR or LF) : '"
                    + rawString + "'.";
            logger.error(errorMessage);
            throw new ConversionException(errorMessage);
        }
        logger.trace("Formatage de la chaîne : '{}' -> '{}'", rawString, trimmedString);
        return trimmedString;
    }
}
