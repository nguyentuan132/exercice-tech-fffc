package com.ikkileague.data.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ikkileague.data.exception.ConversionException;
import com.ikkileague.data.model.ColumnDefinition;
import com.ikkileague.data.model.ColumnType;
//mvn -Dtest=MetadataParserTest test

class MetadataParserTest {

    private static final Logger logger = LoggerFactory.getLogger(MetadataParserTest.class);

    @TempDir
    Path tempDir; // Crée un répertoire temporaire pour les tests

    @Test
    @DisplayName("Should correctly parse a valid metadata file")
    void parse_validFile_returnsCorrectDefinitions() throws IOException, ConversionException {
        logger.info("*** Starting test: Should correctly parse a valid metadata file ***");

        // Given
        Path metadataFilePath = tempDir.resolve("metadata.csv");

        String content = "Date de naissance,10,date\nPrénom,15,chaîne\nNom de famille,15,chaîne\nPoids,5,numérique";
        Files.writeString(metadataFilePath, content);

        MetadataParser parser = new MetadataParser();

        // When
        List<ColumnDefinition> definitions = parser.parse(metadataFilePath);

        for (ColumnDefinition def : definitions) {
            logger.debug("Parsed column: {}", def);
        }

        // Then
        assertNotNull(definitions);
        assertEquals(4, definitions.size());
        assertEquals(new ColumnDefinition("Date de naissance", 10, ColumnType.DATE), definitions.get(0));
        assertEquals(new ColumnDefinition("Prénom", 15, ColumnType.STRING), definitions.get(1));
        assertEquals(new ColumnDefinition("Nom de famille", 15, ColumnType.STRING), definitions.get(2));
        assertEquals(new ColumnDefinition("Poids", 5, ColumnType.NUMERIC), definitions.get(3));
    }

    @Test
    @DisplayName("Should correctly parse a valid metadata file with an empty line")
    void parse_validFileWithEmptyLine_returnsCorrectDefinitions() throws IOException, ConversionException {
        logger.info("*** Starting test: Should correctly parse a valid metadata file ***");

        // Given
        Path metadataFilePath = tempDir.resolve("metadata.csv");

        String content = "Date de naissance,10,date\nPrénom,15,chaîne\r\n\r\nPoids,5,numérique";
        Files.writeString(metadataFilePath, content);

        MetadataParser parser = new MetadataParser();

        // When
        List<ColumnDefinition> definitions = parser.parse(metadataFilePath);

        for (ColumnDefinition def : definitions) {
            logger.debug("Parsed column: {}", def);
        }

        // Then
        assertNotNull(definitions);
        assertEquals(3, definitions.size());
        assertEquals(new ColumnDefinition("Date de naissance", 10, ColumnType.DATE), definitions.get(0));
        assertEquals(new ColumnDefinition("Prénom", 15, ColumnType.STRING), definitions.get(1));
        assertEquals(new ColumnDefinition("Poids", 5, ColumnType.NUMERIC), definitions.get(2));
    }

    @Test
    @DisplayName("Should throw exception for invalid number of columns inmetadata file")
    void parse_invalidColumnCount_throwsException() throws IOException {
        logger.info("*** Starting test: Should throw exception for invalid number of columns inmetadata file ***");

        // Given
        Path metadataFilePath = tempDir.resolve("invalid_metadata.csv");
        String content = "Name,10,chaîne\nAge,5"; // Ligne incorrecte (2 colonnes aulieu de 3)
        Files.writeString(metadataFilePath, content);

        MetadataParser parser = new MetadataParser();

        // When / Then
        ConversionException thrown = assertThrows(ConversionException.class, () -> parser.parse(metadataFilePath));
        logger.debug("Invalid metadata Exception : {}", thrown.getMessage());
        assertTrue(thrown.getMessage().contains("Format de ligne invalide à la ligne 2 : 3 éléments attendus"));
    }

    @Test
    @DisplayName("Should throw exception for unknown column type in metadatafile")
    void parse_unknownColumnType_throwsException() throws IOException {
        logger.info("*** Starting test:Should throw exception for unknown column type in metadatafile ***");

        // Given
        Path metadataFilePath = tempDir.resolve("unknown_type_metadata.csv");
        String content = "Name,10,undefined\nAge,5,numérique";

        Files.writeString(metadataFilePath, content);

        MetadataParser parser = new MetadataParser();

        // When / Then
        ConversionException thrown = assertThrows(ConversionException.class, () -> parser.parse(metadataFilePath));
        logger.debug("Unknown column Exception : {}", thrown.getMessage());
        assertTrue(thrown.getMessage().contains("Type de colonne invalide à la ligne 1 : type inconnu 'undefined'."));
    }

