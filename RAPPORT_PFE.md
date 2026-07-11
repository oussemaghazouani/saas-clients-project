# Rapport de Projet de Fin d'Études

## Conception et développement d'une plateforme SaaS d'analyse et de gestion de projets clients — « SaaS Client »

| | |
|---|---|
| **Étudiant** | Oussama Ghazouani |
| **Établissement** | ESPRIT |
| **Encadrant** | _(à compléter)_ |
| **Année universitaire** | 2025 – 2026 |
| **Version du document** | 1.0 — modules M1, M2, M3 réalisés |

---

## Sommaire

1. Introduction
2. Architecture et technologies
3. Module 1 — Authentification et gestion des comptes
4. Module 2 — Gestion des utilisateurs, des accès et des packs
5. Module 3 — Gestion des projets IT
6. Tests et qualité logicielle
7. Conclusion et perspectives

---

## 1. Introduction

### 1.1 Contexte
**SaaS Client** est une plateforme web de type *Software as a Service* destinée à l'analyse, au suivi et à la statistique des projets et des clients. Elle organise la collaboration entre trois familles d'acteurs : les **clients** (startups), les **prestataires de services** (experts) et un **super administrateur** chargé de l'administration globale.

### 1.2 Problématique
Les prestataires de services manquent souvent d'un outil unifié pour gérer leurs clients, leurs offres commerciales (packs), leurs abonnements et le suivi opérationnel de leurs projets. Le besoin est une plateforme **multi-rôles**, **sécurisée** et **modulaire** centralisant le cycle de vie complet : du compte utilisateur jusqu'au pilotage de projets jalonnés.

### 1.3 Objectifs
- Fournir une authentification robuste (JWT) avec gestion fine des rôles.
- Permettre au super administrateur de gérer les comptes prestataires et les offres (packs/abonnements).
- Permettre aux prestataires de provisionner et gérer leurs clients, puis de piloter leurs projets.
- Offrir aux clients une consultation de leurs projets et l'édition de leur profil.
- Adopter une architecture en couches, testable et extensible (12 modules prévus).

### 1.4 Acteurs et rôles

| Rôle (enum) | Entité métier | Responsabilités |
|---|---|---|
| **CLIENT** | `Startup` | Consulte ses projets, édite son profil. Se connecte via un identifiant unique. |
| **PRESTATAIRE** | `Expert` | Provisionne/gère ses clients, souscrit des packs, crée et pilote ses projets. |
| **SUPER_ADMIN** | `Admin` | Gère les prestataires, les packs et valide les paiements par virement. |

Principe de sécurité : **tout est privé sauf l'authentification publique** ; chaque endpoint est protégé par `@PreAuthorize` selon le rôle.

---

## 2. Architecture et technologies

### 2.1 Architecture générale
Le projet est un **monorepo** client-serveur :
- `brandwood-backend/` : API REST **Spring Boot**.
- `brandwood-frontend/` : application **Angular**.
- Base de données **MySQL/MariaDB** (`saas_client`).

```
Navigateur (Angular 17)  ──HTTP/JSON + JWT──►  API REST (Spring Boot)  ──JPA/Hibernate──►  MySQL
        proxy /api  ───────────────────────►  http://localhost:8081
```

### 2.2 Stack technique

| Couche | Technologies |
|---|---|
| Frontend | Angular 17, TypeScript, Bootstrap 5, Bootstrap Icons, RxJS |
| Backend | Spring Boot 3.2, Java 17, Maven |
| Sécurité | Spring Security, JWT (HS512), BCrypt |
| Persistance | Spring Data JPA / Hibernate, MySQL 8 / MariaDB |
| Outillage | Lombok, Bean Validation (jakarta.validation), JUnit 5 + Mockito |

