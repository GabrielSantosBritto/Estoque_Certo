
package br.com.estoquecerto.mapper;

import br.com.estoquecerto.dto.request.FornecedorProdutoRequestDTO;
import br.com.estoquecerto.dto.response.FornecedorProdutoResponseDTO;
import br.com.estoquecerto.model.entity.Fornecedor;
import br.com.estoquecerto.model.entity.FornecedorProduto;
import br.com.estoquecerto.model.entity.Produto;

public class FornecedorProdutoMapper {

    public static FornecedorProduto paraEntidade(
            FornecedorProdutoRequestDTO dto,
            Fornecedor fornecedor,
            Produto produto) {

        return new FornecedorProduto(fornecedor, produto);
    }

    public static FornecedorProdutoResponseDTO paraResponseDTO(
            FornecedorProduto fornecedorProduto) {

        return new FornecedorProdutoResponseDTO(
            fornecedorProduto.getId(),
            fornecedorProduto.getFornecedor().getId(),
            fornecedorProduto.getFornecedor().getRazaoSocial(),
            fornecedorProduto.getProduto().getId(),
            fornecedorProduto.getProduto().getNome(),
            fornecedorProduto.getProduto().getCodigo()
        );
    }
}
