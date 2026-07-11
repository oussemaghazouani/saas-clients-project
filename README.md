<div align="center">

# 🚀 SaaS Client

### Plateforme SaaS de gestion pour prestataires IT & leurs clients

*Projets · Facturation · Support · Analytique · IA · Messagerie temps réel*

<br/>

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-17-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

![JWT](https://img.shields.io/badge/Auth-JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![WebSocket](https://img.shields.io/badge/Realtime-WebSocket-010101?style=for-the-badge&logo=socketdotio&logoColor=white)
![AI](https://img.shields.io/badge/AI-Assistant-6C63FF?style=for-the-badge&logo=anthropic&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)

<br/>

![Modules](https://img.shields.io/badge/Modules-12-6C63FF?style=flat-square)
![Roles](https://img.shields.io/badge/Rôles-3-3B82F6?style=flat-square)
![Tests](https://img.shields.io/badge/Tests-34_✓-10B981?style=flat-square)
![Git Flow](https://img.shields.io/badge/Workflow-Git_Flow-F05032?style=flat-square&logo=git&logoColor=white)

</div>

---

## 📖 À propos

**SaaS Client** est une plateforme web full-stack qui réunit **prestataires IT** et **clients** dans un
même espace collaboratif. Chaque prestataire pilote ses projets, ses équipes, sa facturation, ses
campagnes et son support ; chaque client suit l'avancement de ses projets en toute transparence.
Le tout est enrichi par des **fonctionnalités d'IA** et une **messagerie temps réel**.

> 🎓 Réalisé dans le cadre d'un **Projet de Fin d'Études** (ingénierie · génie logiciel).

---

## ✨ Fonctionnalités

| Module | Description |
|---|---|
| 🔐 **Authentification** | Inscription, vérification email, connexion **JWT**, reset mot de passe (BCrypt) |
| 👥 **Utilisateurs & Accès** | Rôles **Client / Prestataire / Admin**, packs commerciaux, abonnements (RBAC) |
| 📁 **Projets IT** | Projets, jalons, progression auto-calculée, états dérivés |
| 🧑‍🤝‍🧑 **Équipe** | Membres de projet avec rôles, assignables aux tâches |
| ✅ **Tâches (Kanban)** | Tableau Kanban + **génération de tâches & analyse de risques par IA** |
| 📅 **Réunions** | Planification de réunions avec notifications |
| 📣 **Campagnes** | Campagnes marketing + **génération de contenu par IA** |
| 🧾 **Facturation** | Factures (HT/TVA/TTC), export PDF, rapports projet |
| 🎧 **Support** | Tickets & conversation + **triage IA** (priorité, catégorie, sentiment, réponse suggérée) |
| 🔔 **Notifications** | Notifications in-app temps réel |
| 📊 **Analytique** | Tableaux de bord, KPI, graphiques + **insights IA** |
| 💬 **Messagerie** | Messagerie directe **1-à-1 en temps réel (WebSocket)** |

### 🤖 Intelligence Artificielle
Architecture IA **découplée** : moteur **heuristique local** (fonctionne 100 % hors-ligne) avec, en option,
une bascule vers l'**API Claude** si une clé est fournie — sans changer une ligne de code.

---

## 🛠️ Stack technique

<table>
<tr><td><b>Backend</b></td><td>Java 17 · Spring Boot 3.2.5 · Spring Security · Spring Data JPA / Hibernate · Maven · Lombok</td></tr>
<tr><td><b>Frontend</b></td><td>Angular 17 · TypeScript · RxJS · Bootstrap 5 · Reactive Forms</td></tr>
<tr><td><b>Base de données</b></td><td>MySQL / MariaDB</td></tr>
<tr><td><b>Sécurité</b></td><td>JWT (HS256) · BCrypt · <code>@PreAuthorize</code> (RBAC)</td></tr>
<tr><td><b>Temps réel</b></td><td>WebSocket natif + handshake authentifié par JWT</td></tr>
<tr><td><b>Tests</b></td><td>JUnit 5 · Mockito · AssertJ</td></tr>
</table>

---

## 🏗️ Architecture

Architecture **Client–Serveur 3-tiers**, communication via **API REST (JSON) + JWT** :

```
┌────────────────┐   HTTP/JSON + JWT    ┌────────────────────┐   JPA / JDBC   ┌──────────┐
│   FRONTEND      │ ───────────────────▶│   BACKEND           │ ─────────────▶│  MySQL    │
│   Angular 17    │◀─────────────────── │   Spring Boot 3     │◀───────────── │ saas_client│
│   (port 4200)   │   WebSocket (/ws)   │   API REST (8081)   │               └──────────┘
└────────────────┘                     └────────────────────┘
```

**Backend en couches** : `Controller → Service → Repository → Entity`, avec `DTO` + `Mapper`
(l'entité n'est jamais exposée), gestion d'erreurs centralisée et sécurité par filtre JWT.

<div align="center">

`22 Controllers` · `20 Services` · `18 Repositories` · `35 Entities` · `62 DTOs` · `15 Mappers`

</div>

---

## 🚀 Démarrage rapide

### Prérequis
- **JDK 17+**, **Maven**, **Node.js 18+** & **Angular CLI**, **MySQL** (ou XAMPP)

### 1️⃣ Base de données
```sql
CREATE DATABASE saas_client;
```

### 2️⃣ Backend (port 8081)
```bash
cd brandwood-backend
# configuration via variables d'environnement (voir .env.example)
mvn spring-boot:run
```

### 3️⃣ Frontend (port 4200)
```bash
cd brandwood-frontend
npm install
ng serve
```
➡️ Ouvrir **http://localhost:4200**

> 🔑 **Config** : tous les secrets sont externalisés en variables d'environnement — voir
> [`brandwood-backend/.env.example`](brandwood-backend/.env.example). L'app démarre sans configuration
> (mode dev). Pour l'envoi d'emails réels ou l'IA générative, renseigner les variables correspondantes.

---

## 🔀 Workflow Git (Git Flow)

Le dépôt suit **Git Flow** avec une **branche par module** :

```
main  ──────●───────────────────────────────●   (production)
             \                             /
develop ──────●──●──●──●──●──●──●──●──●──●──●     (intégration)
               \  \  \                    /
   feature/security  feature/projects  feature/messaging  … (21 modules)
```

`config` · `database` · `security` · `auth` · `users` · `projects` · `team` · `tasks` ·
`meetings` · `campaigns` · `invoicing` · `support` · `notifications` · `analytics` · `ai` ·
`messaging` · `frontend-setup` · `frontend-auth` · `frontend-app` · `frontend-realtime` · `docs`

Chaque commit suit la convention **[Conventional Commits](https://www.conventionalcommits.org/)**
(`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`, `config:`).

---

## 🗂️ Structure du projet

```
saas-clients-project/
├── brandwood-backend/        # API Spring Boot
│   └── src/main/java/com/saasclient/
│       ├── controller/  service/  repository/  entity/
│       ├── dto/  mapper/  security/  websocket/  exception/
│       └── resources/   # schema.sql · data.sql · application.properties
└── brandwood-frontend/       # SPA Angular
    └── src/app/
        ├── components/  services/  guards/  interceptors/
        ├── models/  shared/
        └── ...
```

---

## 👤 Auteur

**Oussama Ghazouani** — Ingénierie / Génie Logiciel
[![GitHub](https://img.shields.io/badge/GitHub-oussemaghazouani-181717?style=flat-square&logo=github)](https://github.com/oussemaghazouani)

<div align="center">

⭐ *Si ce projet vous plaît, n'hésitez pas à laisser une étoile !* ⭐

</div>
