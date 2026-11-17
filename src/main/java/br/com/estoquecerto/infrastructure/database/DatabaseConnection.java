
package br.com.estoquecerto.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            System.getenv("DB_URL");

    private static final String USUARIO =
            System.getenv("DB_USER");

    private static final String SENHA =
            System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {

        if (URL == null || USUARIO == null || SENHA == null) {
            throw new IllegalStateException(
                "As variáveis de ambiente do banco não foram configuradas."
            );
        }

        try {
            // Carrega explicitamente o driver JDBC do MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "Driver JDBC do MySQL não encontrado.",
                e
            );
        }

        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}
