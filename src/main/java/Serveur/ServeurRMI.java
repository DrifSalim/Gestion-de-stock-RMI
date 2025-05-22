package Serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServeurRMI {
    private static final Logger LOGGER = Logger.getLogger(ServeurRMI.class.getName());

    public static void main(String[] args) {
        try {


            // Créer le service RMI
            IBricoMerlinService service = new BricoMerlinServiceImpl();

            // Créer ou récupérer le registre RMI
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(1099);
                LOGGER.info("Registry RMI créé sur le port 1099");
            } catch (Exception e) {
                LOGGER.info("Registry RMI déjà démarré, tentative de récupération...");
                registry = LocateRegistry.getRegistry(1099);
            }

            // Enregistrer le service dans le registre
            registry.rebind("BricoMerlinService", service);

            LOGGER.info("Serveur BricoMerlin démarré avec succès");
            LOGGER.info("Attente de connexions des clients...");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du démarrage du serveur", e);
        }
    }
}