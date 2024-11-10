# evo-tp-2

## Getting started

Avant de procéder à l'installation, il faut imprérativement s'assurer d'avoir:
- Java (version >= 17)
- `graphviz` pour la génération de graphe

## Commandes à connaître

Pour lancer la compilation du projet:
```bash
./gradlew shadowJar
```

Graphe de couplage (affichage ici avec Kitty sans sauvegarde du fichier):
```bash
# traduis le résultat en .dot
java -jar evo-eclipse-cli/build/libs/evo-eclipse-cli-1.0-SNAPSHOT-all.jar couplage path/to/project/root
# traduis le résultat en .png puis l'écris dans un fichier (SVG, PNG, ... sont possibles)
java -jar evo-eclipse-cli/build/libs/evo-eclipse-cli-1.0-SNAPSHOT-all.jar couplage path/to/project/root | dot -T png > mon_image.png
# traduis le résultat en png puis l'affiche dans le terminal kitty
java -jar evo-eclipse-cli/build/libs/evo-eclipse-cli-1.0-SNAPSHOT-all.jar couplage path/to/project/root | dot -T png | kitty +kitten icat .
```

Graphe de _clustering_ hiérarchique (affichage ici avec Kitty sans sauvegarde du fichier):
```bash
# traduis le résultat en .dot
java -jar evo-eclipse-cli/build/libs/evo-eclipse-cli-1.0-SNAPSHOT-all.jar clusterize path/to/project/root
# traduis le résultat en .png puis l'écris dans un fichier (SVG, PNG, ... sont possibles)
java -jar evo-eclipse-cli/build/libs/evo-eclipse-cli-1.0-SNAPSHOT-all.jar clusterize path/to/project/root | dot -T png > mon_image.png
# traduis le résultat en png puis l'affiche dans le terminal kitty
java -jar evo-eclipse-cli/build/libs/evo-eclipse-cli-1.0-SNAPSHOT-all.jar clusterize path/to/project/root | dot -T png | kitty +kitten icat .
```

Graphe de _clustering_ hiérarchique avec modules, couplage minimum de `n` (affichage ici avec Kitty sans sauvegarde du fichier):
```bash
# traduis le résultat en .dot
java -jar evo-eclipse-cli/build/libs/evo-eclipse-cli-1.0-SNAPSHOT-all.jar clusterize path/to/project/root -cp n
# traduis le résultat en .png puis l'écris dans un fichier (SVG, PNG, ... sont possibles)
java -jar evo-eclipse-cli/build/libs/evo-eclipse-cli-1.0-SNAPSHOT-all.jar clusterize path/to/project/root -cp n | dot -T png > mon_image.png
# traduis le résultat en png puis l'affiche dans le terminal kitty
java -jar evo-eclipse-cli/build/libs/evo-eclipse-cli-1.0-SNAPSHOT-all.jar clusterize path/to/project/root -cp n | dot -T png | kitty +kitten icat .
```

> Les commandes précédentes s'appliquent aussi pour la version Spoon du projet : il suffit de changer `evo-eclipse-cli` en `evo-spoon-cli`.
