package br.com.estoquecerto.command;

import br.com.estoquecerto.model.entity.Compra;
import br.com.estoquecerto.repository.CompraRepository;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Comando concreto: grava uma compra nova com todos os itens.
 *
 * A compra e os itens são várias linhas em duas tabelas. Executado pelo
 * ExecutorTransacional, ou tudo é gravado, ou nada é: nunca sobra uma
 * compra sem itens ou com só parte deles.
 *
 * As validações de negócio já foram feitas pelo CompraService antes de
 * o comando ser criado.
 */
public class CadastrarCompraCommand implements Command {

    private final Compra compra;
    private final CompraRepository compraRepository;

    private Long compraId;

    public CadastrarCompraCommand(Compra compra) {
        this.compra = compra;
        this.compraRepository = new CompraRepository();
    }

    @Override
    public void executar(Connection conexao) throws SQLException {
        compraId = compraRepository.salvar(conexao, compra);
    }

    // ID da compra gravada; disponível após a execução
    public Long getCompraId() {
        return compraId;
    }
}
