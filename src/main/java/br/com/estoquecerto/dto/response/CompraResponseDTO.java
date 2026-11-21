package br.com.estoquecerto.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class CompraResponseDTO {

    private Long id;
    private Long fornecedorId;
    private String fornecedorRazaoSocial;
    private String dataCompra;      // AAAA-MM-DD
    private String status;
    private BigDecimal frete;
    private BigDecimal desconto;
    private BigDecimal subtotalItens;
    private BigDecimal total;
    private List<ItemCompraResponseDTO> itens;

    public CompraResponseDTO(
            Long id,
            Long fornecedorId,
            String fornecedorRazaoSocial,
            String dataCompra,
            String status,
            BigDecimal frete,
            BigDecimal desconto,
            BigDecimal subtotalItens,
            BigDecimal total,
            List<ItemCompraResponseDTO> itens) {

        this.id = id;
        this.fornecedorId = fornecedorId;
        this.fornecedorRazaoSocial = fornecedorRazaoSocial;
        this.dataCompra = dataCompra;
        this.status = status;
        this.frete = frete;
        this.desconto = desconto;
        this.subtotalItens = subtotalItens;
        this.total = total;
        this.itens = itens;
    }

    public Long getId() {
        return id;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public String getFornecedorRazaoSocial() {
        return fornecedorRazaoSocial;
    }

    public String getDataCompra() {
        return dataCompra;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public BigDecimal getSubtotalItens() {
        return subtotalItens;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<ItemCompraResponseDTO> getItens() {
        return itens;
    }
}
