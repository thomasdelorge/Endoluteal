# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Agenda d'accueil : calendrier, événements du jour et saisie du récap.
- Échelle de douleur 0-10 et silhouette de localisation.
- Rappels exacts reportables (+10 min, +30 min, +1 h).
- Base de médicaments, alertes de prise et saisie d'une prise avec contrôle de douleur.
- Plans de traitement et rappel groupé. La prédiction de règles est masquée sous traitement hormonal continu.
- Sauvegarde automatique dans un dossier local.
- Saisie d’un symptôme douloureux depuis l’agenda (localisation, intensité, notes).

### Changed

- Application entièrement en français ; accueil sur le Journal (plus d’onglet
  Aujourd’hui).
- Suppression du mode Duo, des widgets, de la synchronisation cloud et du
  schéma Room Luteal (base `endoluteal.db`, sauvegardes `endoluteal/v1`).

- Local dev tooling (`mise.toml`, `make avd` / `emu` / `run` / `logcat`).
- CI: debug APK artifact on PRs; signed release APK on version tags for
  Obtainium.
