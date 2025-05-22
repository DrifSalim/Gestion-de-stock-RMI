package Serveur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/brico_merlin";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private Connection connection;

    // Instance unique (Singleton)
    private static DbConnection instance;

    // Constructeur privé pour empêcher l’instanciation externe
    private DbConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données !");
            e.printStackTrace();
        }
    }

    // Méthode d’accès au Singleton
    public static synchronized DbConnection getInstance() {
        if (instance == null) {
            instance = new DbConnection();
        }
        return instance;
    }

    // Accès à la connexion
    public Connection getConnection() {
        return connection;
    }
}