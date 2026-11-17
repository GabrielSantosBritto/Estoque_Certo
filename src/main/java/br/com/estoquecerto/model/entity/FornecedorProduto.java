
package br.com.estoquecerto.model.entity;

public class FornecedorProduto {

    private Long id;
    private Fornecedor fornecedor;
    private Produto produto;

    public FornecedorProduto(
            Long id,
            Fornecedor fornecedor,
            Produto produto) {

        this.id = id;
        this.fornecedor = fornecedor;
        this.produto = produto;
    }

    public FornecedorProduto(
            Fornecedor fornecedor,
            Produto produto) {

        this.fornecedor = fornecedor;
        this.produto = produto;
    }

    public Long getId() {
        return id;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public Produto getProduto() {
        return produto;
    }
}
