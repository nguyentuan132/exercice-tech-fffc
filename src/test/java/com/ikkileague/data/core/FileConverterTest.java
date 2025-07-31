package com.ikkileague.data.core;

import com.ikkileague.data.exception.ConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// mvn -Dtest=FileConverterTest test
class FileConverterTest {

    private static final Logger logger = LoggerFactory.getLogger(FileConverterTest.class);

    @TempDir // Répertoire temporaire fourni automatiquement par JUnit 5
    Path tempDir;

    private FileConverter fileConverter;

    // Fichiers temporaires utilisés dans les tests
    private Path inputFilePath;
    private Path metadataFilePath;
    private Path outputFilePath;

    @BeforeEach
    void setUp() {
        logger.info("--- Initialisation d’un nouveau test FileConverter ---");
        fileConverter = new FileConverter();

        inputFilePath = tempDir.resolve("input_data.txt");
        metadataFilePath = tempDir.resolve("metadata.csv");
        outputFilePath = tempDir.resolve("output.csv");

        logger.debug("Répertoire temporaire : {}", tempDir);
        logger.debug("Chemin fichier d'entrée : {}", inputFilePath);
        logger.debug("Chemin fichier métadonnées : {}", metadataFilePath);
        logger.debug("Chemin fichier de sortie : {}", outputFilePath);
    }

    // --- Scénario 1 : Conversion réussie ---
    @Test
    void convert_validFixedFileAndMetadata_createsCorrectCsv() throws IOException, ConversionException {
        logger.info("Test : Conversion avec fichier et métadonnées valides");

        // GIVEN
        String fixedFileContent = "0000000001Jean Dupont              1990-05-15\n" +
                "0000000002Alice Smith              2000-11-30\n" +
                "0000000003Bob Martin               1985-01-01";
        Files.writeString(inputFilePath, fixedFileContent);

        String metadataContent = "ID,10,numérique\nNom,25,chaîne\nDateNaissance,10,date";
        Files.writeString(metadataFilePath, metadataContent);

        // WHEN
        fileConverter.convert(inputFilePath, metadataFilePath, outputFilePath);

        // THEN
        assertTrue(Files.exists(outputFilePath), "Le fichier de sortie doit exister.");

        List<String> actual = Files.readAllLines(outputFilePath);
        List<String> expected = List.of(
                "ID,Nom,DateNaissance",
                "1,Jean Dupont,15/05/1990",
                "2,Alice Smith,30/11/2000",
                "3,Bob Martin,01/01/1985");

        assertLinesMatch(expected, actual, "Le contenu du fichier CSV ne correspond pas au résultat attendu.");
    }

    // --- Scénario 2 : Fichier d'entrée manquant ---
    @Test
    void convert_nonExistentInputFile_throwsException() throws IOException {
        logger.info("Test : Fichier d'entrée manquant");
        String metadataContent = "ID,10,numérique\nNom,25,chaîne\nDateNaissance,10,date";
        Files.writeString(metadataFilePath, metadataContent);

        inputFilePath = Paths.get("fichier_inexistant.txt");

        ConversionException thrown = assertThrows(ConversionException.class,
                () -> fileConverter.convert(inputFilePath, metadataFilePath, outputFilePath));

        logger.debug("File Data fixed not found : \n{}", thrown.getMessage());
        assertTrue(thrown.getMessage().contains("Fichier d'entrée manquant ou non valide:"));
    }

    // --- Scénario 3 : Fichier de métadonnées manquant ---
    @Test
    @DisplayName("Erreur si le fichier de métadonnées est absent")
    void convert_nonExistentMetadataFile_throwsException() throws IOException {
        logger.info("Test : Métadonnées absentes");

        Files.writeString(inputFilePath, "some_data");

        ConversionException thrown = assertThrows(ConversionException.class,
                () -> fileConverter.convert(inputFilePath, metadataFilePath,
                        outputFilePath));

        logger.debug("File Matadata not found : \n{}", thrown.getMessage());
        assertTrue(thrown.getMessage().contains("Le fichier de métadonnées n'existe pas ou n'est pas un fichier valide"));
    }

    // --- Scénario 4 : Fichier de métadonnées vide ---
    @Test
    void convert_emptyMetadataFile_throwsException() throws IOException {
        logger.info("Test : Fichier de métadonnées vide");

        Files.writeString(inputFilePath, "some_data");
        Files.writeString(metadataFilePath, "");

        ConversionException thrown = assertThrows(ConversionException.class,
                () -> fileConverter.convert(inputFilePath, metadataFilePath,
                        outputFilePath));

        logger.debug("File Matadata empty : \n{}", thrown.getMessage());
        assertTrue(thrown.getMessage().contains("Le fichier de métadonnées est vide ou ne contient aucune définition valide"));
    }

    // --- Scénario 5 : Date invalide dans les données ---
    @Test
    @DisplayName("Erreur si une date est mal formatée")
    void convert_invalidDateFormatInInput_throwsException() throws IOException {
        logger.info("Test : Date au mauvais format");

        Files.writeString(inputFilePath, "00000000011990/05/15");
        Files.writeString(metadataFilePath,
                "ID,10,numérique\nDateNaissance,10,date");

        ConversionException thrown = assertThrows(ConversionException.class,
                () -> fileConverter.convert(inputFilePath, metadataFilePath,
                        outputFilePath));

        logger.debug("Invalid date format : {} \n", thrown.getMessage());
        assertTrue(thrown.getMessage().contains("Format de date invalide : '1990/05/15'"));
    }

    // --- Scénario 6 : Valeur non numérique ---
    @Test
    @DisplayName("Erreur si une valeur numérique est invalide")
    void convert_invalidNumericFormatInInput_throwsException() throws IOException {
        logger.info("Test : Valeur numérique invalide");

        Files.writeString(inputFilePath, "ABCDEFGHIJJean Dupont              ");
        Files.writeString(metadataFilePath,
                "ID,10,numérique\nNom,25,chaîne\n");

        ConversionException thrown = assertThrows(ConversionException.class,
                () -> fileConverter.convert(inputFilePath, metadataFilePath,
                        outputFilePath));

        logger.debug("Invalid numeric format msg: {} \n", thrown.getMessage());
        assertTrue(thrown.getMessage().contains("Format numérique invalide : 'ABCDEFGHIJ'"));
    }

    // --- Scénario 7 : Ligne trop courte ---
    @Test
    @DisplayName("Erreur si une ligne est plus courte que prévu")
    void convert_lineTooShortInInput_throwsException() throws IOException {
        logger.info("Test : Ligne trop courte");

        String fixedFileContent = "0000000001\nSHORT"; // Ligne 2 est trop courte
        Files.writeString(inputFilePath, fixedFileContent);
        Files.writeString(metadataFilePath,
                "ID,10,numérique\nNom,25,chaîne\nDateNaissance,10,date");

        ConversionException thrown = assertThrows(ConversionException.class,
                () -> fileConverter.convert(inputFilePath, metadataFilePath,
                        outputFilePath));

        logger.debug("Line Too Short excep msg: {} \n", thrown.getMessage());
        assertTrue(thrown.getMessage().contains("Ligne 1 : la longueur (10) ne correspond pas à la longueur attendue (45)"));
    }
}