### 2.3 Modèle de sécurité
- Authentification par **JWT** : le token est émis à la connexion et transmis dans l'en-tête `Authorization: Bearer <token>` via un **intercepteur HTTP** Angular.
- `@EnableMethodSecurity` activé : autorisation **par méthode** avec `@PreAuthorize("hasRole('…')")`.
- Mots de passe hachés avec **BCrypt**.
- Côté frontend : **guards** de routes (`AuthGuard`, `GuestGuard`) et filtrage des menus selon le rôle.
- Gestion centralisée des erreurs via `@RestControllerAdvice` (`GlobalExceptionHandler`) : 400 (validation), 401, 403 (accès refusé), 404, 409 (conflit/intégrité), 500.

### 2.4 Architecture en couches (backend)
Package racine `com.saasclient`, organisé par couche :
```
entity → repository → dto (request/response) → mapper → service (interface) → service.impl → controller → exception
```
Aucune logique métier dans les contrôleurs ; les entités JPA ne sont jamais exposées (DTO dédiés) ; les listes sont **paginées** (`Page<…>`).

### 2.5 Structure frontend
Modules Angular **chargés à la demande** (lazy loading), un module par fonctionnalité, un `SharedModule` exposant un `LayoutComponent` (barre latérale adaptée au rôle). Design system : police Inter, dégradé violet→bleu, cartes blanches.

### 2.6 Modèle de données global

| Entité | Description | Relations principales |
|---|---|---|
| `User` | Compte (email, mot de passe, type, rôles) | `*—*` `Role`, `1—1` `Expert`/`Startup` |
| `Role` | Rôle de sécurité (`ROLE_*`) | |
| `Expert` | Profil prestataire | `1—1` `User`, `*—1` `Pack` |
| `Startup` | Profil client | `1—1` `User`, `*—1` `Expert` (prestataire) |
| `Pack` | Offre commerciale | `1—*` `Abonnement` |
| `Abonnement` | Souscription d'un expert à un pack | `*—1` `Expert`, `*—1` `Pack` |
| `Projet` | Projet IT | `*—1` `Startup`, `*—1` `Expert`, `1—*` `Jalon` |
| `Jalon` | Étape/milestone d'un projet | `*—1` `Projet` |

**Énumérations** : `UserType`, `StatutCompte`, `StatutAbonnement`, `MethodePaiement`, `StatutProjet`, `StatutJalon`.

---

## 3. Module 1 — Authentification et gestion des comptes

### 3.1 Objectif
Gérer le cycle de vie de la connexion : inscription, vérification d'email, connexion, déconnexion et réinitialisation de mot de passe, avec émission de jeton JWT et protection des routes.

### 3.2 Fonctionnalités
- Inscription d'un **client** ou d'un **prestataire** (validation des champs, mot de passe fort : 8+ caractères, majuscule, minuscule, chiffre).
- **Vérification d'email** par code OTP à 6 chiffres (mode développement : code journalisé et pré-rempli).
- **Connexion** (JWT) et **déconnexion**.
- **Mot de passe oublié / réinitialisation** par lien à durée limitée.
- Protection des routes (redirection vers `/login` si non authentifié).

### 3.3 Endpoints (`/api/auth`)

| Méthode | URL | Rôle | Description |
|---|---|---|---|
| POST | `/register/client` | public | Inscription client |
| POST | `/register/prestataire` | public | Inscription prestataire |
| POST | `/verify` | public | Vérification du code email |
| POST | `/resend-code` | public | Renvoi du code |
| POST | `/login` | public | Connexion (retourne le JWT) |
| POST | `/forgot-password` | public | Demande de réinitialisation |
| POST | `/reset-password` | public | Réinitialisation via token |

### 3.4 Scénario de test — Module 1
1. **Inscription** : page d'accueil → « S'inscrire » → *Prestataire* → `Paul / Expert / paul.expert@test.co / Passw0rd / Consultant Web` → **Créer**.
   - ✅ Redirection vers la page de vérification avec le **code pré-rempli**.
