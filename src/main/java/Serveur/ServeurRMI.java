package Serveur;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class ServeurRMI extends BricoMerlinServiceImpl {

    public ServeurRMI() {}

    public static void main(String args[]) {
        try {
            // crée l'objet distant
            BricoMerlinServiceImpl obj = new BricoMerlinServiceImpl();

            // ici, nous exportons l'objet distant vers le stub
            IBricoMerlinService stub = (IBricoMerlinService) UnicastRemoteObject.exportObject(obj, 0);

            // Liaison de l'objet distant (stub) dans le Registre
            Registry reg = LocateRegistry.getRegistry();

            reg.rebind("BricoMerlinService", stub);
            System.out.println("Le Serveur BricoMerlin est prêt...");
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }
    }
}
