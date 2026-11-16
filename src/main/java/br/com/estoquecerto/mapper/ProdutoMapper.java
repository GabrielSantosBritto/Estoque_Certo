package br.com.estoquecerto.mapper;

import br.com.estoquecerto.dto.request.ProdutoRequestDTO;
import br.com.estoquecerto.dto.response.ProdutoResponseDTO;
import br.com.estoquecerto.model.entity.Produto;

public class ProdutoMapper {

    public static Produto paraEntidade(ProdutoRequestDTO dto) {

        return new Produto(
            dto.getCodigo(),
            dto.getNome(),
            dto.getDescricao(),
            dto.getUnidadeMedida(),
            dto.getQuantidadeMinima(),
            dto.getPrecoVenda()
        );
    }

    public static ProdutoResponseDTO paraResponseDTO(Produto produto) {

        return new ProdutoResponseDTO(
            produto.getId(),
            produto.getCodigo(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getUnidadeMedida(),
            produto.getQuantidadeEstoque(),
            produto.getQuantidadeMinima(),
            produto.getPrecoVenda(),
            produto.isAtivo()
        );
    }
}
