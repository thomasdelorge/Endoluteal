# Endoluteal — guide pour agents

Application Android de suivi de l'endométriose (hard-fork de
[Luteal](https://github.com/JoeriKaiser/luteal)). Roadmap et issues :
[docs/product/ENDOLUTEAL_ROADMAP.md](docs/product/ENDOLUTEAL_ROADMAP.md).

## Stack

- Module unique `:app`, Kotlin, Jetpack Compose, Hilt, ViewModel + `StateFlow`
- Room (SQLite local), DataStore, WorkManager / `AlarmManager` pour les rappels
- Modèle cible : table `journal_events` + tables d'agrégation (voir roadmap)

Packages Kotlin `fr.luteal.*` conservés ; `applicationId` =
`io.github.thomasdelorge.endoluteal` (suffixe `.dev` en debug).

## Invariants

1. **Pas de permission `INTERNET`** (données de santé hors de l'appareil).
2. **Enregistré vs estimé** : ne jamais présenter une saisie utilisatrice comme
   un calcul ou une prédiction.
3. **Migrations Room** obligatoires dès la v0.1.0 installée ; avant cela, reset de
   schéma acceptable.
4. **Toute nouvelle table** doit être couverte par export/import JSON (backup).
5. **Chaînes UI en français** (`res/values/strings.xml`).
6. **Alertes médicamenteuses** : aide à la prise, pas un avis médical (mention
   UI).

## Commandes

```sh
./gradlew assembleDebug lintDebug testDebugUnitTest   # CI locale / PR
make run      # install + lance sur appareil/émulateur
make logcat   # logs du processus debug
```

## Workflow Git

- Conventional Commits (`feat:`, `fix:`, `chore:`, …)
- Une issue GitHub par PR ; corps de commit avec `Closes #n`
- PR vers `thomasdelorge/Endoluteal` (pas l'upstream Luteal)
- `gh repo set-default thomasdelorge/Endoluteal`

## Terminé quand

- Critères d'acceptation de l'issue cochés
- Tests unitaires pour toute logique non triviale ajoutée
- `./gradlew assembleDebug lintDebug testDebugUnitTest` vert
- Scénario manuel de l'issue rejoué sur émulateur si UI
- Entrée sous `[Unreleased]` dans `CHANGELOG.md` si comportement visible
