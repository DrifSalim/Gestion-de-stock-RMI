package Serveur;

import Model.Article;
import Model.Facture;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IBricoMerlinService extends Remote {
        // Gestion du stock
        Article consulterStock(long reference) throws RemoteException;
        List<Article> rechercherArticlesParFamille(String nomFamille) throws RemoteException;
        boolean acheterArticle(long reference, int quantite, int idFacture) throws RemoteException;

        // Gestion des factures
        int creerNouvelleFacture() throws RemoteException;
        boolean payerFacture(int idFacture, String modePaiement) throws RemoteException;
        Facture consulterFacture(int idFacture) throws RemoteException;
        double calculerChiffreAffaire(LocalDateTime date) throws RemoteException;
        void enregistrerFactureEnPDF(int idFacture) throws RemoteException;

        // Opérations administratives
        void ajouterStock(long reference, int quantite) throws RemoteException;
        boolean mettreAJourPrixLocal(long reference, double nouveauPrix) throws RemoteException;
        List<File> envoyerFacturesPDFDuJour() throws RemoteException;
}