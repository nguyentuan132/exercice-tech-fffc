# Spécifications Exécutables

## Fonction : Conversion de fichiers à format fixe en CSV

### Scénario : Conversion simple d’un fichier fixe en CSV

- **Étant donné** un fichier fixe contenant les lignes suivantes :

```
    1970-01-01John           Smith          81.5
    1975-01-31Jane           Doe            61.1
    1988-11-28Bob            Big            102.4
```

- **Et** un fichier de métadonnées décrivant la structure :

```
    Date de naissance,10,date
    Prénom,15,chaîne
    Nom de famille,15,chaîne
    Poids,5,numérique
```

- **Lorsque** je lance la conversion
- **Alors** le fichier CSV doit contenir :

```
    Date de naissance,Prénom,Nom de famille,Poids
    01/01/1970,John,Smith,81.5
    31/01/1975,Jane,Doe,61.1
    28/11/1988,Bob,Big,102.4
```

### Scénario : Reformater une date

- **Étant donné** une colonne de type date avec la valeur `2025-07-28`
- **Lorsque** je la convertis en CSV
- **Alors** la valeur doit être `28/07/2025`

### Scénario : Gérer un nombre négatif

- **Étant donné** une colonne de type numérique avec la valeur `-1234.56`
- **Lorsque** je la convertis en CSV
- **Alors** la valeur doit être `-1234.56`

### Scénario : Supprimer les espaces de fin dans une chaîne

- **Étant donné** une colonne de type chaîne avec la valeur `Jean  `.
- **Lorsque** je la convertis en CSV
- **Alors** la valeur doit être `Jean`

### Scénario : Échapper une chaîne contenant une virgule

- **Étant donné** une colonne de type chaîne avec la valeur `Jean, Paul`
- **Lorsque** je la convertis en CSV
- **Alors** la valeur doit être `"Jean, Paul"`

### Scénario : Gestion d’un fichier au format incorrect

- **Étant donné** un fichier fixe dont une ligne ne respecte pas la longueur totale attendue
- **Lorsque** je lance la conversion
- **Alors** le programme doit échouer avec un message d’erreur clair

### Scénario : Traitement des fichiers volumineux

- **Étant donné** un fichier fixe de plusieurs gigaoctets conforme à la structure
- **Lorsque** je lance la conversion
- **Alors** le programme doit traiter le fichier en flux sans surcharge mémoire

### Scénario : Format du fichier CSV de sortie

- **Lorsque** le fichier CSV est généré
- **Alors** chaque colonne est séparée par une virgule `,`
- **Et** chaque ligne se termine par un retour chariot suivi d’un saut de ligne (`\r\n`)


### Fonction : Lecture du fichier de métadonnées

#### Scénario : Fichier de métadonnées valide
- **Étant donné** un fichier de métadonnées contenant :
  
  | Nom de colonne      | Longueur | Type      |
  |---------------------|----------|-----------|
  | Date de naissance   | 10       | date      |
  | Prénom              | 15       | chaîne    |
  | Nom de famille      | 15       | chaîne    |
  | Poids               | 5        | numérique |
  
- **Lorsque** je charge le fichier de métadonnées
- **Alors** la structure doit être acceptée
- **Et** chaque ligne doit contenir exactement 3 champs : nom, longueur, type
- **Et** les longueurs doivent être des entiers positifs
- **Et** le type doit être l’un des suivants : `"date"`, `"chaîne"`, `"numérique"`

#### Scénario : Fichier de métadonnées invalide (type incorrect)
- **Étant donné** un fichier de métadonnées contenant :

  | Nom de colonne | Longueur | Type  |
  |----------------|----------|-------|
  | Prénom         | 15       | texte |

- **Lorsque** je charge le fichier de métadonnées
- **Alors** une erreur doit être levée avec le message :
  > `Type de colonne invalide 'texte'. Types acceptés : date, chaîne, numérique`
