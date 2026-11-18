package br.com.estoquecerto.command;

import br.com.estoquecerto.infrastructure.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Invoker do padrão Command.
 *
 * Executa qualquer Command dentro de uma transação: confirma tudo
 * com commit se o comando terminar sem erro, ou desfaz tudo com
 * rollback se qualquer exceção acontecer.
 *
 * O controle de transação fica escrito apenas aqui. Uma nova operação
 * composta só precisa de um novo Command.
 */
public class ExecutorTransacional {

    public void executar(Command comando) throws SQLException {

        if (comando == null) {
            throw new IllegalArgumentException("Comando não informado.");
        }

        try (Connection conexao = DatabaseConnection.getConnection()) {

            conexao.setAutoCommit(false);

            try {
                comando.executar(conexao);
                conexao.commit();

            } catch (SQLException | RuntimeException e) {
                desfazer(conexao, e);
                throw e;
            }
        }
    }

    // Faz o rollback sem esconder o erro original
    private void desfazer(Connection conexao, Exception erroOriginal) {

        try {
            conexao.rollback();
        } catch (SQLException erroRollback) {
            erroOriginal.addSuppressed(erroRollback);
        }
    }
}
