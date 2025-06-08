-- Création de la base de données
CREATE DATABASE IF NOT EXISTS brico_merlin_central;
USE brico_merlin_central;
CREATE TABLE prix_articles (
                               reference BIGINT PRIMARY KEY,
                               prix DOUBLE NOT NULL,
                               date_mise_a_jour TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
INSERT INTO Article VALUES
                                                                           (1001, 15.99),
                                                                           (1002, 8.50),
                                                                           (1003, 45.95),
                                                                           (1004, 12.75,),
                                                                           (1005, 6.99),
                                                                           (1006, 19.99),
                                                                           (1007, 129.99),
                                                                           (1008, 7.50),
                                                                           (1009, 9.95),
                                                                           (1010, 5.45);


