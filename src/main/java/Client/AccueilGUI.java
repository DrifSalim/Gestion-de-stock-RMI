package Client;

import javax.swing.*;
import java.awt.*;

public class AccueilGUI extends JFrame {

    public AccueilGUI() {
        super("Brico-Merlin - Accueil");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        // Police personnalisée
        Font titleFont = new Font("SansSerif", Font.BOLD, 20);
        Font labelFont = new Font("SansSerif", Font.PLAIN, 16);
        Font buttonFont = new Font("SansSerif", Font.BOLD, 14);

        // Composants
        JLabel welcomeLabel = new JLabel("Bienvenue chez Brico-Merlin", JLabel.CENTER);
        welcomeLabel.setFont(titleFont);
        welcomeLabel.setForeground(new Color(0, 102, 204));

        JLabel instructionLabel = new JLabel("Veuillez choisir un mode d'accès :", JLabel.CENTER);
        instructionLabel.setFont(labelFont);

        JButton adminButton = new JButton("Accès Administration");
        JButton clientButton = new JButton("Accès Employé(e)");
        JButton centralButton = new JButton("Accès Serveur Central");

        // Uniformiser les boutons
        JButton[] buttons = {adminButton, clientButton, centralButton};
        for (JButton btn : buttons) {
            btn.setFont(buttonFont);
            btn.setFocusPainted(false);
            btn.setBackground(new Color(230, 230, 250));
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        // Ajout des actions avec lambdas
        adminButton.addActionListener(e -> openAdminInterface());
        clientButton.addActionListener(e -> openEmployeeInterface());
        centralButton.addActionListener(e -> openCentralInterface());

        // Layout
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel labelPanel = new JPanel(new GridLayout(2, 1));
        labelPanel.add(welcomeLabel);
        labelPanel.add(instructionLabel);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.add(adminButton);
        buttonPanel.add(clientButton);
        buttonPanel.add(centralButton);

        content.add(labelPanel, BorderLayout.NORTH);
        content.add(buttonPanel, BorderLayout.CENTER);

        add(content);
    }

    private void openAdminInterface() {
        JOptionPane.showMessageDialog(this,
                "Ouverture de l'interface Administration...", "Information", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        new AdminGUI().setVisible(true);
    }

    private void openEmployeeInterface() {
        JOptionPane.showMessageDialog(this,
                "Ouverture de l'interface Employée...", "Information", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        new ClientGUI().setVisible(true);
    }

    private void openCentralInterface() {
        JOptionPane.showMessageDialog(this,
                "Ouverture du Serveur Central...", "Information", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        new CentralClientGUI().setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AccueilGUI gui = new AccueilGUI();
            gui.setVisible(true);
        });
    }
}
