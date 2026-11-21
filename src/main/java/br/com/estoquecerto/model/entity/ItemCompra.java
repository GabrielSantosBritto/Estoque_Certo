package br.com.estoquecerto.model.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Produto adquirido dentro de uma compra.
 *
 * O precoUnitario é o preço de AQUISIÇÃO pago ao fornecedor, diferente
 * do precoVenda do Produto.
 */
public class ItemCompra {

    private Long id;
    private Long compraId;
    private Produto produto;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;

    // Construtor completo: utilizado nas consultas ao banco
    public ItemCompra(
            Long id,
            Long compraId,
            Produto produto,
            BigDecimal quantidade,
            BigDecimal precoUnitario) {

        this.id = id;
        this.compraId = compraId;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    // Construtor para novo item (ainda sem ID e sem compra gravada)
    public ItemCompra(
            Produto produto,
            BigDecimal quantidade,
            BigDecimal precoUnitario) {

        this(null, null, produto, quantidade, precoUnitario);
    }

    public Long getId() {
        return id;
    }

    public Long getCompraId() {
        return compraId;
    }

    public Produto getProduto() {
        return produto;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    // Subtotal calculado: quantidade x preço unitário, em centavos
    public BigDecimal getSubtotal() {
        return quantidade.multiply(precoUnitario)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
