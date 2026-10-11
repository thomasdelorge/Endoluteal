# Endoluteal

Application Android de **suivi de l'endométriose**, organisée autour d'un agenda :
prises de médicaments, douleurs localisées sur le corps, traitements non
médicamenteux (TENS, bouillotte...), rendez-vous médicaux, hospitalisations et
récap quotidien.

> **Hard-fork de [Luteal](https://github.com/JoeriKaiser/luteal)** (Joeri
> Kaiser), suivi de cycle menstruel sous licence GPL-3.0. Endoluteal diverge
> définitivement de l'upstream pour répondre à un besoin précis : le suivi au
> quotidien de l'endométriose. Ce projet n'est pas affilié à Luteal et ne
> reverse pas ses changements upstream.

## Statut

En développement. Le code est encore celui de Luteal tant que le nettoyage
(milestone M1) n'est pas livré. Le plan complet, les décisions d'architecture
et le découpage en issues sont dans
[docs/product/ENDOLUTEAL_ROADMAP.md](docs/product/ENDOLUTEAL_ROADMAP.md).

## Fonctionnalités visées

- Agenda avec saisie rapide (bouton +) : médicament, symptôme, traitement,
  rendez-vous, hospitalisation.
- Base de médicaments personnelle avec alertes de surdosage (cumul d'une même
  substance sur 24 h), de doublon de classe (deux anti-inflammatoires) et
  d'incompatibilité, et calcul de la prochaine prise possible.
- Plans de traitement avec rappels regroupés et report (+10 min, +30 min, +1 h).
- Carte du corps recto/verso pour localiser et coter chaque douleur (0-10).
- Contrôle de la douleur après chaque prise, pour mesurer ce qui soulage.
- Récap quotidien en cartes : cycle, douleurs et « 5D » de l'endométriose,
  état psychologique, état général, transit.
- Import des rendez-vous depuis l'agenda du téléphone, compte rendu après
  chaque rendez-vous, frise chronologique par praticien, export PDF.
- Sauvegarde quotidienne automatique dans un dossier local.

## Confidentialité

- Les données restent sur l'appareil, dans une base Room locale. La cible est
  une application **sans permission Internet**.
- Pas de publicité, pas d'analytics, pas de Google Play Services, pas de
  Firebase.
- La sauvegarde cloud Android est désactivée (`allowBackup=false`).
- Les rapports de plantage (ACRA) ne partent qu'après consentement explicite,
  par mail, sans données de suivi.

## Avertissement médical

Endoluteal ne pose aucun diagnostic. Les alertes médicamenteuses sont une aide
à la prise et ne remplacent pas l'avis d'un médecin ou d'un pharmacien.

## Installation avec Obtainium

Les APK signés sont publiés dans les
[releases GitHub](https://github.com/thomasdelorge/Endoluteal/releases). Dans
[Obtainium](https://github.com/ImranR98/Obtainium), ajoutez l'application
depuis l'URL `https://github.com/thomasdelorge/Endoluteal`. Les mises à jour
sont ensuite détectées automatiquement.

Endoluteal s'installe à côté de Luteal : identifiant et clé de signature
différents, sans reprise des données Luteal.

## Développement

Prérequis : JDK 21 et SDK Android. Avec [mise](https://mise.jdx.dev/) :
`java = "temurin-21"` et `android-sdk = "13.0"`. Le premier build télécharge
lui-même `platforms;android-37.0` (compileSdk 37, minSdk 26).

Le SDK est trouvé via `ANDROID_HOME` ou `local.properties` (non versionné) :

```properties
sdk.dir=/home/<user>/.local/share/mise/installs/android-sdk/13.0
```

Commandes principales :

```sh
./gradlew assembleDebug                       # build debug
./gradlew assembleDebug lintDebug testDebugUnitTest   # vérification d'une PR
./gradlew installDebug                        # installation sur l'appareil connecté
```

Émulateur dédié (Pixel 8, API 35, sans écran de verrouillage) :

```sh
avdmanager create avd -n endoluteal_api35 \
  -k "system-images;android-35;google_apis;x86_64" -d pixel_8
emulator -avd endoluteal_api35 -gpu host &
adb logcat --pid=$(adb shell pidof -s fr.luteal.app)
```

Sur un téléphone physique : activer le débogage sans fil, puis `adb pair` et
`adb connect`.

Le dépôt GitHub étant un fork, configurez `gh` pour cibler ce dépôt et non
l'upstream :

```sh
gh repo set-default thomasdelorge/Endoluteal
```

La signature des releases lit `keystore.properties` à la racine (voir
`keystore.properties.example`). Sans ce fichier, l'APK release n'est pas signé.

## Organisation du code

| Chemin | Contenu |
| --- | --- |
| `app/src/main/java/fr/luteal/app` | Écrans, navigation, view models, injection |
| `app/src/main/java/fr/luteal/core` | Modèle métier, stockage Room, calculs |
| `docs/product` | Roadmap Endoluteal |
| `docs/research` | Notes de recherche héritées de Luteal |
| `scripts/` | Outils de dev (création des issues, données de démonstration) |

Les packages Kotlin `fr.luteal.*` sont conservés volontairement pour limiter
le diff avec le code d'origine.

## Licence

GNU General Public License v3.0 ou ultérieure. Voir [LICENSE](LICENSE).

Endoluteal est dérivé de [Luteal](https://github.com/JoeriKaiser/luteal),
Copyright (C) Joeri Kaiser, distribué sous GPL-3.0-or-later. Les
modifications apportées par Endoluteal sont distribuées sous la même licence.
Ce programme est distribué dans l'espoir qu'il sera utile, mais SANS AUCUNE
GARANTIE, sans même la garantie implicite de QUALITÉ MARCHANDE ou
d'ADÉQUATION À UN USAGE PARTICULIER.
