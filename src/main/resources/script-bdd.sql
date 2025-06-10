-- Création de la base de données
CREATE DATABASE IF NOT EXISTS brico_merlin;
USE brico_merlin;

-- Table Famille
CREATE TABLE famille (
                         id_famille INT PRIMARY KEY AUTO_INCREMENT,
                         nom VARCHAR(50) NOT NULL UNIQUE
);

-- Table Article
CREATE TABLE article (
                         reference BIGINT PRIMARY KEY,
                         nom VARCHAR(100) NOT NULL,
                         prix DECIMAL(10, 2) NOT NULL CHECK (prix > 0),
                         quantite_stock INT NOT NULL CHECK (quantite_stock >= 0),
                         id_famille INT NOT NULL,
                         FOREIGN KEY (id_famille) REFERENCES famille(id_famille)
);

-- Table Facture
CREATE TABLE facture (
                         id_facture INT PRIMARY KEY AUTO_INCREMENT,
                         montant DECIMAL(10, 2) NOT NULL,
                         date DATETIME NOT NULL,
                         status VARCHAR(20) DEFAULT 'en_attente' CHECK (status IN ('payee', 'en_attente')),
                         mode_paiement VARCHAR(50)
);

-- Table Ligne_Facture
CREATE TABLE ligne_facture (
                               id_facture INT,
                               reference BIGINT,
                               quantite INT NOT NULL CHECK (quantite > 0),
                               prix DECIMAL(10, 2) NOT NULL,
                               PRIMARY KEY (id_facture, reference),
                               FOREIGN KEY (id_facture) REFERENCES facture(id_facture),
                               FOREIGN KEY (reference) REFERENCES article(reference)
);

-- Insertion de données de test
INSERT INTO Famille (nom) VALUES
                              ('Outillage'),
                              ('Plomberie'),
                              ('Électricité'),
                              ('Jardin'),
                              ('Quincaillerie');


INSERT INTO Article (reference, nom, id_famille, prix, quantite_stock) VALUES
                                                                           (1001, 'Marteau de charpentier', 1, 15.99, 50),
                                                                           (1002, 'Tournevis cruciforme', 1, 8.50, 100),
                                                                           (1003, 'Robinet de cuisine', 2, 45.95, 20),
                                                                           (1004, 'Tuyau flexible 2m', 2, 12.75, 30),
                                                                           (1005, 'Ampoule LED 10W', 3, 6.99, 200),
                                                                           (1006, 'Multiprise 6 ports', 3, 19.99, 40),
                                                                           (1007, 'Tondeuse électrique', 4, 129.99, 10),
                                                                           (1008, 'Arrosoir 5L', 4, 7.50, 25),
                                                                           (1009, 'Lot 100 vis à bois', 5, 9.95, 150),
                                                                           (1010, 'Colle à bois 500ml', 5, 5.45, 75);



-- Quelques factures
INSERT INTO Facture (montant, date, status, mode_paiement) VALUES
                                                               (24.49, '2024-05-01 10:15:00', 'payee', 'Carte Bancaire'),
                                                               (129.99, '2024-05-01 14:30:00', 'payee', 'Espèces'),
                                                               (30.92, '2024-05-02 09:45:00', 'en_attente', NULL);

-- Lignes de facture
INSERT INTO Ligne_Facture (id_facture, reference, quantite, prix) VALUES
                                                                      (1, 1001, 1, 15.99),
                                                                      (1, 1002, 1, 8.50),
                                                                      (2, 1007, 1, 129.99),
                                                                      (3, 1005, 3, 6.99),
                                                                      (3, 1009, 1, 9.95);