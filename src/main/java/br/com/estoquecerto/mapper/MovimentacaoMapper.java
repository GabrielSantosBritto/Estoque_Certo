package br.com.estoquecerto.mapper;

import br.com.estoquecerto.dto.response.MovimentacaoResponseDTO;
import br.com.estoquecerto.model.entity.MovimentacaoEstoque;

/**
 * Não há paraEntidade(): movimentações só são criadas pelos geradores
 * do pacote factory (Factory Method).
 */
public class MovimentacaoMapper {

    public static MovimentacaoResponseDTO paraResponseDTO(
            MovimentacaoEstoque movimentacao) {

        return new MovimentacaoResponseDTO(
            movimentacao.getId(),
            movimentacao.getProduto().getId(),
            movimentacao.getProduto().getCodigo(),
            movimentacao.getProduto().getNome(),
            movimentacao.getTipo().name(),
            movimentacao.getOrigem().name(),
            movimentacao.getQuantidade(),
            movimentacao.getDataMovimentacao().toString(),
            movimentacao.getItemCompraId(),
            movimentacao.getItemDevolucaoId(),
            movimentacao.getMotivo()
        );
    }
}
