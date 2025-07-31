package com.ikkileague.data.formatter;

import com.ikkileague.data.exception.ConversionException;
import com.ikkileague.data.model.ColumnType; // Assurez-vous que ColumnType est correctement importé
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class DataFormatterTest {

    private static final Logger logger = LoggerFactory.getLogger(DataFormatterTest.class);

    private DataFormatter dataFormatter;

    @BeforeEach
    void setUp() {
        logger.debug("--- Préparation de DataFormatterTest pour un nouveau test ---");
        dataFormatter = new DataFormatter();
    }

    // --- Tests pour le formatage des DATES ---

    @Test
    @DisplayName("Devrait formater une date valide (YYYY-MM-DD) en DD/MM/YYYY")
    void format_validDate_returnsFormattedString() throws ConversionException {
        logger.debug("Démarrage du test: format_validDate_returnsFormattedString");
        // ÉTANT DONNÉ une date au format YYYY-MM-DD
        String rawDate = "2023-10-26";
        // QUAND la date est formatée comme type DATE
        String formattedDate = dataFormatter.format(rawDate, ColumnType.DATE);
        // ALORS le format doit être DD/MM/YYYY
        assertEquals("26/10/2023", formattedDate, "La date devrait être formatée en DD/MM/YYYY.");
        logger.debug("Test passé: Date valide formatée correctement.");
    }

    @Test
    @DisplayName("Devrait retourner une chaîne vide pour une date vide")
    void format_emptyDate_returnsEmptyString() throws ConversionException {
        logger.debug("Démarrage du test: format_emptyDate_returnsEmptyString");
        // ÉTANT DONNÉ une date vide
        String rawDate = "";
        // QUAND la date est formatée
        String formattedDate = dataFormatter.format(rawDate, ColumnType.DATE);
        // ALORS le résultat doit être une chaîne vide
        assertEquals("", formattedDate, "Une date vide devrait retourner une chaîne vide.");
        logger.debug("Test passé: Date vide gérée correctement.");
    }

    @Test
    @DisplayName("Devrait lancer une ConversionException pour un format de date invalide")
    void format_invalidDateFormat_throwsConversionException() {
        logger.debug("Démarrage du test: format_invalidDateFormat_throwsConversionException");
        // ÉTANT DONNÉ une date avec un format invalide
        String rawDate = "26/10/2023"; // Attendu YYYY-MM-DD
        // QUAND la date est formatée
        // ALORS une ConversionException doit être lancée
        ConversionException thrown = assertThrows(ConversionException.class,
                () -> dataFormatter.format(rawDate, ColumnType.DATE),
                "Devrait lancer ConversionException pour un format de date invalide.");
        logger.debug("Format de date error msg : {}", thrown.getMessage());

        assertTrue(thrown.getMessage().contains("Format de date invalide : '26/10/2023'. Format attendu : YYYY-MM-DD"),
                "Le message d'erreur doit indiquer le format de date attendu.");
    }

    // --- Tests pour le formatage des NOMBRES ---

    @Test
    @DisplayName("Devrait formater un nombre entier valide")
    void format_validIntegerNumeric_returnsString() throws ConversionException {
        logger.debug("Démarrage du test: format_validIntegerNumeric_returnsString");
        // ÉTANT DONNÉ un nombre entier
        String rawNUMERIC = "12345";
        // QUAND le nombre est formaté comme type NUMERIC
        String formattedNUMERIC = dataFormatter.format(rawNUMERIC,
                ColumnType.NUMERIC);
        // ALORS le nombre doit être converti en chaîne de caractères

        assertEquals("12345", formattedNUMERIC, "Un entier devrait être formaté correctement (en double).");
        // Note: Double.parseDouble ajoute .0
    }

    @Test
    @DisplayName("Devrait formater un nombre décimal valide")
    void format_validDecimalNumeric_returnsString() throws ConversionException {
        logger.debug("Démarrage du test: format_validDecimalNumeric_returnsString");
        // ÉTANT DONNÉ un nombre décimal
        String rawNUMERIC = "123.45";
        // QUAND le nombre est formaté
        String formattedNUMERIC = dataFormatter.format(rawNUMERIC,
                ColumnType.NUMERIC);
        // ALORS le nombre doit être converti en chaîne de caractères
        assertEquals("123.45", formattedNUMERIC, "Un nombre décimal devrait être formaté correctement.");
    }

    @Test
    @DisplayName("Devrait retourner une chaîne vide pour un nombre vide")
    void format_emptyNumeric_returnsEmptyString() throws ConversionException {
        logger.debug("Démarrage du test: format_emptyNumeric_returnsEmptyString");
        // ÉTANT DONNÉ un nombre vide
        String rawNUMERIC = "";
        // QUAND le nombre est formaté
        String formattedNUMERIC = dataFormatter.format(rawNUMERIC,
                ColumnType.NUMERIC);
        // ALORS le résultat doit être une chaîne vide
        assertEquals("", formattedNUMERIC, "Un nombre vide devrait retourner une chaîne vide.");
    }

    @Test
    @DisplayName("Devrait lancer une ConversionException pour un format numérique invalide")
    void format_invalidNumericFormat_throwsConversionException() {
        logger.debug("Démarrage du test: format_invalidNumericFormat_throwsConversionException");
        // ÉTANT DONNÉ un texte non numérique
        String rawNUMERIC = "abc";
        // QUAND le texte est formaté comme type NUMERIC
        // ALORS une ConversionException doit être lancée
        ConversionException thrown = assertThrows(ConversionException.class,
                () -> dataFormatter.format(rawNUMERIC, ColumnType.NUMERIC),
                "Devrait lancer ConversionException pour un format numérique invalide.");

        logger.debug("format numérique invalide msg : {} ", thrown.getMessage());

        assertTrue(thrown.getMessage().contains("Format numérique invalide"),
                "Le message d'erreur doit indiquer le format numérique invalide.");
    }

    // // --- Tests pour le formatage des CHAINES (STRING) ---

    @Test
    @DisplayName("Devrait formater une chaîne valide en supprimant les espaces de fin")
    void format_validStringWithTrailingSpaces_returnsTrimmedString() throws ConversionException {
        logger.debug("Démarrage du test: format_validStringWithTrailingSpaces_returnsTrimmedString");
        // ÉTANT DONNÉ une chaîne avec des espaces de fin
        String rawString = "Ceci est une chaîne ";
        // QUAND la chaîne est formatée comme type STRING
        String formattedString = dataFormatter.format(rawString, ColumnType.STRING);
        // ALORS les espaces de fin doivent être supprimés (stripTrailing)
        assertEquals("Ceci est une chaîne", formattedString, "Les espaces de fin devraient être supprimés.");
    }

    @Test
    @DisplayName("Devrait retourner la même chaîne pour une chaîne sans espaces de fin ni caractères interdits")
    void format_plainString_returnsSameString() throws ConversionException {
        logger.debug("Démarrage du test: format_plainString_returnsSameString");
        // ÉTANT DONNÉ une chaîne simple
        String rawString = "Ma  chaine";
        // QUAND la chaîne est formatée
        String formattedString = dataFormatter.format(rawString, ColumnType.STRING);
        // ALORS la chaîne doit rester inchangée (si pas d'espaces de fin)
        assertEquals("Ma  chaine", formattedString, "Une chaîne simple ne devrait pas  être modifiée.");
    }

    @Test
    @DisplayName("Devrait retourner une chaîne vide pour une chaîne vide")
    void format_emptyString_returnsEmptyString() throws ConversionException {
        logger.debug("Démarrage du test: format_emptyString_returnsEmptyString");
        // ÉTANT DONNÉ une chaîne vide
        String rawString = "";
        // QUAND la chaîne est formatée
        String formattedString = dataFormatter.format(rawString, ColumnType.STRING);
        // ALORS le résultat doit être une chaîne vide
        assertEquals("", formattedString, "Une chaîne vide devrait retourner une chaîne vide.");
    }

    @Test
    @DisplayName("Devrait lancer une ConversionException pour une chaîne  contenant des caractères interdits (CR/LF)")

    void format_stringWithForbiddenCharacters_throwsConversionException() {
        logger.debug("Démarrage du test: format_stringWithForbiddenCharacters_throwsConversionException");
        // ÉTANT DONNÉ une chaîne contenant un retour chariot ou un saut de ligne
        String rawStringCR = "Première ligne\rDeuxième ligne";
        String rawStringLF = "Première ligne\nDeuxième ligne";

        // QUAND la chaîne est formatée
        // ALORS une ConversionException doit être lancée pour CR
        ConversionException thrownCR = assertThrows(ConversionException.class,
                () -> dataFormatter.format(rawStringCR, ColumnType.STRING),
                "Devrait lancer ConversionException pour un CR.");

        logger.debug("Caractères interdits (CR) :\n {}", thrownCR.getMessage());
        assertTrue(thrownCR.getMessage().contains("Le champ texte contient des caractères interdits (CR or LF)"),
                "Le message d'erreur doit indiquer des caractères interdits (CR).");

        // ET une ConversionException doit être lancée pour LF
        ConversionException thrownLF = assertThrows(ConversionException.class,
                () -> dataFormatter.format(rawStringLF, ColumnType.STRING),
                "Devrait lancer ConversionException pour un LF.");

        logger.debug("Caractères interdits (LF) :\n {}", thrownCR.getMessage());
        assertTrue(thrownLF.getMessage().contains("Le champ texte contient des caractères interdits (CR or LF)"),
                "Le message d'erreur doit indiquer des caractères interdits (LF).");

    }
}