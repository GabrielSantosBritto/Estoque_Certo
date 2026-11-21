package br.com.estoquecerto.dto.response;

import java.math.BigDecimal;

public class MovimentacaoResponseDTO {

    private Long id;
    private Long produtoId;
    private String produtoCodigo;
    private String produtoNome;
    private String tipo;
    private String origem;
    private BigDecimal quantidade;
    private String dataMovimentacao;   // ISO-8601, ex.: 2026-11-20T15:30:00
    private Long itemCompraId;
    private Long itemDevolucaoId;
    private String motivo;

    public MovimentacaoResponseDTO(
            Long id,
            Long produtoId,
            String produtoCodigo,
            String produtoNome,
            String tipo,
            String origem,
            BigDecimal quantidade,
            String dataMovimentacao,
            Long itemCompraId,
            Long itemDevolucaoId,
            String motivo) {

        this.id = id;
        this.produtoId = produtoId;
        this.produtoCodigo = produtoCodigo;
        this.produtoNome = produtoNome;
        this.tipo = tipo;
        this.origem = origem;
        this.quantidade = quantidade;
        this.dataMovimentacao = dataMovimentacao;
        this.itemCompraId = itemCompraId;
        this.itemDevolucaoId = itemDevolucaoId;
        this.motivo = motivo;
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

    public String getTipo() {
        return tipo;
    }

    public String getOrigem() {
        return origem;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public String getDataMovimentacao() {
        return dataMovimentacao;
    }

    public Long getItemCompraId() {
        return itemCompraId;
    }

    public Long getItemDevolucaoId() {
        return itemDevolucaoId;
    }

    public String getMotivo() {
        return motivo;
    }
}
