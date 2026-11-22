package br.com.estoquecerto.dto.request;

import java.math.BigDecimal;
import java.util.List;

public class CompraRequestDTO {

    private Long fornecedorId;
    private String dataCompra;      // opcional, formato AAAA-MM-DD
    private BigDecimal frete;       // opcional, padrão 0
    private BigDecimal desconto;    // opcional, padrão 0
    private List<ItemCompraRequestDTO> itens;

    public CompraRequestDTO() {
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public String getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(String dataCompra) {
        this.dataCompra = dataCompra;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public void setFrete(BigDecimal frete) {
        this.frete = frete;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public List<ItemCompraRequestDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemCompraRequestDTO> itens) {
        this.itens = itens;
    }
}
