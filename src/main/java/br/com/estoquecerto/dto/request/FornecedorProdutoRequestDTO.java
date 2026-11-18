
package br.com.estoquecerto.dto.request;

public class FornecedorProdutoRequestDTO {

    private Long fornecedorId;
    private Long produtoId;

    public FornecedorProdutoRequestDTO() {
    }

    public FornecedorProdutoRequestDTO(Long fornecedorId, Long produtoId) {
        this.fornecedorId = fornecedorId;
        this.produtoId = produtoId;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }
}

