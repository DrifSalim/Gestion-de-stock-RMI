-- Création de la base de données
CREATE DATABASE IF NOT EXISTS brico_merlin_central;
USE brico_merlin_central;
CREATE TABLE prix_articles (
                               reference BIGINT PRIMARY KEY,
                               prix DOUBLE NOT NULL,
                               date_mise_a_jour TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
INSERT INTO Article VALUES
                                                                           (1001, 50),
                                                                           (1002, 18.50),
                                                                           (1003, 25.95),
                                                                           (1004, 32.75,),
                                                                           (1005, 16.99),
                                                                           (1006, 20.99),
                                                                           (1007, 160.99),
                                                                           (1008, 9.50),
                                                                           (1009, 11.95),
                                                                           (1010, 15.45);


