package br.com.estoquecerto.dto.response;

public class FornecedorProdutoResponseDTO {

    private Long id;
    private Long fornecedorId;
    private String fornecedorRazaoSocial;
    private Long produtoId;
    private String produtoNome;
    private String produtoCodigo;

    public FornecedorProdutoResponseDTO(
            Long id,
            Long fornecedorId,
            String fornecedorRazaoSocial,
            Long produtoId,
            String produtoNome,
            String produtoCodigo) {

        this.id = id;
        this.fornecedorId = fornecedorId;
        this.fornecedorRazaoSocial = fornecedorRazaoSocial;
        this.produtoId = produtoId;
        this.produtoNome = produtoNome;
        this.produtoCodigo = produtoCodigo;
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

    public Long getProdutoId() {
        return produtoId;
    }

    public String getProdutoNome() {
        return produtoNome;
    }

    public String getProdutoCodigo() {
        return produtoCodigo;
    }
}