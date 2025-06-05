package Client;

import Model.Article;
import Model.Facture;
import Model.Ligne_Facture;
import Serveur.IBricoMerlinService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainApplication2 extends JFrame {
    // Couleurs de l'application
    private static final Color PRIMARY_COLOR = new Color(0, 121, 107); // Vert foncé
    private static final Color SECONDARY_COLOR = new Color(38, 166, 154); // Vert clair
    private static final Color ACCENT_COLOR = new Color(255, 152, 0); // Orange
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Gris très clair
    private static final Color TEXT_COLOR = new Color(33, 33, 33); // Presque noir
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80); // Vert
    private static final Color ERROR_COLOR = new Color(244, 67, 54); // Rouge
    private static final Color WARNING_COLOR = new Color(255, 193, 7); // Jaune

    private IBricoMerlinService service;
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
    private JPanel chiffreAffairesPanel;
    private JPanel adminPanel;

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
    private JButton ajouterArticleFactureButton;

    // Composants pour le chiffre d'affaires
    private JTextField dateCAField;
    private JLabel chiffreAffairesLabel;

    // Composants pour l'administration
    private JTabbedPane adminTabbedPane;
    private JPanel stockAdminPanel;
    private JPanel prixAdminPanel;
    private JTextField refStockAdminField;
    private JTextField qteStockAdminField;
    private JTextArea stockAdminInfoArea;
    private JTextField nbArticlesField;
    private JPanel prixUpdatePanel;
    private JButton validerPrixButton;

    public MainApplication2(String host) {
        super("Brico-Merlin - Système de Gestion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Définir l'icône de l'application
        try {
            URL iconURL = getClass().getResource("/icons/logo.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            // Ignorer si l'icône n'est pas trouvée
        }

        // Appliquer le look and feel FlatLaf
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());

            // Personnaliser les couleurs du look and feel
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);

            UIManager.put("Button.background", SECONDARY_COLOR);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.focusedBackground", PRIMARY_COLOR);
            UIManager.put("Button.hoverBackground", PRIMARY_COLOR);
            UIManager.put("Button.pressedBackground", PRIMARY_COLOR.darker());

            UIManager.put("TabbedPane.selectedBackground", SECONDARY_COLOR);
            UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
            UIManager.put("TabbedPane.underlineColor", ACCENT_COLOR);

            UIManager.put("TextField.caretForeground", PRIMARY_COLOR);
            UIManager.put("TextField.focusedBorderColor", PRIMARY_COLOR);
            UIManager.put("TextField.selectionBackground", SECONDARY_COLOR);
            UIManager.put("TextField.selectionForeground", Color.WHITE);

            UIManager.put("Table.selectionBackground", SECONDARY_COLOR);
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.gridColor", new Color(220, 220, 220));

            UIManager.put("ScrollBar.thumb", SECONDARY_COLOR);
            UIManager.put("ScrollBar.thumbHover", PRIMARY_COLOR);
            UIManager.put("ScrollBar.thumbPressed", PRIMARY_COLOR.darker());

            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback au look and feel Nimbus si FlatLaf n'est pas disponible
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                // Fallback au look and feel du système
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        }

        try {
            // Connexion au serveur RMI
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            service = (IBricoMerlinService) registry.lookup("BricoMerlinService");

            showCustomDialog("Connexion au serveur BricoMerlin réussie", "Connexion", JOptionPane.INFORMATION_MESSAGE);

            initUI();
        } catch (RemoteException | NotBoundException e) {
            showCustomDialog("Erreur de connexion au serveur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void showCustomDialog(String message, String title, int messageType) {
        JOptionPane optionPane = new JOptionPane(message, messageType);
        JDialog dialog = optionPane.createDialog(this, title);

        // Personnaliser le dialogue
        dialog.setBackground(BACKGROUND_COLOR);

        // Afficher le dialogue
        dialog.setVisible(true);
    }

    private void initUI() {
        // Initialisation du layout principal
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Création du menu principal
        createMenuPanel();

        // Création des panels pour chaque fonctionnalité
        createStockPanel();
        createRecherchePanel();
        createPanierPanel();
        createFacturePanel();
        createChiffreAffairesPanel();
        createAdminPanel();

        // Ajout des panels au panel principal
        mainPanel.add(menuPanel, "menu");
        mainPanel.add(stockPanel, "stock");
        mainPanel.add(recherchePanel, "recherche");
        mainPanel.add(panierPanel, "panier");
        mainPanel.add(facturePanel, "facture");
        mainPanel.add(chiffreAffairesPanel, "ca");
        mainPanel.add(adminPanel, "admin");

        // Affichage du menu principal
        cardLayout.show(mainPanel, "menu");

        // Ajout du panel principal à la fenêtre
        add(mainPanel);

        // Ajouter une barre d'état en bas
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusBar.setBackground(PRIMARY_COLOR);

        JLabel statusLabel = new JLabel("Brico-Merlin © 2024 - Tous droits réservés");
        statusLabel.setForeground(Color.WHITE);
        statusBar.add(statusLabel, BorderLayout.WEST);

        JLabel versionLabel = new JLabel("Version 2.0");
        versionLabel.setForeground(Color.WHITE);
        statusBar.add(versionLabel, BorderLayout.EAST);

        return statusBar;
    }

    private void createMenuPanel() {
        menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBackground(BACKGROUND_COLOR);

        // Bannière en haut
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(PRIMARY_COLOR);
        bannerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel logoLabel = new JLabel("BRICO-MERLIN");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 32));
        logoLabel.setForeground(Color.WHITE);
        bannerPanel.add(logoLabel, BorderLayout.WEST);

        JLabel subtitleLabel = new JLabel("Système de Gestion");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        subtitleLabel.setForeground(Color.WHITE);
        bannerPanel.add(subtitleLabel, BorderLayout.EAST);

        menuPanel.add(bannerPanel, BorderLayout.NORTH);

        // Panel pour les boutons
        JPanel buttonsPanel = new JPanel(new GridLayout(4, 2, 20, 20));
        buttonsPanel.setBackground(BACKGROUND_COLOR);
        buttonsPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Boutons du menu avec icônes
        JButton stockButton = createMenuButton("Consulter le stock", "/icons/stock.png");
        JButton rechercheButton = createMenuButton("Rechercher des articles", "/icons/search.png");
        JButton nouveauPanierButton = createMenuButton("Créer un nouveau panier", "/icons/cart-new.png");
        JButton ajouterArticleButton = createMenuButton("Ajouter un article au panier", "/icons/cart-add.png");
        JButton factureButton = createMenuButton("Consulter et payer une facture", "/icons/invoice.png");
        JButton caButton = createMenuButton("Consulter le chiffre d'affaires", "/icons/chart.png");
        JButton adminButton = createMenuButton("Gérer le stock (Administration)", "/icons/admin.png");
        JButton quitterButton = createMenuButton("Quitter", "/icons/exit.png");

        // Personnaliser le bouton Quitter
        quitterButton.setBackground(ERROR_COLOR);
        quitterButton.setForeground(Color.WHITE);

        // Actions des boutons
        stockButton.addActionListener(e -> cardLayout.show(mainPanel, "stock"));
        rechercheButton.addActionListener(e -> cardLayout.show(mainPanel, "recherche"));
        nouveauPanierButton.addActionListener(e -> creerNouveauPanier());
        ajouterArticleButton.addActionListener(e -> {
            if (currentFactureId == 0) {
                showCustomDialog("Veuillez d'abord créer un nouveau panier.", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cardLayout.show(mainPanel, "panier");
        });
        factureButton.addActionListener(e -> {
            // Réinitialiser l'état de la facture à chaque fois qu'on accède à l'écran
            facturePayee = false;
            factureIdField.setText("");
            factureInfoArea.setText("");
            updatePaiementPanelVisibility();
            cardLayout.show(mainPanel, "facture");
        });
        caButton.addActionListener(e -> cardLayout.show(mainPanel, "ca"));
        adminButton.addActionListener(e -> cardLayout.show(mainPanel, "admin"));
        quitterButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(this,
                    "Êtes-vous sûr de vouloir quitter l'application ?",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        // Ajout des boutons au panel
        buttonsPanel.add(stockButton);
        buttonsPanel.add(rechercheButton);
        buttonsPanel.add(nouveauPanierButton);
        buttonsPanel.add(ajouterArticleButton);
        buttonsPanel.add(factureButton);
        buttonsPanel.add(caButton);
        buttonsPanel.add(adminButton);
        buttonsPanel.add(quitterButton);

        menuPanel.add(buttonsPanel, BorderLayout.CENTER);
    }

    private JButton createMenuButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(SECONDARY_COLOR);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(SECONDARY_COLOR.darker(), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        // Ajouter l'icône si disponible
        try {
            URL iconURL = getClass().getResource(iconPath);
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image img = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(img));
                button.setHorizontalAlignment(SwingConstants.LEFT);
                button.setIconTextGap(10);
            }
        } catch (Exception e) {
            // Ignorer si l'icône n'est pas trouvée
        }

        // Ajouter des effets de survol
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SECONDARY_COLOR);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    private void createStockPanel() {
        stockPanel = new JPanel(new BorderLayout());
        stockPanel.setBackground(BACKGROUND_COLOR);

        // Titre
        JPanel headerPanel = createHeaderPanel("Consultation du Stock");
        stockPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel pour la saisie
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setBackground(BACKGROUND_COLOR);

        JLabel refLabel = new JLabel("Référence de l'article:");
        refLabel.setFont(new Font("Arial", Font.BOLD, 14));

        refStockField = new JTextField(15);
        refStockField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton consulterButton = createActionButton("Consulter", "/icons/search.png");

        searchPanel.add(refLabel);
        searchPanel.add(refStockField);
        searchPanel.add(consulterButton);

        inputPanel.add(searchPanel);

        // Zone d'affichage des informations
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(BACKGROUND_COLOR);
        infoPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        stockInfoArea = new JTextArea(12, 40);
        stockInfoArea.setEditable(false);
        stockInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        stockInfoArea.setBackground(Color.WHITE);
        stockInfoArea.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(stockInfoArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        infoPanel.add(scrollPane, BorderLayout.CENTER);

        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(infoPanel);

        // Bouton retour
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        JButton retourButton = createNavigationButton("Retour au menu principal", "/icons/home.png");
        retourButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        buttonPanel.add(retourButton);

        inputPanel.add(buttonPanel);

        stockPanel.add(inputPanel, BorderLayout.CENTER);

        // Action du bouton consulter
        consulterButton.addActionListener(e -> {
            try {
                if (refStockField.getText().isEmpty()) {
                    showCustomDialog("Veuillez entrer une référence d'article.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                long reference = Long.parseLong(refStockField.getText());
                consulterStock(reference);
            } catch (NumberFormatException ex) {
                showCustomDialog("Veuillez entrer une référence valide (nombre entier).", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                showCustomDialog("Erreur lors de la consultation: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void consulterStock(long reference) throws RemoteException {
        Article article = service.consulterStock(reference);

        if (article != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("╔══════════════════════════════════════════╗\n");
            sb.append("║           DÉTAILS DE L'ARTICLE           ║\n");
            sb.append("╠══════════════════════════════════════════╣\n");
            sb.append(String.format("║ Référence: %-30s ║\n", article.getReference()));
            sb.append(String.format("║ Nom: %-36s ║\n", article.getNom()));
            sb.append(String.format("║ Famille: %-32s ║\n", article.getFamille().getNom()));
            sb.append(String.format("║ Prix unitaire: %-26.2f € ║\n", article.getPrix()));
            sb.append(String.format("║ Quantité en stock: %-22d ║\n", article.getQuantite_stock()));
            sb.append("╚══════════════════════════════════════════╝\n");

            stockInfoArea.setText(sb.toString());
        } else {
            stockInfoArea.setText("╔══════════════════════════════════════════╗\n" +
                    "║              ARTICLE NON TROUVÉ          ║\n" +
                    "╚══════════════════════════════════════════╝\n");
        }
    }

    private void createRecherchePanel() {
        recherchePanel = new JPanel(new BorderLayout());
        recherchePanel.setBackground(BACKGROUND_COLOR);

        // Titre
        JPanel headerPanel = createHeaderPanel("Recherche d'Articles par Famille");
        recherchePanel.add(headerPanel, BorderLayout.NORTH);

        // Panel pour la saisie
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel familleLabel = new JLabel("Nom de la famille:");
        familleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        familleField = new JTextField(20);
        familleField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton rechercherButton = createActionButton("Rechercher", "/icons/search.png");

        inputPanel.add(familleLabel);
        inputPanel.add(familleField);
        inputPanel.add(rechercherButton);

        // Tableau pour les résultats
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);
        tablePanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        String[] columnNames = {"N°", "Référence", "Nom", "Prix (€)", "Stock"};
        articlesModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Rendre toutes les cellules non éditables
            }
        };

        articlesTable = new JTable(articlesModel);
        articlesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        articlesTable.setRowHeight(30);
        articlesTable.setIntercellSpacing(new Dimension(10, 5));
        articlesTable.setShowGrid(true);
        articlesTable.setGridColor(new Color(230, 230, 230));
        articlesTable.setSelectionBackground(SECONDARY_COLOR);
        articlesTable.setSelectionForeground(Color.WHITE);

        // Personnaliser l'en-tête du tableau
        JTableHeader header = articlesTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        // Centrer le contenu des cellules
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < articlesTable.getColumnCount(); i++) {
            articlesTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(articlesTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());

        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // Bouton retour
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        JButton retourButton = createNavigationButton("Retour au menu principal", "/icons/home.png");
        retourButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        buttonPanel.add(retourButton);

        // Panel central
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        recherchePanel.add(centerPanel, BorderLayout.CENTER);

        // Action du bouton rechercher
        rechercherButton.addActionListener(e -> {
            try {
                if (familleField.getText().isEmpty()) {
                    showCustomDialog("Veuillez entrer un nom de famille d'articles.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String nomFamille = familleField.getText();
                rechercherArticlesParFamille(nomFamille);
            } catch (RemoteException ex) {
                showCustomDialog("Erreur lors de la recherche: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void rechercherArticlesParFamille(String nomFamille) throws RemoteException {
        List<Article> articles = service.rechercherArticlesParFamille(nomFamille);

        // Vider le tableau
        articlesModel.setRowCount(0);

        if (articles.isEmpty()) {
            showCustomDialog("Aucun article trouvé dans cette famille ou la famille n'existe pas.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int c = 1;
        // Remplir le tableau avec les résultats
        for (Article article : articles) {
            Object[] row = {
                    c,
                    article.getReference(),
                    article.getNom(),
                    String.format("%.2f", article.getPrix()),
                    article.getQuantite_stock()
            };
            articlesModel.addRow(row);
            c++;
        }
    }

    private void createPanierPanel() {
        panierPanel = new JPanel(new BorderLayout());
        panierPanel.setBackground(BACKGROUND_COLOR);

        // Titre
        JPanel headerPanel = createHeaderPanel("Ajout d'un Article au Panier");
        panierPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel central
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel pour l'ID du panier
        JPanel panierIdPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panierIdPanel.setBackground(BACKGROUND_COLOR);

        panierIdLabel = new JLabel("Panier actuel: Aucun");
        panierIdLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panierIdLabel.setForeground(PRIMARY_COLOR);
        panierIdLabel.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY_COLOR, 1, true),
                new EmptyBorder(8, 15, 8, 15)
        ));

        panierIdPanel.add(panierIdLabel);

        centerPanel.add(panierIdPanel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Panel pour la saisie
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel refLabel = new JLabel("Référence de l'article:");
        refLabel.setFont(new Font("Arial", Font.BOLD, 14));

        refPanierField = new JTextField(10);
        refPanierField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel qteLabel = new JLabel("Quantité:");
        qteLabel.setFont(new Font("Arial", Font.BOLD, 14));

        qtePanierField = new JTextField(10);
        qtePanierField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton infoButton = createActionButton("Informations sur l'article", "/icons/info.png");
        JButton ajouterButton = createActionButton("Ajouter au panier", "/icons/cart-add.png");
        ajouterButton.setBackground(SUCCESS_COLOR);

        inputPanel.add(refLabel);
        inputPanel.add(refPanierField);
        inputPanel.add(qteLabel);
        inputPanel.add(qtePanierField);
        inputPanel.add(infoButton);
        inputPanel.add(ajouterButton);

        centerPanel.add(inputPanel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Zone d'affichage des informations
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(BACKGROUND_COLOR);

        panierInfoArea = new JTextArea(10, 40);
        panierInfoArea.setEditable(false);
        panierInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        panierInfoArea.setBackground(Color.WHITE);
        panierInfoArea.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(panierInfoArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        infoPanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(infoPanel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Boutons de navigation
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton retourButton = createNavigationButton("Retour au menu principal", "/icons/home.png");
        retourButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        JButton retourFactureButton = createNavigationButton("Retour à la facture", "/icons/invoice.png");
        retourFactureButton.addActionListener(e -> {
            // Rafraîchir la facture si on vient d'y ajouter un article
            if (currentFactureId > 0) {
                try {
                    factureIdField.setText(String.valueOf(currentFactureId));
                    consulterFacture(currentFactureId);
                } catch (RemoteException ex) {
                    showCustomDialog("Erreur lors de la consultation de la facture: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
            cardLayout.show(mainPanel, "facture");
        });

        buttonPanel.add(retourButton);
        buttonPanel.add(retourFactureButton);

        centerPanel.add(buttonPanel);

        panierPanel.add(centerPanel, BorderLayout.CENTER);

        // Action du bouton info
        infoButton.addActionListener(e -> {
            try {
                if (refPanierField.getText().isEmpty()) {
                    showCustomDialog("Veuillez entrer une référence d'article.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                long reference = Long.parseLong(refPanierField.getText());
                Article article = service.consulterStock(reference);

                if (article != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("╔══════════════════════════════════════════╗\n");
                    sb.append("║           DÉTAILS DE L'ARTICLE           ║\n");
                    sb.append("╠══════════════════════════════════════════╣\n");
                    sb.append(String.format("║ Référence: %-30s ║\n", article.getReference()));
                    sb.append(String.format("║ Nom: %-36s ║\n", article.getNom()));
                    sb.append(String.format("║ Famille: %-32s ║\n", article.getFamille().getNom()));
                    sb.append(String.format("║ Prix unitaire: %-26.2f € ║\n", article.getPrix()));
                    sb.append(String.format("║ Quantité en stock: %-22d ║\n", article.getQuantite_stock()));
                    sb.append("╚══════════════════════════════════════════╝\n");

                    panierInfoArea.setText(sb.toString());
                } else {
                    panierInfoArea.setText("╔══════════════════════════════════════════╗\n" +
                            "║              ARTICLE NON TROUVÉ          ║\n" +
                            "╚══════════════════════════════════════════╝\n");
                }
            } catch (NumberFormatException ex) {
                showCustomDialog("Veuillez entrer une référence valide (nombre entier).", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                showCustomDialog("Erreur lors de la consultation: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action du bouton ajouter
        ajouterButton.addActionListener(e -> {
            try {
                if (refPanierField.getText().isEmpty() || qtePanierField.getText().isEmpty()) {
                    showCustomDialog("Veuillez remplir tous les champs.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                long reference = Long.parseLong(refPanierField.getText());
                int quantite = Integer.parseInt(qtePanierField.getText());

                if (quantite <= 0) {
                    showCustomDialog("La quantité doit être supérieure à 0.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Vérifier si l'article existe
                Article article = service.consulterStock(reference);
                if (article == null) {
                    showCustomDialog("Article non trouvé.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (quantite > article.getQuantite_stock()) {
                    showCustomDialog("Stock insuffisant. Il reste seulement " + article.getQuantite_stock() + " exemplaire(s).", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                boolean success = service.acheterArticle(reference, quantite, currentFactureId);

                if (success) {
                    showCustomDialog("Article ajouté au panier avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                    refPanierField.setText("");
                    qtePanierField.setText("");

                    // Afficher les détails de l'article ajouté
                    StringBuilder sb = new StringBuilder();
                    sb.append("╔══════════════════════════════════════════╗\n");
                    sb.append("║           ARTICLE AJOUTÉ AU PANIER       ║\n");
                    sb.append("╠══════════════════════════════════════════╣\n");
                    sb.append(String.format("║ Référence: %-30s ║\n", article.getReference()));
                    sb.append(String.format("║ Nom: %-36s ║\n", article.getNom()));
                    sb.append(String.format("║ Prix unitaire: %-26.2f € ║\n", article.getPrix()));
                    sb.append(String.format("║ Quantité: %-31d ║\n", quantite));
                    sb.append(String.format("║ Total: %-33.2f € ║\n", article.getPrix() * quantite));
                    sb.append("╚══════════════════════════════════════════╝\n");

                    panierInfoArea.setText(sb.toString());
                } else {
                    showCustomDialog("Erreur lors de l'ajout de l'article au panier.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                showCustomDialog("Veuillez entrer des valeurs numériques valides.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                showCustomDialog("Erreur lors de l'ajout au panier: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void creerNouveauPanier() {
        try {
            // Appel de la méthode distante pour créer une nouvelle facture
            this.currentFactureId = service.creerNouvelleFacture();

            panierIdLabel.setText("Panier actuel: " + currentFactureId);

            // Afficher un message de confirmation avec animation
            JDialog dialog = new JDialog(this, "Nouveau Panier", true);
            dialog.setSize(400, 200);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel iconLabel = new JLabel();
            try {
                URL iconURL = getClass().getResource("/icons/cart-success.png");
                if (iconURL != null) {
                    ImageIcon icon = new ImageIcon(iconURL);
                    Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                    iconLabel.setIcon(new ImageIcon(img));
                }
            } catch (Exception ex) {
                // Ignorer si l'icône n'est pas trouvée
            }
            iconLabel.setHorizontalAlignment(JLabel.CENTER);

            JLabel messageLabel = new JLabel("Nouveau panier créé avec succès!");
            messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
            messageLabel.setHorizontalAlignment(JLabel.CENTER);

            JLabel idLabel = new JLabel("ID de facture: " + currentFactureId);
            idLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            idLabel.setHorizontalAlignment(JLabel.CENTER);

            JPanel labelPanel = new JPanel(new GridLayout(2, 1));
            labelPanel.setBackground(Color.WHITE);
            labelPanel.add(messageLabel);
            labelPanel.add(idLabel);

            JButton okButton = new JButton("OK");
            okButton.setFont(new Font("Arial", Font.BOLD, 14));
            okButton.setBackground(SUCCESS_COLOR);
            okButton.setForeground(Color.WHITE);
            okButton.addActionListener(e -> dialog.dispose());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(okButton);

            contentPanel.add(iconLabel, BorderLayout.NORTH);
            contentPanel.add(labelPanel, BorderLayout.CENTER);
            contentPanel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.add(contentPanel);
            dialog.setVisible(true);

            // Rediriger vers l'écran d'ajout d'articles
            cardLayout.show(mainPanel, "panier");
        } catch (RemoteException e) {
            showCustomDialog("Erreur lors de la création du panier: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createFacturePanel() {
        facturePanel = new JPanel(new BorderLayout());
        facturePanel.setBackground(BACKGROUND_COLOR);

        // Titre
        JPanel headerPanel = createHeaderPanel("Consulter et Payer une Facture");
        facturePanel.add(headerPanel, BorderLayout.NORTH);

        // Panel central
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel pour la saisie
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);

        JLabel idLabel = new JLabel("ID de la facture:");
        idLabel.setFont(new Font("Arial", Font.BOLD, 14));

        factureIdField = new JTextField(10);
        factureIdField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton consulterButton = createActionButton("Consulter", "/icons/search.png");

        // Nouveau bouton pour ajouter un article à la facture
        ajouterArticleFactureButton = createActionButton("Ajouter article", "/icons/cart-add.png");
        ajouterArticleFactureButton.setBackground(ACCENT_COLOR);
        ajouterArticleFactureButton.setVisible(false); // Caché par défaut

        inputPanel.add(idLabel);
        inputPanel.add(factureIdField);
        inputPanel.add(consulterButton);
        inputPanel.add(ajouterArticleFactureButton);

        centerPanel.add(inputPanel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Zone d'affichage des informations
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(BACKGROUND_COLOR);

        factureInfoArea = new JTextArea(15, 50);
        factureInfoArea.setEditable(false);
        factureInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        factureInfoArea.setBackground(Color.WHITE);
        factureInfoArea.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(factureInfoArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        infoPanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(infoPanel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Panel pour le paiement
        paiementPanel = new JPanel(new BorderLayout());
        paiementPanel.setBackground(BACKGROUND_COLOR);

        // Panel pour le choix du mode de paiement
        JPanel choixPaiementPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        choixPaiementPanel.setBackground(Color.WHITE);
        choixPaiementPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel modeLabel = new JLabel("Mode de paiement:");
        modeLabel.setFont(new Font("Arial", Font.BOLD, 14));

        String[] modes = {"Carte Bancaire", "Espèces", "Chèque"};
        modePaiementCombo = new JComboBox<>(modes);
        modePaiementCombo.setFont(new Font("Arial", Font.PLAIN, 14));

        payerButton = createActionButton("Payer la facture", "/icons/payment.png");
        payerButton.setBackground(SUCCESS_COLOR);

        choixPaiementPanel.add(modeLabel);
        choixPaiementPanel.add(modePaiementCombo);
        choixPaiementPanel.add(payerButton);

        // Panel pour afficher le statut de paiement
        JPanel statutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statutPanel.setBackground(BACKGROUND_COLOR);
        statutPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        statutPaiementLabel = new JLabel("");
        statutPaiementLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statutPaiementLabel.setForeground(SUCCESS_COLOR);

        statutPanel.add(statutPaiementLabel);

        paiementPanel.add(choixPaiementPanel, BorderLayout.NORTH);
        paiementPanel.add(statutPanel, BorderLayout.CENTER);

        centerPanel.add(paiementPanel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Bouton retour
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton retourButton = createNavigationButton("Retour au menu principal", "/icons/home.png");
        retourButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        buttonPanel.add(retourButton);

        centerPanel.add(buttonPanel);

        facturePanel.add(centerPanel, BorderLayout.CENTER);

        // Action du bouton consulter
        consulterButton.addActionListener(e -> {
            try {
                if (factureIdField.getText().isEmpty()) {
                    showCustomDialog("Veuillez entrer un ID de facture.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int idFacture = Integer.parseInt(factureIdField.getText());
                consulterFacture(idFacture);
            } catch (NumberFormatException ex) {
                showCustomDialog("Veuillez entrer un ID de facture valide (nombre entier).", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                showCustomDialog("Erreur lors de la consultation: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action du bouton ajouter article
        ajouterArticleFactureButton.addActionListener(e -> {
            try {
                int idFacture = Integer.parseInt(factureIdField.getText());

                // Vérifier si la facture existe
                Facture facture = service.consulterFacture(idFacture);
                if (facture == null) {
                    showCustomDialog("Facture non trouvée.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Vérifier si la facture n'est pas déjà payée
                if ("payee".equals(facture.getStatus())) {
                    showCustomDialog("Cette facture est déjà payée. Impossible d'ajouter des articles.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Confirmation avant de rediriger
                int confirmation = JOptionPane.showConfirmDialog(this,
                        "Voulez-vous ajouter un article à la facture n° " + idFacture + " ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (confirmation == JOptionPane.YES_OPTION) {
                    // Définir la facture courante pour l'ajout d'articles
                    currentFactureId = idFacture;
                    // Mettre à jour le label du panier
                    panierIdLabel.setText("Panier actuel: " + currentFactureId);
                    // Aller à l'écran d'ajout d'article
                    cardLayout.show(mainPanel, "panier");
                }
            } catch (NumberFormatException ex) {
                showCustomDialog("ID de facture invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                showCustomDialog("Erreur lors de la consultation de la facture: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action du bouton payer
        payerButton.addActionListener(e -> {
            try {
                if (factureIdField.getText().isEmpty()) {
                    showCustomDialog("Veuillez d'abord consulter une facture.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int idFacture = Integer.parseInt(factureIdField.getText());
                String modePaiement = (String) modePaiementCombo.getSelectedItem();

                // Créer un panneau personnalisé pour la confirmation
                JPanel confirmPanel = new JPanel(new BorderLayout());
                confirmPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

                JLabel confirmLabel = new JLabel("Voulez-vous payer cette facture avec " + modePaiement + " ?");
                confirmLabel.setFont(new Font("Arial", Font.BOLD, 14));

                JLabel totalLabel = new JLabel();
                try {
                    Facture facture = service.consulterFacture(idFacture);
                    if (facture != null) {
                        totalLabel.setText("Montant total: " + String.format("%.2f €", facture.getMontant()));
                        totalLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                    }
                } catch (RemoteException ex) {
                    // Ignorer l'erreur ici
                }

                confirmPanel.add(confirmLabel, BorderLayout.NORTH);
                confirmPanel.add(totalLabel, BorderLayout.CENTER);

                int confirmation = JOptionPane.showConfirmDialog(this,
                        confirmPanel,
                        "Confirmation de paiement",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (confirmation == JOptionPane.YES_OPTION) {
                    boolean success = service.payerFacture(idFacture, modePaiement);

                    if (success) {
                        // Afficher un message de confirmation avec animation
                        JDialog dialog = new JDialog(this, "Paiement Effectué", true);
                        dialog.setSize(400, 250);
                        dialog.setLocationRelativeTo(this);
                        dialog.setLayout(new BorderLayout());

                        JPanel contentPanel = new JPanel(new BorderLayout());
                        contentPanel.setBackground(Color.WHITE);
                        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

                        JLabel iconLabel = new JLabel();
                        try {
                            URL iconURL = getClass().getResource("/icons/payment-success.png");
                            if (iconURL != null) {
                                ImageIcon icon = new ImageIcon(iconURL);
                                Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                                iconLabel.setIcon(new ImageIcon(img));
                            }
                        } catch (Exception ex) {
                            // Ignorer si l'icône n'est pas trouvée
                        }
                        iconLabel.setHorizontalAlignment(JLabel.CENTER);

                        JLabel messageLabel = new JLabel("Paiement effectué avec succès!");
                        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
                        messageLabel.setHorizontalAlignment(JLabel.CENTER);

                        JLabel thanksLabel = new JLabel("Merci de votre achat chez Brico-Merlin!");
                        thanksLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                        thanksLabel.setHorizontalAlignment(JLabel.CENTER);

                        JPanel labelPanel = new JPanel(new GridLayout(2, 1));
                        labelPanel.setBackground(Color.WHITE);
                        labelPanel.add(messageLabel);
                        labelPanel.add(thanksLabel);

                        JButton okButton = new JButton("OK");
                        okButton.setFont(new Font("Arial", Font.BOLD, 14));
                        okButton.setBackground(SUCCESS_COLOR);
                        okButton.setForeground(Color.WHITE);
                        okButton.addActionListener(event -> dialog.dispose());

                        JPanel buttonPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
                        buttonPanel2.setBackground(Color.WHITE);
                        buttonPanel2.add(okButton);

                        contentPanel.add(iconLabel, BorderLayout.NORTH);
                        contentPanel.add(labelPanel, BorderLayout.CENTER);
                        contentPanel.add(buttonPanel2, BorderLayout.SOUTH);

                        dialog.add(contentPanel);
                        dialog.setVisible(true);

                        consulterFacture(idFacture); // Rafraîchir l'affichage
                    } else {
                        showCustomDialog("Erreur lors du paiement de la facture.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException ex) {
                showCustomDialog("Veuillez entrer un ID de facture valide (nombre entier).", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                showCustomDialog("Erreur lors du paiement: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void consulterFacture(int idFacture) throws RemoteException {
        Facture facture = service.consulterFacture(idFacture);

        if (facture == null) {
            factureInfoArea.setText("╔══════════════════════════════════════════════════════════════╗\n" +
                    "║                     FACTURE NON TROUVÉE                      ║\n" +
                    "╚══════════════════════════════════════════════════════════════╝\n");
            facturePayee = false;
            updatePaiementPanelVisibility();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║                         BRICO-MERLIN                         ║\n");
        sb.append("║                      TICKET DE CAISSE                        ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Facture N°: %-47d ║\n", facture.getId_facture()));
        sb.append(String.format("║ Date: %-52s ║\n", facture.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
        sb.append(String.format("║ Statut: %-50s ║\n", facture.getStatus()));
        if (facture.getMode_paiement() != null) {
            sb.append(String.format("║ Mode de paiement: %-41s ║\n", facture.getMode_paiement()));
        }

        sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ %-10s %-25s %-8s %-6s %-10s ║\n", "Réf.", "Article", "Prix", "Qté", "Total"));
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");

        for (Ligne_Facture ligne : facture.getDetails()) {
            double sousTotal = ligne.getPrix() * ligne.getQuantite();
            String nomArticle = (ligne.getArticle() != null ? ligne.getArticle().getNom() : "Article inconnu");
            // Tronquer le nom si trop long
            if (nomArticle.length() > 23) {
                nomArticle = nomArticle.substring(0, 20) + "...";
            }
            sb.append(String.format("║ %-10d %-25s %-8.2f %-6d %-10.2f ║\n",
                    ligne.getReference(),
                    nomArticle,
                    ligne.getPrix(),
                    ligne.getQuantite(),
                    sousTotal));
        }

        sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ %-51s %-10.2f ║\n", "TOTAL A PAYER", facture.getMontant()));
        sb.append("╚══════════════════════════════════════════════════════════════╝\n");

        factureInfoArea.setText(sb.toString());

        // Vérifier si la facture est déjà payée
        facturePayee = "payee".equals(facture.getStatus());

        if (facturePayee && facture.getMode_paiement() != null) {
            // Afficher le message indiquant que la facture est déjà payée avec le mode de paiement
            statutPaiementLabel.setText("Cette facture est déjà payée en " + facture.getMode_paiement());
        } else {
            statutPaiementLabel.setText("");
        }

        // Mettre à jour la visibilité des composants de paiement et du bouton ajouter article
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

    private void createChiffreAffairesPanel() {
        chiffreAffairesPanel = new JPanel(new BorderLayout());
        chiffreAffairesPanel.setBackground(BACKGROUND_COLOR);

        // Titre
        JPanel headerPanel = createHeaderPanel("Consultation du Chiffre d'Affaires");
        chiffreAffairesPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel central
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(40, 20, 40, 20));

        // Panel pour la saisie
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.setBackground(BACKGROUND_COLOR);

        JLabel dateLabel = new JLabel("Date (JJ/MM/AAAA):");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));

        dateCAField = new JTextField(15);
        dateCAField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton consulterButton = createActionButton("Consulter", "/icons/search.png");

        inputPanel.add(dateLabel);
        inputPanel.add(dateCAField);
        inputPanel.add(consulterButton);

        centerPanel.add(inputPanel);
        centerPanel.add(Box.createVerticalStrut(40));

        // Panel pour l'affichage du résultat
        JPanel resultPanel = new JPanel();
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(30, 30, 30, 30)
        ));

        JLabel caLabelText = new JLabel("Chiffre d'affaires:");
        caLabelText.setFont(new Font("Arial", Font.BOLD, 16));

        chiffreAffairesLabel = new JLabel("0.00 €");
        chiffreAffairesLabel.setFont(new Font("Arial", Font.BOLD, 24));
        chiffreAffairesLabel.setForeground(PRIMARY_COLOR);

        resultPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        resultPanel.add(caLabelText);
        resultPanel.add(chiffreAffairesLabel);

        // Wrapper pour centrer le panel de résultat
        JPanel resultWrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        resultWrapperPanel.setBackground(BACKGROUND_COLOR);
        resultWrapperPanel.add(resultPanel);

        centerPanel.add(resultWrapperPanel);
        centerPanel.add(Box.createVerticalStrut(40));

        // Bouton retour
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton retourButton = createNavigationButton("Retour au menu principal", "/icons/home.png");
        retourButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        buttonPanel.add(retourButton);

        centerPanel.add(buttonPanel);

        chiffreAffairesPanel.add(centerPanel, BorderLayout.CENTER);

        // Action du bouton consulter
        consulterButton.addActionListener(e -> {
            try {
                if (dateCAField.getText().isEmpty()) {
                    showCustomDialog("Veuillez entrer une date.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String dateStr = dateCAField.getText();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDateTime date = LocalDateTime.parse(dateStr + " 00:00:00",
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

                double chiffreAffaire = service.calculerChiffreAffaire(date);
                chiffreAffairesLabel.setText(String.format("%.2f €", chiffreAffaire));

                // Animer le changement de valeur
                chiffreAffairesLabel.setForeground(ACCENT_COLOR);
                Timer timer = new Timer(1000, event -> {
                    chiffreAffairesLabel.setForeground(PRIMARY_COLOR);
                });
                timer.setRepeats(false);
                timer.start();
            } catch (Exception ex) {
                showCustomDialog("Format de date invalide ou erreur lors de la consultation.\n" +
                                "Veuillez entrer une date au format JJ/MM/AAAA (exemple: 12/05/2024).",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void createAdminPanel() {
        adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBackground(BACKGROUND_COLOR);

        // Titre
        JPanel headerPanel = createHeaderPanel("Administration");
        adminPanel.add(headerPanel, BorderLayout.NORTH);

        // Création des onglets
        adminTabbedPane = new JTabbedPane();
        adminTabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        adminTabbedPane.setBackground(BACKGROUND_COLOR);
        adminTabbedPane.setForeground(TEXT_COLOR);

        // Onglet gestion du stock
        createStockAdminPanel();

        // Onglet mise à jour des prix
        createPrixAdminPanel();

        // Ajout des onglets
        adminTabbedPane.addTab("Gestion du stock", stockAdminPanel);
        adminTabbedPane.addTab("Mise à jour des prix", prixAdminPanel);

        // Ajouter des icônes aux onglets
        try {
            URL stockIconURL = getClass().getResource("/icons/stock-admin.png");
            if (stockIconURL != null) {
                ImageIcon stockIcon = new ImageIcon(stockIconURL);
                Image stockImg = stockIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                adminTabbedPane.setIconAt(0, new ImageIcon(stockImg));
            }

            URL priceIconURL = getClass().getResource("/icons/price.png");
            if (priceIconURL != null) {
                ImageIcon priceIcon = new ImageIcon(priceIconURL);
                Image priceImg = priceIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                adminTabbedPane.setIconAt(1, new ImageIcon(priceImg));
            }
        } catch (Exception e) {
            // Ignorer si les icônes ne sont pas trouvées
        }

        // Bouton retour
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        JButton retourButton = createNavigationButton("Retour au menu principal", "/icons/home.png");
        retourButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        buttonPanel.add(retourButton);

        // Panel central
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        centerPanel.add(adminTabbedPane, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        adminPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private void createStockAdminPanel() {
        stockAdminPanel = new JPanel(new BorderLayout());
        stockAdminPanel.setBackground(BACKGROUND_COLOR);
        stockAdminPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel pour la saisie
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel refLabel = new JLabel("Référence de l'article:");
        refLabel.setFont(new Font("Arial", Font.BOLD, 14));

        refStockAdminField = new JTextField(10);
        refStockAdminField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel qteLabel = new JLabel("Quantité à ajouter:");
        qteLabel.setFont(new Font("Arial", Font.BOLD, 14));

        qteStockAdminField = new JTextField(10);
        qteStockAdminField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton infoButton = createActionButton("Informations sur l'article", "/icons/info.png");
        JButton ajouterButton = createActionButton("Ajouter au stock", "/icons/add.png");
        ajouterButton.setBackground(SUCCESS_COLOR);

        inputPanel.add(refLabel);
        inputPanel.add(refStockAdminField);
        inputPanel.add(qteLabel);
        inputPanel.add(qteStockAdminField);
        inputPanel.add(infoButton);
        inputPanel.add(ajouterButton);

        // Zone d'affichage des informations
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(BACKGROUND_COLOR);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        stockAdminInfoArea = new JTextArea(10, 40);
        stockAdminInfoArea.setEditable(false);
        stockAdminInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        stockAdminInfoArea.setBackground(Color.WHITE);
        stockAdminInfoArea.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(stockAdminInfoArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        infoPanel.add(scrollPane, BorderLayout.CENTER);

        stockAdminPanel.add(inputPanel, BorderLayout.NORTH);
        stockAdminPanel.add(infoPanel, BorderLayout.CENTER);

        // Action du bouton info
        infoButton.addActionListener(e -> {
            try {
                if (refStockAdminField.getText().isEmpty()) {
                    showCustomDialog("Veuillez entrer une référence d'article.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                long reference = Long.parseLong(refStockAdminField.getText());
                Article article = service.consulterStock(reference);

                if (article != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("╔══════════════════════════════════════════╗\n");
                    sb.append("║           DÉTAILS DE L'ARTICLE           ║\n");
                    sb.append("╠══════════════════════════════════════════╣\n");
                    sb.append(String.format("║ Référence: %-30s ║\n", article.getReference()));
                    sb.append(String.format("║ Nom: %-36s ║\n", article.getNom()));
                    sb.append(String.format("║ Famille: %-32s ║\n", article.getFamille().getNom()));
                    sb.append(String.format("║ Prix unitaire: %-26.2f € ║\n", article.getPrix()));
                    sb.append(String.format("║ Quantité en stock: %-22d ║\n", article.getQuantite_stock()));
                    sb.append("╚══════════════════════════════════════════╝\n");

                    stockAdminInfoArea.setText(sb.toString());
                } else {
                    stockAdminInfoArea.setText("╔══════════════════════════════════════════╗\n" +
                            "║              ARTICLE NON TROUVÉ          ║\n" +
                            "╚══════════════════════════════════════════╝\n");
                }
            } catch (NumberFormatException ex) {
                showCustomDialog("Veuillez entrer une référence valide (nombre entier).", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                showCustomDialog("Erreur lors de la consultation: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action du bouton ajouter
        ajouterButton.addActionListener(e -> {
            try {
                if (refStockAdminField.getText().isEmpty() || qteStockAdminField.getText().isEmpty()) {
                    showCustomDialog("Veuillez remplir tous les champs.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                long reference = Long.parseLong(refStockAdminField.getText());
                int quantite = Integer.parseInt(qteStockAdminField.getText());

                if (quantite <= 0) {
                    showCustomDialog("La quantité doit être supérieure à 0.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Vérifier si l'article existe
                Article article = service.consulterStock(reference);
                if (article == null) {
                    showCustomDialog("Article non trouvé.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                service.ajouterStock(reference, quantite);

                // Afficher un message de confirmation avec animation
                JDialog dialog = new JDialog(this, "Stock Mis à Jour", true);
                dialog.setSize(400, 200);
                dialog.setLocationRelativeTo(this);
                dialog.setLayout(new BorderLayout());

                JPanel contentPanel = new JPanel(new BorderLayout());
                contentPanel.setBackground(Color.WHITE);
                contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

                JLabel iconLabel = new JLabel();
                try {
                    URL iconURL = getClass().getResource("/icons/stock-success.png");
                    if (iconURL != null) {
                        ImageIcon icon = new ImageIcon(iconURL);
                        Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                        iconLabel.setIcon(new ImageIcon(img));
                    }
                } catch (Exception ex) {
                    // Ignorer si l'icône n'est pas trouvée
                }
                iconLabel.setHorizontalAlignment(JLabel.CENTER);

                JLabel messageLabel = new JLabel("Stock mis à jour avec succès!");
                messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
                messageLabel.setHorizontalAlignment(JLabel.CENTER);

                // Afficher le nouveau stock
                article = service.consulterStock(reference);
                JLabel stockLabel = new JLabel("Nouveau stock: " + article.getQuantite_stock());
                stockLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                stockLabel.setHorizontalAlignment(JLabel.CENTER);

                JPanel labelPanel = new JPanel(new GridLayout(2, 1));
                labelPanel.setBackground(Color.WHITE);
                labelPanel.add(messageLabel);
                labelPanel.add(stockLabel);

                JButton okButton = new JButton("OK");
                okButton.setFont(new Font("Arial", Font.BOLD, 14));
                okButton.setBackground(SUCCESS_COLOR);
                okButton.setForeground(Color.WHITE);
                okButton.addActionListener(event -> dialog.dispose());

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                buttonPanel.setBackground(Color.WHITE);
                buttonPanel.add(okButton);

                contentPanel.add(iconLabel, BorderLayout.NORTH);
                contentPanel.add(labelPanel, BorderLayout.CENTER);
                contentPanel.add(buttonPanel, BorderLayout.SOUTH);

                dialog.add(contentPanel);
                dialog.setVisible(true);

                // Afficher le nouveau stock dans la zone d'information
                StringBuilder sb = new StringBuilder();
                sb.append("╔══════════════════════════════════════════╗\n");
                sb.append("║           STOCK MIS À JOUR               ║\n");
                sb.append("╠══════════════════════════════════════════╣\n");
                sb.append(String.format("║ Référence: %-30s ║\n", article.getReference()));
                sb.append(String.format("║ Nom: %-36s ║\n", article.getNom()));
                sb.append(String.format("║ Ancien stock: %-27d ║\n", article.getQuantite_stock() - quantite));
                sb.append(String.format("║ Quantité ajoutée: %-23d ║\n", quantite));
                sb.append(String.format("║ Nouveau stock: %-27d ║\n", article.getQuantite_stock()));
                sb.append("╚══════════════════════════════════════════╝\n");

                stockAdminInfoArea.setText(sb.toString());

                refStockAdminField.setText("");
                qteStockAdminField.setText("");
            } catch (NumberFormatException ex) {
                showCustomDialog("Veuillez entrer des valeurs numériques valides.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                showCustomDialog("Erreur lors de la mise à jour du stock: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void createPrixAdminPanel() {
        prixAdminPanel = new JPanel(new BorderLayout());
        prixAdminPanel.setBackground(BACKGROUND_COLOR);
        prixAdminPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Panel pour la saisie du nombre d'articles
        JPanel nbArticlesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        nbArticlesPanel.setBackground(Color.WHITE);
        nbArticlesPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel nbLabel = new JLabel("Nombre d'articles à mettre à jour:");
        nbLabel.setFont(new Font("Arial", Font.BOLD, 14));

        nbArticlesField = new JTextField(5);
        nbArticlesField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton validerNbButton = createActionButton("Valider", "/icons/check.png");

        nbArticlesPanel.add(nbLabel);
        nbArticlesPanel.add(nbArticlesField);
        nbArticlesPanel.add(validerNbButton);

        // Panel pour la mise à jour des prix
        JPanel scrollContent = new JPanel();
        scrollContent.setBackground(Color.WHITE);

        prixUpdatePanel = new JPanel();
        prixUpdatePanel.setLayout(new BoxLayout(prixUpdatePanel, BoxLayout.Y_AXIS));
        prixUpdatePanel.setBackground(Color.WHITE);
        prixUpdatePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollContent.add(prixUpdatePanel);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Bouton pour valider les prix
        JPanel buttonWrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapperPanel.setBackground(BACKGROUND_COLOR);
        buttonWrapperPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        validerPrixButton = createActionButton("Mettre à jour les prix", "/icons/update.png");
        validerPrixButton.setBackground(SUCCESS_COLOR);
        validerPrixButton.setEnabled(false);

        buttonWrapperPanel.add(validerPrixButton);

        prixAdminPanel.add(nbArticlesPanel, BorderLayout.NORTH);
        prixAdminPanel.add(scrollPane, BorderLayout.CENTER);
        prixAdminPanel.add(buttonWrapperPanel, BorderLayout.SOUTH);

        // Action du bouton valider nombre
        validerNbButton.addActionListener(e -> {
            try {
                if (nbArticlesField.getText().isEmpty()) {
                    showCustomDialog("Veuillez entrer un nombre d'articles.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int nbArticles = Integer.parseInt(nbArticlesField.getText());

                if (nbArticles <= 0) {
                    showCustomDialog("Le nombre d'articles doit être supérieur à 0.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Vider le panel
                prixUpdatePanel.removeAll();

                // Créer les champs pour chaque article
                for (int i = 0; i < nbArticles; i++) {
                    JPanel articlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                    articlePanel.setBackground(Color.WHITE);
                    articlePanel.setBorder(new CompoundBorder(
                            new LineBorder(new Color(230, 230, 230), 1, true),
                            new EmptyBorder(10, 10, 10, 10)
                    ));

                    JLabel numLabel = new JLabel("Article " + (i + 1) + ":");
                    numLabel.setFont(new Font("Arial", Font.BOLD, 14));

                    JLabel refLabel = new JLabel("Référence:");
                    refLabel.setFont(new Font("Arial", Font.PLAIN, 14));

                    JTextField refField = new JTextField(10);
                    refField.setFont(new Font("Arial", Font.PLAIN, 14));

                    JLabel prixLabel = new JLabel("Nouveau prix:");
                    prixLabel.setFont(new Font("Arial", Font.PLAIN, 14));

                    JTextField prixField = new JTextField(10);
                    prixField.setFont(new Font("Arial", Font.PLAIN, 14));

                    // Ajouter un bouton pour vérifier l'article
                    JButton checkButton = new JButton("Vérifier");
                    checkButton.setFont(new Font("Arial", Font.PLAIN, 14));
                    checkButton.setBackground(SECONDARY_COLOR);
                    checkButton.setForeground(Color.WHITE);
                    checkButton.setFocusPainted(false);

                    final int index = i;
                    checkButton.addActionListener(event -> {
                        try {
                            if (refField.getText().isEmpty()) {
                                showCustomDialog("Veuillez entrer une référence d'article.", "Erreur", JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                            long reference = Long.parseLong(refField.getText());
                            Article article = service.consulterStock(reference);

                            if (article != null) {
                                // Créer un panneau personnalisé pour l'information
                                JPanel infoPanel = new JPanel(new BorderLayout());
                                infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

                                JLabel articleLabel = new JLabel("Article trouvé: " + article.getNom());
                                articleLabel.setFont(new Font("Arial", Font.BOLD, 14));

                                JLabel prixActuelLabel = new JLabel("Prix actuel: " + article.getPrix() + " €");
                                prixActuelLabel.setFont(new Font("Arial", Font.PLAIN, 14));

                                infoPanel.add(articleLabel, BorderLayout.NORTH);
                                infoPanel.add(prixActuelLabel, BorderLayout.CENTER);

                                JOptionPane.showMessageDialog(this,
                                        infoPanel,
                                        "Information",
                                        JOptionPane.INFORMATION_MESSAGE);

                                // Pré-remplir le champ de prix avec le prix actuel
                                prixField.setText(String.format("%.2f", article.getPrix()));
                            } else {
                                showCustomDialog("Article non trouvé.", "Erreur", JOptionPane.WARNING_MESSAGE);
                            }
                        } catch (NumberFormatException ex) {
                            showCustomDialog("Veuillez entrer une référence valide (nombre entier).", "Erreur", JOptionPane.ERROR_MESSAGE);
                        } catch (RemoteException ex) {
                            showCustomDialog("Erreur lors de la consultation: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    });

                    articlePanel.add(numLabel);
                    articlePanel.add(refLabel);
                    articlePanel.add(refField);
                    articlePanel.add(prixLabel);
                    articlePanel.add(prixField);
                    articlePanel.add(checkButton);

                    // Ajouter un tag pour identifier les champs
                    refField.setName("ref_" + i);
                    prixField.setName("prix_" + i);

                    prixUpdatePanel.add(articlePanel);
                    prixUpdatePanel.add(Box.createVerticalStrut(10));
                }

                // Activer le bouton de validation
                validerPrixButton.setEnabled(true);

                // Rafraîchir l'affichage
                prixUpdatePanel.revalidate();
                prixUpdatePanel.repaint();
            } catch (NumberFormatException ex) {
                showCustomDialog("Veuillez entrer un nombre valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action du bouton valider prix
        validerPrixButton.addActionListener(e -> {
            try {
                if (nbArticlesField.getText().isEmpty()) {
                    showCustomDialog("Veuillez d'abord spécifier le nombre d'articles.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int nbArticles = Integer.parseInt(nbArticlesField.getText());
                Map<Long, Double> nouveauxPrix = new HashMap<>();

                // Récupérer les valeurs des champs
                for (int i = 0; i < nbArticles; i++) {
                    JTextField refField = (JTextField) findComponentByName(prixUpdatePanel, "ref_" + i);
                    JTextField prixField = (JTextField) findComponentByName(prixUpdatePanel, "prix_" + i);

                    if (refField != null && prixField != null &&
                            !refField.getText().isEmpty() && !prixField.getText().isEmpty()) {

                        long reference = Long.parseLong(refField.getText());
                        double nouveauPrix = Double.parseDouble(prixField.getText());

                        if (nouveauPrix <= 0) {
                            showCustomDialog("Le prix doit être supérieur à 0 pour l'article " + (i + 1) + ".", "Erreur", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        nouveauxPrix.put(reference, nouveauPrix);
                    }
                }

                if (nouveauxPrix.isEmpty()) {
                    showCustomDialog("Aucun prix n'a été spécifié.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                //service.mettreAJourPrix(nouveauxPrix);

                // Afficher un message de confirmation avec animation
                JDialog dialog = new JDialog(this, "Prix Mis à Jour", true);
                dialog.setSize(400, 200);
                dialog.setLocationRelativeTo(this);
                dialog.setLayout(new BorderLayout());

                JPanel contentPanel = new JPanel(new BorderLayout());
                contentPanel.setBackground(Color.WHITE);
                contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

                JLabel iconLabel = new JLabel();
                try {
                    URL iconURL = getClass().getResource("/icons/price-success.png");
                    if (iconURL != null) {
                        ImageIcon icon = new ImageIcon(iconURL);
                        Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                        iconLabel.setIcon(new ImageIcon(img));
                    }
                } catch (Exception ex) {
                    // Ignorer si l'icône n'est pas trouvée
                }
                iconLabel.setHorizontalAlignment(JLabel.CENTER);

                JLabel messageLabel = new JLabel("Prix mis à jour avec succès!");
                messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
                messageLabel.setHorizontalAlignment(JLabel.CENTER);

                JLabel countLabel = new JLabel(nouveauxPrix.size() + " article(s) mis à jour");
                countLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                countLabel.setHorizontalAlignment(JLabel.CENTER);

                JPanel labelPanel = new JPanel(new GridLayout(2, 1));
                labelPanel.setBackground(Color.WHITE);
                labelPanel.add(messageLabel);
                labelPanel.add(countLabel);

                JButton okButton = new JButton("OK");
                okButton.setFont(new Font("Arial", Font.BOLD, 14));
                okButton.setBackground(SUCCESS_COLOR);
                okButton.setForeground(Color.WHITE);
                okButton.addActionListener(event -> dialog.dispose());

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                buttonPanel.setBackground(Color.WHITE);
                buttonPanel.add(okButton);

                contentPanel.add(iconLabel, BorderLayout.NORTH);
                contentPanel.add(labelPanel, BorderLayout.CENTER);
                contentPanel.add(buttonPanel, BorderLayout.SOUTH);

                dialog.add(contentPanel);
                dialog.setVisible(true);

                // Réinitialiser les champs
                prixUpdatePanel.removeAll();
                validerPrixButton.setEnabled(false);
                nbArticlesField.setText("");

                // Rafraîchir l'affichage
                prixUpdatePanel.revalidate();
                prixUpdatePanel.repaint();
            } catch (NumberFormatException ex) {
                showCustomDialog("Veuillez entrer des valeurs numériques valides.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } /*catch (RemoteException ex) {
                showCustomDialog("Erreur lors de la mise à jour des prix: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }*/
        });
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

    // Méthode pour créer un panneau d'en-tête
    private JPanel createHeaderPanel(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    // Méthode pour créer un bouton d'action
    private JButton createActionButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(SECONDARY_COLOR);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(SECONDARY_COLOR.darker(), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        // Ajouter l'icône si disponible
        try {
            URL iconURL = getClass().getResource(iconPath);
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(img));
                button.setIconTextGap(8);
            }
        } catch (Exception e) {
            // Ignorer si l'icône n'est pas trouvée
        }

        // Ajouter des effets de survol
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(button.getBackground().darker());
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(button.getBackground().brighter());
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    // Méthode pour créer un bouton de navigation
    private JButton createNavigationButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(TEXT_COLOR);
        button.setBackground(BACKGROUND_COLOR);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        // Ajouter l'icône si disponible
        try {
            URL iconURL = getClass().getResource(iconPath);
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(img));
                button.setIconTextGap(8);
            }
        } catch (Exception e) {
            // Ignorer si l'icône n'est pas trouvée
        }

        // Ajouter des effets de survol
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(230, 230, 230));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BACKGROUND_COLOR);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String host = "localhost";

            if (args.length > 0) {
                host = args[0];
            }

            MainApplication2 client = new MainApplication2(host);
            client.setVisible(true);
        });
    }
}