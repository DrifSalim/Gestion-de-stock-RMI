package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AccueilGUI extends JFrame {



    public AccueilGUI() {
        super("Brico-Merlin - Accueil");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

        // Création des composants
        JLabel welcomeLabel = new JLabel("Bienvenue dans Brico-Merlin", JLabel.CENTER);
        JLabel instructionLabel = new JLabel("Choisissez votre mode d'accès :", JLabel.CENTER);

        JButton adminButton = new JButton("Accès Administration");
        JButton clientButton = new JButton("Accès Employée");

        // Ajout des actions
        adminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchAdminInterface();
            }
        });

        clientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchClientInterface();
            }
        });

        // Mise en page simple
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(welcomeLabel);
        panel.add(instructionLabel);
        panel.add(adminButton);
        panel.add(clientButton);

        add(panel);
    }

    private void launchAdminInterface() {
        JOptionPane.showMessageDialog(this,
                "Lancement de l'interface d'Administration...", "Info", JOptionPane.INFORMATION_MESSAGE);
        this.dispose();
        AdminGUI admin = new AdminGUI();
        admin.setVisible(true);
    }


    private void launchClientInterface() {
        JOptionPane.showMessageDialog(this,
                "Lancement de l'interface Employée...", "Info", JOptionPane.INFORMATION_MESSAGE);
        this.dispose();
        ClientGUI client = new ClientGUI();
        client.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AccueilGUI gui = new AccueilGUI();
            gui.setVisible(true);
        });
    }
}
