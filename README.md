# NoScrollPerso

Une petite app Android qui bloque les **Reels** d'Instagram, les **Shorts** de YouTube et **TikTok** (toute l'app), pour arrêter de scroller sans s'en rendre compte.

> **Projet vibecodé.**

## Ce que fait l'app

- **Blocage** : quand un Reel, un Short ou TikTok s'ouvre, l'app te ramène en arrière.
- **Mode quota** : tu choisis un temps autorisé, par exemple 5 minutes de reels, puis un verrouillage, par exemple 30 minutes. Le quota est commun aux trois apps, et il n'est décompté que pendant qu'un contenu court est à l'écran. Le quota non utilisé repart à zéro chaque nuit.
- **Chrono flottant** : une petite pastille affiche le temps restant par-dessus l'écran pendant que tu regardes.
- **Autorisation temporaire** : pour regarder le reel qu'un ami t'a envoyé, tu laisses tout passer pendant quelques minutes. Rien n'est bloqué ni décompté.
- **Tuile de réglages rapides** : un bouton « NoScroll » à ajouter dans le panneau qui s'ouvre en glissant depuis le haut de l'écran. Un appui lance une autorisation avec la dernière durée choisie, un second appui l'annule.
- Un compteur de blocages du jour.

C'est un outil d'**autodiscipline** : tu peux le désactiver à tout moment.

## Vie privée

- **Aucune permission Internet.** L'app ne peut rien envoyer hors du téléphone.
- Elle utilise un **service d'accessibilité**, une permission sensible. Techniquement, un tel service peut lire l'écran des apps pour lesquelles il est activé. Ici, il est **limité à Instagram, YouTube et TikTok** (voir `accessibility_service_config.xml`). Il ne cherche que quelques identifiants de vues pour savoir si un Reel ou un Short est affiché, et il ne stocke ni ne transmet aucun contenu.
- Seuls tes réglages et le compteur du jour sont enregistrés, localement, dans l'app.
- Le code est ici, tu peux tout vérifier.

## Installation

1. Télécharge le fichier `.apk` depuis la page [Releases](../../releases), directement sur ton téléphone.
2. Ouvre-le. Android te demandera d'**autoriser l'installation depuis cette source** (ton navigateur ou « Mes fichiers »).
3. Play Protect peut afficher un avertissement du type « application non reconnue » : c'est normal, l'app ne vient pas du Play Store. Choisis d'installer quand même.
4. **Samsung** : si l'installation est refusée, désactive temporairement **Auto Blocker** (Paramètres → Sécurité et confidentialité), puis réactive-le après.
5. Ouvre l'app **NoScrollPerso** une première fois.

### Activer le service (indispensable)

1. **Android 13 et plus** : Paramètres → Applications → NoScrollPerso → menu **⋮** en haut à droite → **Autoriser les paramètres restreints**. Si le menu n'apparaît pas, essaie d'abord l'étape suivante : le téléphone affichera alors un message qui te mènera au bon endroit.
2. Paramètres → Accessibilité → Applis installées (ou Services installés) → **NoScrollPerso** → active-le.
3. Dans l'app, vérifie que **Blocage actif** est activé.

Les noms des menus varient selon la marque du téléphone.

### Conseils

- Si le blocage cesse de marcher au bout de quelques jours, c'est sûrement l'économie de batterie : Paramètres → Applications → NoScrollPerso → Batterie → **Illimitée**.
- Pour la tuile, ouvre le panneau de réglages rapides, modifie les boutons et ajoute **NoScroll**.

## Mise à jour

- **Mise à jour** : installe simplement le nouvel APK par-dessus l'ancien. Si Android refuse (signature différente), désinstalle d'abord l'ancienne version.

## Limites connues

- **Ça peut ne pas marcher sur ton téléphone.** La détection des Reels et des Shorts repose sur des identifiants internes d'Instagram et de YouTube, que leurs développeurs peuvent changer à chaque mise à jour. L'app a été testée **uniquement sur un Samsung Galaxy A56**, avec les versions d'Instagram et de YouTube en place lors du développement. TikTok, lui, est bloqué en entier et ne dépend d'aucun identifiant.
- Android uniquement (Android 8.0 ou plus récent).
- Pas de support garanti, c'est un projet perso.

## Compiler soi-même

Le projet est en Kotlin, sans aucune bibliothèque externe, et il se compile avec Android Studio ou en ligne de commande :

```bash
./gradlew assembleDebug
```

L'APK est créé dans `app/build/outputs/apk/debug/`.
