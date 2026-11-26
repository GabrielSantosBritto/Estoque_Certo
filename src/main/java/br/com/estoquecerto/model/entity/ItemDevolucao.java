package br.com.estoquecerto.model.entity;

import java.math.BigDecimal;

/**
 * Quantidade de um item comprado que volta para o fornecedor.
 *
 * O itemCompra traz o produto e a quantidade comprada, usados para
 * validar a devolução e para gerar a movimentação de saída.
 */
public class ItemDevolucao {

    private Long id;
    private Long devolucaoId;
    private ItemCompra itemCompra;
    private BigDecimal quantidade;

    // Construtor completo: utilizado nas consultas ao banco
    public ItemDevolucao(
            Long id,
            Long devolucaoId,
            ItemCompra itemCompra,
            BigDecimal quantidade) {

        this.id = id;
        this.devolucaoId = devolucaoId;
        this.itemCompra = itemCompra;
        this.quantidade = quantidade;
    }

    // Construtor para novo item (ainda sem ID e sem devolução gravada)
    public ItemDevolucao(ItemCompra itemCompra, BigDecimal quantidade) {
        this(null, null, itemCompra, quantidade);
    }

    public Long getId() {
        return id;
    }

    public Long getDevolucaoId() {
        return devolucaoId;
    }

    public ItemCompra getItemCompra() {
        return itemCompra;
    }

    public Produto getProduto() {
        return itemCompra.getProduto();
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }
}
