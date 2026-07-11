-- Roles (idempotent)
INSERT IGNORE INTO roles (name, description) VALUES
    ('ROLE_CLIENT',      'Client de la plateforme'),
    ('ROLE_PRESTATAIRE', 'Prestataire de services'),
    ('ROLE_SUPER_ADMIN', 'Administrateur global');

-- Packs par défaut (idempotent — nom unique)
INSERT IGNORE INTO packs (nom, description, prix, nb_projets_max, nb_clients_max, duree_mois, actif, created_at, updated_at) VALUES
    ('Starter',  'Idéal pour démarrer : 3 projets, 5 clients.',        29.0,  3,  5,  1, TRUE, NOW(), NOW()),
    ('Business', 'Pour les équipes en croissance : 15 projets, 25 clients.', 99.0, 15, 25,  1, TRUE, NOW(), NOW()),
    ('Premium',  'Sans limite pratique : 100 projets, 200 clients.',  299.0, 100, 200, 12, TRUE, NOW(), NOW());
