package Serveur.Central;



import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class CentralServeurRMI extends CentralBricoMerlinServiceImpl {

    public CentralServeurRMI() {}

    public static void main(String args[]) {
        try {
            // crée l'objet distant
            ICentralBricoMerlinService obj = new CentralBricoMerlinServiceImpl();

            // ici, nous exportons l'objet distant vers le stub
            ICentralBricoMerlinService stub = (ICentralBricoMerlinService) UnicastRemoteObject.exportObject(obj, 0);

            // Liaison de l'objet distant (stub) dans le Registre
            Registry reg = LocateRegistry.getRegistry();

            reg.rebind("CentralBricoMerlinService", stub);
            System.out.println("Le Serveur Central BricoMerlin est prêt...");
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }
    }
}
