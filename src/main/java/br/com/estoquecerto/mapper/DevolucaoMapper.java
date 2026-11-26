package br.com.estoquecerto.mapper;

import br.com.estoquecerto.dto.response.DevolucaoResponseDTO;
import br.com.estoquecerto.dto.response.ItemDevolucaoResponseDTO;
import br.com.estoquecerto.model.entity.Devolucao;
import br.com.estoquecerto.model.entity.ItemDevolucao;

/**
 * Não há paraEntidade(): montar uma devolução exige buscar a compra e
 * os itens comprados no banco, o que é responsabilidade do
 * DevolucaoService.
 */
public class DevolucaoMapper {

    public static DevolucaoResponseDTO paraResponseDTO(Devolucao devolucao) {

        return new DevolucaoResponseDTO(
            devolucao.getId(),
            devolucao.getCompra().getId(),
            devolucao.getCompra().getFornecedor().getId(),
            devolucao.getCompra().getFornecedor().getRazaoSocial(),
            devolucao.getDataDevolucao().toString(),
            devolucao.getMotivo().name(),
            devolucao.getDescricao(),
            devolucao.getStatus().name(),
            devolucao.getItens().stream()
                .map(DevolucaoMapper::paraItemResponseDTO)
                .toList()
        );
    }

    private static ItemDevolucaoResponseDTO paraItemResponseDTO(
            ItemDevolucao item) {

        return new ItemDevolucaoResponseDTO(
            item.getId(),
            item.getItemCompra().getId(),
            item.getProduto().getId(),
            item.getProduto().getCodigo(),
            item.getProduto().getNome(),
            item.getItemCompra().getQuantidade(),
            item.getQuantidade()
        );
    }
}
