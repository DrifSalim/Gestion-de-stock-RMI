package Client;

import Model.Article;
import Model.Facture;
import Model.Ligne_Facture;
import Serveur.IBricoMerlinService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.geom.AffineTransform;

public class MainApplication extends JFrame {
    private IBricoMerlinService service;
    private int currentFactureId = 0;
    private boolean facturePayee = false;

    // Color scheme
    private final Color PRIMARY_COLOR = new Color(0, 121, 107); // Teal
    private final Color SECONDARY_COLOR = new Color(38, 166, 154); // Lighter teal
    private final Color ACCENT_COLOR = new Color(255, 152, 0); // Orange
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Light gray
    private final Color TEXT_COLOR = new Color(33, 33, 33); // Dark gray
    private final Color SUCCESS_COLOR = new Color(76, 175, 80); // Green
    private final Color ERROR_COLOR = new Color(244, 67, 54); // Red

    // Font settings
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

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

    public MainApplication(String host) {
        super("Brico-Merlin - Système de Gestion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Apply modern look and feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            // Customize UI colors
            UIManager.put("nimbusBase", PRIMARY_COLOR);
            UIManager.put("nimbusBlueGrey", SECONDARY_COLOR);
            UIManager.put("control", BACKGROUND_COLOR);
            UIManager.put("text", TEXT_COLOR);
            UIManager.put("nimbusLightBackground", Color.WHITE);
            UIManager.put("nimbusFocus", ACCENT_COLOR);
            UIManager.put("nimbusSelectionBackground", ACCENT_COLOR);

            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            // Fallback to system look and feel if Nimbus is not available
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Ignore if system look and feel is not available
            }
        }

