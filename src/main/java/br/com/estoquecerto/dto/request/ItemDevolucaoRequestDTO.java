package br.com.estoquecerto.dto.request;

import java.math.BigDecimal;

public class ItemDevolucaoRequestDTO {

    private Long itemCompraId;
    private BigDecimal quantidade;

    public ItemDevolucaoRequestDTO() {
    }

    public Long getItemCompraId() {
        return itemCompraId;
    }

    public void setItemCompraId(Long itemCompraId) {
        this.itemCompraId = itemCompraId;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }
}
