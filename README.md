# Convertisseur de formats de fichiers fixes

Votre objectif est d'écrire un outil générique pour convertir des fichiers de format fixe en fichier csv sur la base d'un fichier de métadonnées décrivant leur structure.

N'hésitez pas à utiliser votre langage et vos librairies préférés si nécessaire (mais pas de librairies propriétaires, uniquement des librairies open source), faites un fork de ce projet et fournissez votre code complet sous forme de pull request (incluant les sources et les tests).

## Cas d'utilisation

Nos fichiers au format fixe peuvent avoir n'importe quel nombre de colonnes
Une colonne peut avoir 3 formats :
* date (format aaaa-mm-jj)
* numérique (séparateur décimal '.' ; pas de séparateur de milliers ; peut être négatif)
* chaîne de caractères

La structure du fichier est décrite dans un fichier de métadonnées au format csv avec une ligne pour chaque colonne définissant :
* le nom de la colonne
* la longueur de la colonne
* le type de colonne

Vous devez transformer le fichier en fichier csv (séparateur ',' et séparateur de ligne CRLF).

Les dates doivent être reformatées en jj/mm/aaaa.

Les espaces de fin des colonnes de chaînes de caractères doivent être coupés.

Le fichier csv doit inclure une première ligne avec les noms des colonnes

## Exemple

Fichier de données :
```
1970-01-01John           Smith         81.5
1975-01-31Jane           Doe           61.1
1988-11-28Bob            Big           102.4
```

Fichier de métadonnées :
```
Date de naissance,10,date
Prénom,15,chaîne
Nom de famille,15,chaîne
Poids,5,numérique
```

Fichier de sortie :
```
Date de naissance,Prénom,Nom,Poids
01/01/1970,John,Smith,81.5
31/01/1975,Jane,Doe,61.1
28/11/1988,Bob,Big,102.4
```

## Exigences supplémentaires
* Les fichiers sont encodés en UTF-8 et peuvent contenir des caractères spéciaux.
* Les colonnes de chaînes peuvent contenir des caractères de séparation tels que ',' et la chaîne entière doit alors être échappée par des " (guillemets doubles). Seuls CR ou LF sont interdits
* si le format du fichier n'est pas correct, le programme doit échouer mais en indiquer explicitement la raison
* un fichier au format fixe peut être très volumineux (plusieurs Go)