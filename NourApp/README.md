# Nour – Prière & Coran (Android)

Application Android qui réunit :

- **Horaires de prière** (à la Mawaqit) : les 5 prières + Chourouk calculés hors-ligne selon votre position,
  compte à rebours jusqu'à la prochaine prière, date hégirienne, notification à l'heure de chaque prière,
  15 méthodes de calcul (UOIF 12°, 15°, 18° Mosquée de Paris, Ligue Islamique Mondiale, Umm al-Qura, Maroc, Algérie…),
  Asr standard ou hanafite.
- **Qibla** : boussole qui pointe vers La Mecque (corrigée de la déclinaison magnétique).
- **Coran** (à la Aya) : les 114 sourates en arabe (écriture uthmani) avec la traduction française de Hamidullah,
  recherche, reprise de lecture automatique, favoris, verset du jour, taille du texte réglable.
  Chaque sourate ouverte est gardée en cache pour la lecture hors-ligne.

## Récupérer l'APK

À chaque push, GitHub Actions construit l'application (workflow « Nour – APK Android ») :
onglet **Actions** du dépôt → dernier run → artefact **Nour-apk** → décompresser et installer `app-release.apk`
sur le téléphone (autoriser l'installation d'applications de sources inconnues).

## Développer

Ouvrir le dossier `NourApp/` dans Android Studio, ou en ligne de commande :

```bash
cd NourApp
./gradlew testDebugUnitTest   # tests
./gradlew assembleDebug       # APK de debug
```

Stack : Kotlin, Jetpack Compose (Material 3), [Adhan](https://github.com/batoulapps/adhan-java) pour le calcul
des horaires, API [alquran.cloud](https://alquran.cloud) pour le texte du Coran. minSdk 26 (Android 8).

## Pistes pour la suite

- Récitation audio des versets (alquran.cloud fournit les fichiers de plusieurs récitateurs).
- Son de l'adhan dans la notification.
- Horaires d'une mosquée précise et heures d'iqama.
- Widget d'écran d'accueil.
