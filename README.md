# evo-tp-1

## Getting started

Avant de procéder à l'installation, il faut imprérativement s'assurer d'avoir:
- Java (version >= 11)
- `graphviz` pour la génération de graphe

## Commandes à connaître

Pour lancer la compilation du projet:
```bash
./gradlew shadowJar
```

Pour effectuer l'analyse statistique demandée dans la partie 1 du projet
```bash
java -jar evo-cli/build/libs/evo-cli-1.0-SNAPSHOT-all.jar analyze method_size=4 path/to/project/root
# Ici, method_size indique la valeur X demandées dans le TP
```

Pour effectuer le calcul du graphe d'appel (affichage ici avec Kitty sans sauvegarde du fichier):
```bash
# traduis le résultat en .dot
java -jar evo-cli/build/libs/evo-cli-1.0-SNAPSHOT-all.jar callgraph path/to/project/root
# traduis le résultat en .png puis l'écris dans un fichier
java -jar evo-cli/build/libs/evo-cli-1.0-SNAPSHOT-all.jar callgraph path/to/project/root | dot -T png > mon_image.png
# traduis le résultat en png puis l'affiche dans le terminal kitty
java -jar evo-cli/build/libs/evo-cli-1.0-SNAPSHOT-all.jar callgraph path/to/project/root | dot -T png | kitty +kitten icat .
```
