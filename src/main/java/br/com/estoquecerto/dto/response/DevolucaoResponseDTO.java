package br.com.estoquecerto.dto.response;

import java.util.List;

public class DevolucaoResponseDTO {

    private Long id;
    private Long compraId;
    private Long fornecedorId;
    private String fornecedorRazaoSocial;
    private String dataDevolucao;   // AAAA-MM-DD
    private String motivo;
    private String descricao;
    private String status;
    private List<ItemDevolucaoResponseDTO> itens;

    public DevolucaoResponseDTO(
            Long id,
            Long compraId,
            Long fornecedorId,
            String fornecedorRazaoSocial,
            String dataDevolucao,
            String motivo,
            String descricao,
            String status,
            List<ItemDevolucaoResponseDTO> itens) {

        this.id = id;
        this.compraId = compraId;
        this.fornecedorId = fornecedorId;
        this.fornecedorRazaoSocial = fornecedorRazaoSocial;
        this.dataDevolucao = dataDevolucao;
        this.motivo = motivo;
        this.descricao = descricao;
        this.status = status;
        this.itens = itens;
    }

    public Long getId() {
        return id;
    }

    public Long getCompraId() {
        return compraId;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public String getFornecedorRazaoSocial() {
        return fornecedorRazaoSocial;
    }

    public String getDataDevolucao() {
        return dataDevolucao;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getStatus() {
        return status;
    }

    public List<ItemDevolucaoResponseDTO> getItens() {
        return itens;
    }
}
