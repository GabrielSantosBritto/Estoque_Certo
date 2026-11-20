package br.com.estoquecerto.dto.request;

import java.math.BigDecimal;

public class AjusteEstoqueRequestDTO {

    private Long produtoId;
    private String tipo;          // ENTRADA ou SAIDA
    private BigDecimal quantidade;
    private String motivo;

    public AjusteEstoqueRequestDTO() {
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