        try {
            // Connexion au serveur RMI
            Registry registry = LocateRegistry.getRegistry(host, 1099);
            service = (IBricoMerlinService) registry.lookup("BricoMerlinService");



            initUI();
        } catch (RemoteException | NotBoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de connexion au serveur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }



    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this,
                message,
                title, JOptionPane.ERROR_MESSAGE);
    }

    private void showWarningDialog(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Attention", JOptionPane.WARNING_MESSAGE);
    }

    private void initUI() {
        // Set background color
        getContentPane().setBackground(BACKGROUND_COLOR);

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
    }

    private void createMenuPanel() {
        menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBackground(BACKGROUND_COLOR);

        // Header panel with logo and title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Logo (text-based instead of image)
        JLabel logoLabel = new JLabel("🛠️");
        logoLabel.setFont(new Font("Dialog", Font.BOLD, 48));
        logoLabel.setForeground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("BRICO-MERLIN", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Système de Gestion", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        subtitleLabel.setForeground(Color.WHITE);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        headerPanel.add(logoLabel, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);

        menuPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel for buttons
        JPanel buttonsPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        buttonsPanel.setBackground(BACKGROUND_COLOR);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Menu buttons with text-based icons
        JButton stockButton = createMenuButton("Consulter le stock");
        JButton rechercheButton = createMenuButton("Rechercher des articles");
        JButton nouveauPanierButton = createMenuButton("Créer un nouveau panier");
        JButton ajouterArticleButton = createMenuButton("Ajouter au panier");
        JButton factureButton = createMenuButton("Factures et paiements");
        JButton caButton = createMenuButton("Chiffre d'affaires");
        JButton adminButton = createMenuButton("Administration");
        JButton quitterButton = createMenuButton("Quitter");

        // Actions des boutons
        stockButton.addActionListener(e -> cardLayout.show(mainPanel, "stock"));
        rechercheButton.addActionListener(e -> cardLayout.show(mainPanel, "recherche"));
        nouveauPanierButton.addActionListener(e -> creerNouveauPanier());
        ajouterArticleButton.addActionListener(e -> {
            if (currentFactureId == 0) {
                showWarningDialog("Veuillez d'abord créer un nouveau panier.");
            } else {
                cardLayout.show(mainPanel, "panier");
            }
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
        quitterButton.addActionListener(e -> confirmExit());

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

        // Footer panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(new Color(220, 220, 220));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel versionLabel = new JLabel("Version 2.0 | © 2024 Brico-Merlin");
        versionLabel.setFont(SMALL_FONT);
        versionLabel.setForeground(new Color(100, 100, 100));

        footerPanel.add(versionLabel);
        menuPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    private void confirmExit() {
        int response = JOptionPane.showConfirmDialog(
                this,
                "Êtes-vous sûr de vouloir quitter l'application ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private JButton createMenuButton(String text ) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(new Dimension(200, 80));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(TEXT_COLOR);

        // Add emoji as text
            button.setText(text);
            button.setHorizontalAlignment(SwingConstants.CENTER);


        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
                button.setForeground(TEXT_COLOR);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        // Add rounded border
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));

        return button;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(REGULAR_FONT);
        button.setFocusPainted(false);

            button.setText( text);


        // Style the button
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY_COLOR.darker(), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR.darker());
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    private JButton createReturnButton() {
        JButton button = new JButton("Retour au menu principal");
        button.setFont(REGULAR_FONT);
        button.setFocusPainted(false);

        button.setBackground(new Color(224, 224, 224));
        button.setForeground(TEXT_COLOR);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(200, 200, 200));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(224, 224, 224));
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        button.addActionListener(e -> cardLayout.show(mainPanel, "menu"));

        return button;
    }

    private JPanel createHeaderPanel(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return contentPanel;
    }

    private void createStockPanel() {
        stockPanel = new JPanel(new BorderLayout());
        stockPanel.setBackground(BACKGROUND_COLOR);

        // Header
        stockPanel.add(createHeaderPanel("CONSULTATION DU STOCK"), BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = createContentPanel();

        // Panel pour la saisie
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel instructionLabel = new JLabel("Entrez la référence de l'article pour consulter son stock");
        instructionLabel.setFont(SUBTITLE_FONT);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fieldPanel.setOpaque(false);

        JLabel refLabel = new JLabel("Référence:");
        refLabel.setFont(REGULAR_FONT);
        refStockField = new JTextField(15);
        refStockField.setFont(REGULAR_FONT);

        JButton consulterButton = createActionButton("Consulter");

        fieldPanel.add(refLabel);
        fieldPanel.add(refStockField);
        fieldPanel.add(consulterButton);

        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(instructionLabel);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(fieldPanel);

        // Zone d'affichage des informations
        stockInfoArea = new JTextArea(12, 40);
        stockInfoArea.setEditable(false);
        stockInfoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        stockInfoArea.setBackground(Color.WHITE);
        stockInfoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(stockInfoArea);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));

        // Panel for result
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel resultTitle = new JLabel("Informations sur l'article");
        resultTitle.setFont(SUBTITLE_FONT);
        resultPanel.add(resultTitle, BorderLayout.NORTH);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        // Layout the panels
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(resultPanel, BorderLayout.CENTER);

        // Bouton retour
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(createReturnButton());

        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        stockPanel.add(contentPanel, BorderLayout.CENTER);

        // Action du bouton consulter
        consulterButton.addActionListener(e -> {
            try {
                if (refStockField.getText().isEmpty()) {
                    showWarningDialog("Veuillez entrer une référence d'article.");
                    return;
                }

                long reference = Long.parseLong(refStockField.getText());
                consulterStock(reference);
            } catch (NumberFormatException ex) {
                showErrorDialog("Erreur", "Veuillez entrer une référence valide (nombre entier).");
            } catch (RemoteException ex) {
                showErrorDialog("Erreur", "Erreur lors de la consultation: " + ex.getMessage());
            }
        });

        // Add enter key listener
        refStockField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    consulterButton.doClick();
                }
            }
        });
    }

    private void consulterStock(long reference) throws RemoteException {
        Article article = service.consulterStock(reference);

        if (article != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Référence: ").append(article.getReference()).append("\n\n");
            sb.append("Nom: ").append(article.getNom()).append("\n\n");
            sb.append("Famille: ").append(article.getFamille().getNom()).append("\n\n");
            sb.append("Prix unitaire: ").append(String.format("%.2f €", article.getPrix())).append("\n\n");

            // Add color coding for stock level
            int stock = article.getQuantite_stock();
            sb.append("Quantité en stock: ");

            stockInfoArea.setText(sb.toString());
            stockInfoArea.append(String.valueOf(stock));

            // Apply color based on stock level
            stockInfoArea.setForeground(TEXT_COLOR);

            // Highlight stock level with color
            if (stock <= 5) {
                // Low stock - red
                stockInfoArea.append(" (Stock faible)");
                stockInfoArea.setForeground(ERROR_COLOR);
            } else if (stock <= 20) {
                // Medium stock - orange
                stockInfoArea.append(" (Stock moyen)");
                stockInfoArea.setForeground(ACCENT_COLOR);
            } else {
                // Good stock - green
                stockInfoArea.append(" (Stock suffisant)");
                stockInfoArea.setForeground(SUCCESS_COLOR);
            }
        } else {
            stockInfoArea.setText("Article non trouvé.");
            stockInfoArea.setForeground(ERROR_COLOR);
        }
    }

    private void createRecherchePanel() {
        recherchePanel = new JPanel(new BorderLayout());
        recherchePanel.setBackground(BACKGROUND_COLOR);

        // Header
        recherchePanel.add(createHeaderPanel("RECHERCHE D'ARTICLES PAR FAMILLE"), BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = createContentPanel();

        // Panel pour la saisie
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel instructionLabel = new JLabel("Entrez le nom d'une famille d'articles pour afficher tous les articles correspondants");
        instructionLabel.setFont(SUBTITLE_FONT);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fieldPanel.setOpaque(false);

        JLabel familleLabel = new JLabel("Nom de la famille:");
        familleLabel.setFont(REGULAR_FONT);
        familleField = new JTextField(20);
        familleField.setFont(REGULAR_FONT);

        JButton rechercherButton = createActionButton("Rechercher");

        fieldPanel.add(familleLabel);
        fieldPanel.add(familleField);
        fieldPanel.add(rechercherButton);

        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(instructionLabel);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(fieldPanel);

        // Tableau pour les résultats
        String[] columnNames = {"N°", "Référence", "Nom", "Prix (€)", "Stock"};
        articlesModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        articlesTable = new JTable(articlesModel);
        articlesTable.setFont(REGULAR_FONT);
        articlesTable.setRowHeight(30);
        articlesTable.setShowGrid(true);
        articlesTable.setGridColor(new Color(230, 230, 230));
        articlesTable.setSelectionBackground(new Color(232, 245, 233));
        articlesTable.setSelectionForeground(TEXT_COLOR);
        articlesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        articlesTable.getTableHeader().setBackground(PRIMARY_COLOR);
        articlesTable.getTableHeader().setForeground(Color.WHITE);

        // Center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < articlesTable.getColumnCount(); i++) {
            articlesTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Set column widths
        articlesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        articlesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        articlesTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        articlesTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        articlesTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane tableScrollPane = new JScrollPane(articlesTable);
        tableScrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));

        // Panel for result
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel resultTitle = new JLabel("Résultats de la recherche");
        resultTitle.setFont(SUBTITLE_FONT);
        resultPanel.add(resultTitle, BorderLayout.NORTH);
        resultPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Layout the panels
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(resultPanel, BorderLayout.CENTER);

        // Bouton retour
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(createReturnButton());

        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        recherchePanel.add(contentPanel, BorderLayout.CENTER);

        // Action du bouton rechercher
        rechercherButton.addActionListener(e -> {
            try {
                if (familleField.getText().isEmpty()) {
                    showWarningDialog("Veuillez entrer un nom de famille d'articles.");
                    return;
                }

                String nomFamille = familleField.getText();
                rechercherArticlesParFamille(nomFamille);
            } catch (RemoteException ex) {
                showErrorDialog("Erreur", "Erreur lors de la recherche: " + ex.getMessage());
            }
        });

        // Add enter key listener
        familleField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    rechercherButton.doClick();
                }
            }
        });

        // Add double-click listener to show article details
        articlesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = articlesTable.getSelectedRow();
                    if (row >= 0) {
                        try {
                            long reference = Long.parseLong(articlesTable.getValueAt(row, 1).toString());
                            Article article = service.consulterStock(reference);

                            if (article != null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("Référence: ").append(article.getReference()).append("\n");
                                sb.append("Nom: ").append(article.getNom()).append("\n");
                                sb.append("Famille: ").append(article.getFamille().getNom()).append("\n");
                                sb.append("Prix unitaire: ").append(String.format("%.2f €", article.getPrix())).append("\n");
                                sb.append("Quantité en stock: ").append(article.getQuantite_stock());

                                JOptionPane.showMessageDialog(recherchePanel,
                                        sb.toString(),
                                        "Détails de l'article",
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception ex) {
                            showErrorDialog("Erreur", "Erreur lors de la consultation: " + ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    private void rechercherArticlesParFamille(String nomFamille) throws RemoteException {
        List<Article> articles = service.rechercherArticlesParFamille(nomFamille);

        // Vider le tableau
        articlesModel.setRowCount(0);

        if (articles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Aucun article trouvé dans cette famille ou la famille n'existe pas.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
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

        // Apply custom renderer for stock column
        articlesTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                int stock = Integer.parseInt(value.toString());

                if (stock <= 5) {
                    c.setForeground(ERROR_COLOR);
                } else if (stock <= 20) {
                    c.setForeground(ACCENT_COLOR);
                } else {
                    c.setForeground(SUCCESS_COLOR);
                }

                if (isSelected) {
                    c.setForeground(TEXT_COLOR);
                }

                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });
    }

    private void createPanierPanel() {
        panierPanel = new JPanel(new BorderLayout());
        panierPanel.setBackground(BACKGROUND_COLOR);

        // Header
        panierPanel.add(createHeaderPanel("AJOUT D'UN ARTICLE AU PANIER"), BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = createContentPanel();

        // Panel pour l'ID du panier
        JPanel panierIdPanel = new JPanel();
        panierIdPanel.setBackground(new Color(232, 245, 233));
        panierIdPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        panierIdLabel = new JLabel("Panier actuel: Aucun");
        panierIdLabel.setFont(SUBTITLE_FONT);
        panierIdLabel.setForeground(PRIMARY_COLOR);
        panierIdLabel.setText("🛒 " + panierIdLabel.getText());
        panierIdLabel.setIconTextGap(10);

        panierIdPanel.add(panierIdLabel);

        // Panel principal de saisie
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Ligne avec tous les champs et boutons
        JPanel formRowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        formRowPanel.setOpaque(false);

        // Référence
        JLabel refLabel = new JLabel("Référence:");
        refLabel.setFont(REGULAR_FONT);
        refPanierField = new JTextField(10);
        refPanierField.setFont(REGULAR_FONT);

        JButton infoButton = createActionButton("Informations");

        // Quantité
        JLabel qteLabel = new JLabel("Quantité:");
        qteLabel.setFont(REGULAR_FONT);
        qtePanierField = new JTextField(5);
        qtePanierField.setFont(REGULAR_FONT);

        // Bouton ajouter
        JButton ajouterButton = createActionButton("Ajouter au panier");
        ajouterButton.setBackground(ACCENT_COLOR);

        // Ajouter tous les composants sur la même ligne
        formRowPanel.add(refLabel);
        formRowPanel.add(refPanierField);
        formRowPanel.add(infoButton);
        formRowPanel.add(Box.createHorizontalStrut(10)); // espace
        formRowPanel.add(qteLabel);
        formRowPanel.add(qtePanierField);
        formRowPanel.add(ajouterButton);

        inputPanel.add(formRowPanel);

        // Zone d'information article
        panierInfoArea = new JTextArea(10, 40);
        panierInfoArea.setEditable(false);
        panierInfoArea.setFont(REGULAR_FONT);
        panierInfoArea.setBackground(Color.WHITE);
        panierInfoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(panierInfoArea);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel resultTitle = new JLabel("Informations sur l'article");
        resultTitle.setFont(SUBTITLE_FONT);
        resultPanel.add(resultTitle, BorderLayout.NORTH);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        // Regroupement central
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(panierIdPanel, BorderLayout.NORTH);
        centerPanel.add(inputPanel, BorderLayout.CENTER);
        centerPanel.add(resultPanel, BorderLayout.SOUTH);

        // Navigation
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        bottomPanel.setOpaque(false);

        JButton retourButton = createReturnButton();

        JButton retourFactureButton = new JButton("Retour à la facture");
        retourFactureButton.setFont(REGULAR_FONT);
        retourFactureButton.setFocusPainted(false);
        retourFactureButton.setBackground(new Color(224, 224, 224));
        retourFactureButton.setForeground(TEXT_COLOR);
        retourFactureButton.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        retourFactureButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                retourFactureButton.setBackground(new Color(200, 200, 200));
                retourFactureButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                retourFactureButton.setBackground(new Color(224, 224, 224));
                retourFactureButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        retourFactureButton.addActionListener(e -> {
            if (currentFactureId > 0) {
                try {
                    factureIdField.setText(String.valueOf(currentFactureId));
                    consulterFacture(currentFactureId);
                    cardLayout.show(mainPanel, "facture");
                } catch (RemoteException ex) {
                    showErrorDialog("Erreur", ex.getMessage());
                }
            } else {
                cardLayout.show(mainPanel, "facture");
            }
        });

        bottomPanel.add(retourButton);
        bottomPanel.add(retourFactureButton);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);
        panierPanel.add(contentPanel, BorderLayout.CENTER);

        // Action bouton info
        infoButton.addActionListener(e -> {
            try {
                if (refPanierField.getText().isEmpty()) {
                    showWarningDialog("Veuillez entrer une référence d'article.");
                    return;
                }

                long reference = Long.parseLong(refPanierField.getText());
                Article article = service.consulterStock(reference);

                if (article != null) {
                    StringBuilder sb = new StringBuilder();

                    sb.append("Référence: ").append(article.getReference()).append("\n\n");
                    sb.append("Nom: ").append(article.getNom()).append("\n\n");
                    sb.append("Famille: ").append(article.getFamille().getNom()).append("\n\n");
                    sb.append("Prix unitaire: ").append(String.format("%.2f €", article.getPrix())).append("\n\n");

                    int stock = article.getQuantite_stock();
                    sb.append("Quantité en stock: ");
                    panierInfoArea.setText(sb.toString());
                    panierInfoArea.append(String.valueOf(stock));

                    if (stock <= 5) {
                        panierInfoArea.append(" (Stock faible)");
                        panierInfoArea.setForeground(ERROR_COLOR);
                    } else if (stock <= 20) {
                        panierInfoArea.append(" (Stock moyen)");
                        panierInfoArea.setForeground(ACCENT_COLOR);
                    } else {
                        panierInfoArea.append(" (Stock suffisant)");
                        panierInfoArea.setForeground(SUCCESS_COLOR);
                    }
                } else {
                    panierInfoArea.setText("Article non trouvé.");
                    panierInfoArea.setForeground(ERROR_COLOR);
                }
            } catch (NumberFormatException ex) {
                showErrorDialog("Erreur", "Veuillez entrer une référence valide (nombre entier).");
            } catch (RemoteException ex) {
                showErrorDialog("Erreur", "Erreur lors de la consultation: " + ex.getMessage());
            }
        });

        // Action bouton ajouter
        ajouterButton.addActionListener(e -> {
            try {
                if (refPanierField.getText().isEmpty() || qtePanierField.getText().isEmpty()) {
                    showWarningDialog("Veuillez remplir tous les champs.");
                    return;
                }

                long reference = Long.parseLong(refPanierField.getText());
                int quantite = Integer.parseInt(qtePanierField.getText());

                if (quantite <= 0) {
                    showWarningDialog("La quantité doit être supérieure à 0.");
                    return;
                }

                Article article = service.consulterStock(reference);
                if (article == null) {
                    showWarningDialog("Article non trouvé.");
                    return;
                }

                if (quantite > article.getQuantite_stock()) {
                    showWarningDialog("Stock insuffisant. Il reste seulement " + article.getQuantite_stock() + " exemplaire(s).");
                    return;
                }

                boolean success = service.acheterArticle(reference, quantite, currentFactureId);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Article ajouté au panier avec succès.",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                    refPanierField.setText("");
                    qtePanierField.setText("");
                    panierInfoArea.setText("");
                } else {
                    showErrorDialog("Erreur", "Erreur lors de l'ajout de l'article au panier.");
                }
            } catch (NumberFormatException ex) {
                showErrorDialog("Erreur", "Veuillez entrer des valeurs numériques valides.");
            } catch (RemoteException ex) {
                showErrorDialog("Erreur", "Erreur lors de l'ajout au panier: " + ex.getMessage());
            }
        });

        // Entrée clavier
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.getSource() == refPanierField) {
                        infoButton.doClick();
                        qtePanierField.requestFocus();
                    } else if (e.getSource() == qtePanierField) {
                        ajouterButton.doClick();
                    }
                }
            }
        };

        refPanierField.addKeyListener(enterKeyListener);
        qtePanierField.addKeyListener(enterKeyListener);
    }

    private void creerNouveauPanier() {
        try {
            // Appel de la méthode distante pour créer une nouvelle facture
            this.currentFactureId = service.creerNouvelleFacture();

            panierIdLabel.setText("Panier actuel: " + currentFactureId);

            // Show success dialog
            JDialog successDialog = new JDialog(this, "Nouveau Panier", true);
            successDialog.setLayout(new BorderLayout());

            JPanel panel = new JPanel(new BorderLayout(20, 20));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.setBackground(Color.WHITE);

            JLabel iconLabel = new JLabel("✅", JLabel.CENTER);
            iconLabel.setFont(new Font("Dialog", Font.BOLD, 48));
            iconLabel.setForeground(SUCCESS_COLOR);

            JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>Nouveau panier créé avec succès.<br>ID de facture: " + currentFactureId + "</div></html>");
            messageLabel.setFont(SUBTITLE_FONT);
            messageLabel.setForeground(SUCCESS_COLOR);
            messageLabel.setHorizontalAlignment(JLabel.CENTER);

            panel.add(iconLabel, BorderLayout.NORTH);
            panel.add(messageLabel, BorderLayout.CENTER);

            JButton okButton = new JButton("OK");
            okButton.setFont(REGULAR_FONT);
            okButton.setBackground(SUCCESS_COLOR);
            okButton.setForeground(Color.WHITE);
            okButton.addActionListener(e -> successDialog.dispose());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(okButton);

            panel.add(buttonPanel, BorderLayout.SOUTH);

            successDialog.add(panel);
            successDialog.pack();
            successDialog.setLocationRelativeTo(this);

            // Show dialog
            successDialog.setVisible(true);

            // Go to panier panel
            cardLayout.show(mainPanel, "panier");
        } catch (RemoteException e) {
            showErrorDialog("Erreur", "Erreur lors de la création du panier: " + e.getMessage());
        }
    }

    private void createFacturePanel() {
        facturePanel = new JPanel(new BorderLayout());
        facturePanel.setBackground(BACKGROUND_COLOR);

        // Header
        facturePanel.add(createHeaderPanel("CONSULTER ET PAYER UNE FACTURE"), BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = createContentPanel();

        // Panel pour la saisie
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel instructionLabel = new JLabel("Entrez l'ID de la facture pour la consulter ou la payer");
        instructionLabel.setFont(SUBTITLE_FONT);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fieldPanel.setOpaque(false);

        JLabel idLabel = new JLabel("ID de la facture:");
        idLabel.setFont(REGULAR_FONT);
        factureIdField = new JTextField(10);
        factureIdField.setFont(REGULAR_FONT);

        JButton consulterButton = createActionButton("Consulter");

        // Nouveau bouton pour ajouter un article à la facture
        ajouterArticleFactureButton = createActionButton("Ajouter article");
        ajouterArticleFactureButton.setVisible(false); // Caché par défaut

        fieldPanel.add(idLabel);
        fieldPanel.add(factureIdField);
        fieldPanel.add(consulterButton);
        fieldPanel.add(ajouterArticleFactureButton);

        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(instructionLabel);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(fieldPanel);

        // Zone d'affichage des informations
        factureInfoArea = new JTextArea(15, 50);
        factureInfoArea.setEditable(false);
        factureInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        factureInfoArea.setBackground(Color.WHITE);
        factureInfoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(factureInfoArea);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));

        // Panel for result
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel resultTitle = new JLabel("Détails de la facture");
        resultTitle.setFont(SUBTITLE_FONT);
        resultPanel.add(resultTitle, BorderLayout.NORTH);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel pour le paiement
        paiementPanel = new JPanel(new BorderLayout(0, 10));
        paiementPanel.setBackground(new Color(232, 245, 233));
        paiementPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Panel pour le choix du mode de paiement
        JPanel choixPaiementPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        choixPaiementPanel.setOpaque(false);

        JLabel modeLabel = new JLabel("Mode de paiement:");
        modeLabel.setFont(REGULAR_FONT);

        String[] modes = {"Carte Bancaire", "Espèces", "Chèque"};
        modePaiementCombo = new JComboBox<>(modes);
        modePaiementCombo.setFont(REGULAR_FONT);

        payerButton = createActionButton("Payer la facture");
        payerButton.setBackground(SUCCESS_COLOR);

        choixPaiementPanel.add(modeLabel);
        choixPaiementPanel.add(modePaiementCombo);
        choixPaiementPanel.add(payerButton);

        // Panel pour afficher le statut de paiement
        JPanel statutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statutPanel.setOpaque(false);

        statutPaiementLabel = new JLabel("");
        statutPaiementLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statutPaiementLabel.setForeground(SUCCESS_COLOR);
        statutPaiementLabel.setText("✅ " + statutPaiementLabel.getText());
        statutPaiementLabel.setIconTextGap(10);
        statutPaiementLabel.setVisible(false);

        statutPanel.add(statutPaiementLabel);

        paiementPanel.add(choixPaiementPanel, BorderLayout.NORTH);
        paiementPanel.add(statutPanel, BorderLayout.CENTER);

        // Layout the panels
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(resultPanel, BorderLayout.CENTER);
        centerPanel.add(paiementPanel, BorderLayout.SOUTH);

        // Bouton retour
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(createReturnButton());

        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        facturePanel.add(contentPanel, BorderLayout.CENTER);

        // Action du bouton consulter
        consulterButton.addActionListener(e -> {
            try {
                if (factureIdField.getText().isEmpty()) {
                    showWarningDialog("Veuillez entrer un ID de facture.");
                    return;
                }

                int idFacture = Integer.parseInt(factureIdField.getText());
                consulterFacture(idFacture);
            } catch (NumberFormatException ex) {
                showErrorDialog("Erreur", "Veuillez entrer un ID de facture valide (nombre entier).");
            } catch (RemoteException ex) {
                showErrorDialog("Erreur", "Erreur lors de la consultation: " + ex.getMessage());
            }
        });

        // Add enter key listener
        factureIdField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    consulterButton.doClick();
                }
            }
        });

        // Action du bouton ajouter article
        ajouterArticleFactureButton.addActionListener(e -> {
            try {
                int idFacture = Integer.parseInt(factureIdField.getText());

                // Vérifier si la facture existe
                Facture facture = service.consulterFacture(idFacture);
                if (facture == null) {
                    showWarningDialog("Facture non trouvée.");
                    return;
                }

                // Vérifier si la facture n'est pas déjà payée
                if ("payee".equals(facture.getStatus())) {
                    showWarningDialog("Cette facture est déjà payée. Impossible d'ajouter des articles.");
                    return;
                }

                // Confirmation avant de rediriger
                int confirmation = JOptionPane.showConfirmDialog(this,
                        "Voulez-vous ajouter un article à la facture n° " + idFacture + " ?",
                        "Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (confirmation == JOptionPane.YES_OPTION) {
                    // Définir la facture courante pour l'ajout d'articles
                    currentFactureId = idFacture;
                    // Mettre à jour le label du panier
                    panierIdLabel.setText("Panier actuel: " + currentFactureId);
                    // Aller à l'écran d'ajout d'article
                    cardLayout.show(mainPanel, "panier");
                }
            } catch (NumberFormatException ex) {
                showErrorDialog("Erreur", "ID de facture invalide.");
            } catch (RemoteException ex) {
                showErrorDialog("Erreur", "Erreur lors de la consultation de la facture: " + ex.getMessage());
            }
        });

        // Action du bouton payer
        payerButton.addActionListener(e -> {
            try {
                if (factureIdField.getText().isEmpty()) {
                    showWarningDialog("Veuillez d'abord consulter une facture.");
                    return;
                }

                int idFacture = Integer.parseInt(factureIdField.getText());
                String modePaiement = (String) modePaiementCombo.getSelectedItem();

                // Custom confirmation dialog
                JDialog confirmDialog = new JDialog(this, "Confirmation de paiement", true);
                confirmDialog.setLayout(new BorderLayout());

                JPanel panel = new JPanel(new BorderLayout(20, 20));
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                panel.setBackground(Color.WHITE);

                JLabel iconLabel = new JLabel("💳", JLabel.CENTER);
                iconLabel.setFont(new Font("Dialog", Font.BOLD, 48));

                JLabel messageLabel = new JLabel("Voulez-vous payer cette facture en " + modePaiement );
                messageLabel.setFont(SUBTITLE_FONT);
                messageLabel.setHorizontalAlignment(JLabel.CENTER);

                panel.add(iconLabel, BorderLayout.WEST);
                panel.add(messageLabel, BorderLayout.CENTER);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
                buttonPanel.setBackground(Color.WHITE);

                JButton yesButton = new JButton("Oui");
                yesButton.setFont(REGULAR_FONT);
                yesButton.setBackground(SUCCESS_COLOR);
                yesButton.setForeground(Color.WHITE);

                JButton noButton = new JButton("Non");
                noButton.setFont(REGULAR_FONT);
                noButton.setBackground(new Color(224, 224, 224));
                noButton.setForeground(TEXT_COLOR);

                yesButton.addActionListener(event -> {
                    confirmDialog.dispose();
                    try {
                        boolean success = service.payerFacture(idFacture, modePaiement);

                        if (success) {
                            // Show success animation
                            JDialog successDialog = new JDialog(this, "Paiement effectué", true);
                            successDialog.setLayout(new BorderLayout());

                            JPanel successPanel = new JPanel(new BorderLayout(20, 20));
                            successPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                            successPanel.setBackground(Color.WHITE);

                            JLabel successIconLabel = new JLabel("✅", JLabel.CENTER);
                            successIconLabel.setFont(new Font("Dialog", Font.BOLD, 48));
                            successIconLabel.setForeground(SUCCESS_COLOR);

                            JLabel successMessageLabel = new JLabel("<html><div style='text-align: center;'>Paiement effectué avec succès.<br>Merci de votre achat chez Brico-Merlin!</div></html>");
                            successMessageLabel.setFont(SUBTITLE_FONT);
                            successMessageLabel.setForeground(SUCCESS_COLOR);
                            successMessageLabel.setHorizontalAlignment(JLabel.CENTER);

                            successPanel.add(successIconLabel, BorderLayout.NORTH);
                            successPanel.add(successMessageLabel, BorderLayout.CENTER);

                            JButton okButton = new JButton("OK");
                            okButton.setFont(REGULAR_FONT);
                            okButton.setBackground(SUCCESS_COLOR);
                            okButton.setForeground(Color.WHITE);
                            okButton.addActionListener(evt -> successDialog.dispose());

                            JPanel okButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                            okButtonPanel.setBackground(Color.WHITE);
                            okButtonPanel.add(okButton);

                            successPanel.add(okButtonPanel, BorderLayout.SOUTH);

                            successDialog.add(successPanel);
                            successDialog.pack();
                            successDialog.setLocationRelativeTo(this);

                            // Show dialog
                            successDialog.setVisible(true);

                            // Refresh display
                            consulterFacture(idFacture);
                        } else {
                            showErrorDialog("Erreur", "Erreur lors du paiement de la facture.");
                        }
                    } catch (RemoteException ex) {
                        showErrorDialog("Erreur", "Erreur lors du paiement: " + ex.getMessage());
                    }
                });

                noButton.addActionListener(event -> confirmDialog.dispose());

                buttonPanel.add(yesButton);
                buttonPanel.add(noButton);

                panel.add(buttonPanel, BorderLayout.SOUTH);

                confirmDialog.add(panel);
                confirmDialog.pack();
                confirmDialog.setLocationRelativeTo(this);

                // Show dialog
                confirmDialog.setVisible(true);
            } catch (NumberFormatException ex) {
                showErrorDialog("Erreur", "Veuillez entrer un ID de facture valide (nombre entier).");
            }
        });
    }

    private void consulterFacture(int idFacture) throws RemoteException {
        Facture facture = service.consulterFacture(idFacture);

        if (facture == null) {
            factureInfoArea.setText("Facture non trouvée.");
            factureInfoArea.setForeground(ERROR_COLOR);
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
        factureInfoArea.setForeground(TEXT_COLOR);

        // Vérifier si la facture est déjà payée
        facturePayee = "payee".equals(facture.getStatus());

        if (facturePayee && facture.getMode_paiement() != null) {
            // Afficher le message indiquant que la facture est déjà payée avec le mode de paiement
            statutPaiementLabel.setText("✅ Cette facture est déjà payée en " + facture.getMode_paiement());
            statutPaiementLabel.setVisible(true);
        } else {
            statutPaiementLabel.setVisible(false);
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

        // Header
        chiffreAffairesPanel.add(createHeaderPanel("CONSULTATION DU CHIFFRE D'AFFAIRES"), BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = createContentPanel();

        // Panel pour la saisie
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel instructionLabel = new JLabel("Entrez une date pour consulter le chiffre d'affaires");
        instructionLabel.setFont(SUBTITLE_FONT);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fieldPanel.setOpaque(false);

        JLabel dateLabel = new JLabel("Date (JJ/MM/AAAA):");
        dateLabel.setFont(REGULAR_FONT);
        dateCAField = new JTextField(15);
        dateCAField.setFont(REGULAR_FONT);

        JButton consulterButton = createActionButton("Consulter");

        fieldPanel.add(dateLabel);
        fieldPanel.add(dateCAField);
        fieldPanel.add(consulterButton);

        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(instructionLabel);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(fieldPanel);

        // Panel pour l'affichage du résultat
        JPanel resultPanel = new JPanel();
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(30, 30, 30, 30)
        ));

        JLabel caLabelText = new JLabel("Chiffre d'affaires:");
        caLabelText.setFont(SUBTITLE_FONT);

        chiffreAffairesLabel = new JLabel("0.00 €");
        chiffreAffairesLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        chiffreAffairesLabel.setForeground(PRIMARY_COLOR);



        // Layout the result panel
        resultPanel.setLayout(new BorderLayout(20, 20));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(caLabelText);
        textPanel.add(chiffreAffairesLabel);

        resultPanel.add(textPanel, BorderLayout.NORTH);

        // Layout the panels
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(resultPanel, BorderLayout.CENTER);

        // Bouton retour
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(createReturnButton());

        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        chiffreAffairesPanel.add(contentPanel, BorderLayout.CENTER);

        // Action du bouton consulter
        consulterButton.addActionListener(e -> {
            try {
                if (dateCAField.getText().isEmpty()) {
                    showWarningDialog("Veuillez entrer une date.");
                    return;
                }

                String dateStr = dateCAField.getText();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDateTime date = LocalDateTime.parse(dateStr + " 00:00:00",
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

                double chiffreAffaire = service.calculerChiffreAffaire(date);
                chiffreAffairesLabel.setText(String.format("%.2f €", chiffreAffaire));

                // Change color based on value
                if (chiffreAffaire > 1000) {
                    chiffreAffairesLabel.setForeground(SUCCESS_COLOR);
                } else if (chiffreAffaire > 500) {
                    chiffreAffairesLabel.setForeground(ACCENT_COLOR);
                } else {
                    chiffreAffairesLabel.setForeground(PRIMARY_COLOR);
                }
            } catch (Exception ex) {
                showErrorDialog("Erreur", "Format de date invalide ou erreur lors de la consultation.\n" +
                        "Veuillez entrer une date au format JJ/MM/AAAA (exemple: 12/05/2024).");
            }
        });

        // Add enter key listener
        dateCAField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    consulterButton.doClick();
                }
            }
        });
    }

    private void createAdminPanel() {
        adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBackground(BACKGROUND_COLOR);

        // Header
        adminPanel.add(createHeaderPanel("ADMINISTRATION"), BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = createContentPanel();

        // Création des onglets avec style moderne
        adminTabbedPane = new JTabbedPane();
        adminTabbedPane.setFont(SUBTITLE_FONT);
        adminTabbedPane.setBackground(Color.WHITE);
        adminTabbedPane.setForeground(TEXT_COLOR);

        // Onglet gestion du stock
        createStockAdminPanel();

        // Onglet mise à jour des prix
        createPrixAdminPanel();

        // Ajout des onglets
        adminTabbedPane.addTab("Gestion du stock", stockAdminPanel);
        adminTabbedPane.addTab("Mise à jour des prix", prixAdminPanel);

        // Bouton retour
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(createReturnButton());

        contentPanel.add(adminTabbedPane, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        adminPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private void createStockAdminPanel() {
        stockAdminPanel = new JPanel(new BorderLayout(0, 20));
        stockAdminPanel.setBackground(BACKGROUND_COLOR);
        stockAdminPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel pour la saisie
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel instructionLabel = new JLabel("Gestion du stock des articles");
        instructionLabel.setFont(SUBTITLE_FONT);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Reference panel
        JPanel refPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        refPanel.setOpaque(false);

        JLabel refLabel = new JLabel("Référence de l'article:");
        refLabel.setFont(REGULAR_FONT);
        refStockAdminField = new JTextField(15);
        refStockAdminField.setFont(REGULAR_FONT);

        JButton infoButton = createActionButton("Informations");

        refPanel.add(refLabel);
        refPanel.add(refStockAdminField);
        refPanel.add(infoButton);

        // Quantity panel
        JPanel qtePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        qtePanel.setOpaque(false);

        JLabel qteLabel = new JLabel("Quantité à ajouter:");
        qteLabel.setFont(REGULAR_FONT);
        qteStockAdminField = new JTextField(15);
        qteStockAdminField.setFont(REGULAR_FONT);

        qtePanel.add(qteLabel);
        qtePanel.add(qteStockAdminField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);

        JButton ajouterButton = createActionButton("Ajouter au stock");
        ajouterButton.setBackground(ACCENT_COLOR);

        buttonPanel.add(ajouterButton);

        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(instructionLabel);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(refPanel);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(qtePanel);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(buttonPanel);

        // Zone d'affichage des informations
        stockAdminInfoArea = new JTextArea(10, 40);
        stockAdminInfoArea.setEditable(false);
        stockAdminInfoArea.setFont(REGULAR_FONT);
        stockAdminInfoArea.setBackground(Color.WHITE);
        stockAdminInfoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(stockAdminInfoArea);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));

        // Panel for result
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel resultTitle = new JLabel("Informations sur l'article");
        resultTitle.setFont(SUBTITLE_FONT);
        resultPanel.add(resultTitle, BorderLayout.NORTH);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        stockAdminPanel.add(inputPanel, BorderLayout.NORTH);
        stockAdminPanel.add(resultPanel, BorderLayout.CENTER);

        // Action du bouton info
        infoButton.addActionListener(e -> {
            try {
                if (refStockAdminField.getText().isEmpty()) {
                    showWarningDialog("Veuillez entrer une référence d'article.");
                    return;
                }

                long reference = Long.parseLong(refStockAdminField.getText());
                Article article = service.consulterStock(reference);

                if (article != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Informations sur l'article:\n\n");
                    sb.append("Référence: ").append(article.getReference()).append("\n\n");
                    sb.append("Nom: ").append(article.getNom()).append("\n\n");
                    sb.append("Famille: ").append(article.getFamille().getNom()).append("\n\n");
                    sb.append("Prix unitaire: ").append(String.format("%.2f €", article.getPrix())).append("\n\n");

                    // Add color coding for stock level
                    int stock = article.getQuantite_stock();
                    sb.append("Quantité en stock: ");

                    stockAdminInfoArea.setText(sb.toString());
                    stockAdminInfoArea.append(String.valueOf(stock));
                    stockAdminInfoArea.setForeground(TEXT_COLOR);

                    // Highlight stock level with color
                    if (stock <= 5) {
                        // Low stock - red
                        stockAdminInfoArea.append(" (Stock faible)");
                        stockAdminInfoArea.setForeground(ERROR_COLOR);
                    } else if (stock <= 20) {
                        // Medium stock - orange
                        stockAdminInfoArea.append(" (Stock moyen)");
                        stockAdminInfoArea.setForeground(ACCENT_COLOR);
                    } else {
                        // Good stock - green
                        stockAdminInfoArea.append(" (Stock suffisant)");
                        stockAdminInfoArea.setForeground(SUCCESS_COLOR);
                    }
                } else {
                    stockAdminInfoArea.setText("Article non trouvé.");
                    stockAdminInfoArea.setForeground(ERROR_COLOR);
                }
            } catch (NumberFormatException ex) {
                showErrorDialog("Erreur", "Veuillez entrer une référence valide (nombre entier).");
            } catch (RemoteException ex) {
                showErrorDialog("Erreur", "Erreur lors de la consultation: " + ex.getMessage());
            }
        });

        // Action du bouton ajouter
        ajouterButton.addActionListener(e -> {
            try {
                if (refStockAdminField.getText().isEmpty() || qteStockAdminField.getText().isEmpty()) {
                    showWarningDialog("Veuillez remplir tous les champs.");
                    return;
                }

                long reference = Long.parseLong(refStockAdminField.getText());
                int quantite = Integer.parseInt(qteStockAdminField.getText());

                if (quantite <= 0) {
                    showWarningDialog("La quantité doit être supérieure à 0.");
                    return;
                }

                // Vérifier si l'article existe
                Article article = service.consulterStock(reference);
                if (article == null) {
                    showWarningDialog("Article non trouvé.");
                    return;
                }

                service.ajouterStock(reference, quantite);

                // Show success dialog
                JOptionPane.showMessageDialog(this,
                        "Stock mis à jour avec succès.",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);

                // Afficher le nouveau stock
                article = service.consulterStock(reference);
                stockAdminInfoArea.setText("Stock mis à jour avec succès.\n\nNouveau stock: " + article.getQuantite_stock());
                stockAdminInfoArea.setForeground(SUCCESS_COLOR);

                qteStockAdminField.setText("");
            } catch (NumberFormatException ex) {
                showErrorDialog("Erreur", "Veuillez entrer des valeurs numériques valides.");
            } catch (RemoteException ex) {
                showErrorDialog("Erreur", "Erreur lors de la mise à jour du stock: " + ex.getMessage());
            }
        });

        // Add enter key listener
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.getSource() == refStockAdminField) {
                        infoButton.doClick();
                        qteStockAdminField.requestFocus();
                    } else if (e.getSource() == qteStockAdminField) {
                        ajouterButton.doClick();
                    }
                }
            }
        };

        refStockAdminField.addKeyListener(enterKeyListener);
        qteStockAdminField.addKeyListener(enterKeyListener);
    }

    private void createPrixAdminPanel() {
        prixAdminPanel = new JPanel(new BorderLayout(0, 20));
        prixAdminPanel.setBackground(BACKGROUND_COLOR);
        prixAdminPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel pour la saisie du nombre d'articles
        JPanel nbArticlesPanel = new JPanel();
        nbArticlesPanel.setLayout(new BoxLayout(nbArticlesPanel, BoxLayout.Y_AXIS));
        nbArticlesPanel.setBackground(Color.WHITE);
        nbArticlesPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel instructionLabel = new JLabel("Mise à jour des prix des articles");
        instructionLabel.setFont(SUBTITLE_FONT);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fieldPanel.setOpaque(false);

        JLabel nbLabel = new JLabel("Nombre d'articles à mettre à jour:");
        nbLabel.setFont(REGULAR_FONT);
        nbArticlesField = new JTextField(5);
        nbArticlesField.setFont(REGULAR_FONT);

        JButton validerNbButton = createActionButton("Valider");

        fieldPanel.add(nbLabel);
        fieldPanel.add(nbArticlesField);
        fieldPanel.add(validerNbButton);

        nbArticlesPanel.add(Box.createVerticalStrut(10));
        nbArticlesPanel.add(instructionLabel);
        nbArticlesPanel.add(Box.createVerticalStrut(20));
        nbArticlesPanel.add(fieldPanel);

        // Panel pour la mise à jour des prix
        JPanel updateContainer = new JPanel(new BorderLayout());
        updateContainer.setBackground(Color.WHITE);
        updateContainer.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel updateTitle = new JLabel("Articles à mettre à jour");
        updateTitle.setFont(SUBTITLE_FONT);

        prixUpdatePanel = new JPanel();
        prixUpdatePanel.setLayout(new BoxLayout(prixUpdatePanel, BoxLayout.Y_AXIS));
        prixUpdatePanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(prixUpdatePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        updateContainer.add(updateTitle, BorderLayout.NORTH);
        updateContainer.add(scrollPane, BorderLayout.CENTER);

        // Bouton pour valider les prix
        validerPrixButton = createActionButton("Mettre à jour les prix");
        validerPrixButton.setBackground(ACCENT_COLOR);
        validerPrixButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(validerPrixButton);

        updateContainer.add(buttonPanel, BorderLayout.SOUTH);

        prixAdminPanel.add(nbArticlesPanel, BorderLayout.NORTH);
        prixAdminPanel.add(updateContainer, BorderLayout.CENTER);

        // Action du bouton valider nombre
        validerNbButton.addActionListener(e -> {
            try {
                if (nbArticlesField.getText().isEmpty()) {
                    showWarningDialog("Veuillez entrer le nombre d'articles.");
                    return;
                }

                int nbArticles = Integer.parseInt(nbArticlesField.getText());

                if (nbArticles <= 0) {
                    showWarningDialog("Le nombre d'articles doit être supérieur à 0.");
                    return;
                }

                // Vider le panel
                prixUpdatePanel.removeAll();

                // Créer les champs pour chaque article
                for (int i = 0; i < nbArticles; i++) {
                    JPanel articlePanel = new JPanel();
                    articlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                    articlePanel.setBackground(i % 2 == 0 ? new Color(245, 245, 245) : Color.WHITE);
                    articlePanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

                    JLabel numLabel = new JLabel("Article " + (i + 1) + ":");
                    numLabel.setFont(REGULAR_FONT);
                    numLabel.setPreferredSize(new Dimension(80, 25));

                    JLabel refLabel = new JLabel("Référence:");
                    refLabel.setFont(REGULAR_FONT);

                    JTextField refField = new JTextField(10);
                    refField.setFont(REGULAR_FONT);

                    JLabel prixLabel = new JLabel("Nouveau prix:");
                    prixLabel.setFont(REGULAR_FONT);

                    JTextField prixField = new JTextField(10);
                    prixField.setFont(REGULAR_FONT);

                    articlePanel.add(numLabel);
                    articlePanel.add(refLabel);
                    articlePanel.add(refField);
                    articlePanel.add(prixLabel);
                    articlePanel.add(prixField);

                    // Ajouter un bouton pour vérifier l'article
                    JButton checkButton = createActionButton("Vérifier");
                    checkButton.setPreferredSize(new Dimension(100, 30));

                    final int index = i;
                    checkButton.addActionListener(event -> {
                        try {
                            if (refField.getText().isEmpty()) {
                                showWarningDialog("Veuillez entrer une référence d'article.");
                                return;
                            }

                            long reference = Long.parseLong(refField.getText());
                            Article article = service.consulterStock(reference);

                            if (article != null) {
                                JOptionPane.showMessageDialog(this,
                                        "Article trouvé: " + article.getNom() + "\n" +
                                                "Prix actuel: " + article.getPrix() + " €",
                                        "Information", JOptionPane.INFORMATION_MESSAGE);

                                // Pre-fill the price field with current price
                                prixField.setText(String.format("%.2f", article.getPrix()));
                            } else {
                                showWarningDialog("Article non trouvé.");
                            }
                        } catch (NumberFormatException ex) {
                            showErrorDialog("Erreur", "Veuillez entrer une référence valide (nombre entier).");
                        } catch (RemoteException ex) {
                            showErrorDialog("Erreur", "Erreur lors de la consultation: " + ex.getMessage());
                        }
                    });

                    articlePanel.add(checkButton);

                    // Ajouter un tag pour identifier les champs
                    refField.setName("ref_" + i);
                    prixField.setName("prix_" + i);

                    prixUpdatePanel.add(articlePanel);
                }

                // Activer le bouton de validation
                validerPrixButton.setEnabled(true);

                // Rafraîchir l'affichage
                prixUpdatePanel.revalidate();
                prixUpdatePanel.repaint();
            } catch (NumberFormatException ex) {
                showErrorDialog("Erreur", "Veuillez entrer un nombre valide.");
            }
        });

        // Add enter key listener
        nbArticlesField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    validerNbButton.doClick();
                }
            }
        });

        // Action du bouton valider prix
        validerPrixButton.addActionListener(e -> {
            try {
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
                            showWarningDialog("Le prix doit être supérieur à 0 pour l'article " + (i + 1) + ".");
                            return;
                        }

                        nouveauxPrix.put(reference, nouveauPrix);
                    }
                }

                if (nouveauxPrix.isEmpty()) {
                    showWarningDialog("Aucun prix n'a été spécifié.");
                    return;
                }

                //service.mettreAJourPrix(nouveauxPrix);

                // Show success dialog
                JOptionPane.showMessageDialog(this,
                        "Prix mis à jour avec succès.",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);

                // Réinitialiser les champs
                prixUpdatePanel.removeAll();
                validerPrixButton.setEnabled(false);
                nbArticlesField.setText("");

                // Rafraîchir l'affichage
                prixUpdatePanel.revalidate();
                prixUpdatePanel.repaint();
            } catch (NumberFormatException ex) {
                showErrorDialog("Erreur", "Veuillez entrer des valeurs numériques valides.");
            } /*catch (RemoteException ex) {
                showErrorDialog("Erreur", "Erreur lors de la mise à jour des prix: " + ex.getMessage());
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String host = "localhost";

            if (args.length > 0) {
                host = args[0];
            }

            MainApplication client = new MainApplication(host);
            client.setVisible(true);
        });
    }
}