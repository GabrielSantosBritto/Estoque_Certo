package br.com.estoquecerto.factory;

import br.com.estoquecerto.model.entity.MovimentacaoEstoque;
import br.com.estoquecerto.model.entity.Produto;
import br.com.estoquecerto.model.enums.OrigemMovimentacao;
import br.com.estoquecerto.model.enums.TipoMovimentacao;

import java.math.BigDecimal;

/**
 * Concrete Creator: entrada de estoque gerada pelo recebimento de uma compra.
 *
 * Usado pelo ConfirmarRecebimentoCommand, um gerador por item da compra.
 */
public class GeradorEntradaCompra extends GeradorMovimentacao {

    private final Long itemCompraId;

    public GeradorEntradaCompra(Long itemCompraId) {

        if (itemCompraId == null || itemCompraId <= 0) {
            throw new IllegalArgumentException(
                "Item da compra não informado para a movimentação."
            );
        }

        this.itemCompraId = itemCompraId;
    }

    @Override
    protected MovimentacaoEstoque criarMovimentacao(
            Produto produto,
            BigDecimal quantidade) {

        return new MovimentacaoEstoque(
            produto,
            TipoMovimentacao.ENTRADA,
            OrigemMovimentacao.COMPRA,
            quantidade,
            itemCompraId,
            null,
            null
        );
    }
}
