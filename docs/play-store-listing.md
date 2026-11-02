# Fiche Google Play : Prayer Proclamation

Brouillon à copier-coller dans la Play Console. Les textes décrivent ce qui
existe dans le code. Les champs **À FOURNIR** ne se déduisent pas du dépôt.

## 1. Identité de l'application

| Champ | Valeur |
|---|---|
| Nom (30 caractères max) | `Prayer Proclamation Counter` |
| Nom sous l'icône | `Prayer Proclamation` |
| Package | `cmfi.prayercounter.org` |
| Catégorie | Style de vie |
| Type | Application, gratuite |
| Achats intégrés | Non |
| Publicités | Non |
| E-mail de contact | **À FOURNIR** |
| Site web | **À FOURNIR** |
| Politique de confidentialité | **À FOURNIR** (URL publique obligatoire) |
| Suppression du compte | **À FOURNIR** (URL publique obligatoire) |
| Langues | Anglais et français, les deux présents dans l'application |

`minSdk 24` · `targetSdk 36` · une seule permission : `INTERNET`.

## 2. Description courte (80 caractères max)

**Français**

```
Comptez vos proclamations de prière, fixez un objectif, suivez votre régularité.
```

**Anglais**

```
Count your prayer proclamations, set a target and follow your daily streak.
```

## 3. Description complète

**Français**

```
Prayer Proclamation vous aide à rester fidèle dans la prière de proclamation.

COMPTEZ VOS PROCLAMATIONS
Choisissez un sujet de prière, fixez un objectif et comptez d'un geste. Le
minuteur suit la durée de votre session, que vous pouvez mettre en pause,
enregistrer ou reprendre plus tard.

GARDEZ LA TRACE DE VOTRE MARCHE
Retrouvez vos proclamations enregistrées, ajoutez-y des notes, des références
bibliques ou des témoignages, et cherchez-les par sujet.

SUIVEZ VOTRE RÉGULARITÉ
L'historique présente vos sessions, leur durée et leur rythme, votre série de
jours consécutifs et votre activité de la semaine. Partagez un rapport quand
vous le souhaitez.

RESTEZ INFORMÉ
Le centre de notifications réunit les annonces publiées par les responsables.

Disponible en français et en anglais.
```

**Anglais**

```
Prayer Proclamation helps you stay faithful in proclamation prayer.

COUNT YOUR PROCLAMATIONS
Choose a prayer topic, set a target and count with a single tap. The timer
follows your session, which you can pause, save or resume later.

KEEP A RECORD OF YOUR WALK
Find your saved proclamations, add notes, scripture references or testimonies,
and search them by topic.

FOLLOW YOUR CONSISTENCY
The history shows your sessions, their duration and pace, your daily streak and
your weekly activity. Share a report whenever you wish.

STAY INFORMED
The notification center gathers announcements published by leaders.

Available in English and French.
```

## 4. Nouveautés de cette version

```
Première version publique de Prayer Proclamation.
```

## 5. Sécurité des données (formulaire Play)

À revalider une fois le projet Firebase créé. D'après `FirestoreRepository.kt`,
l'application envoie vers Firestore :

| Donnée | Collectée | Finalité |
|---|---|---|
| Adresse e-mail | Oui | Gestion du compte |
| Nom affiché | Oui | Gestion du compte |
| Sessions, proclamations et notes de prière | Oui | Fonctionnement de l'app |
| Statistiques de prière | Oui | Fonctionnement de l'app |

Les sujets et notes de prière peuvent révéler des **croyances religieuses**,
catégorie que Google considère comme sensible : à déclarer comme telle et à
mentionner dans la politique de confidentialité.

La suppression de compte existe dans l'écran Profil. Il reste à publier la page
web de suppression exigée par Play.

## 6. Visuels à produire

- Icône 512 × 512 px, image de présentation 1024 × 500 px.
- Au moins 2 captures de téléphone, à prendre sur un build relié au vrai Firebase.

## 7. Ce qu'il ne faut pas écrire

- Pas de synchronisation dans le cloud présentée comme acquise tant que Firebase
  n'est pas réellement configuré.
- Pas d'assistant IA : la dépendance `firebase-ai` est présente mais aucun écran
  ne l'utilise.
- Pas de nom d'organisation (CMFI) sans son accord explicite.
- Pas de chiffres d'utilisateurs ni de témoignages : rien n'est mesuré.