2. **Vérification** : le code OTP est visible dans la console du backend ; bouton « Renvoyer le code » fonctionnel.
3. **Connexion** : `paul.expert@test.co / Passw0rd` → ✅ accès au tableau de bord, JWT stocké.
4. **Déconnexion** → ✅ retour au login.
5. **Mot de passe oublié** : saisir l'email → ✅ lien de réinitialisation généré (console) → nouveau mot de passe.
6. **Sécurité** : accéder à `/dashboard` sans être connecté → ✅ redirection vers `/login`.

---

## 4. Module 2 — Gestion des utilisateurs, des accès et des packs

### 4.1 Objectif
Couvrir le cycle de vie des comptes au-delà de l'authentification : administration des prestataires, gestion des offres (packs), abonnements, et **provisioning des clients** par les prestataires.

### 4.2 Modèle de données
- `Pack` : `nom`, `description`, `prix`, `nbProjetsMax`, `nbClientsMax`, `dureeMois`, `actif`.
- `Expert` : `specialite`, `tarifHoraire`, `disponibilite`, `competences[]`, `pack`, `statutCompte`.
- `Startup` : `domaineActivite`, `siret`, `adresse`, `nombreEmployes`, `identifiantUnique` (unique), `prestataire`.
- `Abonnement` : `expert`, `pack`, `dateDebut`, `dateFin`, `statut`, `methodePaiement`, `essaiGratuit`.
- Énumérations : `StatutCompte{ACTIF, DESACTIVE, SUSPENDU, SUPPRIME}`, `StatutAbonnement{EN_ATTENTE, ACTIF, EXPIRE, ANNULE}`, `MethodePaiement{VIREMENT, CARTE_BANCAIRE}`.

