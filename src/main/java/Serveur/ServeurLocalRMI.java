package Serveur;

import Model.Article;
import Serveur.Central.ICentralBricoMerlinService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

public class ServeurLocalRMI extends BricoMerlinServiceImpl {
    private ICentralBricoMerlinService stub_central;

    public ServeurLocalRMI() {
        try {
            // Récupérer le stub du serveur central à la création de l'instance
            Registry registry = LocateRegistry.getRegistry(null);
            stub_central = (ICentralBricoMerlinService) registry.lookup("CentralBricoMerlinService");
            System.out.println("Connexion au serveur central réussie dans le constructeur.");
        } catch (Exception e) {
            System.err.println("Erreur lors de la connexion au serveur central : " + e);
            e.printStackTrace();
        }
    }
    //appel de la fonction de mise à jour de prix
    public List<Article> recupererMiseAjourPrix() {
        List<Article> articles= new ArrayList<>();
        try {
            if (stub_central != null) {
                articles = stub_central.getPrixMisAJour();
            } else {
                System.err.println("Stub central non initialisé, impossible de mettre à jour le prix.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du prix sur le serveur central : " + e);
            e.printStackTrace();
        }
        return articles;
    }

    public void envoyerPDF(List <File> factures) throws IOException {
        if (stub_central != null) {
            for (File f : factures) {
                byte[] contenu = Files.readAllBytes(f.toPath());
                try{
                    stub_central.stockerFacturePDF(contenu, f.getName());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        } else {
            System.err.println("Stub central non initialisé.");
        }
    }


    public static void main(String args[]) {
        try {
            BricoMerlinServiceImpl obj = new BricoMerlinServiceImpl();
            IBricoMerlinService stub = (IBricoMerlinService) java.rmi.server.UnicastRemoteObject.exportObject(obj, 0);
            Registry reg = LocateRegistry.getRegistry();
            reg.rebind("BricoMerlinService", stub);
            System.out.println("Le Serveur BricoMerlin est prêt...");
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }

    }
}
