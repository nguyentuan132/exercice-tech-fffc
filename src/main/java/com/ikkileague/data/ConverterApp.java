package com.ikkileague.data;

import java.nio.file.Path;

import org.slf4j.Logger; // Import SLF4J Logger
import org.slf4j.LoggerFactory; // Import SLF4J LoggerFactory

import com.ikkileague.data.core.FileConverter;
import com.ikkileague.data.exception.ConversionException;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "fixed2csv", mixinStandardHelpOptions = true, version = "fixed2csv 1.0", description = "Converts a fixed-width text file to a CSV file based on a metadata file.")
public class ConverterApp implements Runnable {

    // Déclaration du logger pour cette classe
    private static final Logger logger = LoggerFactory.getLogger(ConverterApp.class);

    @Option(names = { "-m", "--metadata" }, description = "Path to the metadata CSV file.", required = true)
    private Path metadataFilePath;

    @Option(names = { "-i", "--input" }, description = "Path to the fixed-width input data file.", required = true)
    private Path inputFilePath;

    @Option(names = { "-o", "--output" }, description = "Path to the output CSV file.", required = true)
    private Path outputFilePath;

    @Override
    public void run() {

        logger.info("Starting conversion process...");
        logger.debug("Metadata file: {}", metadataFilePath);
        logger.debug("Input file: {}", inputFilePath);
        logger.debug("Output file: {}", outputFilePath);

        FileConverter converter = new FileConverter(); // Utilise le constructeur par défaut
        long startTime = System.nanoTime(); // Démarrage du chronomètre

        try {
            converter.convert(inputFilePath, metadataFilePath, outputFilePath);
            long endTime = System.nanoTime(); // Arrêt du chronomètre
            long durationMillis = (endTime - startTime) / 1_000_000; // Convertir en millisecondes

            logger.info("Conversion completed successfully! Output written to {} in {} ms.", outputFilePath,
                    durationMillis); // System.exit(0) est géré par Picocli si la méthode run() termine normalement.
        } catch (ConversionException e) {
            logger.error("Conversion failed: {}", e.getMessage(), e); // Log l'erreur avec la stack trace
            // Picocli gère le code de sortie non-zéro si une exception est levée de run()
            // ou si on utilise System.exit()
            System.exit(1); // Indique un échec à l'OS
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e); // Log les exceptions inattendues
            System.exit(2); // Indique un échec inattendu à l'OS
        }
    }

    public static void main(String... args) {
        // Picocli configure le logger pour vous si vous implémentez
        // CommandLine.IFactory
        // ou si vous utilisez un logger standard.
        // Pour SLF4J-Simple, pas de configuration spéciale ici, il se configure tout
        // seul.
        int exitCode = new CommandLine(new ConverterApp()).execute(args);
        // CommandLine.execute() retourne le code de sortie, donc System.exit est déjà
        // appelé en interne par Picocli
        // Il est bon de le laisser ici pour la clarté ou si on appelle main directement
        // dans un test par exemple.
        System.exit(exitCode);
    }
}