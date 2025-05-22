package Model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Facture implements Serializable {
    private int id_facture;
    private double montant;
    private LocalDateTime date;
    private String status;
    private String mode_paiement;
    private ArrayList<Ligne_Facture> details;

    public Facture() {}
    public Facture(int id_facture, double montant, LocalDateTime date, String status, String mode_paiement) {
        this.id_facture = id_facture;
        this.montant = montant;
        this.date = date;
        this.status = status;
        this.mode_paiement = mode_paiement;
    }

    public int getId_facture() {
        return id_facture;
    }

    public void setId_facture(int id_facture) {
        this.id_facture = id_facture;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMode_paiement() {
        return mode_paiement;
    }

    public void setMode_paiement(String mode_paiement) {
        this.mode_paiement = mode_paiement;
    }

    public ArrayList<Ligne_Facture> getDetails() {
        return details;
    }

    public void setDetails(ArrayList<Ligne_Facture> details) {
        this.details = details;
    }


    @Override
    public String toString() {
        return "Facture{" +
                "id_facture=" + id_facture +
                ", montant=" + montant +
                ", date=" + date +
                ", status='" + status + '\'' +
                ", mode_paiement='" + mode_paiement + '\'' +
                ", details=" + details +
                '}';
    }
}
