package Client;

import Model.Article;
import Model.Facture;
import Model.Ligne_Facture;
import Serveur.IBricoMerlinService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmployeeGUI extends JFrame {
    private IBricoMerlinService stub;
    private int currentFactureId = 0;
    private boolean facturePayee = false;

    // Composants principaux
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel menuPanel;

    // Panels pour chaque fonctionnalité
    private JPanel stockPanel;
    private JPanel recherchePanel;
    private JPanel panierPanel;
    private JPanel facturePanel;

    // Composants pour consulter le stock
    private JTextField refStockField;
    private JTextArea stockInfoArea;

    // Composants pour rechercher des articles
    private JTextField familleField;
    private JTable articlesTable;
    private DefaultTableModel articlesModel;

    // Composants pour le panier
    private JLabel panierIdLabel;
    private JTextField refPanierField;
    private JTextField qtePanierField;
    private JTextArea panierInfoArea;

    // Composants pour la facture
    private JTextField factureIdField;
    private JTextArea factureInfoArea;
    private JComboBox<String> modePaiementCombo;
    private JButton payerButton;
    private JLabel statutPaiementLabel;
    private JPanel paiementPanel;
    private JButton ajouterArticleFactureButton; // Nouveau bouton pour ajouter un article


    public EmployeeGUI() {
        super("Brico-Merlin - Système de Gestion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        try {
            // Connexion au serveur RMI

            Registry registry = LocateRegistry.getRegistry(null);
            stub = (IBricoMerlinService) registry.lookup("BricoMerlinService");

            initUI();
        } catch (RemoteException | NotBoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de connexion au serveur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initUI() {
        // Initialisation du layout principal
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);

        // Création du menu principal
        createMenuPanel();

        // Création des panels pour chaque fonctionnalité
        createStockPanel();
        createRecherchePanel();
        createPanierPanel();
        createFacturePanel();

        // Ajout des panels au panel principal
        mainPanel.add(menuPanel, "menu");
        mainPanel.add(stockPanel, "stock");
        mainPanel.add(recherchePanel, "recherche");
        mainPanel.add(panierPanel, "panier");
        mainPanel.add(facturePanel, "facture");

        // Affichage du menu principal
        cardLayout.show(mainPanel, "menu");

        // Ajout du panel principal à la fenêtre
        add(mainPanel);
    }

    private void createMenuPanel() {
        menuPanel = new JPanel(new BorderLayout());

        // Titre
        JLabel titleLabel = new JLabel("BRICO-MERLIN - MENU PRINCIPAL", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        menuPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel pour les boutons
        JPanel buttonsPanel = new JPanel(new GridLayout(8, 1, 10, 10));
        buttonsPanel.setBorder(new EmptyBorder(20, 50, 20, 50));

        // Boutons du menu
        JButton stockButton = createMenuButton("1. Consulter le stock d'un article");
        JButton rechercheButton = createMenuButton("2. Rechercher des articles par famille");
        JButton nouveauPanierButton = createMenuButton("3. Créer un nouveau panier");
        JButton ajouterArticleButton = createMenuButton("4. Ajouter un article au panier");
        JButton factureButton = createMenuButton("5. Consulter et payer une facture");
        JButton quitterButton = createMenuButton("8. Quitter");

        // Actions des boutons
        stockButton.addActionListener(e -> cardLayout.show(mainPanel, "stock"));
        rechercheButton.addActionListener(e -> cardLayout.show(mainPanel, "recherche"));
        nouveauPanierButton.addActionListener(e -> creerNouveauPanier());
        ajouterArticleButton.addActionListener(e -> {
            if (currentFactureId == 0) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez d'abord créer un nouveau panier (option 3).",
                        "Erreur", JOptionPane.WARNING_MESSAGE);
            } else {
                cardLayout.show(mainPanel, "panier");
            }
        });
        factureButton.addActionListener(e -> {
            facturePayee = false;
            factureIdField.setText("");
            factureInfoArea.setText("");
            updatePaiementPanelVisibility();
            cardLayout.show(mainPanel, "facture");
        });
        quitterButton.addActionListener(e -> System.exit(0));

        // Ajout des boutons au panel
        buttonsPanel.add(stockButton);
        buttonsPanel.add(rechercheButton);
        buttonsPanel.add(nouveauPanierButton);
        buttonsPanel.add(ajouterArticleButton);
        buttonsPanel.add(factureButton);
        buttonsPanel.add(quitterButton);

        menuPanel.add(buttonsPanel, BorderLayout.CENTER);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setPreferredSize(new Dimension(300, 50));
        return button;
    }

    private void createStockPanel() {
        stockPanel = new JPanel(new BorderLayout());

        // Titre
        JLabel titleLabel = new JLabel("CONSULTATION DU STOCK", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        stockPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel pour la saisie
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel refLabel = new JLabel("Référence de l'article:");
        refStockField = new JTextField(10);
        JButton consulterButton = new JButton("Consulter");

        inputPanel.add(refLabel);
        inputPanel.add(refStockField);
        inputPanel.add(consulterButton);

        // Zone d'affichage des informations
        stockInfoArea = new JTextArea(10, 40);
        stockInfoArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(stockInfoArea);

        // Bouton retour
        JButton retourButton = new JButton("Retour au menu principal");
        retourButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        // Panel central
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(retourButton, BorderLayout.SOUTH);

        stockPanel.add(centerPanel, BorderLayout.CENTER);

        // Action du bouton consulter
        consulterButton.addActionListener(e -> {
            try {
                long reference = Long.parseLong(refStockField.getText());
                afficherconsulterStock(reference);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez entrer une référence valide (nombre entier).",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la consultation: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void afficherconsulterStock(long reference) throws RemoteException {
        Article article = stub.consulterStock(reference);

        if (article != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Informations sur l'article:\n\n");
            sb.append("Référence: ").append(article.getReference()).append("\n");
            sb.append("Nom: ").append(article.getNom()).append("\n");
            sb.append("Famille: ").append(article.getFamille().getNom()).append("\n");
            sb.append("Prix unitaire: ").append(article.getPrix()).append(" €\n");
            sb.append("Quantité en stock: ").append(article.getQuantite_stock());

            stockInfoArea.setText(sb.toString());
        } else {
            stockInfoArea.setText("Article non trouvé.");
        }
    }

    private void createRecherchePanel() {
        recherchePanel = new JPanel(new BorderLayout());

        // Titre
        JLabel titleLabel = new JLabel("RECHERCHE D'ARTICLES PAR FAMILLE", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        recherchePanel.add(titleLabel, BorderLayout.NORTH);

        // Panel pour la saisie
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel familleLabel = new JLabel("Nom de la famille:");
        familleField = new JTextField(20);
        JButton rechercherButton = new JButton("Rechercher");

        inputPanel.add(familleLabel);
        inputPanel.add(familleField);
        inputPanel.add(rechercherButton);

        // Tableau pour les résultats
        String[] columnNames = {"N°", "Référence", "Nom", "Prix (€)", "Stock"};
        articlesModel = new DefaultTableModel(columnNames, 0);
        articlesTable = new JTable(articlesModel);
        JScrollPane tableScrollPane = new JScrollPane(articlesTable);

        // Bouton retour
        JButton retourButton = new JButton("Retour au menu principal");
        retourButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        // Panel central
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        centerPanel.add(retourButton, BorderLayout.SOUTH);

        recherchePanel.add(centerPanel, BorderLayout.CENTER);

        // Action du bouton rechercher
        rechercherButton.addActionListener(e -> {
            try {
                String nomFamille = familleField.getText();
                rechercherArticlesParFamille(nomFamille);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la recherche: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void rechercherArticlesParFamille(String nomFamille) throws RemoteException {
        List<Article> articles = stub.rechercherArticlesParFamille(nomFamille);

        // Vider le tableau
        articlesModel.setRowCount(0);

        if (articles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Aucun article trouvé dans cette famille ou la famille n'existe pas.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int c=1;
        // Remplir le tableau avec les résultats
        for (Article article : articles) {
            Object[] row = {
                    c,
                    article.getReference(),
                    article.getNom(),
                    article.getPrix(),
                    article.getQuantite_stock()
            };
            articlesModel.addRow(row);
            c++;
        }
    }

    private void createPanierPanel() {
        panierPanel = new JPanel(new BorderLayout());

        // Titre
        JLabel titleLabel = new JLabel("AJOUT D'UN ARTICLE AU PANIER", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        panierPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel pour l'ID du panier
        JPanel panierIdPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panierIdLabel = new JLabel("Panier actuel: Aucun");
        panierIdPanel.add(panierIdLabel);

        // Panel pour la saisie
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(20, 50, 20, 50));

        JLabel refLabel = new JLabel("Référence de l'article:");
        refPanierField = new JTextField(10);
        JLabel qteLabel = new JLabel("Quantité:");
        qtePanierField = new JTextField(10);

        inputPanel.add(refLabel);
        inputPanel.add(refPanierField);
        inputPanel.add(qteLabel);
        inputPanel.add(qtePanierField);

        JButton ajouterButton = new JButton("Ajouter au panier");
        JButton infoButton = new JButton("Informations sur l'article");

        inputPanel.add(infoButton);
        inputPanel.add(ajouterButton);

        // Zone d'affichage des informations
        panierInfoArea = new JTextArea(10, 40);
        panierInfoArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(panierInfoArea);

        // Bouton retour
        JButton retourButton = new JButton("Retour au menu principal");
        retourButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        // Bouton pour retourner à la facture après ajout d'article
        JButton retourFactureButton = new JButton("Retour à la facture");
        retourFactureButton.addActionListener(e -> {
            if (currentFactureId > 0) {
                try {
                    // 1. Mettre à jour le champ ID
                    factureIdField.setText(String.valueOf(currentFactureId));

                    // 2. Consulter la facture (cela rafraîchira aussi le statut)
                    consulterFacture(currentFactureId);

                    // 3. Aller à l'écran de facture
                    cardLayout.show(mainPanel, "facture");
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Erreur : " + ex.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                cardLayout.show(mainPanel, "facture");
            }
        });

        // Panel central
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(panierIdPanel, BorderLayout.NORTH);
        centerPanel.add(inputPanel, BorderLayout.CENTER);
        centerPanel.add(scrollPane, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(retourButton);
        bottomPanel.add(retourFactureButton);

        panierPanel.add(centerPanel, BorderLayout.CENTER);
        panierPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Action du bouton info
        infoButton.addActionListener(e -> {
            try {
                if (refPanierField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Veuillez entrer une référence d'article.",
                            "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                long reference = Long.parseLong(refPanierField.getText());
                Article article = stub.consulterStock(reference);

                if (article != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Informations sur l'article:\n\n");
                    sb.append("Référence: ").append(article.getReference()).append("\n");
                    sb.append("Nom: ").append(article.getNom()).append("\n");
                    sb.append("Famille: ").append(article.getFamille().getNom()).append("\n");
                    sb.append("Prix unitaire: ").append(article.getPrix()).append(" €\n");
                    sb.append("Quantité en stock: ").append(article.getQuantite_stock());

                    panierInfoArea.setText(sb.toString());
                } else {
                    panierInfoArea.setText("Article non trouvé.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez entrer une référence valide (nombre entier).",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la consultation: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action du bouton ajouter
        ajouterButton.addActionListener(e -> {
            try {
                if (refPanierField.getText().isEmpty() || qtePanierField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Veuillez remplir tous les champs.",
                            "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                long reference = Long.parseLong(refPanierField.getText());
                int quantite = Integer.parseInt(qtePanierField.getText());

                if (quantite <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "La quantité doit être supérieure à 0.",
                            "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Vérifier si l'article existe
                Article article = stub.consulterStock(reference);
                if (article == null) {
                    JOptionPane.showMessageDialog(this,
                            "Article non trouvé.",
                            "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (quantite > article.getQuantite_stock()) {
                    JOptionPane.showMessageDialog(this,
                            "Stock insuffisant. Il reste seulement " + article.getQuantite_stock() + " exemplaire(s).",
                            "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                boolean success = stub.acheterArticle(reference, quantite, currentFactureId);

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Article ajouté au panier avec succès.",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                    refPanierField.setText("");
                    qtePanierField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Erreur lors de l'ajout de l'article au panier.",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez entrer des valeurs numériques valides.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'ajout au panier: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void creerNouveauPanier() {
        try {
            // Appel de la méthode distante pour créer une nouvelle facture
            this.currentFactureId = stub.creerNouvelleFacture();

            panierIdLabel.setText("Panier actuel: " + currentFactureId);
            JOptionPane.showMessageDialog(this,
                    "Nouveau panier créé avec succès. ID de facture: " + currentFactureId,
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la création du panier: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createFacturePanel() {
        facturePanel = new JPanel(new BorderLayout());

        // Titre
        JLabel titleLabel = new JLabel("CONSULTER ET PAYER UNE FACTURE", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        facturePanel.add(titleLabel, BorderLayout.NORTH);

        // Panel pour la saisie
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel idLabel = new JLabel("ID de la facture:");
        factureIdField = new JTextField(10);
        JButton consulterButton = new JButton("Consulter");

        // Nouveau bouton pour ajouter un article à la facture
        ajouterArticleFactureButton = new JButton("Ajouter article");
        ajouterArticleFactureButton.setVisible(false); // Caché par défaut

        inputPanel.add(idLabel);
        inputPanel.add(factureIdField);
        inputPanel.add(consulterButton);
        inputPanel.add(ajouterArticleFactureButton);

        // Zone d'affichage des informations
        factureInfoArea = new JTextArea(15, 50);
        factureInfoArea.setEditable(false);
        factureInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(factureInfoArea);

        // Panel pour le paiement
        paiementPanel = new JPanel(new BorderLayout());

        // Panel pour le choix du mode de paiement
        JPanel choixPaiementPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel modeLabel = new JLabel("Mode de paiement:");
        String[] modes = {"Carte Bancaire", "Espèces", "Chèque"};
        modePaiementCombo = new JComboBox<>(modes);
        payerButton = new JButton("Payer la facture");

        choixPaiementPanel.add(modeLabel);
        choixPaiementPanel.add(modePaiementCombo);
        choixPaiementPanel.add(payerButton);

        // Panel pour afficher le statut de paiement
        JPanel statutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statutPaiementLabel = new JLabel("");
        statutPaiementLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statutPaiementLabel.setForeground(new Color(0, 128, 0)); // Vert
        statutPanel.add(statutPaiementLabel);

        paiementPanel.add(choixPaiementPanel, BorderLayout.NORTH);
        paiementPanel.add(statutPanel, BorderLayout.CENTER);

        // Bouton retour
        JButton retourButton = new JButton("Retour au menu principal");
        retourButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        // Panel central
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(paiementPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(retourButton);

        facturePanel.add(centerPanel, BorderLayout.CENTER);
        facturePanel.add(bottomPanel, BorderLayout.SOUTH);

        // Action du bouton consulter
        consulterButton.addActionListener(e -> {
            try {
                int idFacture = Integer.parseInt(factureIdField.getText());
                consulterFacture(idFacture);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez entrer un ID de facture valide (nombre entier).",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la consultation: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action du bouton ajouter article
        ajouterArticleFactureButton.addActionListener(e -> {
            try {
                int idFacture = Integer.parseInt(factureIdField.getText());

                // Vérifier si la facture existe
                Facture facture = stub.consulterFacture(idFacture);
                if (facture == null) {
                    JOptionPane.showMessageDialog(this,
                            "Facture non trouvée.",
                            "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Vérifier si la facture n'est pas déjà payée
                if ("payee".equals(facture.getStatus())) {
                    JOptionPane.showMessageDialog(this,
                            "Cette facture est déjà payée. Impossible d'ajouter des articles.",
                            "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Confirmation avant de rediriger
                int confirmation = JOptionPane.showConfirmDialog(this,
                        "Voulez-vous ajouter un article à la facture n° " + idFacture + " ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    // Définir la facture courante pour l'ajout d'articles
                    currentFactureId = idFacture;
                    // Mettre à jour le label du panier
                    panierIdLabel.setText("Panier actuel: " + currentFactureId);
                    // Aller à l'écran d'ajout d'article
                    cardLayout.show(mainPanel, "panier");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "ID de facture invalide.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la consultation de la facture: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action du bouton payer
        payerButton.addActionListener(e -> {
            try {
                if (factureIdField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Veuillez d'abord consulter une facture.",
                            "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int idFacture = Integer.parseInt(factureIdField.getText());
                String modePaiement = (String) modePaiementCombo.getSelectedItem();

                int confirmation = JOptionPane.showConfirmDialog(this,
                        "Voulez-vous payer cette facture avec " + modePaiement + " ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    boolean success = stub.payerFacture(idFacture, modePaiement);

                    if (success) {
                        stub.enregistrerFactureEnPDF(idFacture);

                        JOptionPane.showMessageDialog(this,
                                "Paiement effectué avec succès.\nMerci de votre achat chez Brico-Merlin!",
                                "Succès", JOptionPane.INFORMATION_MESSAGE);

                        consulterFacture(idFacture); // Rafraîchir l'affichage

                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Erreur lors du paiement de la facture.",
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez entrer un ID de facture valide (nombre entier).",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors du paiement: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void consulterFacture(int idFacture) throws RemoteException {
        Facture facture = stub.consulterFacture(idFacture);

        if (facture == null) {
            factureInfoArea.setText("Facture non trouvée.");
            facturePayee = false;
            updatePaiementPanelVisibility();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("              BRICO-MERLIN\n");
        sb.append("          TICKET DE CAISSE\n");
        sb.append("==========================================\n");
        sb.append("Facture N°: ").append(facture.getId_facture()).append("\n");
        sb.append("Date: ").append(facture.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        sb.append("Statut: ").append(facture.getStatus()).append("\n");
        if (facture.getMode_paiement() != null) {
            sb.append("Mode de paiement: ").append(facture.getMode_paiement()).append("\n");
        }

        sb.append("------------------------------------------\n");
        sb.append(String.format("%-10s %-30s %-8s %-8s %-10s\n", "Réf.", "Article", "Prix", "Qté", "Total"));
        sb.append("------------------------------------------\n");

        for (Ligne_Facture ligne : facture.getDetails()) {
            double sousTotal = ligne.getPrix() * ligne.getQuantite();
            sb.append(String.format("%-10d %-30s %-8.2f %-8d %-10.2f\n",
                    ligne.getReference(),
                    (ligne.getArticle() != null ? ligne.getArticle().getNom() : "Article inconnu"),
                    ligne.getPrix(),
                    ligne.getQuantite(),
                    sousTotal));
        }

        sb.append("------------------------------------------\n");
        sb.append(String.format("%-49s %-10.2f\n", "TOTAL A PAYER", facture.getMontant()));
        sb.append("==========================================\n");

        factureInfoArea.setText(sb.toString());

        facturePayee = "payee".equalsIgnoreCase(facture.getStatus());

        if (facturePayee && facture.getMode_paiement() != null) {
            statutPaiementLabel.setText("Cette facture est déjà payée en " + facture.getMode_paiement());
            //enregistrerFactureEnPDF(facture);
        } else {
            statutPaiementLabel.setText("");
        }

        updatePaiementPanelVisibility();
    }

    private void updatePaiementPanelVisibility() {
        // Récupérer les composants du panel de choix de paiement
        Component[] components = ((JPanel)paiementPanel.getComponent(0)).getComponents();

        // Si la facture est payée, masquer les options de paiement
        for (Component component : components) {
            component.setVisible(!facturePayee);
        }

        // Mettre à jour la visibilité du bouton "Ajouter article"
        ajouterArticleFactureButton.setVisible(!facturePayee && !factureIdField.getText().isEmpty());

        // Mettre à jour l'interface
        paiementPanel.revalidate();
        paiementPanel.repaint();
    }


    // Méthode utilitaire pour trouver un composant par son nom
    private Component findComponentByName(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container) {
                Component found = findComponentByName((Container) component, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EmployeeGUI client = new EmployeeGUI();
            client.setVisible(true);
        });
    }
}