package br.com.estoquecerto.dto.response;

import java.math.BigDecimal;

public class ProdutoResponseDTO {

    private Long id;
    private String codigo;
    private String nome;
    private String descricao;
    private String unidadeMedida;
    private BigDecimal quantidadeEstoque;
    private BigDecimal quantidadeMinima;
    private BigDecimal precoVenda;
    private boolean ativo;

    public ProdutoResponseDTO(
            Long id,
            String codigo,
            String nome,
            String descricao,
            String unidadeMedida,
            BigDecimal quantidadeEstoque,
            BigDecimal quantidadeMinima,
            BigDecimal precoVenda,
            boolean ativo) {

        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.unidadeMedida = unidadeMedida;
        this.quantidadeEstoque = quantidadeEstoque;
        this.quantidadeMinima = quantidadeMinima;
        this.precoVenda = precoVenda;
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public BigDecimal getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public BigDecimal getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
