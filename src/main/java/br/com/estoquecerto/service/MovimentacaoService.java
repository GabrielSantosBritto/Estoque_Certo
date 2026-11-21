package br.com.estoquecerto.service;

import br.com.estoquecerto.command.ExecutorTransacional;
import br.com.estoquecerto.command.RegistrarAjusteCommand;
import br.com.estoquecerto.exception.RecursoNaoEncontradoException;
import br.com.estoquecerto.model.entity.MovimentacaoEstoque;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.model.enums.TipoMovimentacao;
import br.com.estoquecerto.repository.MovimentacaoRepository;
import br.com.estoquecerto.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Regras do módulo de movimentações.
 *
 * Entradas por compra e saídas por devolução NÃO passam por aqui: são
 * geradas pelos Commands dos módulos de Compra e Devolução, usando os
 * geradores e o MovimentacaoRepository diretamente.
 */
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final ExecutorTransacional executorTransacional;

    public MovimentacaoService() {
        this.movimentacaoRepository = new MovimentacaoRepository();
        this.produtoRepository = new ProdutoRepository();
        this.executorTransacional = new ExecutorTransacional();
    }

    // Ajuste manual: retorna o ID da movimentação registrada
    public Long registrarAjuste(
            Long produtoId,
            String tipoTexto,
            BigDecimal quantidade,
            String motivo) throws SQLException {

        validarId(produtoId);

        TipoMovimentacao tipo = converterTipo(tipoTexto);

        Produto produto = produtoRepository.buscarPorId(produtoId)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException("Produto não encontrado.")
                );

        if (!produto.isAtivo()) {
            throw new IllegalArgumentException(
                "Não é possível ajustar o estoque de um produto inativo."
            );
        }

        RegistrarAjusteCommand comando = new RegistrarAjusteCommand(
            produto, tipo, quantidade, motivo
        );

        executorTransacional.executar(comando);

        return comando.getMovimentacaoId();
    }

    // Histórico de movimentações de um produto
    public List<MovimentacaoEstoque> listarPorProduto(Long produtoId)
            throws SQLException {

        validarId(produtoId);

        if (produtoRepository.buscarPorId(produtoId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Produto não encontrado.");
        }

        return movimentacaoRepository.listarPorProduto(produtoId);
    }

    private TipoMovimentacao converterTipo(String tipoTexto) {

        if (tipoTexto == null || tipoTexto.isBlank()) {
            throw new IllegalArgumentException(
                "Tipo do ajuste não informado (ENTRADA ou SAIDA)."
            );
        }

        try {
            return TipoMovimentacao.valueOf(tipoTexto.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Tipo do ajuste inválido. Use ENTRADA ou SAIDA."
            );
        }
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID do produto inválido.");
        }
    }
}
