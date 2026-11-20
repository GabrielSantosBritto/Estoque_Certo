package br.com.estoquecerto.factory;

import br.com.estoquecerto.model.entity.MovimentacaoEstoque;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.model.enums.OrigemMovimentacao;
import br.com.estoquecerto.model.enums.TipoMovimentacao;

import java.math.BigDecimal;

/**
 * Concrete Creator: saída de estoque gerada pela conclusão de uma devolução.
 *
 * Usado pelo ConcluirDevolucaoCommand, um gerador por item devolvido.
 * A checagem de estoque suficiente é feita em registrar(), no Creator.
 */
public class GeradorSaidaDevolucao extends GeradorMovimentacao {

    private final Long itemDevolucaoId;

    public GeradorSaidaDevolucao(Long itemDevolucaoId) {

        if (itemDevolucaoId == null || itemDevolucaoId <= 0) {
            throw new IllegalArgumentException(
                "Item da devolução não informado para a movimentação."
            );
        }

        this.itemDevolucaoId = itemDevolucaoId;
    }

    @Override
    protected MovimentacaoEstoque criarMovimentacao(
            Produto produto,
            BigDecimal quantidade) {

        return new MovimentacaoEstoque(
            produto,
            TipoMovimentacao.SAIDA,
            OrigemMovimentacao.DEVOLUCAO,
            quantidade,
            null,
            itemDevolucaoId,
            null
        );
    }
}
