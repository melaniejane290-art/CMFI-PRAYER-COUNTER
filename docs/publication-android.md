# Publication Android : Google Play

Même chaîne que WiNet : le build se lance depuis l'onglet **Actions** de GitHub,
signe l'application, publie l'AAB et l'APK en release GitHub, et peut envoyer
l'AAB sur la piste **interne** de Play. La production reste un geste manuel
dans la Play Console.

La version iOS est un projet séparé : voir `docs/publication-ios.md`.

## À faire une seule fois

### 1. Récupérer la clé du build déjà déposé

Le package `cmfi.prayercounter.org` a déjà un build sur Play : tout nouveau
build doit être signé avec **la même clé de téléversement**. Si elle est perdue,
demander sa réinitialisation dans la Play Console (*Intégrité de l'application >
Signature de l'application*), puis utiliser la nouvelle clé de l'étape 3.

Le premier build a déjà pris un `versionCode` : tant que
`PLAY_SERVICE_ACCOUNT_JSON` n'est pas renseigné, saisir `build_number` à la main,
au-dessus de ce que Play affiche.

### 2. Créer le projet Firebase

La configuration écrite dans `PrayerApplication.kt` est factice : sans
`google-services.json`, l'application fonctionne en local, mais la connexion
Google, la synchronisation et l'espace administrateur ne marchent pas.

1. Créer un projet Firebase et y ajouter une application Android avec le package.
2. Activer Authentication (e-mail et Google) et Cloud Firestore.
3. Écrire les règles de sécurité Firestore : le dépôt n'en contient aucune.
4. Déclarer les empreintes SHA-1 et SHA-256 de la clé de téléversement, et plus
   tard celles de la clé de signature Play (Play Console > Intégrité de l'application).
   Sans elles, la connexion Google échoue.
5. Télécharger `google-services.json` : il ira dans un secret, pas dans le dépôt, qui est public.

### 3. Créer la clé de téléversement

À faire une seule fois, hors du dépôt. Conserver le fichier et son mot de passe
dans un endroit sûr et sauvegardé : `keytool` demande le mot de passe sans
l'afficher.

```bash
keytool -genkeypair -v -keystore prayer-upload.jks -alias upload -keyalg RSA -keysize 4096 -validity 10000
```

### 4. Renseigner les secrets GitHub

Dans *Settings > Secrets and variables > Actions*. Un compte sans droit
d'administration sur le dépôt peut ne pas y avoir accès : il faut alors passer
par le propriétaire.

| Secret | Contenu | Requis |
|---|---|---|
| `ANDROID_KEYSTORE_BASE64` | le fichier `.jks` encodé en base64 | oui |
| `ANDROID_KEYSTORE_PASSWORD` | mot de passe du keystore | oui |
| `ANDROID_KEY_PASSWORD` | mot de passe de la clé | oui |
| `ANDROID_KEY_ALIAS` | `upload` | oui |
| `GOOGLE_SERVICES_JSON` | le contenu entier de `google-services.json` | pour Firebase |
| `PLAY_SERVICE_ACCOUNT_JSON` | clé JSON du compte de service Play | pour l'envoi automatique |

Encoder et enregistrer la clé, en PowerShell :

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("prayer-upload.jks")) | gh secret set ANDROID_KEYSTORE_BASE64 --repo melaniejane290-art/CMFI-PRAYER-COUNTER
```

### 5. Créer l'application sur Play

1. Créer l'application dans la Play Console (voir `docs/play-store-listing.md`).
2. Lancer le workflow sans cocher `publish`, puis téléverser à la main le premier
   AAB de la release GitHub sur la piste de test interne.
3. Pour l'envoi automatique : inviter le compte de service dans *Utilisateurs et
   autorisations*, limité à cette application et à « Publier sur les pistes de
   test », jamais la production.

## À chaque version

1. *Actions > Build Android (APK + AAB Play Store) > Run workflow*.
2. Renseigner `version_name`. Laisser `build_number` vide : le versionCode est
   calculé pour rester au-dessus de ce que Play a déjà reçu.
3. Cocher `publish` pour envoyer sur la piste interne. Garder `play_status` à
   `draft` tant que l'application n'a jamais été publiée, puis passer à `completed`.
4. Récupérer l'AAB et l'APK dans la release `android-v<version>-<code>`.
5. Promouvoir vers la production depuis la Play Console.

## Reste à faire avant la production

- **Politique de confidentialité et page de suppression de compte** : Play exige
  une URL publique pour chacune.
- **Règles Firestore** : le rôle administrateur n'est vérifié que dans
  l'application ; seules les règles de sécurité peuvent le protéger côté serveur.
