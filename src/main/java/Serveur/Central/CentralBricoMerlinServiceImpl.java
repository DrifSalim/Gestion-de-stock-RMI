package Serveur.Central;

import Model.Article;
import Model.Famille;
import Serveur.DbConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CentralBricoMerlinServiceImpl implements ICentralBricoMerlinService {

    private Connection connection;

    public CentralBricoMerlinServiceImpl() {
        try {
            connection = CentralDbConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean mettreAJourPrix(long reference, double nouveauPrix) throws RemoteException {
        String sql = "UPDATE prix_articles SET prix = ?, date_mise_a_jour = CURRENT_TIMESTAMP WHERE reference = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, nouveauPrix);
            ps.setLong(2, reference);

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new RemoteException("Erreur lors de la mise à jour du prix", e);
        }
    }

    @Override
    public List<Article> getPrixMisAJour() throws RemoteException{
        String sql = "SELECT * FROM prix_articles";
        try (PreparedStatement ps = connection.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            List<Article> articles = new ArrayList<>();
            while (rs.next()){
                Article article = new Article(rs.getLong("reference"), rs.getDouble("prix"));
                articles.add(article);
            }
            return articles;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




    @Override
    public boolean stockerFacturePDF(byte[] contenuPDF, String nomFichier) throws RemoteException {
        if (contenuPDF == null || contenuPDF.length == 0) {
            System.out.println("Tentative de stockage d'un PDF vide");
            return false;
        }

        // Créer le répertoire de stockage s'il n'existe pas
        String cheminBase = "archives/factures_magasins_pdf/";
        File repertoire = new File(cheminBase);
        if (!repertoire.exists()) {
            if (!repertoire.mkdirs()) {
                System.out.println("Impossible de créer le répertoire de stockage: " + cheminBase);
                return false;
            }
        }

        // Chemin complet du fichier
        String cheminComplet = cheminBase + nomFichier;

        try (FileOutputStream fos = new FileOutputStream(cheminComplet)) {
            fos.write(contenuPDF);
            fos.flush();

            System.out.println("Facture PDF stockée avec succès: " + cheminComplet);


            return true;
        } catch (IOException e) {
            System.out.println("Erreur lors du stockage du fichier PDF"+e.getMessage());
            return false;
        }
    }

    @Override
    public Article consulterArticle(long reference) throws RemoteException {
        String sql = "SELECT * " +
                "FROM prix_articles " +
                "WHERE reference = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, reference);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Article(
                        rs.getLong("reference"),
                        rs.getDouble("prix")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RemoteException("Erreur SQL", e);
        }
    }


}
