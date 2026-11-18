package br.com.estoquecerto.command;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Padrão Command (GoF).
 *
 * Encapsula uma operação de negócio composta por várias gravações
 * que precisam acontecer juntas, como confirmar o recebimento de uma
 * compra ou concluir uma devolução.
 *
 * O comando recebe a conexão já aberta pelo ExecutorTransacional e
 * deve usá-la em TODAS as gravações. Não abra outra conexão aqui
 * dentro: o que for gravado fora dela não será desfeito no rollback.
 */
public interface Command {

    void executar(Connection conexao) throws SQLException;
}
