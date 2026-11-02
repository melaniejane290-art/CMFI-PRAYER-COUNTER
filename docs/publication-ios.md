# Publication iOS : TestFlight

Application SwiftUI dans `ios/`, avec le Bundle ID `cmfi.prayercounter.org`. Le
projet Xcode est généré sur le runner macOS par XcodeGen. Même chaîne qu'EFDET et
Dabar : signature par l'API App Store Connect, puis envoi sur TestFlight.

## Contenu de cette version

Compteur, sujets et objectifs, chronomètre, proclamations enregistrées avec notes
et recherche, historique avec série et activité de la semaine, partage, français
et anglais.

Les données restent sur l'appareil. Il n'y a ni compte, ni synchronisation, ni
notifications, ni espace administrateur : ces fonctions dépendent de Firebase,
qui n'est pas encore configuré.

## Prérequis

1. Créer l'application dans App Store Connect avec ce Bundle ID.
2. Renseigner les secrets GitHub : `APP_STORE_CONNECT_ISSUER_ID`,
   `APP_STORE_CONNECT_KEY_IDENTIFIER`, `APP_STORE_CONNECT_PRIVATE_KEY` et
   `CERTIFICATE_PRIVATE_KEY`.

## Envoyer un build

*Actions > iOS TestFlight > Run workflow*. Le workflow vérifie aussi le traitement
par Apple. Aucune soumission à la revue n'est faite.

## Déclaration de confidentialité

« Aucune donnée collectée » tant que l'application reste locale. Une URL de
politique de confidentialité reste obligatoire.
