package br.com.estoquecerto.command;

import br.com.estoquecerto.model.entity.Devolucao;
import br.com.estoquecerto.repository.DevolucaoRepository;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Comando concreto: grava uma devolução nova com todos os itens.
 *
 * Executado pelo ExecutorTransacional: ou a devolução e todos os itens
 * são gravados, ou nada é. Cadastrar não mexe no estoque; isso só
 * acontece na conclusão.
 *
 * As validações de negócio já foram feitas pelo DevolucaoService antes
 * de o comando ser criado.
 */
public class CadastrarDevolucaoCommand implements Command {

    private final Devolucao devolucao;
    private final DevolucaoRepository devolucaoRepository;

    private Long devolucaoId;

    public CadastrarDevolucaoCommand(Devolucao devolucao) {
        this.devolucao = devolucao;
        this.devolucaoRepository = new DevolucaoRepository();
    }

    @Override
    public void executar(Connection conexao) throws SQLException {
        devolucaoId = devolucaoRepository.salvar(conexao, devolucao);
    }

    // ID da devolução gravada; disponível após a execução
    public Long getDevolucaoId() {
        return devolucaoId;
    }
}
