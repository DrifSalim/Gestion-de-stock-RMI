package Client;

import Model.Article;
import Model.Facture;
import Model.Ligne_Facture;
import Serveur.IBricoMerlinService;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ClientRMI {
    private IBricoMerlinService service;
    private Scanner scanner;
    private int currentFactureId;

    public ClientRMI(String host) {
        try {


            // Connexion au serveur RMI
            Registry registry = LocateRegistry.getRegistry(null);
            service = (IBricoMerlinService) registry.lookup("BricoMerlinService");

            System.out.println("Connexion au serveur BricoMerlin réussie");

            scanner = new Scanner(System.in);
            showMainMenu();
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Erreur de connexion au serveur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showMainMenu() {
        boolean exit = false;

        while (!exit) {
            System.out.println("\n===== BRICO-MERLIN - MENU PRINCIPAL =====");
            System.out.println("1. Consulter le stock d'un article");
            System.out.println("2. Rechercher des articles par famille");
            System.out.println("3. Créer un nouveau panier");
            System.out.println("4. Ajouter un article au panier");
            System.out.println("5. Consulter et payer une facture");
            System.out.println("6. Consulter le chiffre d'affaires");
            System.out.println("7. Gérer le stock (Administration)");
            System.out.println("8. Quitter");
            System.out.print("Votre choix: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        consulterStock();
                        break;
                    case 2:
                        rechercherArticlesParFamille();
                        break;
                    case 3:
                        creerNouveauPanier();
                        break;
                    case 4:
                        ajouterArticleAuPanier();
                        break;
                    case 5:
                        consulterEtPayerFacture();
                        break;
                    case 6:
                        consulterChiffreAffaires();
                        break;
                    case 7:
                        menuAdministration();
                        break;
                    case 8:
                        exit = true;
                        System.out.println("Merci d'avoir utilisé le système Brico-Merlin. Au revoir !");
                        break;
                    default:
                        System.out.println("Option invalide. Veuillez réessayer.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un nombre valide.");
            } catch (Exception e) {
                System.err.println("Erreur: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void consulterStock() throws RemoteException {
        System.out.println("\n===== CONSULTATION DU STOCK =====");
        System.out.print("Entrez la référence de l'article: ");
        try {
            long reference = Long.parseLong(scanner.nextLine());

            Article article = service.consulterStock(reference);

            if (article != null) {
                System.out.println("\nInformations sur l'article:");
                System.out.println("Référence: " + article.getReference());
                System.out.println("Nom: " + article.getNom());
                System.out.println("Famille: " + article.getFamille().getNom());
                System.out.println("Prix unitaire: " + article.getPrix() + " €");
                System.out.println("Quantité en stock: " + article.getQuantite_stock());
            } else {
                System.out.println("Article non trouvé.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Référence invalide. Veuillez entrer un nombre.");
        }
    }

    private void rechercherArticlesParFamille() throws RemoteException {
        System.out.println("\n===== RECHERCHE D'ARTICLES PAR FAMILLE =====");
        System.out.print("Entrez le nom de la famille d'articles: ");
        String nomFamille = scanner.nextLine();

        List<Article> articles = service.rechercherArticlesParFamille(nomFamille);

        if (articles.isEmpty()) {
            System.out.println("Aucun article trouvé dans cette famille ou la famille n'existe pas.");
            return;
        }

        System.out.println("\nArticles disponibles dans la famille '" + nomFamille + "':");
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-10s %-30s %-10s %-10s\n", "Référence", "Nom", "Prix (€)", "Stock");
        System.out.println("------------------------------------------------------------");

        for (Article article : articles) {
            System.out.printf("%-10d %-30s %-10.2f %-10d\n",
                    article.getReference(),
                    article.getNom(),
                    article.getPrix(),
                    article.getQuantite_stock());
        }
    }

    private void creerNouveauPanier() {
        System.out.println("\n===== CRÉATION D'UN NOUVEAU PANIER =====");
        try {
            this.currentFactureId = service.creerNouvelleFacture();
            System.out.println("Nouveau panier créé avec succès. ID de facture: " + currentFactureId);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du panier: " + e.getMessage());
        }
    }

    private void ajouterArticleAuPanier() throws RemoteException {
        if (currentFactureId == 0) {
            System.out.println("Veuillez d'abord créer un nouveau panier (option 3).");
            return;
        }

        System.out.println("\n===== AJOUT D'UN ARTICLE AU PANIER =====");
        System.out.print("Référence de l'article à ajouter: ");
        try {
            long reference = Long.parseLong(scanner.nextLine());

            // Vérifier si l'article existe
            Article article = service.consulterStock(reference);
            if (article == null) {
                System.out.println("Article non trouvé.");
                return;
            }

            System.out.println("Article: " + article.getNom() + " - Prix: " + article.getPrix() + " €");
            System.out.print("Quantité à ajouter: ");
            int quantite = Integer.parseInt(scanner.nextLine());

            if (quantite <= 0) {
                System.out.println("La quantité doit être supérieure à 0.");
                return;
            }

            if (quantite > article.getQuantite_stock()) {
                System.out.println("Stock insuffisant. Il reste seulement " + article.getQuantite_stock() + " exemplaire(s).");
                return;
            }

            boolean success = service.acheterArticle(reference, quantite, currentFactureId);

            if (success) {
                System.out.println("Article ajouté au panier avec succès.");
            } else {
                System.out.println("Erreur lors de l'ajout de l'article au panier.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Veuillez entrer des valeurs numériques valides.");
        }
    }

    private void consulterEtPayerFacture() throws RemoteException {
            System.out.println("\n===== CONSULTER ET PAYER UNE FACTURE =====");
            System.out.print("Entrez l'ID de la facture: ");
        try {
            int idFacture = Integer.parseInt(scanner.nextLine());

            Facture facture = service.consulterFacture(idFacture);

            if (facture == null) {
                System.out.println("Facture non trouvée.");
                return;
            }

            afficherFacture(facture);

            if ("payee".equals(facture.getStatus())) {
                System.out.println("Cette facture est déjà payée.");
                return;
            }

            System.out.print("Voulez-vous payer cette facture ? (O/N): ");
            String reponse = scanner.nextLine();

            if (reponse.equalsIgnoreCase("O")) {
                System.out.println("\nModes de paiement disponibles:");
                System.out.println("1. Carte Bancaire");
                System.out.println("2. Espèces");
                System.out.println("3. Chèque");

                System.out.print("Choisissez un mode de paiement (1-3): ");
                int choixPaiement = Integer.parseInt(scanner.nextLine());

                String modePaiement;
                switch (choixPaiement) {
                    case 1:
                        modePaiement = "Carte Bancaire";
                        break;
                    case 2:
                        modePaiement = "Espèces";
                        break;
                    case 3:
                        modePaiement = "Chèque";
                        break;
                    default:
                        System.out.println("Choix invalide. Paiement annulé.");
                        return;
                }

                boolean success = service.payerFacture(idFacture, modePaiement);

                if (success) {
                    System.out.println("Paiement effectué avec succès.");
                    System.out.println("Merci de votre achat chez Brico-Merlin!");
                } else {
                    System.out.println("Erreur lors du paiement de la facture.");
                }
            } else {
                System.out.println("Paiement annulé.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Veuillez entrer un ID de facture valide.");
        }
    }

    private void afficherFacture(Facture facture) {
        System.out.println("\n==========================================");
        System.out.println("              BRICO-MERLIN");
        System.out.println("          TICKET DE CAISSE");
        System.out.println("==========================================");
        System.out.println("Facture N°: " + facture.getId_facture());
        System.out.println("Date: " + facture.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("Statut: " + facture.getStatus());
        if (facture.getMode_paiement() != null) {
            System.out.println("Mode de paiement: " + facture.getMode_paiement());
        }

        System.out.println("------------------------------------------");
        System.out.printf("%-10s %-30s %-8s %-8s %-10s\n", "Réf.", "Article", "Prix", "Qté", "Total");
        System.out.println("------------------------------------------");

        double total = 0;
        for (Ligne_Facture ligne : facture.getDetails()) {
            double sousTotal = ligne.getPrix() * ligne.getQuantite();
            System.out.printf("%-10d %-30s %-8.2f %-8d %-10.2f\n",
                    ligne.getReference(),
                    (ligne.getArticle() != null ? ligne.getArticle().getNom() : "Article inconnu"),
                    ligne.getPrix(),
                    ligne.getQuantite(),
                    sousTotal);
            total += sousTotal;
        }

        System.out.println("------------------------------------------");
        System.out.printf("%-49s %-10.2f\n", "TOTAL", facture.getMontant());
        System.out.println("==========================================");
    }

    private void consulterChiffreAffaires() throws RemoteException {
        System.out.println("\n===== CONSULTATION DU CHIFFRE D'AFFAIRES =====");
        System.out.println("Entrez la date (format: JJ/MM/AAAA):");

        try {
            String dateStr = scanner.nextLine();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDateTime date = LocalDateTime.parse(dateStr + " 00:00:00", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            double chiffreAffaire = service.calculerChiffreAffaire(date);

            System.out.println("\nChiffre d'affaires pour le " + dateStr + ": " + chiffreAffaire + " €");
        } catch (Exception e) {
            System.out.println("Format de date invalide ou erreur lors de la consultation.");
            System.out.println("Veuillez entrer une date au format JJ/MM/AAAA (exemple: 12/05/2024).");
        }
    }

    private void menuAdministration() {
        System.out.println("\n===== MENU ADMINISTRATION =====");
        System.out.println("1. Ajouter du stock à un article");
        System.out.println("2. Mettre à jour les prix");
        System.out.println("3. Retour au menu principal");
        System.out.print("Votre choix: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    ajouterStock();
                    break;
                case 2:
                    mettreAJourPrix();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Option invalide. Veuillez réessayer.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Veuillez entrer un nombre valide.");
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    private void ajouterStock() throws RemoteException {
        System.out.println("\n===== AJOUT DE STOCK =====");
        System.out.print("Référence de l'article: ");

        try {
            long reference = Long.parseLong(scanner.nextLine());

            // Vérifier si l'article existe
            Article article = service.consulterStock(reference);
            if (article == null) {
                System.out.println("Article non trouvé.");
                return;
            }

            System.out.println("Article: " + article.getNom() + " - Stock actuel: " + article.getQuantite_stock());
            System.out.print("Quantité à ajouter: ");
            int quantite = Integer.parseInt(scanner.nextLine());

            if (quantite <= 0) {
                System.out.println("La quantité doit être supérieure à 0.");
                return;
            }

            service.ajouterStock(reference, quantite);
            System.out.println("Stock mis à jour avec succès.");

            // Afficher le nouveau stock
            article = service.consulterStock(reference);
            System.out.println("Nouveau stock: " + article.getQuantite_stock());

        } catch (NumberFormatException e) {
            System.out.println("Veuillez entrer des valeurs numériques valides.");
        }
    }

    private void mettreAJourPrix() throws RemoteException {
        System.out.println("\n===== MISE À JOUR DES PRIX =====");
        System.out.println("Combien d'articles souhaitez-vous mettre à jour ?");

        try {
            int nbArticles = Integer.parseInt(scanner.nextLine());

            if (nbArticles <= 0) {
                System.out.println("Le nombre d'articles doit être supérieur à 0.");
                return;
            }

            Map<Long, Double> nouveauxPrix = new HashMap<>();

            for (int i = 0; i < nbArticles; i++) {
                System.out.println("\nArticle " + (i + 1) + " :");
                System.out.print("Référence: ");
                long reference = Long.parseLong(scanner.nextLine());

                // Vérifier si l'article existe
                Article article = service.consulterStock(reference);
                if (article == null) {
                    System.out.println("Article non trouvé. Passage au suivant.");
                    continue;
                }

                System.out.println("Article: " + article.getNom() + " - Prix actuel: " + article.getPrix() + " €");
                System.out.print("Nouveau prix: ");
                double nouveauPrix = Double.parseDouble(scanner.nextLine());

                if (nouveauPrix <= 0) {
                    System.out.println("Le prix doit être supérieur à 0. Passage au suivant.");
                    continue;
                }

                nouveauxPrix.put(reference, nouveauPrix);
            }

            if (!nouveauxPrix.isEmpty()) {
                service.mettreAJourPrix(nouveauxPrix);
                System.out.println("Prix mis à jour avec succès.");
            } else {
                System.out.println("Aucun prix n'a été mis à jour.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Veuillez entrer des valeurs numériques valides.");
        }
    }

    public static void main(String[] args) {
        String host = "localhost";

        if (args.length > 0) {
            host = args[0];
        }

        new ClientRMI(host);
    }
}