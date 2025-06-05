package Serveur.Central;

import Model.Article;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICentralBricoMerlinService extends Remote {

    boolean mettreAJourPrix(long reference, double nouveauPrix) throws RemoteException;

    boolean stockerFacturePDF(byte[] contenuPDF, String nomFichier) throws RemoteException;

    Article consulterArticle(long reference) throws RemoteException;

}
