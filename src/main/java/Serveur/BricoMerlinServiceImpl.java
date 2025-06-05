package Serveur;

import Model.Article;
import Model.Facture;
import Model.Famille;
import Model.Ligne_Facture;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BricoMerlinServiceImpl implements IBricoMerlinService {

        private Connection connection;

    public BricoMerlinServiceImpl() {
        try {
            connection = DbConnection.getInstance().getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Consulter le stock d'un article
    @Override
    public Article consulterStock(long reference) throws RemoteException {
        String sql = "SELECT a.*, f.nom AS nom_famille " +
                "FROM article a " +
                "JOIN famille f ON a.id_famille = f.id_famille " +
                "WHERE a.reference = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, reference);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Famille famille = new Famille(
                        rs.getInt("id_famille"),
                        rs.getString("nom_famille")
                );

                return new Article(
                        rs.getLong("reference"),
                        rs.getString("nom"),
                        rs.getDouble("prix"),
                        rs.getInt("quantite_stock"),
                        famille
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RemoteException("Erreur SQL", e);
        }
    }


    // Rechercher des articles par famille (stock > 0)
    @Override
    public List<Article> rechercherArticlesParFamille(String nomFamille) throws RemoteException {
        List<Article> articles = new ArrayList<>();
        String sql = "SELECT a.*, f.nom AS nom_famille " +
                "FROM article a " +
                "JOIN famille f ON a.id_famille = f.id_famille " +
                "WHERE f.nom = ? AND a.quantite_stock > 0";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nomFamille);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Création de l'objet Famille
                Famille famille = new Famille(
                        rs.getInt("id_famille"),
                        rs.getString("nom_famille")
                );

                // Création de l'objet Article avec Famille
                Article article = new Article(
                        rs.getLong("reference"),
                        rs.getString("nom"),
                        rs.getDouble("prix"),
                        rs.getInt("quantite_stock"),
                        famille // ici, tu passes l'objet famille
                );

                articles.add(article);
            }
            return articles;
        } catch (SQLException e) {
            throw new RemoteException("Erreur lors de la recherche par nom de famille", e);
        }
    }


    // Acheter un article
    @Override
    public boolean acheterArticle(long reference, int quantite, int idFacture) throws RemoteException {
        try {
            connection.setAutoCommit(false); // Exécution atomique

            // Vérifier que la facture n'est pas déjà payée
            try (PreparedStatement psCheckFacture = connection.prepareStatement(
                    "SELECT status FROM facture WHERE id_facture = ?")) {
                psCheckFacture.setInt(1, idFacture);
                ResultSet rsFacture = psCheckFacture.executeQuery();

                if (rsFacture.next()) {
                    String status = rsFacture.getString("status");
                    if ("payee".equalsIgnoreCase(status)) {
                        connection.rollback();
                        throw new RemoteException("Facture déja payée, impossible d'ajouter un article");
                    }
                } else {
                    connection.rollback();
                    return false; // Facture inexistante
                }
            }

            // Vérifier et verrouiller le stock
            try (PreparedStatement psStock = connection.prepareStatement(
                    "SELECT quantite_stock FROM article WHERE reference = ? FOR UPDATE")) {
                psStock.setLong(1, reference);
                ResultSet rs = psStock.executeQuery();

                if (rs.next() && rs.getInt("quantite_stock") >= quantite) {
                    // Mettre à jour le stock
                    try (PreparedStatement psUpdate = connection.prepareStatement(
                            "UPDATE article SET quantite_stock = quantite_stock - ? WHERE reference = ?")) {
                        psUpdate.setInt(1, quantite);
                        psUpdate.setLong(2, reference);
                        psUpdate.executeUpdate();
                    }

                    // Vérifier si l'article existe déjà dans la facture
                    try (PreparedStatement psCheck = connection.prepareStatement(
                            "SELECT quantite FROM ligne_facture WHERE id_facture = ? AND reference = ?")) {
                        psCheck.setInt(1, idFacture);
                        psCheck.setLong(2, reference);
                        ResultSet rsCheck = psCheck.executeQuery();

                        if (rsCheck.next()) {
                            // Mettre à jour la quantité
                            int ancienneQuantite = rsCheck.getInt("quantite");
                            try (PreparedStatement psUpdateLigne = connection.prepareStatement(
                                    "UPDATE ligne_facture SET quantite = ? WHERE id_facture = ? AND reference = ?")) {
                                psUpdateLigne.setInt(1, ancienneQuantite + quantite);
                                psUpdateLigne.setInt(2, idFacture);
                                psUpdateLigne.setLong(3, reference);
                                psUpdateLigne.executeUpdate();
                            }
                        } else {
                            // Insérer une nouvelle ligne
                            Article article = consulterStock(reference);
                            try (PreparedStatement psInsert = connection.prepareStatement(
                                    "INSERT INTO ligne_facture (id_facture, reference, quantite, prix) VALUES (?, ?, ?, ?)")) {
                                psInsert.setInt(1, idFacture);
                                psInsert.setLong(2, reference);
                                psInsert.setInt(3, quantite);
                                psInsert.setDouble(4, article.getPrix());
                                psInsert.executeUpdate();
                            }
                        }
                    }

                    // Mise à jour du montant total
                    updateMontantFacture(idFacture);
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false; // Stock insuffisant
                }
            }
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            throw new RemoteException("Erreur lors de l'achat", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }



    //payer une facture
    @Override
    public boolean payerFacture(int idFacture, String modePaiement) throws RemoteException {
        Facture facture = consulterFacture(idFacture);
        if (facture == null || facture.getMontant() <= 0) {
            throw new RemoteException("Impossible de payer une facture vide ou nulle");
        }
        if (facture.getStatus().equals("payee")) {
            throw new RemoteException("La facture est déjà payée en "+facture.getMode_paiement());
        }
        String sql = "UPDATE facture SET mode_paiement = ?, status = 'payee', date = NOW() WHERE id_facture = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, modePaiement);
            ps.setInt(2, idFacture);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0; // True si la facture existe et a été mise à jour
        } catch (SQLException e) {
            throw new RemoteException("Erreur lors du paiement de la facture", e);
        }
    }

    // Consulter une facture
    @Override
    public Facture consulterFacture(int idFacture) throws RemoteException {
        try {
            Facture facture = null;

            // Récupération des informations principales de la facture
            String sqlFacture = "SELECT * FROM facture WHERE id_facture = ?";
            try (PreparedStatement ps = connection.prepareStatement(sqlFacture)) {
                ps.setInt(1, idFacture);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    facture = new Facture(
                            rs.getInt("id_facture"),
                            rs.getDouble("montant"),
                            rs.getTimestamp("date").toLocalDateTime(),
                            rs.getString("status"),
                            rs.getString("mode_paiement")
                    );
                }
            }

            if (facture != null) {
                List<Ligne_Facture> lignes = new ArrayList<>();

                String sqlLignes = "SELECT lf.*, a.nom AS nom_article, a.prix AS prix_article, " +
                        "a.quantite_stock, a.id_famille, f.nom AS nom_famille " +
                        "FROM ligne_facture lf " +
                        "JOIN article a ON lf.reference = a.reference " +
                        "JOIN famille f ON a.id_famille = f.id_famille " +
                        "WHERE lf.id_facture = ?";

                try (PreparedStatement ps = connection.prepareStatement(sqlLignes)) {
                    ps.setInt(1, idFacture);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        Famille famille = new Famille(
                                rs.getInt("id_famille"),
                                rs.getString("nom_famille")
                        );

                        Article article = new Article(
                                rs.getLong("reference"),
                                rs.getString("nom_article"),
                                rs.getDouble("prix_article"),
                                rs.getInt("quantite_stock"),
                                famille
                        );

                        Ligne_Facture ligne = new Ligne_Facture(
                                rs.getInt("id_facture"),
                                rs.getLong("reference"),
                                rs.getInt("quantite"),
                                rs.getDouble("prix"),
                                article
                        );

                        lignes.add(ligne);
                    }
                }

                facture.setDetails((ArrayList<Ligne_Facture>) lignes);
            }

            return facture;

        } catch (SQLException e) {
            throw new RemoteException("Erreur SQL lors de la consultation de la facture", e);
        }
    }

    @Override
    public double calculerChiffreAffaire(LocalDateTime date) throws RemoteException {
        String sql = "SELECT SUM(montant) AS total FROM facture WHERE DATE(date) = ? and status = 'payee'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // Convertir LocalDateTime en java.sql.Date
            ps.setDate(1, java.sql.Date.valueOf(date.toLocalDate()));
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("total") : 0.0;
        } catch (SQLException e) {
            throw new RemoteException("Erreur lors du calcul du CA", e);
        }
    }

    // Ajouter du stock à un article existant
    @Override
    public void ajouterStock(long reference, int quantite) throws RemoteException {
        if (quantite <= 0) {
            throw new RemoteException("La quantité doit être positive");
        }
        String sql = "UPDATE article SET quantite_stock = quantite_stock + ? WHERE reference = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quantite);
            ps.setLong(2, reference);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RemoteException("Aucun article trouvé avec la référence " + reference);
            }
        } catch (SQLException e) {
            throw new RemoteException("Erreur lors de l'ajout de stock", e);
        }
    }

    // Mise à jour des prix en masse (transactionnelle)
    @Override
    public void mettreAJourPrix(Map<Long, Double> nouveauxPrix) throws RemoteException {
        try {
            connection.setAutoCommit(false); // Désactiver l'autocommit
            String sql = "UPDATE article SET prix = ? WHERE reference = ?";

            for (Map.Entry<Long, Double> entry : nouveauxPrix.entrySet()) {
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setDouble(1, entry.getValue());
                    ps.setLong(2, entry.getKey());
                    ps.executeUpdate();
                }
            }

            connection.commit(); // Valider toutes les mises à jour
        } catch (SQLException e) {
            try {
                connection.rollback(); // Annuler en cas d'erreur
            } catch (SQLException ignored) {
            }
            throw new RemoteException("Erreur lors de la mise à jour des prix", e);
        } finally {
            try {
                connection.setAutoCommit(true); // Réactiver l'autocommit
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    public int creerNouvelleFacture() throws RemoteException {
        String sql = "INSERT INTO facture (montant, date, status) VALUES (0, NOW(), 'en_attente')";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Retourne l'ID généré
                }
            }
            throw new RemoteException("Échec de création de la facture");
        } catch (SQLException e) {
            throw new RemoteException("Erreur SQL: " + e.getMessage());
        }
    }

    //fonction pour modifier ke montant total d'une facture
    private void updateMontantFacture(int idFacture) throws RemoteException {
        String sql = "UPDATE facture f " +
                "SET montant = (" +
                "    SELECT COALESCE(SUM(lf.quantite * lf.prix), 0) " +
                "    FROM ligne_facture lf " +
                "    WHERE lf.id_facture = ?" +
                ") " +
                "WHERE f.id_facture = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFacture);
            ps.setInt(2, idFacture);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RemoteException("Erreur de calcul du montant: " + e.getMessage());
        }
    }
    @Override
    public void enregistrerFactureEnPDF(int idFacture) throws RemoteException {
        Facture facture = consulterFacture(idFacture);
        if (facture == null) {
            throw new RemoteException("Facture non trouvée");
        }

        // Créer le nom de fichier unique
        String nomFichier = "Facture-" + idFacture + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        String cheminFichier = "tickets-de-caisse/" + nomFichier;

        // Créer le dossier s'il n'existe pas
        File dossier = new File("tickets-de-caisse");
        if (!dossier.exists()) {
            dossier.mkdir();
        }

        try {
            PdfWriter writer = new PdfWriter(cheminFichier);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("==========================================").setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("BRICO-MERLIN").setTextAlignment(TextAlignment.CENTER).setBold());
            document.add(new Paragraph("TICKET DE CAISSE").setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("==========================================").setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Facture N°: " + facture.getId_facture()));
            document.add(new Paragraph("Date: " + facture.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
            document.add(new Paragraph("------------------------------------------"));
            document.add(new Paragraph("Statut : " + facture.getStatus()));
            if (facture.getMode_paiement() != null) {
                document.add(new Paragraph("Mode de paiement : " + facture.getMode_paiement()));
            }

            document.add(new Paragraph("------------------------------------------"));

            // Créer un tableau pour afficher les articles
            Table table = new Table(new float[]{1, 3, 1, 1, 1})
                    .setWidth(UnitValue.createPercentValue(100))
                    .setFontSize(10);

            // En-têtes de colonnes
            table.addHeaderCell(new Cell().add(new Paragraph("Réf.")).setBold().setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Article")).setBold().setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Prix")).setBold().setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Qté")).setBold().setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Total")).setBold().setTextAlignment(TextAlignment.CENTER));

            // Contenu du tableau
            for (Ligne_Facture ligne : facture.getDetails()) {
                double sousTotal = ligne.getPrix() * ligne.getQuantite();

                table.addCell(new Cell().add(new Paragraph(String.valueOf(ligne.getReference()))).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(ligne.getArticle() != null ? ligne.getArticle().getNom() : "Article inconnu").setTextAlignment(TextAlignment.CENTER)));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", ligne.getPrix()))).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(ligne.getQuantite()))).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", sousTotal))).setTextAlignment(TextAlignment.CENTER));
            }

            document.add(table);
            document.add(new Paragraph("=========================================================================="));
            document.add(new Paragraph(String.format("TOTAL A PAYER : %.2f €", facture.getMontant()))
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("=========================================================================="));

            document.close();

        } catch (FileNotFoundException e) {
            throw new RemoteException("Erreur lors de la génération du PDF", e);
        }
    }


}