package br.com.estoquecerto.mapper;

import br.com.estoquecerto.dto.response.CompraResponseDTO;
import br.com.estoquecerto.dto.response.ItemCompraResponseDTO;
import br.com.estoquecerto.model.entity.Compra;
import br.com.estoquecerto.model.entity.ItemCompra;

/**
 * Não há paraEntidade(): montar uma compra exige buscar fornecedor e
 * produtos no banco, o que é responsabilidade do CompraService.
 */
public class CompraMapper {

    public static CompraResponseDTO paraResponseDTO(Compra compra) {

        return new CompraResponseDTO(
            compra.getId(),
            compra.getFornecedor().getId(),
            compra.getFornecedor().getRazaoSocial(),
            compra.getDataCompra().toString(),
            compra.getStatus().name(),
            compra.getFrete(),
            compra.getDesconto(),
            compra.getSubtotalItens(),
            compra.getTotal(),
            compra.getItens().stream()
                .map(CompraMapper::paraItemResponseDTO)
                .toList()
        );
    }

    private static ItemCompraResponseDTO paraItemResponseDTO(ItemCompra item) {

        return new ItemCompraResponseDTO(
            item.getId(),
            item.getProduto().getId(),
            item.getProduto().getCodigo(),
            item.getProduto().getNome(),
            item.getQuantidade(),
            item.getPrecoUnitario(),
            item.getSubtotal()
        );
    }
}
