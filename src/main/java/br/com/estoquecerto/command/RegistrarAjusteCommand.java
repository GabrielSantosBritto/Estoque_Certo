package br.com.estoquecerto.command;

import br.com.estoquecerto.exception.ConflitoException;
import br.com.estoquecerto.factory.GeradorAjuste;
import br.com.estoquecerto.model.entity.MovimentacaoEstoque;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.model.enums.TipoMovimentacao;
import br.com.estoquecerto.repository.MovimentacaoRepository;
import br.com.estoquecerto.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Comando concreto: ajuste manual de estoque.
 *
 * Registra a movimentação de origem AJUSTE e atualiza o estoque do
 * produto na mesma transação. Se o estoque ficaria negativo, lança
 * ConflitoException e o ExecutorTransacional desfaz a movimentação.
 */
public class RegistrarAjusteCommand implements Command {

    private final Produto produto;
    private final TipoMovimentacao tipo;
    private final BigDecimal quantidade;
    private final String motivo;

    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;

    private Long movimentacaoId;

    public RegistrarAjusteCommand(
            Produto produto,
            TipoMovimentacao tipo,
            BigDecimal quantidade,
            String motivo) {

        this.produto = produto;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.motivo = motivo;
        this.movimentacaoRepository = new MovimentacaoRepository();
        this.produtoRepository = new ProdutoRepository();
    }

    @Override
    public void executar(Connection conexao) throws SQLException {

        MovimentacaoEstoque movimentacao = new GeradorAjuste(tipo, motivo)
                .registrar(produto, quantidade);

        movimentacaoId = movimentacaoRepository.salvar(conexao, movimentacao);

        if (!produtoRepository.atualizarEstoque(
                conexao,
                produto.getId(),
                movimentacao.getVariacaoEstoque())) {

            throw new ConflitoException(
                "Estoque insuficiente para o produto "
                + produto.getCodigo() + "."
            );
        }
    }

    // ID da movimentação gravada; disponível após a execução
    public Long getMovimentacaoId() {
        return movimentacaoId;
    }
}
