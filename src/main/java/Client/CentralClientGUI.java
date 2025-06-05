package Client;

import Model.Article;
import Serveur.Central.ICentralBricoMerlinService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class CentralClientGUI extends JFrame {
    private JPanel prixAdminPanel;
    private JPanel prixUpdatePanel;
    private JButton validerPrixButton;
    private ICentralBricoMerlinService stub;

    public CentralClientGUI() {
        super("Brico-Merlin - Système de Gestion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        try {
            Registry registry = LocateRegistry.getRegistry(null);
            stub = (ICentralBricoMerlinService) registry.lookup("CentralBricoMerlinService");

            createAdminPanel();
            setVisible(true); // important pour l'affichage
        } catch (RemoteException | NotBoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur de connexion au serveur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void createAdminPanel() {
        JPanel adminPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("ADMINISTRATION", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        adminPanel.add(titleLabel, BorderLayout.NORTH);

        JTabbedPane adminTabbedPane = new JTabbedPane();
        createPrixAdminPanel();
        adminTabbedPane.addTab("Mise à jour des prix", prixAdminPanel);
        adminPanel.add(adminTabbedPane, BorderLayout.CENTER);

        JButton retourButton = new JButton("Quitter");
        retourButton.addActionListener(e -> System.exit(0));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(retourButton);
        adminPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(adminPanel);
    }

    private void createPrixAdminPanel() {
        prixAdminPanel = new JPanel(new BorderLayout());

        prixUpdatePanel = new JPanel();
        prixUpdatePanel.setLayout(new BoxLayout(prixUpdatePanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(prixUpdatePanel);

        JButton ajouterArticleButton = new JButton("Ajouter un article");
        ajouterArticleButton.addActionListener(e -> ajouterChampArticle());

        validerPrixButton = new JButton("Mettre à jour les prix");
        validerPrixButton.setEnabled(true);
        validerPrixButton.addActionListener(e -> mettreAJourPrix());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(ajouterArticleButton);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(validerPrixButton);

        prixAdminPanel.add(topPanel, BorderLayout.NORTH);
        prixAdminPanel.add(scrollPane, BorderLayout.CENTER);
        prixAdminPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void ajouterChampArticle() {
        int index = prixUpdatePanel.getComponentCount();

        JPanel articlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel numLabel = new JLabel("Article " + (index + 1) + ": ");
        JLabel refLabel = new JLabel("Référence:");
        JTextField refField = new JTextField(10);
        refField.setName("ref_" + index);

        JLabel prixLabel = new JLabel("Nouveau prix:");
        JTextField prixField = new JTextField(10);
        prixField.setName("prix_" + index);

        JButton checkButton = new JButton("Vérifier");
        checkButton.addActionListener(e -> {
            try {
                if (refField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Veuillez entrer une référence d'article.",
                            "Erreur", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                long reference = Long.parseLong(refField.getText());
                Article article = stub.consulterArticle(reference);

                if (article != null) {
                    JOptionPane.showMessageDialog(this,
                                    "Prix actuel: " + article.getPrix() + " €",
                            "Information", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Article non trouvé.",
                            "Erreur", JOptionPane.WARNING_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Référence invalide. Veuillez entrer un nombre entier.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la consultation: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        articlePanel.add(numLabel);
        articlePanel.add(refLabel);
        articlePanel.add(refField);
        articlePanel.add(prixLabel);
        articlePanel.add(prixField);
        articlePanel.add(checkButton);

        prixUpdatePanel.add(articlePanel);
        prixUpdatePanel.revalidate();
        prixUpdatePanel.repaint();
    }

    private void mettreAJourPrix() {
        for (Component component : prixUpdatePanel.getComponents()) {
            if (component instanceof JPanel panel) {
                JTextField refField = null;
                JTextField prixField = null;

                for (Component subComponent : panel.getComponents()) {
                    if (subComponent instanceof JTextField textField) {
                        if (textField.getName() != null) {
                            if (textField.getName().startsWith("ref_")) {
                                refField = textField;
                            } else if (textField.getName().startsWith("prix_")) {
                                prixField = textField;
                            }
                        }
                    }
                }

                if (refField != null && prixField != null &&
                        !refField.getText().isEmpty() && !prixField.getText().isEmpty()) {
                    try {
                        long reference = Long.parseLong(refField.getText());
                        double prix = Double.parseDouble(prixField.getText());

                        if (prix <= 0) {
                            JOptionPane.showMessageDialog(this,
                                    "Le prix de l'article " + reference + " doit être supérieur à 0.",
                                    "Erreur", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        // Appel RMI individuel pour chaque article
                        stub.mettreAJourPrix(reference, prix);

                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this,
                                "Valeurs incorrectes pour la référence ou le prix.",
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                        return;
                    } catch (RemoteException e) {
                        JOptionPane.showMessageDialog(this,
                                "Erreur lors de la mise à jour de l'article " + refField.getText() + " : " + e.getMessage(),
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
        }

        JOptionPane.showMessageDialog(this,
                "Tous les prix ont été mis à jour avec succès.",
                "Succès", JOptionPane.INFORMATION_MESSAGE);

        prixUpdatePanel.removeAll();
        prixUpdatePanel.revalidate();
        prixUpdatePanel.repaint();
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CentralClientGUI client = new CentralClientGUI();
            client.setVisible(true);
        });
    }
}

