# Convertisseur de Fichiers à Largeur Fixe vers CSV

Ce projet est une application Java en ligne de commande (CLI) pour convertir des fichiers de données à **largeur fixe** en fichiers **CSV**. La structure des données est définie par un simple fichier de métadonnées.

## 🚀 Démarrage rapide

### 1.Prérequis

* **Java 17+**
* **Maven 3.6+**

### 1. Construction du projet

Exécutez cette commande pour compiler le projet et créer un fichier JAR exécutable dans le dossier `target/`.

```bash
mvn clean install
```

### 2. Utilisation
Le convertisseur prend trois arguments :

- -i ou --input : Chemin du fichier à largeur fixe d'entrée.

- -m ou --metadata : Chemin du fichier de métadonnées (CSV).

- -o ou --output : Chemin du fichier de sortie (CSV).

```bash
java -jar target/fixed-to-csv-converter-10-SNAPSHOT-jar-with-dependencies.jar \
     -i chemin/vers/input.txt \
     -m chemin/vers/metadata.csv \
     -o chemin/vers/output.csv
```

### 3. Exemple de fichiers
#### Fichier d'entrée (input.txt)
Chaque ligne a une largeur fixe.
```
0000000001Jean Dupont              1990-05-15
0000000002Alice Smith              2000-11-30
```

#### Fichier de métadonnées (metadata.csv)
Il définit le nom, la longueur et le type de chaque colonne.

```
ID,10,numérique
Nom,25,chaîne
DateNaissance,10,date
```
### 4.Tests
#### Pour exécuter tous les tests unitaires et d'intégration :
```
mvn test
```


