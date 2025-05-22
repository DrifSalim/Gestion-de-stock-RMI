package Model;

import java.io.Serializable;

public class Ligne_Facture implements Serializable {
    private int id_facture;
    private long reference;
    private int quantite;
    private double prix;
    private Article article;
    private Facture facture;

    public Ligne_Facture(int id_facture, long reference, int quantite, double prix, Article article) {
        if(quantite <= 0) throw new IllegalArgumentException("Quantité invalide");
        this.id_facture = id_facture;
        this.reference = reference;
        this.quantite = quantite;
        this.prix = prix;
        this.article = article;
    }

    public int getId_facture() {
        return id_facture;
    }

    public long getReference() {
        return reference;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrix() {
        return prix;
    }

    public Article getArticle() {
        return article;
    }

    @Override
    public String toString() {
        return "Ligne_Facture{" +
                "id_facture=" + id_facture +
                ", reference=" + reference +
                ", quantite=" + quantite +
                ", prix=" + prix +
                '}';
    }
}
