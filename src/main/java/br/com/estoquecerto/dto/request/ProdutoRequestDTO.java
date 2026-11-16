package br.com.estoquecerto.dto.request;

import java.math.BigDecimal;

public class ProdutoRequestDTO {

    private String codigo;
    private String nome;
    private String descricao;
    private String unidadeMedida;
    private BigDecimal precoVenda;
    private BigDecimal quantidadeMinima;

    public ProdutoRequestDTO() {
    }

    public ProdutoRequestDTO(
            String codigo,
            String nome,
            String descricao,
            String unidadeMedida,
            BigDecimal precoVenda,
            BigDecimal quantidadeMinima) {

        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.unidadeMedida = unidadeMedida;
        this.precoVenda = precoVenda;
        this.quantidadeMinima = quantidadeMinima;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public BigDecimal getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public void setQuantidadeMinima(BigDecimal quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima;
    }
}
