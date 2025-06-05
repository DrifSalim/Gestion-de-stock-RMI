package Model;

import java.io.Serializable;

public class Article implements Serializable {
    private long reference;
    private String nom;
    private int id_famille;
    private double prix;
    private int quantite_stock;
    private Famille famille;

    public Article() {}
    public Article(long reference, double prix) {
        this.reference = reference;
        this.prix = prix;
    }
    public Article(long reference, String nom, double prix, int quantite_stock, Famille famille) {
        if(prix <= 0) throw new IllegalArgumentException("Prix invalide");
        if(quantite_stock < 0) throw new IllegalArgumentException("Stock invalide");
        this.reference = reference;
        this.nom = nom;
        this.id_famille = id_famille;
        this.prix = prix;
        this.quantite_stock = quantite_stock;
        this.famille = famille;
    }

    public long getReference() {
        return reference;
    }

    public void setReference(long reference) {
        this.reference = reference;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getId_famille() {
        return id_famille;
    }

    public void setId_famille(int id_famille) {
        this.id_famille = id_famille;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        if(prix <= 0) throw new IllegalArgumentException("Prix doit être > 0");
        this.prix = prix;
    }

    public int getQuantite_stock() {
        return quantite_stock;
    }

    public void setQuantite_stock(int quantite_stock) {
        if(quantite_stock < 0) throw new IllegalArgumentException("Stock doit être >= 0");
        this.quantite_stock = quantite_stock;
    }

    public Famille getFamille() {
        return famille;
    }

    public void setFamille(Famille famille) {
        this.famille = famille;
    }

    @Override
    public String toString() {
        return "Article{" +
                "reference=" + reference +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", quantite_stock=" + quantite_stock +
                ", famille=" + famille +
                '}';
    }
}