    @Test
    @DisplayName("Should throw exception for non-integer length in metadatafile")
    void parse_nonIntegerLength_throwsException() throws IOException {
        logger.info("*** Starting test:Should throw exception for non-integer length in metadatafile ***");

        // Given
        Path metadataFilePath = tempDir.resolve("non_int_length_metadata.csv");
        String content = "Name,0.5,string\nAge,5,numeric";
        Files.writeString(metadataFilePath, content);

        MetadataParser parser = new MetadataParser();

        // When / Then
        ConversionException thrown = assertThrows(ConversionException.class, () -> parser.parse(metadataFilePath));
        logger.debug("Not a valid integer Exception : {}", thrown.getMessage());

        assertTrue(thrown.getMessage().contains("Longueur de colonne invalide à la ligne 1 : valeur non numérique."));
    }

    @Test
    @DisplayName("Devrait lancer une exception si la longueur est un nombre négatif ou zéro")
    void parse_non_positive_length_throwsException() throws IOException {
        logger.info("*** Démarrage du test : Devrait lancer une exception si la longueur est négative ou zéro ***");

        // Given
        Path metadataFilePath = tempDir.resolve("negative_zero_length_metadata.csv");
        // Le contenu contient une longueur négative
        String content = "Name,-1,chaîne\nAge,0,numérique";
        Files.writeString(metadataFilePath, content);

        MetadataParser parser = new MetadataParser();

        // When / Then
        // La première ligne échouera avec la longueur négative
        ConversionException thrown = assertThrows(ConversionException.class, () -> parser.parse(metadataFilePath));
        logger.debug("Exception capturée : {}", thrown.getMessage());

        // On s'attend à ce que le message contienne "Length must be a positive integer"
        logger.debug("La longueur est négative Exception : {}", thrown.getMessage());

        assertTrue(thrown.getMessage().contains(
                "Longueur de colonne invalide à la ligne 1 : la longueur doit être un entier strictement positif"));
        assertTrue(thrown.getMessage().contains("Name, -1, chaîne")); // Vérifier le contenu de la ligne incriminée
    }

    @Test
    @DisplayName("Devrait lancer une exception si le fichier de métadonnées est vide")
    void parse_emptyMetadataFile_throwsException() throws IOException {
        logger.info("*** Démarrage du test : Devrait lancer une exception si le fichier de métadonnées est vide ***");

        // Given
        Path metadataFilePath = tempDir.resolve("empty_metadata.csv");
        // Créer un fichier vide
        Files.writeString(metadataFilePath, ""); // Le fichier est explicitement vide

        MetadataParser parser = new MetadataParser();

        // When / Then
        ConversionException thrown = assertThrows(ConversionException.class, () -> parser.parse(metadataFilePath));
        logger.debug("Exception capturée : {}", thrown.getMessage());

        assertTrue(thrown.getMessage()
                .contains("Le fichier de métadonnées est vide ou ne contient aucune définition valide"));
        assertTrue(thrown.getMessage().contains(metadataFilePath.getFileName().toString()));
        logger.info("Test passed: Correctement lancé l'exception pour un fichier de métadonnées vide.");
    }

    @Test
    @DisplayName("Devrait lancer une exception si le fichier de métadonnées n'existe pas")
    void parse_nonExistentMetadataFile_throwsException() {
        logger.info(
                "*** Démarrage du test : Devrait lancer une exception si le fichier de métadonnées n'existe pas ***");

        // Given
        Path nonExistentPath = tempDir.resolve("non_existent_metadata.csv");
        // Le fichier n'est pas créé, il n'existe donc pas

        MetadataParser parser = new MetadataParser();

        // When / Then
        ConversionException thrown = assertThrows(ConversionException.class, () -> parser.parse(nonExistentPath));
        logger.debug("Exception capturée : {}", thrown.getMessage());

        assertTrue(
                thrown.getMessage().contains("Le fichier de métadonnées n'existe pas ou n'est pas un fichier valide"));
        assertTrue(thrown.getMessage().contains(nonExistentPath.getFileName().toString()));
        logger.info("Test passed: Correctement lancé l'exception pour un fichier de métadonnées inexistant.");
    }
}
