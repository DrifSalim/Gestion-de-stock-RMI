package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AccueilGUI extends JFrame {

    private String host;


    public AccueilGUI(String host) {
        super("Brico-Merlin - Accueil");
        this.host = host;
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
                "Lancement de l'interface d'administration...", "Info", JOptionPane.INFORMATION_MESSAGE);
        this.dispose();
        MainApplicationV0 admin = new MainApplicationV0();
        admin.setVisible(true);
    }


    private void launchClientInterface() {
        JOptionPane.showMessageDialog(this,
                "Lancement de l'interface client...", "Info", JOptionPane.INFORMATION_MESSAGE);
        this.dispose();
        MainApplication client = new MainApplication(host);
        client.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String host = "localhost";
            if (args.length > 0) {
                host = args[0];
            }
            AccueilGUI gui = new AccueilGUI(host);
            gui.setVisible(true);
        });
    }
}
