package br.com.estoquecerto.dto.request;

import java.util.List;

public class DevolucaoRequestDTO {

    private Long compraId;
    private String dataDevolucao;   // opcional, formato AAAA-MM-DD
    private String motivo;          // valor de MotivoDevolucao
    private String descricao;       // opcional; obrigatória quando motivo = OUTROS
    private List<ItemDevolucaoRequestDTO> itens;

    public DevolucaoRequestDTO() {
    }

    public Long getCompraId() {
        return compraId;
    }

    public void setCompraId(Long compraId) {
        this.compraId = compraId;
    }

    public String getDataDevolucao() {
        return dataDevolucao;
    }

    public void setDataDevolucao(String dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<ItemDevolucaoRequestDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemDevolucaoRequestDTO> itens) {
        this.itens = itens;
    }
}
