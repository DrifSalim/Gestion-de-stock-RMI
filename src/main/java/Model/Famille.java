package Model;

import java.util.ArrayList;

public class Famille implements java.io.Serializable{
    private int id_famille;
    private String nom;
    private ArrayList<Article> articles;
    public Famille() {}
    public Famille(int id_famille, String nom) {
        this.id_famille = id_famille;
        this.nom = nom;
    }

    public int getId_famille() {
        return id_famille;
    }

    public void setId_famille(int id_famille) {
        this.id_famille = id_famille;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public ArrayList<Article> getArticles() {
        return articles;
    }

    @Override
    public String toString() {
        return "Famille{" +
                "id_famille=" + id_famille +
                ", nom='" + nom + '\'' +
                '}';
    }
}
