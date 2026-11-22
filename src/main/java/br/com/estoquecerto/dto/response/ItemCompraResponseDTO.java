package br.com.estoquecerto.dto.response;

import java.math.BigDecimal;

public class ItemCompraResponseDTO {

    private Long id;
    private Long produtoId;
    private String produtoCodigo;
    private String produtoNome;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;

    public ItemCompraResponseDTO(
            Long id,
            Long produtoId,
            String produtoCodigo,
            String produtoNome,
            BigDecimal quantidade,
            BigDecimal precoUnitario,
            BigDecimal subtotal) {

        this.id = id;
        this.produtoId = produtoId;
        this.produtoCodigo = produtoCodigo;
        this.produtoNome = produtoNome;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public String getProdutoCodigo() {
        return produtoCodigo;
    }

    public String getProdutoNome() {
        return produtoNome;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
