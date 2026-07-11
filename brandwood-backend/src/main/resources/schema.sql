CREATE TABLE IF NOT EXISTS roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name   VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(180) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    phone        VARCHAR(30),
    company_name VARCHAR(150),
    job_title    VARCHAR(150),
    user_type    VARCHAR(20)  NOT NULL,
    tenant_id    VARCHAR(100),
    enabled      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS verification_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(6)   NOT NULL,
    user_id     BIGINT       NOT NULL,
    expiry_date DATETIME     NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_vt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(36)  NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    expiry_date DATETIME     NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ─── Module 2 : Gestion des Utilisateurs, Accès & Packs ──────────────────────────

CREATE TABLE IF NOT EXISTS packs (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom            VARCHAR(100) NOT NULL UNIQUE,
    description    VARCHAR(500),
    prix           DOUBLE       NOT NULL,
    nb_projets_max INT          NOT NULL,
    nb_clients_max INT          NOT NULL,
    duree_mois     INT          NOT NULL,
    actif          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     DATETIME     NOT NULL,
    updated_at     DATETIME     NOT NULL
);

CREATE TABLE IF NOT EXISTS experts (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE,
    specialite    VARCHAR(150),
    tarif_horaire DOUBLE,
    disponibilite BOOLEAN      NOT NULL DEFAULT TRUE,
    pack_id       BIGINT,
    statut_compte VARCHAR(20)  NOT NULL,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    CONSTRAINT fk_expert_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_expert_pack FOREIGN KEY (pack_id) REFERENCES packs(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS expert_competences (
    expert_id  BIGINT       NOT NULL,
    competence VARCHAR(100) NOT NULL,
    CONSTRAINT fk_ec_expert FOREIGN KEY (expert_id) REFERENCES experts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS startups (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT       NOT NULL UNIQUE,
    domaine_activite   VARCHAR(150),
    siret              VARCHAR(20),
    adresse            VARCHAR(255),
    nombre_employes    INT,
    identifiant_unique VARCHAR(100) NOT NULL UNIQUE,
    prestataire_id     BIGINT,
    actif              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         DATETIME     NOT NULL,
    updated_at         DATETIME     NOT NULL,
    CONSTRAINT fk_startup_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_startup_prestataire FOREIGN KEY (prestataire_id) REFERENCES experts(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS abonnements (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    expert_id        BIGINT       NOT NULL,
    pack_id          BIGINT       NOT NULL,
    date_debut       DATE,
    date_fin         DATE,
    statut           VARCHAR(20)  NOT NULL,
    methode_paiement VARCHAR(20)  NOT NULL,
    essai_gratuit    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME     NOT NULL,
    CONSTRAINT fk_abo_expert FOREIGN KEY (expert_id) REFERENCES experts(id) ON DELETE CASCADE,
    CONSTRAINT fk_abo_pack FOREIGN KEY (pack_id) REFERENCES packs(id)
);

-- ─── Module 3 : Gestion des Projets IT ───────────────────────────────────────────

CREATE TABLE IF NOT EXISTS projets (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    date_debut  DATE,
    date_fin    DATE,
    budget      DOUBLE,
    statut      VARCHAR(20)  NOT NULL,
    progression DOUBLE       NOT NULL DEFAULT 0,
    startup_id  BIGINT       NOT NULL,
    expert_id   BIGINT       NOT NULL,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    CONSTRAINT fk_projet_startup FOREIGN KEY (startup_id) REFERENCES startups(id) ON DELETE CASCADE,
    CONSTRAINT fk_projet_expert  FOREIGN KEY (expert_id)  REFERENCES experts(id)  ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS projet_technologies (
    projet_id   BIGINT       NOT NULL,
    technologie VARCHAR(100) NOT NULL,
    CONSTRAINT fk_pt_projet FOREIGN KEY (projet_id) REFERENCES projets(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS jalons (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(150) NOT NULL,
    date_prevue DATE,
    statut      VARCHAR(20)  NOT NULL,
    description VARCHAR(500),
    projet_id   BIGINT       NOT NULL,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    CONSTRAINT fk_jalon_projet FOREIGN KEY (projet_id) REFERENCES projets(id) ON DELETE CASCADE
);

-- ─── Module 4 : Équipe & Membres ─────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS membres (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(100) NOT NULL,
    prenom      VARCHAR(100) NOT NULL,
    email       VARCHAR(180),
    role_projet VARCHAR(30)  NOT NULL,
    projet_id   BIGINT       NOT NULL,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    CONSTRAINT fk_membre_projet FOREIGN KEY (projet_id) REFERENCES projets(id) ON DELETE CASCADE
);

-- ─── Module 5 : Tâches & Kanban ──────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS taches (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre         VARCHAR(200) NOT NULL,
    description   VARCHAR(1000),
    statut        VARCHAR(20)  NOT NULL,
    priorite      VARCHAR(20)  NOT NULL,
    date_echeance DATE,
    position      INT          NOT NULL DEFAULT 0,
    projet_id     BIGINT       NOT NULL,
    membre_id     BIGINT,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    CONSTRAINT fk_tache_projet FOREIGN KEY (projet_id) REFERENCES projets(id) ON DELETE CASCADE,
    CONSTRAINT fk_tache_membre FOREIGN KEY (membre_id) REFERENCES membres(id) ON DELETE SET NULL
);

-- ─── Module 6 : Calendrier & Réunions ────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS reunions (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre         VARCHAR(200) NOT NULL,
    description   VARCHAR(1000),
    date_heure    DATETIME     NOT NULL,
    duree_minutes INT,
    lien          VARCHAR(500),
    statut        VARCHAR(20)  NOT NULL,
    projet_id     BIGINT       NOT NULL,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    CONSTRAINT fk_reunion_projet FOREIGN KEY (projet_id) REFERENCES projets(id) ON DELETE CASCADE
);

-- ─── Module 7 : Campagnes marketing ──────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS campagnes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    canal       VARCHAR(30)  NOT NULL,
    statut      VARCHAR(20)  NOT NULL,
    budget      DOUBLE,
    date_debut  DATE,
    date_fin    DATE,
    impressions INT          NOT NULL DEFAULT 0,
    clics       INT          NOT NULL DEFAULT 0,
    conversions INT          NOT NULL DEFAULT 0,
    startup_id  BIGINT       NOT NULL,
    expert_id   BIGINT       NOT NULL,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    CONSTRAINT fk_campagne_startup FOREIGN KEY (startup_id) REFERENCES startups(id) ON DELETE CASCADE,
    CONSTRAINT fk_campagne_expert  FOREIGN KEY (expert_id)  REFERENCES experts(id)  ON DELETE CASCADE
);

-- ─── Module 9 : Support & Tickets ────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS tickets (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    sujet       VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    statut      VARCHAR(20)  NOT NULL,
    priorite    VARCHAR(20)  NOT NULL,
    startup_id  BIGINT       NOT NULL,
    expert_id   BIGINT       NOT NULL,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    CONSTRAINT fk_ticket_startup FOREIGN KEY (startup_id) REFERENCES startups(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_expert  FOREIGN KEY (expert_id)  REFERENCES experts(id)  ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS messages_ticket (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id   BIGINT        NOT NULL,
    auteur_nom  VARCHAR(150),
    auteur_role VARCHAR(20),
    contenu     VARCHAR(1000) NOT NULL,
    date_envoi  DATETIME      NOT NULL,
    CONSTRAINT fk_msg_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
);

-- ─── Module 11 : Notifications ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS notifications (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    titre      VARCHAR(200) NOT NULL,
    message    VARCHAR(500),
    lien       VARCHAR(200),
    lu         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ─── Module 10 : Facturation ─────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS factures (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero        VARCHAR(40)  NOT NULL UNIQUE,
    startup_id    BIGINT       NOT NULL,
    expert_id     BIGINT       NOT NULL,
    projet_id     BIGINT,
    montant_ht    DOUBLE       NOT NULL DEFAULT 0,
    taux_tva      DOUBLE       NOT NULL DEFAULT 0,
    montant_ttc   DOUBLE       NOT NULL DEFAULT 0,
    statut        VARCHAR(20)  NOT NULL,
    date_emission DATE,
    date_echeance DATE,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    CONSTRAINT fk_facture_startup FOREIGN KEY (startup_id) REFERENCES startups(id) ON DELETE CASCADE,
    CONSTRAINT fk_facture_expert  FOREIGN KEY (expert_id)  REFERENCES experts(id)  ON DELETE CASCADE,
    CONSTRAINT fk_facture_projet  FOREIGN KEY (projet_id)  REFERENCES projets(id)  ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS lignes_facture (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    facture_id    BIGINT       NOT NULL,
    description   VARCHAR(300) NOT NULL,
    quantite      DOUBLE       NOT NULL DEFAULT 1,
    prix_unitaire DOUBLE       NOT NULL DEFAULT 0,
    montant       DOUBLE       NOT NULL DEFAULT 0,
    CONSTRAINT fk_ligne_facture FOREIGN KEY (facture_id) REFERENCES factures(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS messages_prives (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    expediteur_id   BIGINT        NOT NULL,
    destinataire_id BIGINT        NOT NULL,
    contenu         VARCHAR(2000) NOT NULL,
    lu              BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      DATETIME      NOT NULL,
    CONSTRAINT fk_mp_expediteur   FOREIGN KEY (expediteur_id)   REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_mp_destinataire FOREIGN KEY (destinataire_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ─── Métier avancé M2 : date de début d'essai gratuit (idempotent) ───────────────
ALTER TABLE experts ADD COLUMN IF NOT EXISTS date_debut_essai DATE;