### 4.3 Règles métier
- **Identifiant unique** : à la création d'un client, un identifiant de connexion unique est généré et sert d'accès au client.
- **Essai gratuit** : un prestataire sans pack actif peut provisionner **1 client** (offre d'essai).
- **Limites du pack** : le nombre de clients est plafonné par `nbClientsMax` du pack souscrit (vérifié côté service avant création).
- **Paiement par virement** : l'abonnement est créé en `EN_ATTENTE`, puis le **SUPER_ADMIN valide** manuellement → activation du compte et attribution du pack.
- **Paiement par carte** : prévu via Stripe (sandbox) — désactivé dans cette version, refusé proprement avec message.
- **Soft delete vs cascade** : la désactivation d'un client est **réversible** (flag `actif` + `enabled`) ; sa suppression est **en cascade** (définitive). La suppression d'un prestataire par l'admin est **irréversible**.

### 4.4 Endpoints

| Méthode | URL | Rôle | Description |
|---|---|---|---|
| GET | `/api/admin/prestataires` | SUPER_ADMIN | Liste paginée (filtre statut) |
| PATCH | `/api/admin/prestataires/{id}/statut` | SUPER_ADMIN | Activer / Désactiver / Suspendre |
| DELETE | `/api/admin/prestataires/{id}` | SUPER_ADMIN | Suppression définitive |
| GET/POST/PUT/DELETE | `/api/admin/packs` | SUPER_ADMIN | CRUD des packs |
| GET | `/api/packs` | authentifié | Catalogue des packs actifs |
| GET | `/api/admin/abonnements` | SUPER_ADMIN | Abonnements (filtre statut) |
| PATCH | `/api/admin/abonnements/{id}/valider` | SUPER_ADMIN | Validation du virement |
| POST | `/api/prestataire/clients` | PRESTATAIRE | Provisioning d'un client |
| GET | `/api/prestataire/clients` | PRESTATAIRE | Liste de ses clients |
| PATCH | `/api/prestataire/clients/{id}/desactiver` | PRESTATAIRE | Désactivation (soft) |
| PATCH | `/api/prestataire/clients/{id}/reactiver` | PRESTATAIRE | Réactivation |
| DELETE | `/api/prestataire/clients/{id}` | PRESTATAIRE | Suppression en cascade |
| POST/GET | `/api/prestataire/abonnements` | PRESTATAIRE | Souscription / liste |
| GET/PUT | `/api/profil` | CLIENT, PRESTATAIRE | Consultation / édition du profil |

### 4.5 Écrans
- **SUPER_ADMIN** : table des prestataires (statut, actions), CRUD des packs, validation des abonnements.
- **PRESTATAIRE** : liste de ses clients + formulaire de provisioning, gestion du profil + souscription d'un pack.
- **CLIENT** : édition de son profil d'entreprise.

### 4.6 Scénario de test — Module 2
1. **Admin / Packs** : `admin@saas.co / Admin@123` → menu **Packs** → 3 packs (Starter/Business/Premium) ; créer, modifier, supprimer un pack. ✅
2. **Prestataire / souscription** : se connecter en prestataire → **Mon profil** (spécialité, tarif, compétences) → carte « Mon abonnement » → souscrire **Business** → statut **EN_ATTENTE**. ✅
3. **Admin / validation** : menu **Abonnements** (EN_ATTENTE) → **Valider** → statut **ACTIF**, le prestataire obtient le pack. ✅
4. **Admin / prestataires** : menu **Prestataires** → Désactiver/Activer/Suspendre (réversible), Supprimer (définitif). ✅
5. **Provisioning client** : prestataire → **Mes clients** → « Provisionner » (`Clara / Client / Client01A / E-commerce`) → ✅ **identifiant unique** `cli-xxxx@saas-client.app` affiché ; désactiver/réactiver/supprimer disponibles.
6. **Client** : se connecter avec l'identifiant unique + mot de passe → **Mon profil** (adresse, employés) → Enregistrer. ✅
7. **Sécurité** : un client qui ouvre `/admin/packs` → ✅ accès refusé (403).

---

## 5. Module 3 — Gestion des projets IT

### 5.1 Objectif
Entité centrale de la plateforme : un **projet** appartient à un client (Startup), est géré par un prestataire (Expert), porte des **jalons** et une **progression** calculée automatiquement.

### 5.2 Modèle de données
- `Projet` : `nom`, `description`, `dateDebut`, `dateFin`, `budget`, `statut`, `progression`, `technologies[]`, `startup` (client), `expert` (prestataire).
- `Jalon` : `nom`, `datePrevue`, `statut`, `description`, `projet`.
- Énumérations : `StatutProjet{PLANIFIE, EN_COURS, EN_PAUSE, TERMINE, ANNULE}`, `StatutJalon{A_VENIR, ATTEINT, EN_RETARD}`.

### 5.3 Règles métier
- **Progression** = (jalons `ATTEINT` / total des jalons) × 100, **recalculée à chaque modification de jalon**.
- **Jalon en retard** : un jalon dont la `datePrevue` est dépassée et qui n'est pas `ATTEINT` est présenté comme `EN_RETARD` (statut dérivé).
- **Cloisonnement par rôle** : un **client ne voit que ses propres projets** ; un **prestataire ne gère que les projets qui lui sont assignés** (filtrage côté service selon l'utilisateur authentifié).
- Un prestataire ne peut rattacher un projet qu'à **l'un de ses propres clients**.

### 5.4 Endpoints

| Méthode | URL | Rôle | Description |
|---|---|---|---|
| GET | `/api/projets` | CLIENT, PRESTATAIRE | Liste filtrée selon le rôle (paginée) |
| GET | `/api/projets/{id}` | CLIENT, PRESTATAIRE | Détail (vérifie l'appartenance) |
| POST | `/api/projets` | PRESTATAIRE | Création (assigné à un de ses clients) |
| PUT | `/api/projets/{id}` | PRESTATAIRE | Mise à jour |
| DELETE | `/api/projets/{id}` | PRESTATAIRE | Suppression |
| GET | `/api/projets/{id}/jalons` | CLIENT, PRESTATAIRE | Liste des jalons |
| POST | `/api/projets/{id}/jalons` | PRESTATAIRE | Ajout d'un jalon |
| PUT | `/api/jalons/{id}` | PRESTATAIRE | Mise à jour d'un jalon |
| PATCH | `/api/jalons/{id}/statut` | PRESTATAIRE | Changement de statut (recalcule la progression) |
| DELETE | `/api/jalons/{id}` | PRESTATAIRE | Suppression d'un jalon |

### 5.5 Écrans
- **Liste des projets** : cartes avec barre de progression, statut et informations clés ; création par le prestataire.
- **Détail du projet** : 4 cartes d'indicateurs (progression, jalons, budget, échéance) + onglets **Aperçu / Jalons / Membres / Activité**. L'onglet **Jalons** permet au prestataire d'ajouter, modifier et marquer les jalons (le client est en lecture seule).

### 5.6 Scénario de test — Module 3
1. **Pré-requis** : être connecté en **prestataire** disposant d'au moins un client (cf. Module 2).
2. **Création** : menu **Projets** → « Nouveau projet » → choisir le client `Clara`, saisir `nom`, `dates`, `budget`, `technologies` → **Créer**. ✅ Le projet apparaît avec progression 0 %.
3. **Jalons** : ouvrir le détail → onglet **Jalons** → ajouter 3 jalons. ✅
4. **Progression** : marquer 1 jalon sur 3 comme **ATTEINT** → ✅ progression passe à **33 %** automatiquement.
5. **Retard** : un jalon dont la date prévue est passée et non atteint s'affiche **EN_RETARD**. ✅
6. **Cloisonnement** : se connecter en **client** → menu **Projets** → ✅ ne voit **que** le(s) projet(s) le concernant, en lecture seule.
7. **Sécurité** : un prestataire qui tente d'ouvrir un projet non assigné → ✅ accès refusé (403).

---

## 6. Tests et qualité logicielle

- **Tests unitaires (Mockito + JUnit 5)** sur la couche service : isolation des dépendances par *mocks* des repositories. Le Module 2 comporte **13 tests** couvrant la création de packs (unicité), les limites de provisioning et l'essai gratuit, le refus du paiement par carte, la validation des virements et la mise à jour de profil — **tous passants**. Le Module 3 suit la même approche (règles de progression et contrôle d'accès).
- **Configuration spécifique** : Mockito est configuré en *subclass mock maker* pour compatibilité avec le JDK utilisé.
- **Validation des entrées** : Bean Validation sur tous les DTO d'entrée, messages d'erreur explicites en français.
- **Sécurité** : vérification systématique du rôle (`@PreAuthorize`) et de l'**appartenance** des ressources (un acteur n'agit que sur ses propres données).

---

## 7. Conclusion et perspectives

### 7.1 Bilan
Trois modules sont **réalisés, intégrés et testés** :
- **M1** — Authentification et gestion des comptes (JWT, rôles, vérification, réinitialisation).
- **M2** — Utilisateurs, accès et packs (administration, provisioning, abonnements).
- **M3** — Projets IT (projets, jalons, progression, cloisonnement par rôle).

L'architecture en couches, le découpage par modules et la sécurité par rôle constituent un socle **robuste et extensible**.

### 7.2 Perspectives (modules à venir)
| Module | Objet |
|---|---|
| **M4** | Gestion des équipes et des membres de projet |
| M5 | Tâches / Kanban |
| M6 | Calendrier de contenu & réunions |
| M7 | Campagnes |
| M8 | Rapports et exports PDF |
| M9 | Support / tickets |
| M10 | Facturation & paiement (Stripe) |
| M11 | Notifications |
| M12 | Tableaux de bord analytiques |

Le prochain incrément est le **Module 4 (Équipe & Membres)** : l'onglet « Membres » du détail de projet y est déjà préparé.

---

*Document généré pour accompagner la démonstration de la plateforme « SaaS Client ». Les comptes de démonstration et le détail des scénarios sont reproductibles localement (MySQL + backend port 8081 + frontend port 4200).*
