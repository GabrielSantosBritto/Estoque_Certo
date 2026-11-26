package br.com.estoquecerto.dto.response;

import java.math.BigDecimal;

public class ItemDevolucaoResponseDTO {

    private Long id;
    private Long itemCompraId;
    private Long produtoId;
    private String produtoCodigo;
    private String produtoNome;
    private BigDecimal quantidadeComprada;
    private BigDecimal quantidade;

    public ItemDevolucaoResponseDTO(
            Long id,
            Long itemCompraId,
            Long produtoId,
            String produtoCodigo,
            String produtoNome,
            BigDecimal quantidadeComprada,
            BigDecimal quantidade) {

        this.id = id;
        this.itemCompraId = itemCompraId;
        this.produtoId = produtoId;
        this.produtoCodigo = produtoCodigo;
        this.produtoNome = produtoNome;
        this.quantidadeComprada = quantidadeComprada;
        this.quantidade = quantidade;
    }

    public Long getId() {
        return id;
    }

    public Long getItemCompraId() {
        return itemCompraId;
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

    public BigDecimal getQuantidadeComprada() {
        return quantidadeComprada;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }
}
